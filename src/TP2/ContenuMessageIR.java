package TP2;
public class ContenuMessageIR {
    int id;
    int phase;
    int hops;
    boolean estUnique;
    boolean estTermine;
    public int emetteurID;
  
    public ContenuMessageIR(int id, int phase, int hops, boolean unique,
                            boolean termine, int emetteurID) {
      this.id = id;
      this.phase = phase;
      this.hops = hops;
      this.estUnique = unique;
      this.estTermine = termine;
      this.emetteurID = emetteurID;
    }
    @Override
    public String toString() {
      return "[ID=" + id + ", phase=" + phase + ", hops=" + hops +
          ", unique=" + estUnique + ", terminé=" + estTermine + "]";
    }
  }
  