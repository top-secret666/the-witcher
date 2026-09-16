package main.java.com.witcher.ui.settings;

import main.java.com.witcher.ui.audio.GameAudio;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Окно настроек: скорость печати, мгновенный текст, скорость авто, громкость музыки.
 */
public final class SettingsOverlay {

  public enum Action {
    NONE, BACK
  }

  private enum DragTarget {
    NONE, TEXT_SPEED, AUTO_SPEED, MUSIC
  }

  private static final Color DIM = new Color(0, 0, 0, 160);
  private static final Color PANEL_BG = new Color(18, 12, 8, 240);
  private static final Color PANEL_BORDER = new Color(200, 160, 70);
  private static final Color TITLE = new Color(255, 220, 140);
  private static final Color LABEL = new Color(220, 205, 175);
  private static final Color VALUE = new Color(255, 230, 160);
  private static final Color TRACK = new Color(40, 28, 14);
  private static final Color TRACK_FILL = new Color(180, 140, 50);
  private static final Color KNOB = new Color(255, 220, 120);
  private static final Color CHECK_BG = new Color(28, 18, 8);
  private static final Color CHECK_ON = new Color(230, 185, 80);
  private static final Color BTN_IDLE = new Color(32, 22, 12, 230);
  private static final Color BTN_HOT = new Color(55, 38, 14, 240);
  private static final Color BTN_BORDER_IDLE = new Color(140, 100, 45);
  private static final Color BTN_BORDER_HOT = new Color(230, 185, 80);
  private static final Color BTN_TEXT_IDLE = new Color(220, 195, 140);
  private static final Color BTN_TEXT_HOT = new Color(255, 240, 190);

  private final Rectangle panel = new Rectangle();
  private final Rectangle textSpeedTrack = new Rectangle();
  private final Rectangle autoSpeedTrack = new Rectangle();
  private final Rectangle musicTrack = new Rectangle();
  private final Rectangle typewriterBox = new Rectangle();
  private final Rectangle typewriterHit = new Rectangle();
  private final Rectangle backButton = new Rectangle();

  private Action pending = Action.NONE;
  private boolean backHovered;
  private boolean typewriterHovered;
  private DragTarget drag = DragTarget.NONE;

  public void reset() {
    pending = Action.NONE;
    backHovered = false;
    typewriterHovered = false;
    drag = DragTarget.NONE;
  }

  public void update(int sw, int sh, int mouseX, int mouseY, boolean clicked, boolean mouseDown,
                     boolean activate, boolean esc) {
    layout(sw, sh);
    GameSettings s = GameSettings.get();

    if (esc) {
      pending = Action.BACK;
      drag = DragTarget.NONE;
      return;
    }
    if (activate && drag == DragTarget.NONE) {
      pending = Action.BACK;
      return;
    }

    backHovered = backButton.contains(mouseX, mouseY);
    typewriterHovered = typewriterHit.contains(mouseX, mouseY);

    if (drag != DragTarget.NONE) {
      if (mouseDown) {
        applyDrag(s, mouseX);
      } else {
        drag = DragTarget.NONE;
      }
    }

    if (clicked) {
      if (backHovered) {
        pending = Action.BACK;
        drag = DragTarget.NONE;
        return;
      }
      if (typewriterHit.contains(mouseX, mouseY)) {
        s.setTypewriterEnabled(!s.typewriterEnabled());
        return;
      }
      if (textSpeedTrack.contains(mouseX, mouseY)) {
        drag = DragTarget.TEXT_SPEED;
        applyDrag(s, mouseX);
        return;
      }
      if (autoSpeedTrack.contains(mouseX, mouseY)) {
        drag = DragTarget.AUTO_SPEED;
        applyDrag(s, mouseX);
        return;
      }
      if (musicTrack.contains(mouseX, mouseY)) {
        drag = DragTarget.MUSIC;
        applyDrag(s, mouseX);
      }
    }
  }

  private void applyDrag(GameSettings s, int mouseX) {
    switch (drag) {
      case TEXT_SPEED -> s.setTextSpeed(valueFromTrack(textSpeedTrack, mouseX,
          GameSettings.SPEED_MIN, GameSettings.SPEED_MAX));
      case AUTO_SPEED -> s.setAutoSpeed(valueFromTrack(autoSpeedTrack, mouseX,
          GameSettings.SPEED_MIN, GameSettings.SPEED_MAX));
      case MUSIC -> {
        s.setMusicVolume(valueFromTrack(musicTrack, mouseX,
            GameSettings.VOLUME_MIN, GameSettings.VOLUME_MAX));
        GameAudio.applyMusicVolume();
      }
      case NONE -> {
      }
    }
  }

  private static int valueFromTrack(Rectangle track, int mouseX, int min, int max) {
    if (track.width <= 1) {
      return min;
    }
    float t = (mouseX - track.x) / (float) track.width;
    t = Math.max(0f, Math.min(1f, t));
    return min + Math.round(t * (max - min));
  }

  public Action consumeAction() {
    Action a = pending;
    pending = Action.NONE;
    return a;
  }

  public void draw(Graphics2D g, int sw, int sh) {
    layout(sw, sh);
    GameSettings s = GameSettings.get();

    g.setColor(DIM);
    g.fillRect(0, 0, sw, sh);

    g.setColor(PANEL_BG);
    g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 8, 8);
    g.setColor(PANEL_BORDER);
    g.drawRoundRect(panel.x, panel.y, panel.width - 1, panel.height - 1, 8, 8);

