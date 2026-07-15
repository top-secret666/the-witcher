package main.java.com.witcher.chapter1;

import main.java.com.witcher.shop.EquipSlot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Автосохранение мета-прогресса главы 1 (счётчики, фрагменты).
 * Лавка ({@code ShopModel}) сохраняется отдельно, когда понадобится.
 *
 * <p>Пока выключено: каждый вход в игру — чистая сессия с нулевыми счётчиками.
 */
public final class Chapter1Save {

  /** Временно: не читать и не писать {@code chapter1_session.properties}. */
  private static final boolean DISABLED_FOR_TESTING = true;

  private static final String FILE_NAME = "chapter1_session.properties";
  private static final String KEY_LOOP = "loop";
  private static final String KEY_PRISON = "prison";
  private static final String KEY_SUSPICION = "suspicion";
  private static final String KEY_TRUST = "trust";
  private static final String KEY_FRAGMENTS = "fragments";
  private static final String KEY_INSPECTED = "inspected";
  private static final String KEY_TERMINAL = "terminal";
  private static final String KEY_CIPHER_SOLVED = "cipherSolved";
  private static final String KEY_ESCAPE = "escapeUnlocked";
  private static final String KEY_HACK_ATTEMPTS = "hackAttempts";
  private static final String KEY_BATTLE_CARD = "battleCardGranted";
  private static final String KEY_BATTLE_ICON = "battleCardIcon";

  private Chapter1Save() {
  }

  public static Path defaultPath() {
    return Path.of(System.getProperty("user.dir", "."), "saves", FILE_NAME);
  }

  public static void save(Chapter1Session session) {
    if (DISABLED_FOR_TESTING || session == null) {
      return;
    }
    Chapter1Session.Chapter1Snapshot snap = session.snapshot();
    Properties props = new Properties();
    props.setProperty(KEY_LOOP, Integer.toString(snap.loop()));
    props.setProperty(KEY_PRISON, Integer.toString(snap.prison()));
    props.setProperty(KEY_SUSPICION, Integer.toString(snap.suspicion()));
    props.setProperty(KEY_TRUST, Integer.toString(snap.trust()));
    props.setProperty(KEY_FRAGMENTS, encodeFlags(snap.cipherFragments()));
    props.setProperty(KEY_INSPECTED, encodeInspected(snap.inspectedSlots()));
    props.setProperty(KEY_TERMINAL, Boolean.toString(snap.terminalAccessGranted()));
    props.setProperty(KEY_CIPHER_SOLVED, Boolean.toString(snap.cipherSolved()));
    props.setProperty(KEY_ESCAPE, Boolean.toString(snap.escapeAttemptUnlocked()));
    props.setProperty(KEY_HACK_ATTEMPTS, Integer.toString(snap.hackAttemptsThisLoop()));
    props.setProperty(KEY_BATTLE_CARD, Boolean.toString(snap.battleCardGranted()));
    props.setProperty(KEY_BATTLE_ICON, Boolean.toString(snap.battleCardIconVisible()));

    Path path = defaultPath();
    try {
      Files.createDirectories(path.getParent());
      try (OutputStream out = Files.newOutputStream(path)) {
        props.store(out, "Chapter 1 session");
      }
    } catch (IOException error) {
      System.err.println("Не удалось сохранить главу 1: " + error.getMessage());
    }
  }

  public static Chapter1Session loadOrNew() {
    if (DISABLED_FOR_TESTING) {
      deleteSaveFileQuietly();
      return Chapter1Session.newGame();
    }
    Path path = defaultPath();
    if (!Files.isRegularFile(path)) {
      return Chapter1Session.newGame();
    }
    Properties props = new Properties();
    try (InputStream in = Files.newInputStream(path)) {
      props.load(in);
    } catch (IOException error) {
      System.err.println("Не удалось загрузить главу 1: " + error.getMessage());
      return Chapter1Session.newGame();
    }

    EquipSlot[] slots = EquipSlot.values();
    boolean[] fragments = decodeFlags(props.getProperty(KEY_FRAGMENTS, ""), slots.length);
    EquipSlot[] inspected = decodeInspected(props.getProperty(KEY_INSPECTED, ""));

    Chapter1Session session = Chapter1Session.newGame();
    session.restore(new Chapter1Session.Chapter1Snapshot(
        parseInt(props, KEY_LOOP, 1),
        parseInt(props, KEY_PRISON, 0),
        parseInt(props, KEY_SUSPICION, 0),
        parseInt(props, KEY_TRUST, 0),
        fragments,
        inspected,
        Boolean.parseBoolean(props.getProperty(KEY_TERMINAL, "false")),
        Boolean.parseBoolean(props.getProperty(KEY_CIPHER_SOLVED, "false")),
        Boolean.parseBoolean(props.getProperty(KEY_ESCAPE, "false")),
        parseInt(props, KEY_HACK_ATTEMPTS, 0),
        Boolean.parseBoolean(props.getProperty(KEY_BATTLE_CARD, "false")),
        Boolean.parseBoolean(props.getProperty(KEY_BATTLE_ICON, "false"))));
    return session;
  }

  private static void deleteSaveFileQuietly() {
    try {
      Files.deleteIfExists(defaultPath());
    } catch (IOException ignored) {
      // тест-режим: старый файл не обязателен
    }
  }

  private static int parseInt(Properties props, String key, int fallback) {
    try {
      return Integer.parseInt(props.getProperty(key, Integer.toString(fallback)));
    } catch (NumberFormatException error) {
      return fallback;
    }
  }

  private static String encodeFlags(boolean[] flags) {
    StringBuilder sb = new StringBuilder(flags.length);
    for (boolean flag : flags) {
      sb.append(flag ? '1' : '0');
    }
    return sb.toString();
  }

  private static boolean[] decodeFlags(String raw, int length) {
    boolean[] flags = new boolean[length];
    if (raw == null) {
      return flags;
    }
    for (int i = 0; i < length && i < raw.length(); i++) {
      flags[i] = raw.charAt(i) == '1';
    }
    return flags;
  }

  private static String encodeInspected(EquipSlot[] slots) {
    if (slots == null || slots.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < slots.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(slots[i].name());
    }
    return sb.toString();
  }

  private static EquipSlot[] decodeInspected(String raw) {
    if (raw == null || raw.isBlank()) {
      return new EquipSlot[0];
    }
    String[] parts = raw.split(",");
    EquipSlot[] out = new EquipSlot[parts.length];
    int count = 0;
    for (String part : parts) {
      try {
        out[count++] = EquipSlot.valueOf(part.trim());
      } catch (IllegalArgumentException ignored) {
        // пропускаем неизвестные значения
      }
    }
    if (count == parts.length) {
      return out;
    }
    return java.util.Arrays.copyOf(out, count);
  }
}
