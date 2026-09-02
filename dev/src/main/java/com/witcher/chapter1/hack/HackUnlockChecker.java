package main.java.com.witcher.chapter1.hack;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.shop.CipherFragments;

/** Проверка финальной команды шифра. */
public final class HackUnlockChecker {

  private HackUnlockChecker() {
  }

  public static boolean matches(String line, Chapter1Session session) {
    if (line == null || session == null) {
      return false;
    }
    String expected = CipherFragments.orderedUnlockCommand(session);
    if (expected.isEmpty()) {
      return false;
    }
    return normalize(line).equals(normalize(expected));
  }

  private static String normalize(String raw) {
    return raw.trim().toUpperCase().replaceAll("\\s+", "");
  }
}