    Object prevAa = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    Font titleFont = GameFonts.get().uiBold(14);
    g.setFont(titleFont);
    FontMetrics tfm = g.getFontMetrics();
    String title = "Настройки";
    g.setColor(TITLE);
    g.drawString(title, panel.x + (panel.width - tfm.stringWidth(title)) / 2,
        panel.y + 14 + tfm.getAscent());

    Font labelFont = GameFonts.get().uiPlain(10);
    g.setFont(labelFont);
    FontMetrics lfm = g.getFontMetrics();
    int labelX = panel.x + 16;

    g.setColor(LABEL);
    g.drawString("Скорость печати текста", labelX, textSpeedTrack.y - 6);
    drawSlider(g, textSpeedTrack, s.textSpeed(), GameSettings.SPEED_MIN, GameSettings.SPEED_MAX,
        String.valueOf(s.textSpeed()), lfm);

    drawCheckbox(g, typewriterBox, s.typewriterEnabled(), typewriterHovered);
    g.setColor(LABEL);
    g.drawString("Печатать текст по буквам", typewriterBox.x + typewriterBox.width + 8,
        typewriterBox.y + typewriterBox.height - 2);
    g.setColor(new Color(170, 155, 125));
    Font hintFont = GameFonts.get().uiPlain(8);
    g.setFont(hintFont);
    FontMetrics hfm = g.getFontMetrics();
    String hint = s.typewriterEnabled()
        ? "Снимите галочку — реплика появится сразу целиком."
        : "Текст показывается сразу, без печатной машинки.";
    g.drawString(hint, labelX, typewriterBox.y + typewriterBox.height + hfm.getAscent() + 3);

    g.setFont(labelFont);
    g.setColor(LABEL);
    g.drawString("Скорость авто", labelX, autoSpeedTrack.y - 6);
    drawSlider(g, autoSpeedTrack, s.autoSpeed(), GameSettings.SPEED_MIN, GameSettings.SPEED_MAX,
        String.valueOf(s.autoSpeed()), lfm);

    g.setColor(LABEL);
    g.drawString("Громкость музыки", labelX, musicTrack.y - 6);
    drawSlider(g, musicTrack, s.musicVolume(), GameSettings.VOLUME_MIN, GameSettings.VOLUME_MAX,
        s.musicVolume() + "%", lfm);

    // --- Назад ---
    Font btnFont = GameFonts.get().uiBold(11);
    g.setFont(btnFont);
    FontMetrics btnFm = g.getFontMetrics();
    g.setColor(backHovered ? BTN_HOT : BTN_IDLE);
    g.fillRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 5, 5);
    g.setColor(backHovered ? BTN_BORDER_HOT : BTN_BORDER_IDLE);
    g.drawRoundRect(backButton.x, backButton.y, backButton.width - 1, backButton.height - 1, 5, 5);
    String back = "Назад";
    g.setColor(backHovered ? BTN_TEXT_HOT : BTN_TEXT_IDLE);
    g.drawString(back,
        backButton.x + (backButton.width - btnFm.stringWidth(back)) / 2,
        backButton.y + (backButton.height + btnFm.getAscent() - btnFm.getDescent()) / 2);

    if (prevAa != null) {
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, prevAa);
    }
  }

  private void drawSlider(Graphics2D g, Rectangle track, int value, int min, int max,
                          String valueText, FontMetrics fm) {
    g.setColor(TRACK);
    g.fillRoundRect(track.x, track.y, track.width, track.height, 4, 4);
    g.setColor(PANEL_BORDER);
    g.drawRoundRect(track.x, track.y, track.width - 1, track.height - 1, 4, 4);

    float t = (value - min) / (float) Math.max(1, max - min);
    int fillW = Math.max(0, Math.round(track.width * t));
    if (fillW > 0) {
      g.setColor(TRACK_FILL);
      g.fillRoundRect(track.x + 1, track.y + 1, Math.min(fillW, track.width - 2), track.height - 2, 3, 3);
    }

    int knobX = track.x + Math.round((track.width - 8) * t);
    g.setColor(KNOB);
    g.fillRoundRect(knobX, track.y - 2, 8, track.height + 4, 3, 3);
    g.setColor(PANEL_BORDER);
    g.drawRoundRect(knobX, track.y - 2, 7, track.height + 3, 3, 3);

    g.setColor(VALUE);
    g.drawString(valueText, track.x + track.width + 10, track.y + track.height - 2);
  }

  private void drawCheckbox(Graphics2D g, Rectangle box, boolean on, boolean hot) {
    g.setColor(CHECK_BG);
    g.fillRect(box.x, box.y, box.width, box.height);
    g.setColor(hot ? BTN_BORDER_HOT : PANEL_BORDER);
    g.drawRect(box.x, box.y, box.width - 1, box.height - 1);
    if (on) {
      g.setColor(CHECK_ON);
      g.fillRect(box.x + 3, box.y + 3, box.width - 6, box.height - 6);
    }
  }

  private void layout(int sw, int sh) {
    int panelW = 300;
    int panelH = 248;
    int px = (sw - panelW) / 2;
    int py = (sh - panelH) / 2;
    panel.setBounds(px, py, panelW, panelH);

    int trackX = px + 16;
    int trackW = 200;
    int trackH = 10;
    textSpeedTrack.setBounds(trackX, py + 48, trackW, trackH);

    typewriterBox.setBounds(trackX, py + 92, 12, 12);
    typewriterHit.setBounds(trackX, py + 88, panelW - 40, 36);

    autoSpeedTrack.setBounds(trackX, py + 148, trackW, trackH);
    musicTrack.setBounds(trackX, py + 182, trackW, trackH);

    int btnW = 120;
    int btnH = 26;
    backButton.setBounds(px + (panelW - btnW) / 2, py + panelH - btnH - 12, btnW, btnH);
  }
}
