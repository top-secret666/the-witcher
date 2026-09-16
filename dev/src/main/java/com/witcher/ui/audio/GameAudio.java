package main.java.com.witcher.ui.audio;

import main.java.com.witcher.ui.settings.GameSettings;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Фоновая музыка через Windows MediaPlayer (PresentationCore).
 * Зацикливание / плейлист / пауза; громкость из {@link GameSettings}.
 */
public final class GameAudio {

  public enum Scene {
    NONE,
    MENU,
    INTRO,
    SHOP,
    WOLF,
    VESEMIR,
    FINALE,
    CREDITS
  }

  private static final String MENU_MP3 = "/assets/audio/main_menu.mp3";
  private static final String INTRO_MP3 = "/assets/audio/intro.mp3";
  private static final String SHOP_A_MP3 = "/assets/audio/shop_theme_a.mp3";
  private static final String SHOP_B_MP3 = "/assets/audio/shop_theme_b.mp3";
  private static final String SHOP_AMBIENCE_MP3 = "/assets/audio/shop_ambience.mp3";
  private static final String WOLF_MP3 = "/assets/audio/wolf.mp3";
  private static final String VESEMIR_MP3 = "/assets/audio/vesemir.mp3";
  private static final String FINALE_MP3 = "/assets/audio/finale.mp3";
  private static final String CREDITS_MP3 = "/assets/audio/demo_credits.mp3";

  private static final float SHOP_AMBIENCE_REL = 0.32f;

  private static final List<Layer> layers = new ArrayList<>();
  private static final List<String> shopPlaylist = List.of(SHOP_A_MP3, SHOP_B_MP3);
  private static int shopPlaylistIndex;
  private static Layer shopThemeLayer;
  private static Scene currentScene = Scene.NONE;
  private static boolean musicPaused;
  private static boolean shutdownHookInstalled;
  private static Path sharedPauseFlag;
  private static boolean fadingOut;
  private static long fadeStartMs;
  private static int fadeDurationMs;
  private static float fadeMul = 1f;

  private GameAudio() {
  }

  public static synchronized Scene currentScene() {
    return currentScene;
  }

  public static synchronized void playMainMenu() {
    switchScene(Scene.MENU, List.of(spec(MENU_MP3, 1f, true)));
  }

  public static synchronized void playIntro() {
    switchScene(Scene.INTRO, List.of(spec(INTRO_MP3, 1f, true)));
  }

  public static synchronized void playShop() {
    if (currentScene == Scene.SHOP && !layers.isEmpty() && !fadingOut) {
      applyMusicVolume();
      writePauseFlag(musicPaused);
      return;
    }
    stopAll();
    currentScene = Scene.SHOP;
    shopPlaylistIndex = 0;
    ensurePauseFlag();
    ensureShutdownHook();
    float master = GameSettings.get().musicVolume01();
    // Один процесс крутит A↔B; иначе после конца A PS не выходил и B не стартовал.
    shopThemeLayer = startShopThemePlaylist(master);
    if (shopThemeLayer != null) {
      layers.add(shopThemeLayer);
    }
    Layer ambience = startLayer(spec(SHOP_AMBIENCE_MP3, SHOP_AMBIENCE_REL, true), master);
    if (ambience != null) {
      layers.add(ambience);
    }
    writePauseFlag(musicPaused);
  }

  public static synchronized void playWolf() {
    switchScene(Scene.WOLF, List.of(spec(WOLF_MP3, 1f, true)));
  }

  public static synchronized void playVesemir() {
    switchScene(Scene.VESEMIR, List.of(spec(VESEMIR_MP3, 1f, true)));
  }

  public static synchronized void playFinale() {
    switchScene(Scene.FINALE, List.of(spec(FINALE_MP3, 1f, true)));
  }

  public static synchronized void playCreditsTheme() {
    switchScene(Scene.CREDITS, List.of(spec(CREDITS_MP3, 1f, true)));
  }

  public static synchronized void stopCreditsTheme() {
    if (currentScene == Scene.CREDITS) {
      stopAll();
    }
  }

