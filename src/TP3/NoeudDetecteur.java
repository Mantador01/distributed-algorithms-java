package TP3;

import io.jbotsim.core.Node;
import io.jbotsim.core.Message;
import io.jbotsim.core.Color;
import java.util.HashSet;
import java.util.Set;

public class NoeudDetecteur extends Node {
    protected boolean alive;
    private int clockCount;

    // Pour détecteur Heartbeat
    protected Set<Node> trusted;      // actuellement fiables
    private Set<Node> heardRecent;   // ont émis un HB dans la fenêtre courante

    @Override
    public void onStart() {
        // héritage de NoeudFaillible
        alive = true;
        setColor(Color.white);

        // initialisation du détecteur
        clockCount   = 0;
        trusted      = new HashSet<>(getTopology().getNodes()); // au départ, on fait confiance à tous
        heardRecent  = new HashSet<>();
        updateLabel();
    }

    @Override
    public void onSelection() {
        if (alive) {
            alive = false;
            setColor(Color.red);
            System.out.println("Node " + getID() + " panne simulée");
        }
    }

    @Override
    public void onClock() {
        if (!alive) return;

        clockCount++;

        // 1) Tous les 10 ticks, j'envoie un heartbeat à tous mes voisins
        if (clockCount % 10 == 0) {
            sendAll(new Message("HB"));
        }

        // 2) Toutes les 20 ticks, je mets à jour trusted
        if (clockCount % 20 == 0) {
            // reconstruire le set de confiance : seuls ceux qui ont envoyé un HB,
            // plus moi-même
            Set<Node> newTrusted = new HashSet<>(heardRecent);
            newTrusted.add(this);

            if (!newTrusted.equals(trusted)) {
                trusted = newTrusted;
                updateLabel();
                System.out.println("Node " + getID() +
                    " met à jour trusted : " + trusted.stream()
                                                        .map(Node::getID)
                                                        .sorted()
                                                        .toList());
            }
            // réinitialiser la fenêtre
            heardRecent.clear();
        }
    }

    @Override
    public void onMessage(Message msg) {
        if (!alive) return;
        // ne traiter que les heartbeats
        if ("HB".equals(msg.getContent())) {
            Node sender = msg.getSender();
            heardRecent.add(sender);
            // si on le suspectait, on peut déjà pré-ajouter dans trusted
            if (!trusted.contains(sender)) {
                trusted.add(sender);
                updateLabel();
                System.out.println("Node " + getID() +
                    " rétablit confiance en Node " + sender.getID());
            }
        }
    }

    /** Affiche la liste des nœuds de confiance dans l’infobulle */
    private void updateLabel() {
        setLabel("T=" + trusted.stream()
                                .map(Node::getID)
                                .sorted()
                                .toList());
    }

    /** Pour expérimentation : expose trusted */
    public Set<Node> getTrusted() {
        return trusted;
    }
}
