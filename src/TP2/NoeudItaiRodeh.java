package TP2;
import io.jbotsim.core.Node;
import io.jbotsim.core.Message;
import io.jbotsim.core.Color;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NoeudItaiRodeh extends Node {
    private static final int   MAX_ID      = 20;   // Taille de l’espace d’identifiants
    private static final double PROBA_CAND  = 0.2;  // 20% de chance d’être candidat
    private static final int   MAX_PHASE    = 10;   // Nombre maximal de phases

    // Statistiques sur 100 exécutions
    private static int runCount        = 0;
    private static int messagesThisRun = 0;
    private static long sumMessages    = 0;
    private static int minMessages     = Integer.MAX_VALUE;
    private static int maxMessages     = 0;

    // État local
    private int   phase;
    private int   myId;
    private boolean isCandidate;
    private boolean finished;

    @Override
    public void onStart() {
        if (getID()==0 && runCount==0) messagesThisRun = 0;
        phase       = 1;
        finished    = false;
        isCandidate = (getID()==0) || (Math.random()<PROBA_CAND);
        setColor(isCandidate ? Color.getColorAt(0) : Color.white);

        if (isCandidate) {
            myId = (int)(Math.random()*(MAX_ID+1));
            ContenuIR cm = new ContenuIR(myId, phase, 1, true, false);
            setColor(Color.getColorAt(myId));
            sendAndCount(cm);
        }
    }

    @Override
    public void onMessage(Message m) {
        ContenuIR cm = (ContenuIR)m.getContent();
        int N = getTopology().getNodes().size();

        // 1) Gestion DONE
        if (cm.isDone) {
            if (!finished) {
                finished = true;
                setColor(Color.green);
                sendAndCount(cm);
                if (getID()==cm.candidateId) endRun();
            }
            return;
        }
        if (finished) return;

        // 2) Si phase > MAX_PHASE, on force la fin
        if (phase>MAX_PHASE && isCandidate) {
            finished = true;
            setColor(Color.green);
            ContenuIR done = new ContenuIR(myId, phase, 0, true, true);
            sendAndCount(done);
            endRun();
            return;
        }

        // 3) Préparer next hop
        ContenuIR next = cm.hops< N ? cm.withIncrementedHop() : cm;

        // 4) Non-candidat : relay jusqu’à N
        if (!isCandidate) {
            if (cm.hops< N) sendAndCount(next);
            return;
        }

        // 5) Abandon ID/phase supérieurs
        if (cm.phase>phase || (cm.phase==phase && cm.candidateId>myId)) {
            isCandidate=false; setColor(Color.white);
            if (cm.hops< N) sendAndCount(next);
            return;
        }

        // 6) Collision avant tour complet
        if (cm.phase==phase && cm.candidateId==myId && cm.hops< N) {
            ContenuIR col = cm.markCollision();
            sendAndCount(col);
            return;
        }

        // 7) Mon ELEC revient après N hops
        if (cm.phase==phase && cm.candidateId==myId && cm.hops==N) {
            if (cm.isUnique) {
                finished=true; setColor(Color.green);
                ContenuIR done = cm.done();
                sendAndCount(done);
                endRun();
            } else {
                phase++;
                myId = (int)(Math.random()*(MAX_ID+1));
                isCandidate=true;
                setColor(Color.getColorAt(myId));
                ContenuIR retry = cm.nextPhase(myId);
                sendAndCount(retry);
            }
            return;
        }

        // 8) Relay standard
        if (cm.hops< N) sendAndCount(next);
    }

    private void sendAndCount(ContenuIR cm) {
        sendAll(new Message(cm));
        messagesThisRun++;
    }

    private void endRun() {
        runCount++;
        sumMessages += messagesThisRun;
        minMessages  = Math.min(minMessages, messagesThisRun);
        maxMessages  = Math.max(maxMessages, messagesThisRun);
        System.out.println("Run #" + runCount + " — messages = " + messagesThisRun);

        if (runCount < 100) {
            reshuffleIDs();
            getTopology().restart();
        } else {
            double moyenne = sumMessages / 100.0;
            System.out.println("=== Stats Itai–Rodeh sur " + runCount + " exécutions ===");
            System.out.println("Moyenne = " + moyenne);
            System.out.println("Minimum = " + minMessages);
            System.out.println("Maximum = " + maxMessages);
            // **Terminer proprement la JVM :**
            System.exit(0);
        }
    }


    private void reshuffleIDs() {
        List<Node> nodes = getTopology().getNodes().stream().collect(Collectors.toList());
        List<Integer> ids = IntStream.range(0,nodes.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(ids);
        for(int i=0;i<nodes.size();i++) nodes.get(i).setID(ids.get(i));
    }
}