  /** Пауза игры — музыка ставится на паузу (не убивается). */
  public static synchronized void setPaused(boolean paused) {
    musicPaused = paused;
    writePauseFlag(paused);
  }

  /** Плавное затухание текущей музыки (клик по иконке на карте). */
  public static synchronized void beginFadeOut(int durationMs) {
    if (layers.isEmpty()) {
      stopAll();
      return;
    }
    fadingOut = true;
    fadeStartMs = System.currentTimeMillis();
    fadeDurationMs = Math.max(1, durationMs);
    fadeMul = 1f;
    applyMusicVolume();
  }

  public static synchronized boolean isFadingOut() {
    return fadingOut;
  }

  public static synchronized void stopAll() {
    fadingOut = false;
    fadeMul = 1f;
    for (Layer layer : layers) {
      layer.stop();
    }
    layers.clear();
    shopThemeLayer = null;
    shopPlaylistIndex = 0;
    currentScene = Scene.NONE;
  }

  /** Тик фейда (плейлист лавки крутится внутри своего PS-процесса). */
  public static synchronized void tick() {
    if (fadingOut) {
      float t = (System.currentTimeMillis() - fadeStartMs) / (float) fadeDurationMs;
      if (t >= 1f) {
        stopAll();
      } else {
        fadeMul = Math.max(0f, 1f - t);
        applyMusicVolume();
      }
    }
  }

  public static synchronized void applyMusicVolume() {
    float master = GameSettings.get().musicVolume01() * fadeMul;
    for (Layer layer : layers) {
      layer.writeVolume(master * layer.relativeVolume);
    }
  }

  private static void switchScene(Scene scene, List<LayerSpec> specs) {
    ensureShutdownHook();
    if (currentScene == scene && !layers.isEmpty() && !fadingOut) {
      applyMusicVolume();
      writePauseFlag(musicPaused);
      return;
    }
    stopAll();
    currentScene = scene;
    ensurePauseFlag();
    float master = GameSettings.get().musicVolume01();
    for (LayerSpec spec : specs) {
      Layer layer = startLayer(spec, master);
      if (layer != null) {
        layers.add(layer);
      }
    }
    writePauseFlag(musicPaused);
  }

  private static LayerSpec spec(String resource, float relativeVolume, boolean loop) {
    return new LayerSpec(resource, relativeVolume, loop);
  }

