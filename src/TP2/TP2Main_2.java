package TP2;
import io.jbotsim.core.Topology;
import io.jbotsim.core.Node;
import io.jbotsim.core.Link;
import io.jbotsim.core.Link.Orientation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TP2Main_2 {
    public static final int TAILLE_ANNEAU = 10;

    public static void main(String[] args){ 
        Topology tp = new Topology(); 

        // On veut 100 runs rapides, sans interface ni time unit :
        tp.setTimeUnit(1);
        //new JViewer(tp);

        tp.disableWireless();
        tp.setOrientation(Orientation.DIRECTED);

        // Paramètres du cercle
        double rayon   = 200, cx = 300, cy = 300;

        // Création des nœuds et de l’anneau
        List<Node> listeNoeuds = new ArrayList<>();
        for (int i = 0; i < TAILLE_ANNEAU; i++) {
            // Node noeud = new NoeudElection();
            // Node noeud = new NoeudItaiRodeh();
            Node noeud = new NoeudItai100();  
            double theta = Math.toRadians(360.0 * i / TAILLE_ANNEAU);
            int x = (int)(Math.cos(theta) * rayon + cx);
            int y = (int)(Math.sin(theta) * rayon + cy);
            tp.addNode(x, y, noeud);
            listeNoeuds.add(noeud);
        }

        // Shuffle initial des IDs
        List<Integer> ids = IntStream.range(0, TAILLE_ANNEAU)
                                     .boxed()
                                     .collect(Collectors.toList());
        Collections.shuffle(ids);
        for (int i = 0; i < TAILLE_ANNEAU; i++) {
            listeNoeuds.get(i).setID(ids.get(i));
        }

        // Construction de l’anneau unidirectionnel
        for (int i = 0; i < TAILLE_ANNEAU; i++) {
            Node src  = listeNoeuds.get(i);
            Node dest = listeNoeuds.get((i + 1) % TAILLE_ANNEAU);
            tp.addLink(new Link(src, dest, Orientation.DIRECTED));
        }

        // Lancement unique : NoeudElection gère les 100 runs via restart()
        tp.start();
    }
}
