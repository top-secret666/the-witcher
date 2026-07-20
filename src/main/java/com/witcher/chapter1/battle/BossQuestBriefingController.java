package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.ui.intro.IntroVnUi;
import main.java.com.witcher.ui.shop.view.ShopViewConstants;

import java.util.ArrayList;
import java.util.List;

/** Диалог с ложным контрактом в лавке → нарастающий глитч → чёрный экран. */
public final class BossQuestBriefingController {

  public enum Phase {
    DIALOG,
    TRANSITION
  }

  private static final int MS_PER_TICK = 16;
  private static final int TICKS_PER_CHAR = 2;
  private static final int AUTO_DELAY_TICKS = 50;
  private static final int AUTO_TICKS_PER_CHAR = 1;
  private static final int DISSOLVE_RAMP_MS = 2400;
  private static final int TRANSITION_TOTAL_MS = 2750;
  private static final float SLIDE_SPEED = 0.04f;
  private static final float ACTIVE_SPEED = 0.06f;

  private final BossEntry boss;
  private final BossQuestBriefingScript.NoticeContent notice;
  private final List<BossQuestBriefingScript.DialogLine> lines;

  private Phase phase = Phase.DIALOG;
  private int ticks;
  private int transitionTicks;
  private int noticeOpenTicks;

  private float geraltSlide;
  private float dukeSlide;
  private float leftActiveAnim;
  private float rightActiveAnim;

  private int currentLine;
  private int charIndex;
  private int typeTickCounter;
  private boolean waitingForAdvance;
  private boolean dialogFinished;

  private boolean historyOpen;
  private boolean autoMode;
  private int historyScroll;
  private int autoWaitTicks;
  private boolean historyCloseHovered;

  private final IntroVnUi.ButtonLayout buttons = new IntroVnUi.ButtonLayout();
  private int layoutSw = 480;
  private int layoutSh = 360;

  public BossQuestBriefingController(BossEntry boss) {
    this.boss = boss != null ? boss : BossCatalog.byId("duke");
    this.notice = BossQuestBriefingScript.noticeFor(this.boss);
    this.lines = BossQuestBriefingScript.dialogFor(this.boss);
  }

  public BossEntry boss() {
    return boss;
  }

  public BossQuestBriefingScript.NoticeContent notice() {
    return notice;
  }

  public Phase phase() {
    return phase;
  }

  public boolean inTransition() {
    return phase == Phase.TRANSITION;
  }

  public void tick() {
    ticks++;
    if (phase == Phase.DIALOG) {
      noticeOpenTicks++;
      updateCharacterAnimation();
    }
    if (phase == Phase.TRANSITION) {
      transitionTicks++;
    }
  }

  public void updateDialog(int mouseX, int mouseY, boolean clicked, int wheelNotches, boolean advanceKey) {
    if (phase != Phase.DIALOG || dialogFinished || !showDialog()) {
      return;
    }
    refreshButtonLayout();

    if (historyOpen) {
      if (clicked) {
        if (buttons.historyClose.contains(mouseX, mouseY)
            || !buttons.historyPanel.contains(mouseX, mouseY)) {
          historyOpen = false;
        }
      }
      if (wheelNotches != 0) {
        historyScroll = Math.max(0, historyScroll - wheelNotches * 18);
      }
      historyCloseHovered = buttons.historyClose.contains(mouseX, mouseY);
      return;
    }

    boolean advance = advanceKey;
    if (clicked) {
      if (buttons.backButton.contains(mouseX, mouseY)) {
        if (currentLine > 0) {
          goToPreviousLine();
        }
        return;
      }
      if (buttons.historyButton.contains(mouseX, mouseY)) {
        historyOpen = true;
        historyScroll = 0;
        return;
      }
      if (buttons.autoButton.contains(mouseX, mouseY)) {
        autoMode = !autoMode;
        autoWaitTicks = 0;
        return;
      }
      if (IntroVnUi.isVnButtonRowClick(buttons, mouseX, mouseY)) {
        return;
      }
      advance = true;
    }

    BossQuestBriefingScript.DialogLine line = lines.get(currentLine);
    int totalChars = line.text().length();

    if (waitingForAdvance) {
      if (advance) {
        advanceLine();
      } else if (autoMode) {
        autoWaitTicks++;
        if (autoWaitTicks >= AUTO_DELAY_TICKS) {
          advanceLine();
        }
      }
    } else if (autoMode) {
      typeTickCounter++;
      if (typeTickCounter >= AUTO_TICKS_PER_CHAR) {
        typeTickCounter = 0;
        charIndex++;
        if (charIndex >= totalChars) {
          charIndex = totalChars;
          waitingForAdvance = true;
          autoWaitTicks = 0;
        }
      }
    } else if (advance && charIndex < totalChars) {
      charIndex = totalChars;
      waitingForAdvance = true;
    } else {
      typeTickCounter++;
      if (typeTickCounter >= TICKS_PER_CHAR) {
        typeTickCounter = 0;
        charIndex++;
        if (charIndex >= totalChars) {
          charIndex = totalChars;
          waitingForAdvance = true;
        }
      }
    }
  }

  public BossQuestBriefingScript.DialogLine currentLine() {
    if (phase != Phase.DIALOG || dialogFinished || currentLine < 0 || currentLine >= lines.size()) {
      return null;
    }
    return lines.get(currentLine);
  }

  public String visibleText() {
    BossQuestBriefingScript.DialogLine line = currentLine();
    if (line == null) {
      return "";
    }
    int end = Math.min(charIndex, line.text().length());
    return line.text().substring(0, end);
  }

