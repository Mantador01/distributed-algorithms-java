package TP3;

import io.jbotsim.core.Node;
import io.jbotsim.core.Message;
import io.jbotsim.core.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * NoeudCoordinateur : algorithme de coordinateur tournant hérité de NoeudDetecteur (heartbeat + alive + trusted).
 */
public class NoeudCoordinateur extends NoeudDetecteur {
    // --- Variables de l’algorithme de consensus ---
    protected int v;       // valeur courante
    protected int ts;      // timestamp du dernier update de v
    protected int ronde;   // numéro de la ronde courante
    protected int phase;   // 1=estimation, 2=proposition, 3=ack, 4=décision
    protected boolean decided;  // true une fois la décision prise

    // --- Structures de stockage des messages entre rondes ---
    private Map<Integer, Set<Message>> estimationsReceived;
    private Map<Integer, Message>      propsReceived;
    private Set<Message>               acksReceived;

    @Override
    public void onStart() {
        super.onStart();
        // 1) Initialisations
        v      = (int) Math.round(Math.random() * 100);
        ts     = 0;
        ronde  = 1;
        phase  = 0;      // sera fixé dans newRonde()
        decided = false;

        estimationsReceived = new HashMap<>();
        propsReceived       = new HashMap<>();
        acksReceived        = new HashSet<>();

        setLabel("v=" + v);
        // 2) Lancement de la première ronde
        newRonde();
    }

    @Override
    public void onClock() {
        if (!alive || decided) return;
        // Héritage du heartbeat
        super.onClock();
        // Si je ne fais plus confiance au coord de la ronde, on change de ronde
        // 3.2 Q4
        Node coord = coordinateur(ronde);
        if (!trusted.contains(coord)) {
            System.out.println("Node " + getID() + " suspecte coord " + coord.getID() + " en rnd=" + ronde);
            newRonde();
        }
    }

    @Override
    public void onMessage(Message m) {
        if (!alive || decided) return;
        Object c = m.getContent();
        // 1) Heartbeat
        if (c instanceof String && "HB".equals(c)) {
            super.onMessage(m);
            return;
        }
        // 2) Phase 1 : estimation
        if (c instanceof EstimationMessage) {
            handleEstimation(m);
            return;
        }
        // 3) Phase 2 : proposition
        if (c instanceof PropositionMessage) {
            handleProposition(m);
            return;
        }
        // 4) Phase 3 : acks
        if (c instanceof AckMessage) {
            handleAck(m);
            return;
        }
        // 5) Phase 4 : décision
        if (c instanceof DecisionMessage) {
            // DecisionMessage dm = (DecisionMessage) c;
            // // 1) j’adopte la décision
            // this.v = dm.v;
            // System.out.println("Node " + getID() + " décide v=" + v);
            // // 2) je me colore en vert pour montrer que j’ai décidé
            // setColor(Color.green);
            // // 3) je ne participe plus à l’algorithme
            // alive = false;
            handleDecision(m);
            return;
        }
    }

    /**
     * Lance une nouvelle ronde : gestion des propositions buffered ou phase d'estimation.
     */
    protected void newRonde() {
        ronde++;
        phase = 1;
        setLabel("R=" + ronde + " P=" + phase + " v=" + v);
        System.out.println("Node " + getID() + " passe à la ronde " + ronde);

        // 1) Si proposition buffered pour cette ronde, on la traite immédiatement
        Message buf = propsReceived.remove(ronde);
        if (buf != null && buf.getContent() instanceof PropositionMessage) {
            PropositionMessage pm = (PropositionMessage) buf.getContent();
            Node coord = coordinateur(ronde);
            // si coord suspecté → NACK et nouvelle ronde
            if (!trusted.contains(coord)) {
                send(coord, new Message(new AckMessage(ronde, false)));
                System.out.println("Node " + getID() +
                    " buffered PROP(r=" + ronde + ") suspecté → NACK");
                newRonde();
                return;
            }
            // sinon j'accepte
            this.v = pm.v;
            this.ts = ronde;
            System.out.println("Node " + getID() +
                " buffered PROP(v=" + v + ",r=" + ronde + ") → ACK");
            send(coord, new Message(new AckMessage(ronde, true)));
            newRonde();
            return;
        }

        // 2) Pas de proposition buffered => phase d'estimation
        setLabel("R=" + ronde + " P=" + phase + " v=" + v);
        EstimationMessage em = new EstimationMessage(v, ts, ronde);
        Node coord = coordinateur(ronde);
        send(coord, new Message(em));
        System.out.println("Node " + getID() +
            " envoie EST(v=" + v + ",ts=" + ts + ",rnd=" + ronde + ")→" + coord.getID());
    }

