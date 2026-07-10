package main.java.com.witcher.chapter1.cutscene;

import java.util.EnumMap;
import java.util.Map;

/**
 * Пути к GIF/видео катсцен. Файлы кладутся в {@code assets/cutscenes/chapter1/}.
 * Отсутствующий файл — {@link Chapter1Director} пропускает катсцену.
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

  /** Катсцена зациклена (дверь, бой на фоне терминала). */
  public static boolean loops(CutsceneId id) {
    return id == CutsceneId.HACK_DOOR_POUND || id == CutsceneId.BATTLE_INTRO;
  }
}
