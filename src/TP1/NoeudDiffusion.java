package TP1;
import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;

public class NoeudDiffusion extends Node {
    // --- Variables statiques partagées par TOUS les nœuds de ce type ---
    private static int compteurMessages = 0;     // Nombre total de messages envoyés
    private static int nbNoeudsAyantRecu = 0;    // Nombre de nœuds ayant déjà reçu le message

    // --- Variable locale à chaque nœud ---
    private boolean aRecu;

    @Override
    public void onStart() {
        aRecu = false;
        setColor(Color.white);
    }

    @Override
    public void onSelection() {
        // Quand on Ctrl+clic, si on n'a pas encore reçu le message, on initie la diffusion
        if (!aRecu){
            aRecu = true;
            setColor(Color.red);

            // Ce nœud vient de recevoir le message pour la première fois => on s'ajoute au compteur
            nbNoeudsAyantRecu++;
            // Vérifier si on est potentiellement le dernier
            if (nbNoeudsAyantRecu == getTopology().getNodes().size()){
                System.out.println("Tous les nœuds ont reçu le message !");
                System.out.println("Nombre total de messages envoyés = " + compteurMessages);
            }

            // On envoie le message à tous les voisins
            sendAll(new Message("BCAST"));
            // Incrémenter le compteur de messages (autant que le nombre de voisins)
            compteurMessages += getNeighbors().size();
        }
    }

    @Override
    public void onMessage(Message message) {
        // Lors de la réception d'un message : si ce nœud ne l'avait jamais reçu, il se propage
        if (!aRecu){
            aRecu = true;
            setColor(Color.red);

            // Incrémentation du nombre de nœuds ayant reçu
            nbNoeudsAyantRecu++;
            if (nbNoeudsAyantRecu == getTopology().getNodes().size()){
                System.out.println("Tous les nœuds ont reçu le message !");
                System.out.println("Nombre total de messages envoyés = " + compteurMessages);
            }

            // Envoi aux voisins
            sendAll(new Message("BCAST"));
            compteurMessages += getNeighbors().size();
        }
    }
}
