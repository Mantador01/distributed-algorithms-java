package TP2;
import io.jbotsim.core.Topology;
import io.jbotsim.core.Node;
import io.jbotsim.core.Link;
import io.jbotsim.core.Link.Orientation;
import io.jbotsim.ui.JViewer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class TP2Main {
    public static final int TAILLE_ANNEAU = 10;

    public static void main(String[] args){ 
        Topology tp = new Topology(); 

        // tp.setTimeUnit(1500);
        tp.setTimeUnit(500);

        tp.disableWireless();

        double rayon        = 200;    // rayon du cercle (en pixels)
        double centreX      = 300;    // abscisse du centre
        double centreY      = 300;    // ordonnée du centre


        tp.setOrientation(Link.Orientation.DIRECTED);

        List<Node> listeNoeuds = new ArrayList<>();


        for (int i = 0; i < TAILLE_ANNEAU; i++) {

            // Node noeud = new NoeudDiffusion();
            // Node noeud = new NoeudElection();
            // Node noeud = new NoeudItaiRodeh();  
            // Node noeud = new NoeudItai();
            Node noeud = new NoeudItai100();

            double angleDeg   = 360.0 * i / TAILLE_ANNEAU;
            double angleRad   = Math.toRadians(angleDeg);

            // int x = 50 + i * 50;   // colonne i
            // int y = 100;           // ligne fixe

            int x = (int) (Math.cos(angleRad) * rayon + centreX);
            int y = (int) (Math.sin(angleRad) * rayon + centreY);

            tp.addNode(x, y, noeud);
            listeNoeuds.add(noeud);  
        }

        List<Integer> ids = IntStream.range(0, TAILLE_ANNEAU)
                                     .boxed()
                                     .collect(Collectors.toList());
        Collections.shuffle(ids);

        for (int i = 0; i < TAILLE_ANNEAU; i++) {
            listeNoeuds.get(i).setID(ids.get(i));
        }

        System.out.println("Nouvelle attribution des IDs dans l'anneau :");
        for (Node n : listeNoeuds) {
            System.out.println("  nœud en " + n.getLocation() + " → ID=" + n.getID());
        }



        for (int i = 0; i < TAILLE_ANNEAU; i++) {
            Node src  = listeNoeuds.get(i);
            Node dest = listeNoeuds.get((i + 1) % TAILLE_ANNEAU);

            tp.addLink(new Link(src, dest, Orientation.DIRECTED));
        }

        new JViewer(tp);
        tp.start();
    }
}
