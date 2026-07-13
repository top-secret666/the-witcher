package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.cutscene.CutsceneCatalog;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Воспроизведение GIF- и PNG-катсцен главы 1. */
public final class CutscenePlayer {

  private CutsceneCache.ScaledSequence scaled;
  private boolean loop;
  private int frameIndex;
  private int frameTick;
  private boolean finished = true;

  public void start(CutsceneId id) {
    start(id, Chapter1ViewConstants.VIRTUAL_W, Chapter1ViewConstants.VIRTUAL_H);
  }

  public void start(CutsceneId id, int sw, int sh) {
    stop();
    if (id == null) {
      finished = true;
      return;
    }
    loop = CutsceneCatalog.loops(id);
    scaled = CutsceneCache.scaledSequence(id, sw, sh);
    if (scaled != null && scaled.frameCount() > 0) {
      frameIndex = 0;
      frameTick = 0;
      finished = false;
      return;
    }
    String[] sequence = CutsceneCatalog.frameSequence(id);
    if (sequence != null) {
      scaled = CutsceneCache.scaledPngSequence(
          sequence, CutsceneCatalog.frameDelayMs(id), sw, sh);
      if (scaled != null && scaled.frameCount() > 0) {
        frameIndex = 0;
        frameTick = 0;
        finished = false;
        return;
      }
    }
    finished = true;
  }

  public void stop() {
    scaled = null;
    finished = true;
  }

  public boolean isFinished() {
    return finished;
  }

  public void tick() {
    if (finished || scaled == null) {
      return;
    }
    frameTick++;
    int delayTicks = currentDelayTicks();
    if (frameTick < delayTicks) {
      return;
    }
    frameTick = 0;
    frameIndex++;
    if (frameIndex >= scaled.frameCount()) {
      if (loop) {
        frameIndex = 0;
      } else {
        finished = true;
      }
    }
  }

  public void render(Graphics2D g, int sw, int sh) {
    if (finished || scaled == null || scaled.frames() == null) {
      return;
    }
    int idx = Math.min(frameIndex, scaled.frameCount() - 1);
    BufferedImage frame = scaled.frames()[idx];
    if (frame == null) {
      return;
    }
    g.drawImage(frame, scaled.dx()[idx], scaled.dy()[idx], null);
  }

  private int currentDelayTicks() {
    if (scaled == null || scaled.delaysMs() == null) {
      return 1;
    }
    int idx = Math.min(frameIndex, scaled.delaysMs().length - 1);
    return Math.max(1, scaled.delaysMs()[idx] / 16);
  }
}