  public boolean showDialog() {
    return phase == Phase.DIALOG && !dialogFinished && noticeAnimProgress() >= 0.52f;
  }

  public boolean showNotice() {
    return phase == Phase.DIALOG || (phase == Phase.TRANSITION && noticeFade() > 0.05f);
  }

  public float noticeAnimProgress() {
    if (phase == Phase.TRANSITION) {
      return 1f;
    }
    return Math.min(1f, noticeOpenTicks / (float) ShopViewConstants.CATEGORY_OPEN_DURATION_TICKS);
  }

  public float noticeFade() {
    return 1f;
  }

  public float geraltSlide() {
    return geraltSlide;
  }

  public float dukeSlide() {
    return dukeSlide;
  }

  public float leftActiveAnim() {
    return leftActiveAnim;
  }

  public float rightActiveAnim() {
    return rightActiveAnim;
  }

  /** 0..1 — нарастающий dissolve (обратный shard_reveal). */
  public float dissolveT() {
    if (phase != Phase.TRANSITION) {
      return 0f;
    }
    int ms = transitionTicks * MS_PER_TICK;
    return easeIn(Math.min(1f, ms / (float) DISSOLVE_RAMP_MS));
  }

  public boolean isComplete() {
    return phase == Phase.TRANSITION && transitionTicks * MS_PER_TICK >= TRANSITION_TOTAL_MS;
  }

  public int tickCount() {
    return ticks;
  }

  public boolean waitingForAdvance() {
    return waitingForAdvance;
  }

  public boolean autoMode() {
    return autoMode;
  }

  public boolean historyOpen() {
    return historyOpen;
  }

  public boolean historyCloseHovered() {
    return historyCloseHovered;
  }

  public int historyScroll() {
    return historyScroll;
  }

  public void setHistoryScroll(int scroll) {
    historyScroll = Math.max(0, scroll);
  }

  public boolean backEnabled() {
    return currentLine > 0;
  }

  public IntroVnUi.ButtonLayout buttons() {
    refreshButtonLayout();
    return buttons;
  }

  public void setLayoutSize(int sw, int sh) {
    if (sw > 0 && sh > 0) {
      layoutSw = sw;
      layoutSh = sh;
    }
  }

  public List<String> buildHistoryLogLines() {
    List<String> out = new ArrayList<>();
    int limit = Math.min(currentLine + 1, lines.size());
    for (int i = 0; i < limit; i++) {
      BossQuestBriefingScript.DialogLine line = lines.get(i);
      if (line.speaker() != null && !line.speaker().isBlank()) {
        out.add(line.speaker() + ":");
      }
      for (String part : line.text().split("\n", -1)) {
        out.add(part);
      }
      out.add("");
    }
    return out;
  }

  private void advanceLine() {
    if (currentLine >= lines.size() - 1) {
      beginTransition();
      return;
    }
    resetLine(currentLine + 1);
  }

  private void goToPreviousLine() {
    if (currentLine <= 0) {
      return;
    }
    resetLine(currentLine - 1);
  }

  private void beginTransition() {
    phase = Phase.TRANSITION;
    dialogFinished = true;
    transitionTicks = 0;
    historyOpen = false;
    waitingForAdvance = false;
  }

  private void resetLine(int index) {
    currentLine = Math.max(0, Math.min(index, lines.size() - 1));
    charIndex = 0;
    typeTickCounter = 0;
    waitingForAdvance = false;
    autoWaitTicks = 0;
    historyOpen = false;
  }

  private void refreshButtonLayout() {
    IntroVnUi.ButtonLayout layout = IntroVnUi.layoutVnButtons(layoutSw, layoutSh, 0);
    buttons.backButton.set(layout.backButton.x, layout.backButton.y,
        layout.backButton.width, layout.backButton.height);
    buttons.historyButton.set(layout.historyButton.x, layout.historyButton.y,
        layout.historyButton.width, layout.historyButton.height);
    buttons.autoButton.set(layout.autoButton.x, layout.autoButton.y,
        layout.autoButton.width, layout.autoButton.height);
    buttons.historyPanel.set(layout.historyPanel.x, layout.historyPanel.y,
        layout.historyPanel.width, layout.historyPanel.height);
    buttons.historyClose.set(layout.historyClose.x, layout.historyClose.y,
        layout.historyClose.width, layout.historyClose.height);
  }

  private void updateCharacterAnimation() {
    boolean dialogVisible = showDialog();
    boolean geraltWanted = dialogVisible;
    boolean dukeWanted = dialogVisible;

    geraltSlide = geraltWanted
        ? Math.min(1f, geraltSlide + SLIDE_SPEED)
        : Math.max(0f, geraltSlide - SLIDE_SPEED);
    dukeSlide = dukeWanted
        ? Math.min(1f, dukeSlide + SLIDE_SPEED)
        : Math.max(0f, dukeSlide - SLIDE_SPEED * 1.5f);

    BossQuestBriefingScript.DialogLine line = currentLine();
    boolean dukeActive = dialogVisible && line != null && "Герцог".equals(line.speaker());
    boolean geraltActive = dialogVisible && line != null
        && line.speaker() != null && line.speaker().contains("Геральт");

    leftActiveAnim = geraltActive
        ? Math.min(1f, leftActiveAnim + ACTIVE_SPEED)
        : Math.max(0f, leftActiveAnim - ACTIVE_SPEED);
    rightActiveAnim = dukeActive
        ? Math.min(1f, rightActiveAnim + ACTIVE_SPEED)
        : Math.max(0f, rightActiveAnim - ACTIVE_SPEED);
  }

  private static float easeIn(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return c * c;
  }
}
