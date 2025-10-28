package TP3;
import io.jbotsim.core.Node;
import io.jbotsim.core.Message;
import io.jbotsim.core.Color;

public class NoeudFaillible extends Node {
    private boolean alive;

    @Override
    public void onStart() {
        // Au démarrage, le nœud est en vie et en blanc
        alive = true;
        setColor(Color.white);
    }

    @Override
    public void onSelection() {
        // Ctrl+clic fait tomber le nœud en panne
        if (alive) {
            alive = false;
            setColor(Color.red);
            System.out.println("Node " + getID() + " est tombé en panne");
        }
    }

    @Override
    public void onClock() {
        if (!alive) return;
        // Ici, votre logique périodique (e.g. for consensus)…
    }

    @Override
    public void onMessage(Message m) {
        if (!alive) return;
        // Ici, votre gestion des messages…
    }

    /** Permet de savoir si le nœud est toujours en vie */
    public boolean isAlive() {
        return alive;
    }
}
