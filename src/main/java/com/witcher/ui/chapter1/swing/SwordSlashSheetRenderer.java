package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.SwordSlashShowTimeline;
import main.java.com.witcher.chapter1.battle.SwordSlashShowTimeline.ActiveFrame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Отрисовка спрайтовых проблесков мечей на чёрном фоне. */
public final class SwordSlashSheetRenderer {

  private SwordSlashSheetRenderer() {
  }

  public static void paint(Graphics2D g, int width, int height, long renderMs) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, width, height);

    ActiveFrame frame = SwordSlashShowTimeline.frameAt(renderMs);
    if (frame == null) {
      return;
    }
    BufferedImage img = Chapter1UiAssets.swordSlashFrame(frame.sheet(), frame.index());
    if (img == null) {
      return;
    }

    float cover = 0.92f;
    int dw = Math.round(width * cover);
    int dh = Math.round(height * cover);
    float aspect = img.getWidth() / (float) Math.max(1, img.getHeight());
    if (dw / (float) dh > aspect) {
      dw = Math.round(dh * aspect);
    } else {
      dh = Math.round(dw / aspect);
    }
    int x = (width - dw) / 2;
    int y = (height - dh) / 2;
    if (frame.flip()) {
      g.drawImage(img, x + dw, y, -dw, dh, null);
    } else {
      g.drawImage(img, x, y, dw, dh, null);
    }
  }
}
