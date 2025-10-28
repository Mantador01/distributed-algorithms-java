package TP3;
import io.jbotsim.core.Topology;
import io.jbotsim.contrib.messaging.AsyncMessageEngine;
import io.jbotsim.core.Link;
import io.jbotsim.core.Link.Orientation;
import io.jbotsim.core.MessageEngine;
import io.jbotsim.ui.JViewer;
import java.util.ArrayList;
import java.util.List;

public class TP3Main {
    public static void main(String[] args) {
        // 1) Créez la topologie
        Topology tp = new Topology();
        tp.disableWireless();  // on ne veut pas de liens automatiques
        
        // 2) Instanciez 5 nœuds faillibles
        // List<NoeudFaillible> nodes = new ArrayList<>();
        List<NoeudDetecteur> nodes = new ArrayList<>();



        for (int i = 0; i < 5; i++) {
            // NoeudFaillible n = new NoeudFaillible();
            NoeudDetecteur n = new NoeudDetecteur();

            tp.addNode(100 + i * 80, 200, n);  // positionnez-les en ligne
            nodes.add(n);
        }
        
        // 3) Connectez-les en graphe complet (liens bidirectionnels)
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                tp.addLink(new Link(nodes.get(i), nodes.get(j), Orientation.UNDIRECTED));
            }
        }

        MessageEngine me = new AsyncMessageEngine(tp, 5, AsyncMessageEngine.Type.FIFO);
        tp.setMessageEngine(me);
        
        // 4) Lancez l’interface graphique  
        new JViewer(tp);
        tp.start();
    }
}
