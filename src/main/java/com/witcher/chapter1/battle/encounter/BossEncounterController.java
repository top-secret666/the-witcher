package main.java.com.witcher.chapter1.battle.encounter;

import main.java.com.witcher.chapter1.battle.BossCatalog;
import main.java.com.witcher.chapter1.battle.BossEntry;
import main.java.com.witcher.chapter1.battle.BossVnTypingEngine;
import main.java.com.witcher.chapter1.battle.BossVnTypingState;
import main.java.com.witcher.chapter1.battle.wolf.WolfBossEncounterScript;
import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.chapter1.vn.VnChoiceEffects;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.intro.IntroVnUi;
import main.java.com.witcher.ui.intro.view.IntroHistoryLayout;
import main.java.com.witcher.ui.intro.view.IntroHistoryLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * После loop_wake: лес, только Волк — выбор №2, затем финал с выбором №3.
 */
public final class BossEncounterController {

  private static final int MS_PER_TICK = BossEncounterConstants.MS_PER_TICK;
  private static final int CLOSED_HOLD_MS = BossEncounterConstants.CLOSED_HOLD_MS;
  private static final int OPEN_MS = BossEncounterConstants.OPEN_MS;
  private static final int TICKS_PER_CHAR = BossEncounterConstants.TICKS_PER_CHAR;
  private static final int AUTO_DELAY_TICKS = BossEncounterConstants.AUTO_DELAY_TICKS;
  private static final int AUTO_TICKS_PER_CHAR = BossEncounterConstants.AUTO_TICKS_PER_CHAR;

  private final BossEntry boss;
  private final List<BossEncounterScript.DialogEntry> entries;

  private WolfBossEncounterScript.MemoryBranch memoryBranch =
      WolfBossEncounterScript.MemoryBranch.NONE;

  private int ticks;
  private boolean dialogFinished;

  private int currentEntry;
  private final BossVnTypingState typing = new BossVnTypingState();
  private boolean vnStarted;

  private boolean awaitingChoice;
  private VnSceneState choiceScene;

  private boolean historyOpen;
  private boolean autoMode;
  private int historyScroll;
  private boolean historyCloseHovered;

  private final IntroVnUi.ButtonLayout buttons = new IntroVnUi.ButtonLayout();
  private int layoutSw = 480;
  private int layoutSh = 360;

  public BossEncounterController(BossEntry boss, Chapter1Session session) {
    this.boss = boss != null ? boss : BossCatalog.byId("duke");
    this.entries = new ArrayList<>(WolfBossEncounterScript.introLines(session));
  }

  public BossEntry boss() {
    return boss;
  }

  public WolfBossEncounterScript.MemoryBranch memoryBranch() {
    return memoryBranch;
  }

  public int elapsedMs() {
    return ticks * MS_PER_TICK;
  }

  public void tick() {
    ticks++;
  }