  /**
   * Лавка: A → B → A → B… в одном процессе.
   * MediaEnded через файл-флаг (script-scope в PS-событиях ненадёжен).
   */
  private static Layer startShopThemePlaylist(float masterVolume) {
    try {
      Path[] mp3Files = new Path[shopPlaylist.size()];
      for (int i = 0; i < shopPlaylist.size(); i++) {
        try (InputStream raw = openAudioStream(shopPlaylist.get(i))) {
          if (raw == null) {
            System.err.println("[GameAudio] Missing " + shopPlaylist.get(i));
            return null;
          }
          Path mp3File = Files.createTempFile("witcher_shop_pl_" + i + "_", ".mp3");
          mp3File.toFile().deleteOnExit();
          Files.copy(raw, mp3File, StandardCopyOption.REPLACE_EXISTING);
          mp3Files[i] = mp3File;
        }
      }

      Path stopFlag = Files.createTempFile("witcher_audio_stop_", ".flag");
      Files.deleteIfExists(stopFlag);
      stopFlag.toFile().deleteOnExit();

      Path volFile = Files.createTempFile("witcher_audio_vol_", ".txt");
      volFile.toFile().deleteOnExit();

      Path endFlag = Files.createTempFile("witcher_audio_end_", ".flag");
      Files.deleteIfExists(endFlag);
      endFlag.toFile().deleteOnExit();

      ensurePauseFlag();
      Layer layer = new Layer(1f, stopFlag, volFile, mp3Files[0]);
      layer.extraTempFiles = mp3Files;
      layer.writeVolume(masterVolume);

      StringBuilder filesPs = new StringBuilder("@(");
      for (int i = 0; i < mp3Files.length; i++) {
        if (i > 0) {
          filesPs.append(',');
        }
        filesPs.append("'").append(escapePs(mp3Files[i].toAbsolutePath().toString())).append("'");
      }
      filesPs.append(')');

      String stop = escapePs(stopFlag.toAbsolutePath().toString());
      String vol = escapePs(volFile.toAbsolutePath().toString());
      String end = escapePs(endFlag.toAbsolutePath().toString());
      String pause = sharedPauseFlag != null
          ? escapePs(sharedPauseFlag.toAbsolutePath().toString())
          : "";
      long parentPid = ProcessHandle.current().pid();
      String startVol = String.format(Locale.US, "%.3f",
          Math.max(0f, Math.min(1f, masterVolume)));

      String script =
          "Add-Type -AssemblyName PresentationCore; "
              + "$files = " + filesPs + "; "
              + "$idx = 0; "
              + "$p = New-Object System.Windows.Media.MediaPlayer; "
              + "$p.Volume = " + startVol + "; "
              + "$paused = $false; "
              + "$stop = '" + stop + "'; "
              + "$volFile = '" + vol + "'; "
              + "$endFlag = '" + end + "'; "
              + "$pauseFile = '" + pause + "'; "
              + "$parent = " + parentPid + "; "
              + "$p.add_MediaEnded({ "
              + "  try { [System.IO.File]::WriteAllText($endFlag, '1') } catch {} "
              + "}); "
              + "try { "
              + "  while ($true) { "
              + "    if (Test-Path -LiteralPath $stop) { break } "
              + "    try { Remove-Item -LiteralPath $endFlag -Force -ErrorAction SilentlyContinue } catch {} "
              + "    $path = $files[$idx]; "
              + "    $p.Open([uri]([System.IO.Path]::GetFullPath($path))); "
              + "    for ($i = 0; $i -lt 80; $i++) { "
              + "      if (Test-Path -LiteralPath $stop) { break } "
              + "      $p.Play(); "
              + "      Start-Sleep -Milliseconds 40; "
              + "      if ($p.Position.TotalMilliseconds -gt 0) { break } "
              + "    } "
              + "    while (-not (Test-Path -LiteralPath $endFlag)) { "
              + "      if (Test-Path -LiteralPath $stop) { break } "
              + "      $parentAlive = $null -ne (Get-Process -Id $parent -ErrorAction SilentlyContinue); "
              + "      if (-not $parentAlive) { "
              + "        $any = Get-Process -ErrorAction SilentlyContinue | "
              + "          Where-Object { $_.ProcessName -match 'The Witcher|java|javaw' }; "
              + "        if (-not $any) { break } "
              + "      } "
              + "      if ($pauseFile -ne '' -and (Test-Path -LiteralPath $pauseFile)) { "
              + "        if (-not $paused) { try { $p.Pause() } catch {}; $paused = $true } "
              + "      } else { "
              + "        if ($paused) { try { $p.Play() } catch {}; $paused = $false } "
              + "      } "
              + "      if (Test-Path -LiteralPath $volFile) { "
              + "        try { "
              + "          $raw = (Get-Content -LiteralPath $volFile -Raw).Trim(); "
              + "          if ($raw -ne '') { $p.Volume = [double]$raw } "
              + "        } catch {} "
              + "      } "
              // fallback если MediaEnded не сработал
              + "      try { "
              + "        if (-not $paused -and $p.NaturalDuration.HasTimeSpan) { "
              + "          $d = $p.NaturalDuration.TimeSpan.TotalMilliseconds; "
              + "          $pos = $p.Position.TotalMilliseconds; "
              + "          if ($d -gt 800 -and $pos -ge ($d - 120)) { "
              + "            [System.IO.File]::WriteAllText($endFlag, '1') "
              + "          } "
              + "        } "
              + "      } catch {} "
              + "      Start-Sleep -Milliseconds 200 "
              + "    } "
              + "    if (Test-Path -LiteralPath $stop) { break } "
              + "    try { $p.Stop() } catch {} "
              + "    $idx = ($idx + 1) % $files.Count "
              + "  } "
              + "} finally { "
              + "  try { $p.Stop() } catch {} "
              + "  try { $p.Close() } catch {} "
              + "  try { Remove-Item -LiteralPath $endFlag -Force -ErrorAction SilentlyContinue } catch {} "
              + "}";

      ProcessBuilder pb = new ProcessBuilder(
          "powershell", "-NoProfile", "-WindowStyle", "Hidden", "-Command", script);
      pb.redirectErrorStream(true);
      layer.process = pb.start();
      startDrainThread(layer.process);
      System.out.println("[GameAudio] Started shop playlist A↔B (loop)");
      return layer;
    } catch (Exception e) {
      System.err.println("[GameAudio] Failed shop playlist: " + e.getMessage());
      return null;
    }
  }

