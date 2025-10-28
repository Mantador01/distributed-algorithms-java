package TP1;
import io.jbotsim.core.Node;
public class NoeudTest extends Node {
private int compte; // Chaque noeud garde un entier en mémoire
public void onStart() {
compte = 0; // onStart est souvent utilisé pour initialiser les variables
}
public void onSelection() {
compte++;
System.out.println("Vous avez cliqué sur ce noeud " + compte + " fois!");
// Le nombre affiché augmente à chaque clic, mais est différent pour chaque noeud
}
}