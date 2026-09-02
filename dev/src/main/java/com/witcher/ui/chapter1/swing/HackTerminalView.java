package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.hack.HackConsoleModel;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.PixelScaler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Отрисовка терминала взлома поверх лавки. */
public final class HackTerminalView {

  private HackTerminalView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, HackConsoleModel hack, int hackShakeTick) {
    BufferedImage boot = Chapter1UiAssets.bootBackground();
    if (boot != null) {
      g.drawImage(PixelScaler.sharpScale(boot, sw, sh), 0, 0, null);
    } else {
      g.setColor(new Color(0, 0, 0, 200));
      g.fillRect(0, 0, sw, sh);
    }
    if (hack == null) {
      return;
    }

    int shake = shakeOffset(hack, hackShakeTick);
    BufferedImage frame = Chapter1UiAssets.terminalFrame();
    int tx = 0;
    int ty = 0;
    int tw = sw;
    int th = sh;
    if (frame != null) {
      float scale = Math.min(sw / (float) frame.getWidth(), sh / (float) frame.getHeight());
      tw = Math.max(1, Math.round(frame.getWidth() * scale));
      th = Math.max(1, Math.round(frame.getHeight() * scale));
      tx = (sw - tw) / 2 + shake;
      ty = (sh - th) / 2;
      g.drawImage(PixelScaler.sharpScale(frame, tw, th), tx, ty, null);
    }

    BufferedImage timer = Chapter1UiAssets.timerBar();
    if (timer != null) {
      int barW = Math.min(Math.round(timer.getWidth() * 1.6f), tw - Math.round(tw * 0.2f));
      int barH = Math.max(10, Math.round(timer.getHeight() * (barW / (float) timer.getWidth())));
      int barX = tx + (tw - barW) / 2;
      int barY = ty + Math.round(th * 0.07f);
      g.drawImage(PixelScaler.sharpScale(timer, barW, barH), barX, barY, null);
      float progress = hack.ticksRemaining() / (float) HackConsoleModel.TIMER_TICKS;
      int inset = Math.max(2, Math.round(barW * 0.03f));
      int fillW = Math.round((barW - inset * 2) * progress);
      g.setColor(new Color(200, 50, 50, 230));
      g.fillRect(barX + inset, barY + inset, fillW, barH - inset * 2);
    }

    int padX = Math.round(tw * 0.14f);
    int padTop = Math.round(th * 0.18f);
    int textX = tx + padX;
    int textY = ty + padTop;
    int textBottom = ty + th - Math.round(th * 0.16f);

    g.setFont(GameFonts.get().uiPlain(8));
    g.setColor(new Color(120, 255, 120));
    int y = textY;
    for (String line : hack.logText().split("\n")) {
      if (line.isEmpty() || y > textBottom - 24) {
        continue;
      }
      g.drawString(line, textX, y);
      y += 11;
    }
    g.drawString("> " + hack.inputLine() + "_", textX, textBottom - 8);
    g.setColor(new Color(160, 220, 160));
    g.drawString("ENTER — выполнить, ESC — выход", textX, textBottom + 10);
  }

  private static int shakeOffset(HackConsoleModel hack, int hackShakeTick) {
    double urgency = 1.0 - (hack.ticksRemaining() / (double) HackConsoleModel.TIMER_TICKS);
    return (int) Math.round(Math.sin(hackShakeTick * 0.65) * (1 + urgency * 5));
  }
}
