package main.java.com.witcher.ui.settings;

import java.util.prefs.Preferences;

/**
 * Глобальные настройки игры (печать, авто, музыка).
 * Сохраняются через {@link Preferences}.
 */
public final class GameSettings {

  private static final GameSettings INSTANCE = new GameSettings();
  private static final String PREF_NODE = "com/witcher/game";
  private static final String KEY_TEXT_SPEED = "textSpeed";
  private static final String KEY_TYPEWRITER = "typewriter";
  private static final String KEY_AUTO_SPEED = "autoSpeed";
  private static final String KEY_MUSIC_VOLUME = "musicVolume";

  /** 1 (медленно) … 10 (быстро). По умолчанию 5 → как старые 2 тика/символ. */
  public static final int SPEED_MIN = 1;
  public static final int SPEED_MAX = 10;
  public static final int SPEED_DEFAULT = 5;

  /** 0 … 100 %. */
  public static final int VOLUME_MIN = 0;
  public static final int VOLUME_MAX = 100;
  public static final int VOLUME_DEFAULT = 85;

  private int textSpeed = SPEED_DEFAULT;
  private boolean typewriterEnabled = true;
  private int autoSpeed = SPEED_DEFAULT;
  private int musicVolume = VOLUME_DEFAULT;

  private GameSettings() {
    load();
  }

  public static GameSettings get() {
    return INSTANCE;
  }

  public int textSpeed() {
    return textSpeed;
  }

  public void setTextSpeed(int value) {
    textSpeed = clamp(value, SPEED_MIN, SPEED_MAX);
    save();
  }

  /** {@code true} — печать по буквам; {@code false} — реплика целиком сразу. */
  public boolean typewriterEnabled() {
    return typewriterEnabled;
  }

  public void setTypewriterEnabled(boolean enabled) {
    typewriterEnabled = enabled;
    save();
  }

  public int autoSpeed() {
    return autoSpeed;
  }

  public void setAutoSpeed(int value) {
    autoSpeed = clamp(value, SPEED_MIN, SPEED_MAX);
    save();
  }

  public int musicVolume() {
    return musicVolume;
  }

  public void setMusicVolume(int percent) {
    musicVolume = clamp(percent, VOLUME_MIN, VOLUME_MAX);
    save();
  }

  public float musicVolume01() {
    return musicVolume / 100f;
  }

  /**
   * Тиков на один символ при обычной печати.
   * Скорость 5 → 2 (канон), 1 → 6, 10 → 1.
   */
  public int ticksPerChar() {
    return Math.max(1, 7 - textSpeed);
  }

  /** Тиков на символ в режиме «Авто». */
  public int autoTicksPerChar() {
    if (autoSpeed >= 8) {
      return 1;
    }
    if (autoSpeed <= 3) {
      return 2;
    }
    return 1;
  }

  /**
   * Пауза на полной реплике в «Авто» (тики).
   * Скорость 5 → 50 (канон), 1 → 90, 10 → 10.
   */
  public int autoDelayTicks() {
    return Math.max(10, 100 - autoSpeed * 10);
  }

  private void load() {
    try {
      Preferences p = Preferences.userRoot().node(PREF_NODE);
      textSpeed = clamp(p.getInt(KEY_TEXT_SPEED, SPEED_DEFAULT), SPEED_MIN, SPEED_MAX);
      typewriterEnabled = p.getBoolean(KEY_TYPEWRITER, true);
      autoSpeed = clamp(p.getInt(KEY_AUTO_SPEED, SPEED_DEFAULT), SPEED_MIN, SPEED_MAX);
      musicVolume = clamp(p.getInt(KEY_MUSIC_VOLUME, VOLUME_DEFAULT), VOLUME_MIN, VOLUME_MAX);
    } catch (Exception ignored) {
      // defaults already set
    }
  }

  private void save() {
    try {
      Preferences p = Preferences.userRoot().node(PREF_NODE);
      p.putInt(KEY_TEXT_SPEED, textSpeed);
      p.putBoolean(KEY_TYPEWRITER, typewriterEnabled);
      p.putInt(KEY_AUTO_SPEED, autoSpeed);
      p.putInt(KEY_MUSIC_VOLUME, musicVolume);
      p.flush();
    } catch (Exception ignored) {
      // best-effort
    }
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }
}
