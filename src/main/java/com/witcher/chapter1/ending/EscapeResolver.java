package main.java.com.witcher.chapter1.ending;

import main.java.com.witcher.chapter1.Chapter1Session;

/** Итог: подозрение vs доверие после успешного взлома. */
public final class EscapeResolver {

  private EscapeResolver() {
  }

  public static EscapeEnding resolve(Chapter1Session session) {
    if (session == null || !session.escapeAttemptUnlocked()) {
      return EscapeEnding.LOCKED;
    }
    return session.suspicionDominates() ? EscapeEnding.TRUE_ESCAPE : EscapeEnding.FALSE_ESCAPE;
  }
}
