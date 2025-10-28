package TP2;

public class ContenuMessage {
    public enum Type { ELEC, LEAD }
    public final Type type;   // ELEC = candidature, LEAD = annonce de victoire
    public final int  id;     // identifiant du candidat / leader

    public ContenuMessage(Type type, int id) {
        this.type = type;
        this.id   = id;
    }
}
