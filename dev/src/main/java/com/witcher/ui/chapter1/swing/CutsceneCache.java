package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.cutscene.CutsceneCatalog;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.ui.graphics.GifFrames;
import main.java.com.witcher.ui.graphics.PixelScaler;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Декодированные GIF и предмасштабированные кадры катсцен. */
public final class CutsceneCache {

  private static final Map<CutsceneId, GifFrames> GIFS = new EnumMap<>(CutsceneId.class);
  private static final Map<String, ScaledSequence> SCALED = new HashMap<>();

  private CutsceneCache() {
  }

  public static void warm(CutsceneId... ids) {
    if (ids == null) {
      return;
    }
    for (CutsceneId id : ids) {
      gif(id);
    }
  }

  public static void prewarmScaled(CutsceneId id, int sw, int sh) {
    scaledSequence(id, sw, sh);
  }

  public static GifFrames gif(CutsceneId id) {
    if (id == null) {
      return null;
    }
    return GIFS.computeIfAbsent(id, k -> {
      String path = CutsceneCatalog.resourcePath(k);
      return path != null ? GifFrames.load(path) : null;
    });
  }

  public static ScaledSequence scaledSequence(CutsceneId id, int sw, int sh) {
    if (id == null || sw <= 0 || sh <= 0) {
      return null;
    }
    String key = id.name() + "@" + sw + "x" + sh;
    return SCALED.computeIfAbsent(key, k -> buildScaled(id, sw, sh));
  }

  public static ScaledSequence scaledPngSequence(String[] paths, int defaultDelayMs, int sw, int sh) {
    if (paths == null || paths.length == 0 || sw <= 0 || sh <= 0) {
      return null;
    }
    String key = "png:" + String.join("|", paths) + "@" + sw + "x" + sh;
    return SCALED.computeIfAbsent(key, k -> buildScaledPng(paths, defaultDelayMs, sw, sh));
  }

  private static ScaledSequence buildScaled(CutsceneId id, int sw, int sh) {
    GifFrames gif = gif(id);
    if (gif == null || gif.frames.length == 0) {
      return null;
    }
    BufferedImage[] frames = new BufferedImage[gif.frames.length];
    int[] dx = new int[gif.frames.length];
    int[] dy = new int[gif.frames.length];
    for (int i = 0; i < gif.frames.length; i++) {
      Fit fit = fitFrame(gif.frames[i], sw, sh);
      frames[i] = fit.image;
      dx[i] = fit.dx;
      dy[i] = fit.dy;
    }
    return new ScaledSequence(frames, gif.delaysMs, dx, dy);
  }

  private static ScaledSequence buildScaledPng(String[] paths, int defaultDelayMs, int sw, int sh) {
    BufferedImage[] raw = new BufferedImage[paths.length];
    int count = 0;
    for (String path : paths) {
      Sprite sprite = Sprite.loadOptional(path);
      if (sprite != null) {
        raw[count++] = sprite.getImage();
      }
    }
    if (count == 0) {
      return null;
    }
    BufferedImage[] frames = new BufferedImage[count];
    int[] delays = new int[count];
    int[] dx = new int[count];
    int[] dy = new int[count];
    for (int i = 0; i < count; i++) {
      Fit fit = fitFrame(raw[i], sw, sh);
      frames[i] = fit.image;
      delays[i] = defaultDelayMs;
      dx[i] = fit.dx;
      dy[i] = fit.dy;
    }
    return new ScaledSequence(frames, delays, dx, dy);
  }

  private static Fit fitFrame(BufferedImage frame, int sw, int sh) {
    if (frame == null) {
      return new Fit(null, 0, 0);
    }
    int fw = frame.getWidth();
    int fh = frame.getHeight();
    if (fw <= 0 || fh <= 0) {
      return new Fit(null, 0, 0);
    }
    float scale = Math.min(sw / (float) fw, sh / (float) fh);
    int dw = Math.max(1, Math.round(fw * scale));
    int dh = Math.max(1, Math.round(fh * scale));
    int dx = (sw - dw) / 2;
    int dy = (sh - dh) / 2;
    return new Fit(PixelScaler.sharpScale(frame, dw, dh), dx, dy);
  }

  private record Fit(BufferedImage image, int dx, int dy) {
  }

  public record ScaledSequence(
      BufferedImage[] frames,
      int[] delaysMs,
      int[] dx,
      int[] dy) {

    public int frameCount() {
      return frames != null ? frames.length : 0;
    }
  }
}
