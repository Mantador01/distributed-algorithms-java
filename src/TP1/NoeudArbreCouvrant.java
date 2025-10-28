package TP1;
import io.jbotsim.core.Node;
import io.jbotsim.core.Link;
import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import java.awt.desktop.SystemEventListener;
import java.util.ArrayList;
import java.util.List;

public class NoeudArbreCouvrant extends Node {
    // Variables d'instance pour la construction de l'arbre
    private Node pere;
    private boolean aRejointArbre;
    private List<Node> enfants;
    private int enAttente;      // Nombre de JOIN envoyés
    private int reponsesRecues; // Nombre de réponses BACK/NO reçues

    // Variables statiques pour le comptage des messages
    private static int compteurConstruction = 0; // Compte les messages JOIN, BACK et BACKNO
    private static int compteurDiffusion   = 0;    // Compte les messages BCAST
    private static int nbNoeudsBcast = 0;            // Nombre de nœuds ayant reçu le BCAST

    // Variable locale pour savoir si ce nœud a déjà reçu le BCAST
    private boolean aRecuBcast = false;

    @Override
    public void onStart() {
        aRejointArbre = false;
        pere = null;
        enfants = new ArrayList<>();
        setColor(Color.white);
        enAttente = 0;
        reponsesRecues = 0;
    }

    @Override
    public void onSelection() {
        // Devenir la racine si ce n'est pas encore dans l'arbre
        if (!aRejointArbre) {
            aRejointArbre = true;
            setColor(Color.yellow);
            // Envoyer JOIN à tous les voisins et incrémenter le compteur de construction
            for (Node voisin : getNeighbors()) {
                send(voisin, new Message("JOIN"));
                compteurConstruction++;
                enAttente++;
            }
            // Si aucun voisin n'est invité, on est terminé (racine sans enfant)
            if (enAttente == 0) {
                setColor(Color.green);
            }
        }
    }

    @Override
    public void onMessage(Message m) {
        String type = (String) m.getContent();
        switch (type) {
            case "JOIN":
                // Si le nœud n'a pas encore rejoint l'arbre, il accepte l'invitation
                if (!aRejointArbre) {
                    aRejointArbre = true;
                    pere = m.getSender();
                    setColor(Color.yellow);
                    // Colorer le lien avec le père
                    Link lien = getCommonLinkWith(pere);
                    if (lien != null) {
                        lien.setColor(Color.blue);
                        lien.setWidth(2);
                    }
                    // Envoyer JOIN aux autres voisins (sauf le père)
                    for (Node v : getNeighbors()) {
                        if (v != pere) {
                            send(v, new Message("JOIN"));
                            compteurConstruction++;
                            enAttente++;
                        }
                    }
                    // Si aucun voisin n'est invité, renvoyer BACK directement au père
                    if (enAttente == 0) {
                        send(pere, new Message("BACK"));
                        compteurConstruction++;
                        setColor(Color.green);
                    }
                } else {
                    // Si le nœud a déjà rejoint l'arbre, il refuse en renvoyant BACKNO
                    send(m.getSender(), new Message("BACKNO"));
                    System.out.println("JOIN refusé à " + m.getSender() + "Je suis " + this);
                    compteurConstruction++;
                }
                break;

            case "BACK":
                // L'émetteur devient enfant (s'il n'est pas déjà dans la liste)
                if (!enfants.contains(m.getSender())) {
                    enfants.add(m.getSender());
                }
                reponsesRecues++;
                // compteurConstruction++;
                System.out.println("BACK reçu de " + m.getSender() + "Je suis " + this);
                checkIfDone();
                break;

            case "BACKNO":
                reponsesRecues++;
                // compteurConstruction++;
                System.out.println("BACKNO reçu de " + m.getSender() + "Je suis " + this);
                checkIfDone();
                break;

            case "BCAST": // diffusion
                if (!aRecuBcast) {
                    aRecuBcast = true;
                    setColor(Color.red);
                    nbNoeudsBcast++;
                    // Relayer le BCAST aux enfants en incrémentant le compteur de diffusion
                    for (Node child : enfants) {
                        send(child, new Message("BCAST"));
                        compteurDiffusion++;
                    }
                    // Si c'est le dernier nœud à recevoir le BCAST, afficher les totaux
                    if (nbNoeudsBcast == getTopology().getNodes().size()-1) {
                        System.out.println("Diffusion terminée !");
                        System.out.println("Messages de construction = " + compteurConstruction);
                        System.out.println("Messages de diffusion    = " + compteurDiffusion);
                        System.out.println("TOTAL = " + (compteurConstruction + compteurDiffusion));
                    }
                }
                break;

            default:
                break;
        }
    }

    // Vérifie si le nœud a reçu toutes les réponses à ses invitations JOIN
    private void checkIfDone() {
        if (reponsesRecues == enAttente) {
            // Passage en vert, indiquant la fin de la construction pour ce nœud
            setColor(Color.green);
            if (pere != null) {
                send(pere, new Message("BACK"));
                compteurConstruction++;
            } else {
                // Si c'est la racine, lancer la diffusion
                startBroadcast();
            }
        }
    }

    // Méthode appelée par la racine pour lancer la diffusion via BCAST
    private void startBroadcast() {
        setColor(Color.red);
        // Envoyer BCAST à chacun des enfants et incrémenter le compteur de diffusion
        for (Node child : enfants) {
            send(child, new Message("BCAST"));
            compteurDiffusion++;
        }
    }
}