  public void updateDialog(int mouseX, int mouseY, boolean clicked, int wheelNotches, boolean advanceKey) {
    if (!eyesFullyOpen()) {
      return;
    }
    if (!vnStarted) {
      vnStarted = true;
      resetEntry(0);
    }
    refreshButtonLayout();

    if (awaitingChoice) {
      return;
    }

    if (historyOpen) {
      if (clicked) {
        if (buttons.historyClose.contains(mouseX, mouseY)
            || !buttons.historyPanel.contains(mouseX, mouseY)) {
          historyOpen = false;
        }
      }
      if (wheelNotches != 0) {
        historyScroll = Math.max(0, historyScroll - wheelNotches * IntroHistoryLayout.SCROLL_STEP_PX);
      }
      historyCloseHovered = buttons.historyClose.contains(mouseX, mouseY);
      return;
    }

    boolean advance = advanceKey;
    if (clicked) {
      if (buttons.backButton.contains(mouseX, mouseY)) {
        if (currentEntry > 0) {
          goToPreviousEntry();
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
        typing.clearAutoWait();
        return;
      }
      if (IntroVnUi.isVnButtonRowClick(buttons, mouseX, mouseY)) {
        return;
      }
      advance = true;
    }

    BossEncounterScript.DialogEntry entry = entries.get(currentEntry);
    int totalChars = entry.text().length();

    if (BossVnTypingEngine.tick(
            typing, totalChars, advance, autoMode,
            TICKS_PER_CHAR, AUTO_TICKS_PER_CHAR, AUTO_DELAY_TICKS)
        == BossVnTypingEngine.TickResult.ADVANCE_LINE) {
      advanceDialogueEntry();
    }
  }

  public void choose(int index, Chapter1Session session) {
    if (!awaitingChoice || choiceScene == null || !choiceScene.waitingForChoice()) {
      return;
    }
    choiceScene.select(index);
    VnChoice choice = choiceScene.selectedChoice();
    if (choice != null && session != null) {
      VnChoiceEffects.apply(session, choice);
    }
    memoryBranch = index == 0
        ? WolfBossEncounterScript.MemoryBranch.DISMISS
        : WolfBossEncounterScript.MemoryBranch.ACKNOWLEDGE;
    entries.addAll(WolfBossEncounterScript.continuation(memoryBranch));
    awaitingChoice = false;
    choiceScene = null;
    resetEntry(WolfBossEncounterScript.CHOICE_GATE_INDEX + 1);
  }

  public boolean waitingForChoice() {
    return awaitingChoice && choiceScene != null && choiceScene.waitingForChoice();
  }

  public VnSceneState choiceScene() {
    return choiceScene;
  }

  public float eyelidOpenT() {
    int ms = elapsedMs();
    if (ms < CLOSED_HOLD_MS) {
      return 0f;
    }
    if (ms >= CLOSED_HOLD_MS + OPEN_MS) {
      return 1f;
    }
    float t = (ms - CLOSED_HOLD_MS) / (float) OPEN_MS;
    return easeOutCubic(t);
  }

  public boolean eyesFullyOpen() {
    return eyelidOpenT() >= 1f;
  }

  public float portraitScale() {
    return 1f;
  }

  public boolean showDialog() {
    return eyesFullyOpen() && vnStarted && !dialogFinished && !awaitingChoice;
  }

  public BossEncounterScript.DialogEntry currentEntry() {
    if (!showDialog() || currentEntry < 0 || currentEntry >= entries.size()) {
      return null;
    }
    return entries.get(currentEntry);
  }

  public String visibleText() {
    BossEncounterScript.DialogEntry entry = currentEntry();
    if (entry == null) {
      return "";
    }
    return typing.visibleText(entry.text());
  }

  public int tickCount() {
    return ticks;
  }

  public boolean waitingForAdvance() {
    return typing.waitingForAdvance();
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
    return currentEntry > 0 && !awaitingChoice;
  }

  public IntroVnUi.ButtonLayout buttons() {
    refreshButtonLayout();
    return buttons;
  }

  public String spritePath() {
    BossEncounterScript.Expression expr = BossEncounterScript.Expression.MAP;
    BossEncounterScript.DialogEntry entry = currentEntry();
    if (entry != null) {
      expr = entry.expression();
    }
    return BossEncounterScript.spritePathFor(expr);
  }

  public String spritePathForScene() {
    if (!eyesFullyOpen() || currentEntry() == null) {
      return BossEncounterScript.spritePathFor(BossEncounterScript.Expression.MAP);
    }
    return spritePath();
  }

  public List<String> buildHistoryLogLines() {
    List<String> lines = new ArrayList<>();
    int limit = Math.min(currentEntry + 1, entries.size());
    for (int i = 0; i < limit; i++) {
      BossEncounterScript.DialogEntry e = entries.get(i);
      if (e.speaker() != null && !e.speaker().isBlank()) {
        lines.add(e.speaker() + ":");
      }
      String body = DialogBoxRenderer.normalizeFlowText(e.text());
      if (!body.isBlank()) {
        lines.add(body);
      }
      lines.add("");
    }
    return lines;
  }

  public boolean isDialogComplete() {
    return dialogFinished && eyesFullyOpen();
  }

  private void advanceDialogueEntry() {
    if (currentEntry == WolfBossEncounterScript.CHOICE_GATE_INDEX
        && memoryBranch == WolfBossEncounterScript.MemoryBranch.NONE) {
      awaitingChoice = true;
      choiceScene = WolfBossEncounterScript.memoryChoiceScene();
      typing.clearWaitingForAdvance();
      return;
    }
    if (currentEntry >= entries.size() - 1) {
      dialogFinished = true;
      return;
    }
    resetEntry(currentEntry + 1);
  }

  private void goToPreviousEntry() {
    if (currentEntry <= 0 || awaitingChoice) {
      return;
    }
    resetEntry(currentEntry - 1);
  }

  private void resetEntry(int index) {
    currentEntry = Math.max(0, Math.min(index, entries.size() - 1));
    typing.reset();
    historyOpen = false;
  }

  private void refreshButtonLayout() {
    IntroVnUi.copyButtonLayout(IntroVnUi.layoutVnButtons(layoutSw, layoutSh, 0), buttons);
  }

  public void setLayoutSize(int sw, int sh) {
    if (sw > 0 && sh > 0) {
      layoutSw = sw;
      layoutSh = sh;
    }
  }

  private static float easeOutCubic(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return 1f - (float) Math.pow(1f - c, 3);
  }
}
