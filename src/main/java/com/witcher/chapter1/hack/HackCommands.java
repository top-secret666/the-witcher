package main.java.com.witcher.chapter1.hack;

/** Белый список команд терминала. */
public enum HackCommands {
  HELP,
  SCAN,
  DECRYPT,
  UNLOCK,
  BREAK_LOOP,
  EXIT;

  public static HackCommands parse(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String upper = raw.trim().toUpperCase();
    if (upper.startsWith("BREAK_LOOP")) {
      return BREAK_LOOP;
    }
    int space = upper.indexOf(' ');
    String head = space > 0 ? upper.substring(0, space) : upper;
    try {
      return HackCommands.valueOf(head);
    } catch (IllegalArgumentException error) {
      return null;
    }
  }
}