    /**
     * Phase 1 : réception des messages d'estimation.
     */
    private void handleEstimation(Message m) {
        EstimationMessage em = (EstimationMessage) m.getContent();
        int msgRnd = em.rnd;
        // ronde passée => ignorer
        if (msgRnd < this.ronde) return;
        // ronde future => bufferiser
        if (msgRnd > this.ronde) {
            estimationsReceived
                .computeIfAbsent(msgRnd, k -> new HashSet<>())
                .add(m);
            return;
        }
        // sinon msgRnd == ronde => agréger
        Set<Message> buf = estimationsReceived
            .computeIfAbsent(msgRnd, k -> new HashSet<>());
        buf.add(m);

        // test de majorité
        int N        = getTopology().getNodes().size();
        int maj      = N/2 + 1;
        int received = buf.size() + 1; // +1 pour moi-même
        if (received >= maj && phase == 1) {
            // je choisis la valeur au ts max
            int bestTs = this.ts;
            int bestV  = this.v;
            for (Message mm : buf) {
                EstimationMessage eem = (EstimationMessage) mm.getContent();
                if (eem.ts > bestTs) {
                    bestTs = eem.ts;
                    bestV  = eem.v;
                }
            }
            this.v  = bestV;
            this.ts = bestTs;
            System.out.println("Node " + getID() +
                " majorité EST reçue → v=" + v + ", ts=" + ts);
            // passer en phase 2 : proposition
            propose();
        }
    }

    /**
     * Phase 2 : envoi de la proposition par le coordinateur.
     */
    private void propose() {
        phase = 2;
        acksReceived.clear();
        setLabel("R=" + ronde + " P=" + phase + " v=" + v);
        PropositionMessage pm = new PropositionMessage(v, ronde);
        sendAll(new Message(pm));
        System.out.println("Coord " + getID() +
            " diffuse PROP(v=" + v + ",r=" + ronde + ")");
    }

    /**
     * Phase 2 côté non-coordinateur : réception de la proposition.
     */
    private void handleProposition(Message m) {
        PropositionMessage pm = (PropositionMessage) m.getContent();
        int msgRnd = pm.rnd;
        Node sender = m.getSender();
        // trop vieux => NACK
        if (msgRnd < this.ronde) {
            send(sender, new Message(new AckMessage(msgRnd, false)));
            System.out.println("Node " + getID() +
                " PROP(r=" + msgRnd + ") passée → NACK");
            return;
        }
        // future => bufferiser
        if (msgRnd > this.ronde) {
            propsReceived.put(msgRnd, m);
            return;
        }
        // prop pour ma ronde => ACK+
        this.v  = pm.v;
        this.ts = msgRnd;
        System.out.println("Node " + getID() +
            " accepte PROP(v=" + v + ",r=" + ronde + ")");
        send(sender, new Message(new AckMessage(msgRnd, true)));
        // puis on passe à la ronde suivante
        newRonde();
    }

    /**
     * Phase 3 : collecte des ACKs côté coordinateur.
     */
    private void handleAck(Message m) {
        AckMessage am = (AckMessage) m.getContent();
        int msgRnd = am.rnd;
        // ne traiter que les ACKs pour la ronde courante
        if (msgRnd != this.ronde) return;
        acksReceived.add(m);
        int N        = getTopology().getNodes().size();
        int maj      = N/2 + 1;
        int count    = acksReceived.size() + 1; // +1 pour moi-même
        if (count >= maj && phase == 2) {
            System.out.println("Coord " + getID() +
                " majorité de ACKs reçue en rnd=" + ronde);
            // si tous positifs => décision
            boolean allOk = true;
            for (Message mm : acksReceived) {
                if (!((AckMessage) mm.getContent()).ok) {
                    allOk = false; break;
                }
            }
            if (allOk) {
                phase = 4;
                System.out.println("Coord " + getID() +
                    " décide v=" + v + " en rnd=" + ronde);
                DecisionMessage dm = new DecisionMessage(v);
                sendAll(new Message(dm));
                decided = true;
            }
            else {
                System.out.println("Coord " + getID() +
                    " échec PROP en rnd=" + ronde + " → next ronde");
                newRonde();
            }
        }
    }

    /**
     * Phase 4 : réception de la décision projetée.
     */
    private void handleDecision(Message m) {
        DecisionMessage dm = (DecisionMessage) m.getContent();
        if (!decided) {
            decided = true;
            alive   = false;           // ← on désactive toute participation ultérieure
            this.v = dm.v;
            setColor(Color.green);
            System.out.println("Node " + getID() + " décide v=" + v);
        }
    }
    

    /** Retourne le coordinateur pour la ronde donnée */
    private Node coordinateur(int rnd) {
        int id = rnd % getTopology().getNodes().size();
        return getTopology().findNodeById(id);
    }

    // ==== Classes de messages ====

    private static class EstimationMessage {
        final int v, ts, rnd;
        EstimationMessage(int v, int ts, int rnd) {
            this.v = v; this.ts = ts; this.rnd = rnd;
        }
    }

    private static class PropositionMessage {
        final int v, rnd;
        PropositionMessage(int v, int rnd) {
            this.v = v; this.rnd = rnd;
        }
    }

    private static class AckMessage {
        final int rnd;
        final boolean ok;
        AckMessage(int rnd, boolean ok) {
            this.rnd = rnd; this.ok = ok;
        }
    }

    private static class DecisionMessage {
        final int v;
        DecisionMessage(int v) { this.v = v; }
    }
}
