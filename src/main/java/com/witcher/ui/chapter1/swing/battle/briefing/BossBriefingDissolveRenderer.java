package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.ui.chapter1.swing.WakeVisionRenderer;
import main.java.com.witcher.ui.chapter1.swing.glitch.CutsceneNoiseOverlay;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Чёрный pixel-dissolve после брифинга (обратный wolf_shard_reveal). */
public final class BossBriefingDissolveRenderer {

  private BossBriefingDissolveRenderer() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BufferedImage snap, float dissolveT) {
    float clarity = Math.max(0f, 1f - dissolveT);
    WakeVisionRenderer.drawFrame(g, snap, 0, 0, clarity);

    float noise = dissolveT <= 0.01f ? 0f : (1f - clarity) * 0.95f + 0.08f;
    if (noise > 0.02f) {
      CutsceneNoiseOverlay.draw(g, sw, sh, Math.min(1f, noise));
    }

    if (clarity < 0.48f) {
      float fog = (0.48f - clarity) * 0.62f;
      Composite prev = g.getComposite();
      g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, fog));
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      g.setComposite(prev);
    }

    if (dissolveT > 0.68f) {
      float extra = (dissolveT - 0.68f) / 0.32f;
      g.setColor(new Color(0, 0, 0, Math.round(255 * Math.min(1f, extra))));
      g.fillRect(0, 0, sw, sh);
    }
  }
}
