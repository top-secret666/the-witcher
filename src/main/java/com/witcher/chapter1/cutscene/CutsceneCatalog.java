package main.java.com.witcher.chapter1.cutscene;

import java.util.EnumMap;
import java.util.Map;

/**
 * Пути к GIF/PNG-катсценам. Файлы — в {@code assets/cutscenes/chapter1/}.
 * Отсутствующий файл — {@link main.java.com.witcher.chapter1.Chapter1Director} пропускает катсцену.
 */
public final class CutsceneCatalog {

  private static final String ROOT = "/assets/cutscenes/chapter1/";

  private static final Map<CutsceneId, String> PATHS = new EnumMap<>(CutsceneId.class);

  static {
    PATHS.put(CutsceneId.LOOP_WAKE, ROOT + "loop_wake.gif");
    PATHS.put(CutsceneId.ILLUSION_WRONG, ROOT + "illusion_wrong.gif");
    PATHS.put(CutsceneId.BATTLE_INTRO, ROOT + "battle_intro.gif");
    PATHS.put(CutsceneId.BATTLE_DEFEAT, ROOT + "battle_defeat.gif");
    PATHS.put(CutsceneId.HACK_DOOR_POUND, ROOT + "hack_door_pound.gif");
    PATHS.put(CutsceneId.HACK_UNLOCK, ROOT + "hack_unlock.gif");
    PATHS.put(CutsceneId.ESCAPE_TRUE, ROOT + "escape_true.gif");
    PATHS.put(CutsceneId.ESCAPE_FALSE, ROOT + "escape_false.gif");
  }

  private CutsceneCatalog() {
  }

  public static String resourcePath(CutsceneId id) {
    return PATHS.get(id);
  }

  /** Покадровая PNG/JPG-последовательность, если GIF ещё нет. */
  public static String[] frameSequence(CutsceneId id) {
    if (id == null) {
      return null;
    }
    return switch (id) {
      case BATTLE_INTRO -> new String[] {
          ROOT + "battle_intro_01.png",
          ROOT + "battle_intro_02.jpg"
      };
      case BATTLE_DEFEAT -> new String[] {
          ROOT + "battle_defeat_01.png",
          ROOT + "battle_defeat_02.png"
      };
      default -> null;
    };
  }

  public static int frameDelayMs(CutsceneId id) {
    return switch (id) {
      case BATTLE_INTRO -> 2200;
      case BATTLE_DEFEAT -> 2800;
      default -> 2000;
    };
  }

  /** GIF или полная PNG-последовательность на диске. */
  public static boolean isAvailable(CutsceneId id) {
    if (id == null) {
      return false;
    }
    String gif = resourcePath(id);
    if (gif != null && CutsceneCatalog.class.getResource(gif) != null) {
      return true;
    }
    String[] frames = frameSequence(id);
    if (frames == null || frames.length == 0) {
      return false;
    }
    for (String frame : frames) {
      if (CutsceneCatalog.class.getResource(frame) == null) {
        return false;
      }
    }
    return true;
  }

  /** Катсцена зациклена (дверь на фоне терминала). */
  public static boolean loops(CutsceneId id) {
    return id == CutsceneId.HACK_DOOR_POUND;
  }
}
