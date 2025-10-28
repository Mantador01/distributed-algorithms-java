package TP2;
import io.jbotsim.core.Node;
import io.jbotsim.core.Message;
import io.jbotsim.core.Color;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NoeudItai100 extends Node {
    // === Constantes de l’algorithme ===
    private static final double PROBA_INIT = 0.2;
    private static final int    MAX_ID      = 20;

    // === Statistiques globales sur 100 exécutions ===
    private static int runCount        = 0;
    private static int messagesThisRun = 0;
    private static long sumMessages    = 0;
    private static int minMessages     = Integer.MAX_VALUE;
    private static int maxMessages     = 0;

    // État local du nœud
    private int     phase;
    private boolean isCandidate;
    private int     myId;
    private boolean electionDone;
    private boolean electionLaunched;
    private final Random rand = new Random();

    @Override
    public void onStart() {
        // Au début de la **première** exécution, on remet messagesThisRun à 0
        if (getID() == 0 && runCount == 0) {
            messagesThisRun = 0;
        }
        phase            = 1;
        electionDone     = false;
        electionLaunched = false;
        isCandidate      = (getID() == 0) || (rand.nextDouble() < PROBA_INIT);
        setColor(isCandidate ? Color.getColorAt(getID()) : Color.white);
    }

    @Override
    public void onClock() {
        // Démarrage de l’élection une seule fois par run
        if (isCandidate && !electionLaunched) {
            electionLaunched = true;
            startElection();
        }
    }

    private void startElection() {
        myId = rand.nextInt(MAX_ID + 1);
        ElecMessage em = new ElecMessage(ElecMessage.Type.ELEC, myId, phase, 1, true);
        sendAndCount(em);
    }

    @Override
    public void onMessage(Message msg) {
        ElecMessage m = (ElecMessage) msg.getContent();
        int N = getTopology().getNodes().size();

        // 1) Si message LEAD et pas encore traité → fin d’élection
        if (m.type == ElecMessage.Type.LEAD && !electionDone) {
            electionDone = true;
            setColor(Color.green);
            // Relais final du LEAD
            if (m.hops < N) {
                m.hops++;
                sendAndCount(m);
            }
            // Le leader lui-même termine la run
            if (getID() == m.originId) {
                endRun();
            }
            return;
        }
        // Si déjà terminé, on ignore tout
        if (electionDone) {
            return;
        }

        // 2) Nœud non-candidat → simple relais ELEC
        if (!isCandidate) {
            if (m.type == ElecMessage.Type.ELEC && m.hops < N) {
                m.hops++;
                sendAndCount(m);
            }
            return;
        }

        // 3) Candidat : cas de réception d’un ELEC
        if (m.type == ElecMessage.Type.ELEC) {
            // a) Mon propre ELEC revenu au bout de N sauts
            if (m.originId == myId && m.originPhase == phase && m.hops == N) {
                if (m.unique) {
                    // victoire → lancer LEAD
                    electionDone = true;
                    setColor(Color.green);
                    ElecMessage lead = new ElecMessage(ElecMessage.Type.LEAD, myId, phase, 1, true);
                    sendAndCount(lead);
                    // et le leader clôt la run
                    endRun();
                } else {
                    // collision → nouvelle phase
                    phase++;
                    startElection();
                }
                return;
            }
            // b) Collision détectée avant N sauts
            if (m.originId == myId && m.originPhase == phase && m.hops < N) {
                m.unique = false;
                m.hops++;
                sendAndCount(m);
                return;
            }
            // c) Un autre candidat plus fort ou phase supérieure
            if (m.originPhase > phase || (m.originPhase == phase && m.originId > myId)) {
                isCandidate = false;
                setColor(Color.white);
                if (m.hops < N) {
                    m.hops++;
                    sendAndCount(m);
                }
                return;
            }
            // d) Sinon, relais normal si hops < N
            if (m.hops < N) {
                m.hops++;
                sendAndCount(m);
            }
        }
    }

    /** Envoie un ElecMessage, incrémente le compteur */
    private void sendAndCount(ElecMessage em) {
        sendAll(new Message(em));
        messagesThisRun++;
    }

    /** Conclut une exécution, affiche stats partielles et relance ou termine */
    private void endRun() {
        runCount++;
        // On récupère le nombre de messages échangés **pendant cette** exécution
        sumMessages += messagesThisRun;
        minMessages  = Math.min(minMessages, messagesThisRun);
        maxMessages  = Math.max(maxMessages, messagesThisRun);
        System.out.println("Run #" + runCount + " — messages=" + messagesThisRun);
    
        if (runCount < 100) {
            // 1) Remélange des IDs
            List<Node> nodes = getTopology().getNodes().stream().collect(Collectors.toList());
            List<Integer> ids = IntStream.range(0, nodes.size()).boxed().collect(Collectors.toList());
            Collections.shuffle(ids);
            for (int i = 0; i < nodes.size(); i++)
                nodes.get(i).setID(ids.get(i));
    
            // 2) **Réinitialisation** du compteur pour la prochaine run
            messagesThisRun = 0;
    
            // 3) Relance de la topologie
            getTopology().restart();
        }
        else {
            // Après la 100ᵉ run, on calcule la moyenne des **100** valeurs individuelles
            double moyenne = sumMessages / 100.0;
            System.out.println("=== Statistiques d’Itai–Rodeh (100 runs) ===");
            System.out.println("Moyenne = " + moyenne);
            System.out.println("Minimum = " + minMessages);
            System.out.println("Maximum = " + maxMessages);
            System.exit(0);
        }
    }
    

    /** Représentation des messages ELEC / LEAD */
    private static class ElecMessage {
        enum Type { ELEC, LEAD }
        Type    type;
        final int originId;
        final int originPhase;
        int     hops;
        boolean unique;

        ElecMessage(Type t, int id, int ph, int h, boolean u) {
            this.type        = t;
            this.originId    = id;
            this.originPhase = ph;
            this.hops        = h;
            this.unique      = u;
        }
    }
}
