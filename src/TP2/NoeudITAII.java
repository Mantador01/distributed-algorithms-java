package TP2;
import io.jbotsim.core.Color;
import io.jbotsim.core.Link;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;

public class NoeudITAII extends Node {
  private int idCandidat;
  private int phase = 1;
  private boolean estCandidat = false;
  private boolean estElu = false;
  private static boolean electionAlreadyHappened = false;
  private static int compteurMessages = 0;

  @Override
  public void onStart() {
    estCandidat = Math.random() < 0.2 || getID() == 0;

    if (estCandidat) {
      tirerNouvelID();
      System.out.println("🎲 " + getID() + " candidat avec ID " + idCandidat +
                         " en phase " + phase);
      envoyer(new ContenuMessageIR(idCandidat, phase, 1, true, false, getID()));
    }
  }

  @Override
  public void onMessage(Message message) {
    ContenuMessageIR contenu = (ContenuMessageIR)message.getContent();
    int nbNoeuds = getTopology().getNodes().size();

    System.out.println("🔔 " + getID() + " reçoit msg : " + contenu);

    if (contenu.estTermine) {
      estCandidat = false;
      if (!estElu) {
        estElu = true;
        setColor(Color.GREEN);
        System.out.println("✅ " + getID() + " reconnaît " + contenu.id +
                           " comme élu.");
        envoyer(contenu);
      }

      return;
    }

    // Si c’est mon propre message qui revient
    if (contenu.emetteurID == getID() && contenu.phase == phase) {
      if (contenu.hops == nbNoeuds && contenu.estUnique) {
        if (!electionAlreadyHappened) {
          electionAlreadyHappened = true;
          estElu = true;
          setColor(Color.RED);
          envoyer(
              new ContenuMessageIR(idCandidat, phase, 1, true, true, getID()));
        } else {
          System.out.println("⚠️ " + getID() +
                             " trop tard : élection déjà terminée");
          estCandidat = false;
        }
      } else {
        System.out.println(
            "🚫 " + getID() +
            " détecte un conflit ou non unicité → nouvelle phase");
        phase++;
        tirerNouvelID();
        System.out.println("🔁 " + getID() + " passe à la phase " + phase +
                           " avec ID " + idCandidat);
        envoyer(
            new ContenuMessageIR(idCandidat, phase, 1, true, false, getID()));
      }
      return;
    }

    // Marquer comme non unique si même ID détecté avant retour complet
    if (contenu.id == idCandidat && contenu.phase == phase &&
        contenu.hops < nbNoeuds) {
      contenu.estUnique = false;
    }

    // Abandon si un ID plus grand ou une phase plus avancée arrive
    if ((contenu.phase > phase) ||
        (contenu.phase == phase && contenu.id > idCandidat)) {
      if (estCandidat) {
        System.out.println("❌ " + getID() +
                           " abandonne sa candidature (msg plus fort)");
      }
      estCandidat = false;
    }

    // Propagation
    if (!estElu) {
      contenu.hops++;
      envoyer(contenu);
    }
    if (contenu.estTermine && getID() == 0 &&
        contenu.hops == getTopology().getNodes().size()) {
      System.out.println("Nombre total de messages envoyés : " +
                         compteurMessages);
    }
  }

  private void tirerNouvelID() {
    idCandidat = (int)(Math.random() * 21); // entre 0 et 20
  }

  private void envoyer(ContenuMessageIR contenu) {
    for (Link l : getOutLinks()) {
      if (l.endpoint(0) == this) {
        compteurMessages++;
        Node cible = l.endpoint(1);
        System.out.println("--> " + getID() + " envoie à " + cible.getID() +
                           " → " + contenu);
        send(cible, new Message(contenu));
      }
    }
  }

  public static void resetCompteurs() {
    compteurMessages = 0;
    electionAlreadyHappened = false;
  }

  public static int getCompteurMessages() { return compteurMessages; }

  public static boolean electionFinished() { return electionAlreadyHappened; }
}
