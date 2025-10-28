package TP2;
import io.jbotsim.core.Node;
import io.jbotsim.core.Message;
import io.jbotsim.core.Color;
import java.util.Random;
import java.util.List;

public class NoeudItai extends Node {
    // État local
    private int phase;               // phase courante
    private boolean isCandidate;     // vrai si se porte candidat
    private int myId;                // identifiant aléatoire
    private boolean electionDone;    // vrai si l'élection est terminée
    private boolean electionLaunched;// vrai si startElection() déjà appelé
    private final Random rand = new Random();

    // Constantes
    private static final double PROBA_INIT = 0.2;
    private static final int MAX_ID = 20;

    @Override
    public void onStart() {
        phase = 1;
        electionDone = false;
        electionLaunched = false;
        isCandidate = (getID() == 0) || (Math.random() < PROBA_INIT);
        setColor(isCandidate ? Color.getColorAt(getID()) : Color.white);
        System.out.println("Node " + getID() + " onStart: isCandidate=" + isCandidate);
    }

    @Override
    public void onClock() {
        if (isCandidate && !electionLaunched) {
            electionLaunched = true;
            System.out.println("Node " + getID() + " onClock: launching election");
            startElection();
        }
    }

    /**
     * Démarre une phase d'élection
     */
    private void startElection() {
        myId = rand.nextInt(MAX_ID + 1);
        System.out.println("Node " + getID() + " startElection: phase=" + phase + ", myId=" + myId);
        ElecMessage m = new ElecMessage(myId, phase, 1, true);
        Node succ = getNext();
        if (succ != null) {
            System.out.println("Node " + getID() + " sending ELEC to " + succ.getID() + ": {id=" + myId + ", phase=" + phase + ", hops=1, unique=true}");
            send(succ, new Message(m));
        } else {
            System.out.println("Node " + getID() + " startElection: no successor to send");
        }
    }

    @Override
    public void onMessage(Message msg) {
        ElecMessage m = (ElecMessage) msg.getContent();
        int N = getTopology().getNodes().size();
        System.out.println("Node " + getID() + " onMessage: received " + m.type + " {id=" + m.originId + ", phase=" + m.originPhase + ", hops=" + m.hops + ", unique=" + m.unique + "}");

        // Message LEAD
        if (m.type == ElecMessage.Type.LEAD && !electionDone) {
            electionDone = true;
            setColor(Color.green);
            System.out.println("Node " + getID() + " elected as leader");
            if (m.hops < N) {
                m.hops++;
                Node succ = getNext();
                if (succ != null) send(succ, new Message(m));
            }
            return;
        }
        if (electionDone) {
            System.out.println("Node " + getID() + " ignoring message, election done");
            return;
        }

        // Non-candidat
        if (!isCandidate) {
            System.out.println("Node " + getID() + " relaying as non-candidate");
            m.hops++;
            Node succ = getNext();
            if (succ != null) send(succ, new Message(m));
            return;
        }

        // Candidat : message ELEC
        if (m.originId == myId && m.originPhase == phase && m.hops == N) {
            if (m.unique) {
                electionDone = true;
                setColor(Color.green);
                System.out.println("Node " + getID() + " wins election with id=" + myId);
                ElecMessage lead = new ElecMessage(myId, phase, 1, true);
                lead.type = ElecMessage.Type.LEAD;
                Node succ = getNext();
                if (succ != null) send(succ, new Message(lead));
            } else {
                phase++;
                System.out.println("Node " + getID() + " conflict on id=" + myId + ", advancing to phase=" + phase);
                startElection();
            }
            return;
        }

        if (m.originId == myId && m.originPhase == phase && m.hops < N) {
            System.out.println("Node " + getID() + " detected conflict mid-ring on id=" + myId);
            m.unique = false;
            m.hops++;
            Node succ = getNext();
            if (succ != null) send(succ, new Message(m));
            return;
        }

        if (m.originPhase > phase || (m.originPhase == phase && m.originId > myId)) {
            isCandidate = false;
            setColor(Color.white);
            System.out.println("Node " + getID() + " abandoning as id=" + myId + " is weaker than incoming {id=" + m.originId + ", phase=" + m.originPhase + "}");
            m.hops++;
            Node succ = getNext();
            if (succ != null) send(succ, new Message(m));
            return;
        }

        System.out.println("Node " + getID() + " ignoring weaker message");
    }

    /**
     * Successeur direct dans l'anneau dirigé
     */
    private Node getNext() {
        List<Node> out = getOutNeighbors();
        return out.isEmpty() ? null : out.get(0);
    }

    /**
     * Message d'élection
     */
    private static class ElecMessage {
        enum Type { ELEC, LEAD }
        Type type = Type.ELEC;
        final int originId;
        final int originPhase;
        int hops;
        boolean unique;

        ElecMessage(int id, int phase, int hops, boolean unique) {
            this.originId = id;
            this.originPhase = phase;
            this.hops = hops;
            this.unique = unique;
        }
    }
}
