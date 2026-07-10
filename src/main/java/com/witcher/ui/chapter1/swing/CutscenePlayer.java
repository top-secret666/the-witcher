package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.cutscene.CutsceneCatalog;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.ui.graphics.GifFrames;
import main.java.com.witcher.ui.graphics.PixelScaler;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Воспроизведение GIF-катсцен главы 1. */
public final class CutscenePlayer {

  private GifFrames gif;
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
    String path = CutsceneCatalog.resourcePath(id);
    gif = GifFrames.load(path);
    if (gif == null || gif.frames.length == 0) {
      finished = true;
      return;
    }
    loop = CutsceneCatalog.loops(id);
    frameIndex = 0;
    frameTick = 0;
    finished = false;
  }

  public void stop() {
    gif = null;
    finished = true;
  }

  public boolean isFinished() {
    return finished;
  }

  public void tick() {
    if (finished || gif == null) {
      return;
    }
    frameTick++;
    int delayTicks = Math.max(1, gif.delaysMs[frameIndex] / 16);
    if (frameTick < delayTicks) {
      return;
    }
    frameTick = 0;
    frameIndex++;
    if (frameIndex >= gif.frames.length) {
      if (loop) {
        frameIndex = 0;
      } else {
        finished = true;
      }
    }
  }

  public void render(Graphics2D g, int sw, int sh) {
    if (gif == null) {
      return;
    }
    BufferedImage frame = gif.frames[Math.min(frameIndex, gif.frames.length - 1)];
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
}
