package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.cutscene.CutsceneCatalog;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.ui.graphics.GifFrames;
import main.java.com.witcher.ui.graphics.PixelScaler;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Воспроизведение GIF- и PNG-катсцен главы 1. */
public final class CutscenePlayer {

  private GifFrames gif;
  private BufferedImage[] pngFrames;
  private int[] pngDelaysMs;
  private boolean usePng;
  private boolean loop;
  private int frameIndex;
  private int frameTick;
  private boolean finished = true;

  public void start(CutsceneId id) {
    stop();
    if (id == null) {
      finished = true;
      return;
    }
    loop = CutsceneCatalog.loops(id);
    String path = CutsceneCatalog.resourcePath(id);
    gif = GifFrames.load(path);
    if (gif != null && gif.frames.length > 0) {
      frameIndex = 0;
      frameTick = 0;
      finished = false;
      return;
    }
    String[] sequence = CutsceneCatalog.frameSequence(id);
    if (sequence != null && loadPngSequence(sequence, CutsceneCatalog.frameDelayMs(id))) {
      finished = false;
      return;
    }
    finished = true;
  }

  public void stop() {
    gif = null;
    pngFrames = null;
    pngDelaysMs = null;
    usePng = false;
    finished = true;
  }

  public boolean isFinished() {
    return finished;
  }

  public void tick() {
    if (finished) {
      return;
    }
    frameTick++;
    int delayTicks = currentDelayTicks();
    if (frameTick < delayTicks) {
      return;
    }
    frameTick = 0;
    frameIndex++;
    int frameCount = currentFrameCount();
    if (frameIndex >= frameCount) {
      if (loop) {
        frameIndex = 0;
      } else {
        finished = true;
      }
    }
  }

  public void render(Graphics2D g, int sw, int sh) {
    BufferedImage frame = currentFrame();
    if (frame == null) {
      return;
    }
    int fw = frame.getWidth();
    int fh = frame.getHeight();
    if (fw <= 0 || fh <= 0) {
      return;
    }
    float scale = Math.min(sw / (float) fw, sh / (float) fh);
    int dw = Math.max(1, Math.round(fw * scale));
    int dh = Math.max(1, Math.round(fh * scale));
    int dx = (sw - dw) / 2;
    int dy = (sh - dh) / 2;
    BufferedImage scaled = PixelScaler.sharpScale(frame, dw, dh);
    g.drawImage(scaled, dx, dy, null);
  }

  private boolean loadPngSequence(String[] paths, int defaultDelayMs) {
    List<BufferedImage> loaded = new ArrayList<>();
    for (String path : paths) {
      Sprite sprite = Sprite.loadOptional(path);
      if (sprite != null) {
        loaded.add(sprite.getImage());
      }
    }
    if (loaded.isEmpty()) {
      return false;
    }
    pngFrames = loaded.toArray(new BufferedImage[0]);
    pngDelaysMs = new int[pngFrames.length];
    for (int i = 0; i < pngDelaysMs.length; i++) {
      pngDelaysMs[i] = defaultDelayMs;
    }
    usePng = true;
    frameIndex = 0;
    frameTick = 0;
    return true;
  }

  private BufferedImage currentFrame() {
    if (usePng && pngFrames != null && pngFrames.length > 0) {
      return pngFrames[Math.min(frameIndex, pngFrames.length - 1)];
    }
    if (gif != null && gif.frames.length > 0) {
      return gif.frames[Math.min(frameIndex, gif.frames.length - 1)];
    }
    return null;
  }

  private int currentFrameCount() {
    if (usePng && pngFrames != null) {
      return pngFrames.length;
    }
    if (gif != null) {
      return gif.frames.length;
    }
    return 0;
  }

  private int currentDelayTicks() {
    if (usePng && pngDelaysMs != null && frameIndex < pngDelaysMs.length) {
      return Math.max(1, pngDelaysMs[frameIndex] / 16);
    }
    if (gif != null && frameIndex < gif.delaysMs.length) {
      return Math.max(1, gif.delaysMs[frameIndex] / 16);
    }
    return 1;
  }
}