  private static void startDrainThread(Process process) {
    Thread drain = new Thread(() -> {
      try (BufferedReader r = new BufferedReader(
          new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        while (r.readLine() != null) {
          // discard
        }
      } catch (Exception ignored) {
        // best-effort
      }
    }, "game-audio-drain");
    drain.setDaemon(true);
    drain.start();
  }

  private static void ensurePauseFlag() {
    if (sharedPauseFlag != null) {
      return;
    }
    try {
      sharedPauseFlag = Files.createTempFile("witcher_audio_pause_", ".flag");
      sharedPauseFlag.toFile().deleteOnExit();
      Files.deleteIfExists(sharedPauseFlag);
    } catch (Exception ignored) {
      // best-effort
    }
  }

  private static void writePauseFlag(boolean paused) {
    ensurePauseFlag();
    if (sharedPauseFlag == null) {
      return;
    }
    try {
      if (paused) {
        Files.writeString(sharedPauseFlag, "pause", StandardCharsets.UTF_8);
      } else {
        Files.deleteIfExists(sharedPauseFlag);
      }
    } catch (Exception ignored) {
      // best-effort
    }
  }

  private static Layer startLayer(LayerSpec spec, float masterVolume) {
    try (InputStream raw = openAudioStream(spec.resource)) {
      if (raw == null) {
        System.err.println("[GameAudio] Missing " + spec.resource);
        return null;
      }
      Path mp3File = Files.createTempFile("witcher_audio_", ".mp3");
      mp3File.toFile().deleteOnExit();
      Files.copy(raw, mp3File, StandardCopyOption.REPLACE_EXISTING);

      Path stopFlag = Files.createTempFile("witcher_audio_stop_", ".flag");
      Files.deleteIfExists(stopFlag);
      stopFlag.toFile().deleteOnExit();

      Path volFile = Files.createTempFile("witcher_audio_vol_", ".txt");
      volFile.toFile().deleteOnExit();

      ensurePauseFlag();
      Layer layer = new Layer(spec.relativeVolume, stopFlag, volFile, mp3File);
      layer.writeVolume(masterVolume * spec.relativeVolume);

      String mp3 = escapePs(mp3File.toAbsolutePath().toString());
      String stop = escapePs(stopFlag.toAbsolutePath().toString());
      String vol = escapePs(volFile.toAbsolutePath().toString());
      String pause = sharedPauseFlag != null
          ? escapePs(sharedPauseFlag.toAbsolutePath().toString())
          : "";
      long parentPid = ProcessHandle.current().pid();
      String startVol = String.format(Locale.US, "%.3f",
          Math.max(0f, Math.min(1f, masterVolume * spec.relativeVolume)));
      String loopFlag = spec.loop ? "$true" : "$false";

      // loop: при конце трека Open+Play заново (MediaEnded Position=0 часто молчит).
      String script =
          "Add-Type -AssemblyName PresentationCore; "
              + "$mp3Path = [System.IO.Path]::GetFullPath('" + mp3 + "'); "
              + "$p = New-Object System.Windows.Media.MediaPlayer; "
              + "$p.Volume = " + startVol + "; "
              + "$loop = " + loopFlag + "; "
              + "$paused = $false; "
              + "$stop = '" + stop + "'; "
              + "$volFile = '" + vol + "'; "
              + "$pauseFile = '" + pause + "'; "
              + "$parent = " + parentPid + "; "
              + "$endFlag = [System.IO.Path]::GetTempFileName(); "
              + "Remove-Item -LiteralPath $endFlag -Force -ErrorAction SilentlyContinue; "
              + "function Start-Track { "
              + "  param($player, $path) "
              + "  Remove-Item -LiteralPath $endFlag -Force -ErrorAction SilentlyContinue; "
              + "  $player.Open([uri]$path); "
              + "  for ($i = 0; $i -lt 80; $i++) { "
              + "    $player.Play(); "
              + "    Start-Sleep -Milliseconds 40; "
              + "    if ($player.Position.TotalMilliseconds -gt 0) { break } "
              + "  } "
              + "} "
              + "$p.add_MediaEnded({ "
              + "  try { [System.IO.File]::WriteAllText($endFlag, '1') } catch {} "
              + "}); "
              + "Start-Track $p $mp3Path; "
              + "try { "
              + "  while ($true) { "
              + "    if (Test-Path -LiteralPath $stop) { break } "
              + "    $parentAlive = $null -ne (Get-Process -Id $parent -ErrorAction SilentlyContinue); "
              + "    if (-not $parentAlive) { "
              + "      $any = Get-Process -ErrorAction SilentlyContinue | "
              + "        Where-Object { $_.ProcessName -match 'The Witcher|java|javaw' }; "
              + "      if (-not $any) { break } "
              + "    } "
              + "    if ($pauseFile -ne '' -and (Test-Path -LiteralPath $pauseFile)) { "
              + "      if (-not $paused) { try { $p.Pause() } catch {}; $paused = $true } "
              + "    } else { "
              + "      if ($paused) { try { $p.Play() } catch {}; $paused = $false } "
              + "    } "
              + "    if (Test-Path -LiteralPath $volFile) { "
              + "      try { "
              + "        $raw = (Get-Content -LiteralPath $volFile -Raw).Trim(); "
              + "        if ($raw -ne '') { $p.Volume = [double]$raw } "
              + "      } catch {} "
              + "    } "
              + "    $trackDone = Test-Path -LiteralPath $endFlag; "
              + "    if (-not $trackDone) { "
              + "      try { "
              + "        if (-not $paused -and $p.NaturalDuration.HasTimeSpan) { "
              + "          $d = $p.NaturalDuration.TimeSpan.TotalMilliseconds; "
              + "          $pos = $p.Position.TotalMilliseconds; "
              + "          if ($d -gt 800 -and $pos -ge ($d - 120)) { $trackDone = $true } "
              + "        } "
              + "      } catch {} "
              + "    } "
              + "    if ($trackDone) { "
              + "      if ($loop) { "
              + "        try { $p.Stop() } catch {} "
              + "        Start-Track $p $mp3Path; "
              + "        if ($paused) { try { $p.Pause() } catch {} } "
              + "      } else { "
              + "        break "
              + "      } "
              + "    } "
              + "    Start-Sleep -Milliseconds 200 "
              + "  } "
              + "} finally { "
              + "  try { $p.Stop() } catch {} "
              + "  try { $p.Close() } catch {} "
              + "  try { Remove-Item -LiteralPath $endFlag -Force -ErrorAction SilentlyContinue } catch {} "
              + "}";

      ProcessBuilder pb = new ProcessBuilder(
          "powershell", "-NoProfile", "-WindowStyle", "Hidden", "-Command", script);
      pb.redirectErrorStream(true);
      layer.process = pb.start();
      startDrainThread(layer.process);
      System.out.println("[GameAudio] Started " + spec.resource
          + " (vol×" + String.format(Locale.US, "%.2f", spec.relativeVolume)
          + ", loop=" + spec.loop + ")");
      return layer;
    } catch (Exception e) {
      System.err.println("[GameAudio] Failed " + spec.resource + ": " + e.getMessage());
      return null;
    }
  }

  private static InputStream openAudioStream(String resource) {
    InputStream in = GameAudio.class.getResourceAsStream(resource);
    if (in != null) {
      return new BufferedInputStream(in);
    }
    String rel = resource.startsWith("/") ? resource.substring(1) : resource;
    in = ClassLoader.getSystemResourceAsStream(rel);
    if (in != null) {
      return new BufferedInputStream(in);
    }
    Path[] candidates = {
        Path.of(rel),
        Path.of("app", rel),
        Path.of(System.getProperty("user.dir", "."), rel),
        Path.of(System.getProperty("user.dir", "."), "app", rel),
    };
    for (Path p : candidates) {
      if (Files.isRegularFile(p)) {
        try {
          return new BufferedInputStream(Files.newInputStream(p));
        } catch (Exception ignored) {
          // try next
        }
      }
    }
    return null;
  }

  private static void ensureShutdownHook() {
    if (shutdownHookInstalled) {
      return;
    }
    shutdownHookInstalled = true;
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        stopAll();
        killOrphanAudioPlayers();
        if (sharedPauseFlag != null) {
          Files.deleteIfExists(sharedPauseFlag);
        }
      } catch (Throwable ignored) {
        // JVM exiting
      }
    }, "game-audio-shutdown"));
  }

  private static void killProcessTree(long pid) {
    if (pid <= 0) {
      return;
    }
    try {
      Process killer = new ProcessBuilder(
          "taskkill", "/F", "/T", "/PID", Long.toString(pid))
          .redirectErrorStream(true)
          .start();
      killer.waitFor(2, TimeUnit.SECONDS);
    } catch (Exception ignored) {
      // best-effort
    }
  }

  private static void killOrphanAudioPlayers() {
    try {
      String script =
          "Get-CimInstance Win32_Process -Filter \"Name='powershell.exe'\" "
              + "| Where-Object { $_.CommandLine -like '*witcher_audio_*' "
              + "-or $_.CommandLine -like '*witcher_demo_credits_*' } "
              + "| ForEach-Object { "
              + "  try { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue } catch {} "
              + "}";
      Process killer = new ProcessBuilder(
          "powershell", "-NoProfile", "-WindowStyle", "Hidden", "-Command", script)
          .redirectErrorStream(true)
          .start();
      killer.waitFor(2, TimeUnit.SECONDS);
    } catch (Exception ignored) {
      // best-effort
    }
  }

  private static String escapePs(String path) {
    return path.replace("'", "''");
  }

  private record LayerSpec(String resource, float relativeVolume, boolean loop) {
  }

  private static final class Layer {
    final float relativeVolume;
    final Path stopFlag;
    final Path volumeFile;
    final Path mp3File;
    Path[] extraTempFiles;
    Process process;

    Layer(float relativeVolume, Path stopFlag, Path volumeFile, Path mp3File) {
      this.relativeVolume = relativeVolume;
      this.stopFlag = stopFlag;
      this.volumeFile = volumeFile;
      this.mp3File = mp3File;
    }

    void writeVolume(float volume01) {
      try {
        Files.writeString(volumeFile,
            String.format(Locale.US, "%.3f", Math.max(0f, Math.min(1f, volume01))),
            StandardCharsets.UTF_8);
      } catch (Exception ignored) {
        // best-effort
      }
    }

    void stop() {
      try {
        Files.writeString(stopFlag, "stop", StandardCharsets.UTF_8);
      } catch (Exception ignored) {
        // best-effort
      }
      Process proc = process;
      if (proc != null) {
        try {
          if (!proc.waitFor(400, TimeUnit.MILLISECONDS)) {
            killProcessTree(proc.pid());
            proc.destroyForcibly();
            proc.waitFor(800, TimeUnit.MILLISECONDS);
          }
        } catch (Exception ignored) {
          try {
            killProcessTree(proc.pid());
            proc.destroyForcibly();
          } catch (Exception ignored2) {
            // best-effort
          }
        }
        process = null;
      }
      try {
        Files.deleteIfExists(stopFlag);
      } catch (Exception ignored) {
        // temp
      }
      try {
        Files.deleteIfExists(volumeFile);
      } catch (Exception ignored) {
        // temp
      }
      try {
        Files.deleteIfExists(mp3File);
      } catch (Exception ignored) {
        // temp
      }
      if (extraTempFiles != null) {
        for (Path extra : extraTempFiles) {
          if (extra == null || extra.equals(mp3File)) {
            continue;
          }
          try {
            Files.deleteIfExists(extra);
          } catch (Exception ignored) {
            // temp
          }
        }
      }
    }
  }
}
