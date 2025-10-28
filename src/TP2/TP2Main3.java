package TP2;
// import io.jbotsim.core.Link;
// import io.jbotsim.core.Node;
// import io.jbotsim.core.Topology;
// import io.jbotsim.ui.JViewer;

// public class TP2Main {
//     public static final int TAILLE_ANNEAU = 10;

//     public static void main(String[] args) {
//         Topology tp = new Topology();
//         tp.setTimeUnit(1500); 

//         Node[] noeuds = new Node[TAILLE_ANNEAU];

//         int rayon = 150;
//         int centerX = 250;
//         int centerY = 250;

//         // Crée les noeuds en cercle
//         for (int i = 0; i < TAILLE_ANNEAU; i++) {
//             double angle = 2 * Math.PI * i / TAILLE_ANNEAU;
//             int x = centerX + (int)(rayon * Math.cos(angle));
//             int y = centerY + (int)(rayon * Math.sin(angle));

//             Node noeud = new NoeudTP2();              // Classe perso
//             noeud.setLabel(String.valueOf(i));        // Affiche son numéro
//             noeuds[i] = noeud;
//             tp.addNode(x, y, noeud);
//         }

//         // Crée un lien entre chaque noeud et le suivant (anneau)
//         for (int i = 0; i < TAILLE_ANNEAU; i++) {
//             Node source = noeuds[i];
//             Node cible = noeuds[(i + 1) % TAILLE_ANNEAU];

//             // Ajoute le lien de i → i+1 (le lien est bidirectionnel par défaut) 
//             tp.addLink(new Link(source, cible), true);

//             // Simulation d’un lien unidirectionnel : le récepteur ne renvoie rien 
//             cible.setCommunicationRange(0); // coupe l’émission
//         }

//         System.out.println("✅ Nombre de noeuds : " + tp.getNodes().size());

//         new JViewer(tp); // Interface graphique
//         tp.start();      // Lancement
//     }
// }





import io.jbotsim.core.Link;
import io.jbotsim.core.Link.Orientation;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// public class TP2Main3 {
//   public static final int TAILLE_ANNEAU = 10;

//   public static void main(String[] args) {
//     Topology tp = new Topology();
//     tp.disableWireless(); // Désactive les connexions automatiques
//     // tp.setTimeUnit(1500); // Pour ralentir la simulation

//     Node[] noeuds = new Node[TAILLE_ANNEAU];

//     int rayon = 150;
//     int centerX = 250;
//     int centerY = 250;

//     for (int i = 0; i < TAILLE_ANNEAU; i++) {
//       double angle = 2 * Math.PI * i / TAILLE_ANNEAU;
//       int x = centerX + (int)(rayon * Math.cos(angle));
//       int y = centerY + (int)(rayon * Math.sin(angle));

//       Node noeud = new NoeudITAII();
//       noeud.setLabel(String.valueOf(i));
//       noeuds[i] = noeud;
//       tp.addNode(x, y, noeud);
//     }
//     List<Integer> idsMelanges = new ArrayList<>();
//     for (int i = 0; i < TAILLE_ANNEAU; i++)
//       idsMelanges.add(i);

//     Collections.shuffle(idsMelanges);
//     for (int i = 0; i < TAILLE_ANNEAU; i++) {
//       noeuds[i].setID(idsMelanges.get(i));
//     }

//     // 🔁 Connexions unidirectionnelles explicites
//     for (int i = 0; i < TAILLE_ANNEAU; i++) {
//       tp.addLink(new Link(noeuds[i], noeuds[(i + 1) % TAILLE_ANNEAU],
//                           Orientation.DIRECTED));
//     }

//     System.out.println("✅ Anneau unidirectionnel créé avec " +
//                        tp.getNodes().size() + " noeuds.");

//     new JViewer(tp);
//     tp.start();
//   }
// }






public class TP2Main3 {
  public static final int TAILLE_ANNEAU = 10;
  public static final int NB_EXECUTIONS = 100;

  public static void main(String[] args) {
    int totalMessages = 0;
    int minMessages = Integer.MAX_VALUE;
    int maxMessages = Integer.MIN_VALUE;

    for (int exec = 1; exec <= NB_EXECUTIONS; exec++) {
      NoeudITAII.resetCompteurs(); // Reset static counters
      Topology tp = new Topology();
      tp.disableWireless();

      Node[] noeuds = new Node[TAILLE_ANNEAU];
      int rayon = 150;
      int centerX = 250;
      int centerY = 250;

      for (int i = 0; i < TAILLE_ANNEAU; i++) {
        double angle = 2 * Math.PI * i / TAILLE_ANNEAU;
        int x = centerX + (int)(rayon * Math.cos(angle));
        int y = centerY + (int)(rayon * Math.sin(angle));

        Node noeud = new NoeudITAII();
        noeuds[i] = noeud;
        tp.addNode(x, y, noeud);
      }

      List<Integer> idsMelanges = new ArrayList<>();
      for (int i = 0; i < TAILLE_ANNEAU; i++)
        idsMelanges.add(i);
      Collections.shuffle(idsMelanges);
      for (int i = 0; i < TAILLE_ANNEAU; i++)
        noeuds[i].setID(idsMelanges.get(i));

      for (int i = 0; i < TAILLE_ANNEAU; i++) {
        tp.addLink(new Link(noeuds[i], noeuds[(i + 1) % TAILLE_ANNEAU],
                            Orientation.DIRECTED));
      }

      tp.start();

      // Wait for election to finish
      while (!NoeudITAII.electionFinished()) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      
      int count = NoeudITAII.getCompteurMessages();
      totalMessages += count;
      minMessages = Math.min(minMessages, count);
      maxMessages = Math.max(maxMessages, count);

      System.out.println("🔁 Exécution " + exec + " → " + count + " messages");
    }

    System.out.println("\n📊 Résumé sur " + NB_EXECUTIONS + " exécutions :");
    System.out.println("🔸 Moyenne : " + (totalMessages / NB_EXECUTIONS));
    System.out.println("🔹 Minimum : " + minMessages);
    System.out.println("🔺 Maximum : " + maxMessages);
  }
}
