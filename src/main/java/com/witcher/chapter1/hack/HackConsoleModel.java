package main.java.com.witcher.chapter1.hack;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.shop.CipherFragments;

/**
 * Модель терминала взлома: ввод, таймер, одна попытка за заход.
 */
public final class HackConsoleModel {

  public static final int TIMER_TICKS = 900;
  public static final int SCAN_HINT_AFTER_FAILURE = 1;

  private final Chapter1Session session;
  private final StringBuilder input = new StringBuilder();
  private final StringBuilder log = new StringBuilder();
  private int ticksRemaining = TIMER_TICKS;
  private int failedAttempts;
  private boolean closed;

  public HackConsoleModel(Chapter1Session session) {
    this.session = session;
    appendLine("TERMINAL v0.9 — type HELP");
  }

  public String logText() {
    return log.toString();
  }

  public String inputLine() {
    return input.toString();
  }

  public int ticksRemaining() {
    return ticksRemaining;
  }

  public boolean isClosed() {
    return closed;
  }

  public boolean tick() {
    if (closed) {
      return false;
    }
    ticksRemaining--;
    return ticksRemaining <= 0;
  }

  public void appendChar(char c) {
    if (closed || c < 32) {
      return;
    }
    input.append(c);
  }

  public void backspace() {
    if (closed || input.isEmpty()) {
      return;
    }
    input.deleteCharAt(input.length() - 1);
  }

  public HackResult submitLine() {
    if (closed) {
      return HackResult.none();
    }
    String line = input.toString().trim();
    input.setLength(0);
    if (line.isEmpty()) {
      return HackResult.none();
    }
    appendLine("> " + line);
    return dispatch(line);
  }

  private HackResult dispatch(String line) {
    HackCommands cmd = HackCommands.parse(line);
    if (cmd == null) {
      appendLine("UNKNOWN COMMAND");
      return HackResult.none();
    }
    return switch (cmd) {
      case HELP -> {
        appendLine("HELP | SCAN | DECRYPT <code> | UNLOCK | EXIT");
        yield HackResult.none();
      }
      case SCAN -> {
        if (failedAttempts >= SCAN_HINT_AFTER_FAILURE) {
          appendLine("ORDER MATTERS, WHITE WOLF.");
        } else {
          appendLine("FOUR SLOTS. FOUR KEYS.");
        }
        yield HackResult.none();
      }
      case DECRYPT -> {
        String arg = line.length() > 7 ? line.substring(7).trim() : "";
        appendLine(arg.isEmpty() ? "MISSING FRAGMENT" : "STORED: " + arg);
        yield HackResult.none();
      }
      case UNLOCK -> {
        boolean ok = HackUnlockChecker.matches(line, session);
        if (ok) {
          appendLine("LOOP SIGNATURE ACCEPTED");
          closed = true;
          yield HackResult.success();
        }
        failedAttempts++;
        appendLine("SIGNATURE REJECTED");
        yield HackResult.none();
      }
      case EXIT -> {
        closed = true;
        yield HackResult.exit();
      }
    };
  }

  private void appendLine(String line) {
    log.append(line).append('\n');
  }

  public enum HackResultType {
    NONE, SUCCESS, EXIT, TIMEOUT
  }

  public record HackResult(HackResultType type) {
    static HackResult none() {
      return new HackResult(HackResultType.NONE);
    }

    static HackResult success() {
      return new HackResult(HackResultType.SUCCESS);
    }

    static HackResult exit() {
      return new HackResult(HackResultType.EXIT);
    }

    static HackResult timeout() {
      return new HackResult(HackResultType.TIMEOUT);
    }
  }
}
