package TP3;

import io.jbotsim.core.Topology;
import io.jbotsim.core.Link;
import io.jbotsim.core.Link.Orientation;
import io.jbotsim.contrib.messaging.AsyncMessageEngine;
import io.jbotsim.ui.JViewer;

import java.util.ArrayList;
import java.util.List;

public class TP3Main2 {
    public static void main(String[] args) {
        Topology tp = new Topology();
        tp.disableWireless();

        // pour aller plus lentement
        // tp.setTimeUnit(1000);  // 1 seconde par tick

        AsyncMessageEngine ame = new AsyncMessageEngine(tp, 5, AsyncMessageEngine.Type.FIFO);
        tp.setMessageEngine(ame);

        // Instancie 5 coordinateurs tournants
        List<NoeudCoordinateur> nodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            NoeudCoordinateur n = new NoeudCoordinateur();
            tp.addNode(100 + i*80, 200, n);
            nodes.add(n);
        }

        // Graphe complet
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i+1; j < nodes.size(); j++) {
                tp.addLink(new Link(nodes.get(i), nodes.get(j), Orientation.UNDIRECTED));
            }
        }

        new JViewer(tp);
        tp.start();
    }
}
