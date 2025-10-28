package TP2;
public class ContenuIR {
    public final int  candidateId;  // identifiant aléatoire [0…20]
    public final int  phase;        // numéro de phase
    public final int  hops;         // nombre de relais actuels
    public final boolean isUnique;  // reste unique sur le tour en cours ?
    public final boolean isDone;    // true quand l’élection est terminée

    public ContenuIR(int candidateId, int phase, int hops, boolean isUnique, boolean isDone) {
        this.candidateId = candidateId;
        this.phase       = phase;
        this.hops        = hops;
        this.isUnique    = isUnique;
        this.isDone      = isDone;
    }

    public ContenuIR withIncrementedHop() {
        return new ContenuIR(candidateId, phase, hops + 1, isUnique, isDone);
    }
    public ContenuIR markCollision() {
        return new ContenuIR(candidateId, phase, hops, false, isDone);
    }
    public ContenuIR nextPhase(int newId) {
        return new ContenuIR(newId, phase + 1, 1, true, false);
    }
    public ContenuIR done() {
        return new ContenuIR(candidateId, phase, hops, isUnique, true);
    }
}
