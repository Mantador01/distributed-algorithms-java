package TP1;
import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;

public class TP1Main {
    public static void main(String[] args){ // On déclare le programme principal
        Topology tp = new Topology(); // Création d’un nouveau système distribué

        tp.setDefaultNodeModel(NoeudDiffusion.class);
        // tp.setDefaultNodeModel(NoeudArbreCouvrant.class);

        tp.setTimeUnit(1500); // Définit l’intervalle de temps entre deux ticks d’horloge dans la simulation



        new JViewer(tp); // On active l’interface graphique de JBotSim
        tp.start(); // On démarre le tout
    }
}
