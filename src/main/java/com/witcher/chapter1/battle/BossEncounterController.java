package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.intro.IntroVnUi;

import java.util.ArrayList;
import java.util.List;

/**
 * После loop_wake: тьма → веки → VN с выбором и ветвлением (Волк).
 */
public final class BossEncounterController {

  private static final int MS_PER_TICK = 16;
  private static final int CLOSED_HOLD_MS = 900;
  private static final int OPEN_MS = 1600;
  private static final int TICKS_PER_CHAR = 2;
  private static final int AUTO_DELAY_TICKS = 50;
  private static final int AUTO_TICKS_PER_CHAR = 1;

  private final BossEntry boss;
  private final List<BossEncounterScript.DialogEntry> entries =
      new ArrayList<>(WolfBossEncounterScript.introLines());

  private WolfBossEncounterScript.Branch branch = WolfBossEncounterScript.Branch.NONE;

  private int ticks;
  private boolean dialogFinished;

  private int currentEntry;
  private int charIndex;
  private int typeTickCounter;
  private boolean waitingForAdvance;
  private boolean vnStarted;

  private boolean awaitingFirstChoice;
  private VnSceneState choiceScene;

  private boolean historyOpen;
  private boolean autoMode;
  private int historyScroll;
  private int autoWaitTicks;
  private boolean historyCloseHovered;

  private final IntroVnUi.ButtonLayout buttons = new IntroVnUi.ButtonLayout();
  private int layoutSw = 480;
  private int layoutSh = 360;

  public BossEncounterController(BossEntry boss) {
    this.boss = boss != null ? boss : BossCatalog.byId("duke");
  }

  public BossEntry boss() {
    return boss;
  }

  public WolfBossEncounterScript.Branch branch() {
    return branch;
  }

  public boolean choseListenPath() {
    return branch == WolfBossEncounterScript.Branch.LISTEN;
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

    if (awaitingFirstChoice) {
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
        historyScroll = Math.max(0, historyScroll - wheelNotches * 18);
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
        autoWaitTicks = 0;
        return;
      }
      if (IntroVnUi.isVnButtonRowClick(buttons, mouseX, mouseY)) {
        return;
      }
      advance = true;
    }

    BossEncounterScript.DialogEntry entry = entries.get(currentEntry);
    int totalChars = entry.text().length();

    if (waitingForAdvance) {
      if (advance) {
        advanceDialogueEntry();
      } else if (autoMode) {
        autoWaitTicks++;
        if (autoWaitTicks >= AUTO_DELAY_TICKS) {
          advanceDialogueEntry();
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

  public void choose(int index, Chapter1Session session) {
    if (!awaitingFirstChoice || choiceScene == null || !choiceScene.waitingForChoice()) {
      return;
    }
    choiceScene.select(index);
    VnChoice choice = choiceScene.selectedChoice();
    if (choice != null && session != null) {
      if (choice.suspicionDelta() > 0) {
        session.addSuspicion(choice.suspicionDelta());
      }
      if (choice.trustDelta() > 0) {
        session.addTrust(choice.trustDelta());
      }
    }
    branch = index == 0
        ? WolfBossEncounterScript.Branch.HURRY
        : WolfBossEncounterScript.Branch.LISTEN;
    entries.addAll(WolfBossEncounterScript.continuation(branch));
    awaitingFirstChoice = false;
    choiceScene = null;
    resetEntry(WolfBossEncounterScript.CHOICE_GATE_INDEX + 1);
  }

  public boolean waitingForChoice() {
    return awaitingFirstChoice && choiceScene != null && choiceScene.waitingForChoice();
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
    return eyesFullyOpen() && vnStarted && !dialogFinished && !awaitingFirstChoice;
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
    int end = Math.min(charIndex, entry.text().length());
    return entry.text().substring(0, end);
  }

  public int charIndex() {
    return charIndex;
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
    return currentEntry > 0 && !awaitingFirstChoice;
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
    } else if (eyesFullyOpen()) {
      expr = BossEncounterScript.Expression.MAP;
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
      for (String part : e.text().split("\n", -1)) {
        lines.add(part);
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
        && branch == WolfBossEncounterScript.Branch.NONE) {
      awaitingFirstChoice = true;
      choiceScene = WolfBossEncounterScript.firstChoiceScene();
      waitingForAdvance = false;
      return;
    }
    if (currentEntry >= entries.size() - 1) {
      dialogFinished = true;
      return;
    }
    resetEntry(currentEntry + 1);
  }

  private void goToPreviousEntry() {
    if (currentEntry <= 0 || awaitingFirstChoice) {
      return;
    }
    resetEntry(currentEntry - 1);
  }

  private void resetEntry(int index) {
    currentEntry = Math.max(0, Math.min(index, entries.size() - 1));
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
