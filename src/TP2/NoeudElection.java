package TP2;
import io.jbotsim.core.Node;
import io.jbotsim.core.Message;
import io.jbotsim.core.Color;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NoeudElection extends Node {
    // === compteurs globaux pour 100 exécutions ===
    private static int runCount               = 0;
    private static int messagesThisRun        = 0;
    private static long sumMessages           = 0;
    private static int minMessages            = Integer.MAX_VALUE;
    private static int maxMessages            = 0;
    private static int initialCandidatesCount = 0;

    // probabilité de candidature initiale
    private static final double PROBA_CANDIDAT = 1.0;

    // état local
    private int    leaderID;
    private boolean isElected;
    private boolean isCandidate;

    @Override
    public void onStart() {
        // reset une seule fois par run (node 0)
        if (getID() == 0) {
            messagesThisRun        = 0;
            initialCandidatesCount = 0;
        }

        // tirage du statut candidat
        isCandidate = (getID() == 0) || (Math.random() < PROBA_CANDIDAT);
        if (isCandidate) {
            initialCandidatesCount++;
            setColor(Color.getColorAt(getID()));
        } else {
            setColor(Color.white);
        }

        leaderID  = getID();
        isElected = false;

        // si candidat, on propose son ID
        if (isCandidate) {
            ContenuMessage cm = new ContenuMessage(ContenuMessage.Type.ELEC, leaderID);
            sendAndCount(cm);
        }
    }

    @Override
    public void onMessage(Message m) {
        ContenuMessage cm = (ContenuMessage) m.getContent();
        switch (cm.type) {

            case ELEC:
                // (a) un non-candidat reçoit un ID plus petit → il devient candidat
                if (!isCandidate && cm.id < getID()) {
                    isCandidate = true;
                    initialCandidatesCount++;
                    leaderID = getID();
                    setColor(Color.getColorAt(leaderID));
                    sendAndCount(new ContenuMessage(ContenuMessage.Type.ELEC, leaderID));
                    break;
                }
                // (b) si c’est son propre ELEC qui revient → élu
                if (cm.id == getID()) {
                    isElected = true;
                    setColor(Color.green);
                    sendAndCount(new ContenuMessage(ContenuMessage.Type.LEAD, getID()));
                }
                // (c) sinon si cm.id > leaderID → adoption et relais
                else if (cm.id > leaderID) {
                    leaderID = cm.id;
                    setColor(Color.getColorAt(leaderID));
                    sendAndCount(cm);
                }
                // sinon on ignore
                break;

            case LEAD:
                // (i) le leader récupère son propre LEAD → fin de run
                if (cm.id == getID() && isElected) {
                    runCount++;
                    sumMessages += messagesThisRun;
                    minMessages  = Math.min(minMessages, messagesThisRun);
                    maxMessages  = Math.max(maxMessages, messagesThisRun);

                    System.out.println("Run #" + runCount
                        + " — candidats initiaux = " + initialCandidatesCount
                        + " — messages = "       + messagesThisRun);

                    if (runCount < 100) {
                        shuffleNodeIDs();
                        getTopology().restart();
                    } else {
                        double moyenne = sumMessages / 100.0;
                        System.out.println("=== Statistiques sur 100 exécutions ===");
                        System.out.println("Moyenne = "  + moyenne);
                        System.out.println("Minimum = "  + minMessages);
                        System.out.println("Maximum = "  + maxMessages);
                    }
                }
                // (ii) sinon, tout autre nœud relaie le LEAD
                else {
                    leaderID = cm.id;
                    setColor(Color.getColorAt(leaderID));
                    sendAndCount(cm);
                }
                break;
        }
    }

    /** Mélange aléatoire des IDs (exercice 1.3) */
    private void shuffleNodeIDs() {
        List<Node> nodes = getTopology().getNodes().stream().collect(Collectors.toList());
        List<Integer> ids = IntStream.range(0, nodes.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(ids);
        for (int i = 0; i < nodes.size(); i++)
            nodes.get(i).setID(ids.get(i));
    }

    /** Envoie un message et incrémente messagesThisRun systématiquement */
    private void sendAndCount(ContenuMessage cm) {
        sendAll(new Message(cm));
        messagesThisRun++;
    }
}
