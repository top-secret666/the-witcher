package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Director;
import main.java.com.witcher.chapter1.Chapter1Phase;
import main.java.com.witcher.chapter1.battle.BattleOutcome;
import main.java.com.witcher.chapter1.battle.BattleVnController;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.chapter1.cutscene.CutsceneCatalog;
import main.java.com.witcher.chapter1.ending.EscapeEnding;
import main.java.com.witcher.chapter1.hack.HackConsoleModel;
import main.java.com.witcher.chapter1.shop.Chapter1ShopBridge;
import main.java.com.witcher.chapter1.vn.DukeDialogController;
import main.java.com.witcher.chapter1.vn.EndingVnController;
import main.java.com.witcher.chapter1.vn.VnChoiceLayout;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.swing.ShopScreen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Фасад главы 1 для {@link main.java.com.witcher.ui.graphics.GameWindow}.
 * Логика — {@link Chapter1Director}; лавка — {@link ShopScreen}.
 */
public final class Chapter1Screen {

  private final Chapter1Director director;
  private final Chapter1ShopBridge shopBridge;
  private final ShopModel shopModel;
  private final ShopScreen shopScreen;
  private final CutscenePlayer cutscenePlayer = new CutscenePlayer();
  private final CutscenePlayer doorLoopPlayer = new CutscenePlayer();

  private BattleVnController battle;
  private EndingVnController ending;
  private DukeDialogController dukeDialog = new DukeDialogController();
  private HackConsoleModel hack;
  private List<VnChoiceLayout.ChoiceRect> choiceRects = List.of();
  private boolean exitRequested;

  public Chapter1Screen() {
    this(Chapter1Director.loadOrNew(), ShopModel.createNewSession());
  }

  public Chapter1Screen(Chapter1Director director, ShopModel shopModel) {
    this.director = director;
    this.shopModel = shopModel;
    this.shopBridge = new Chapter1ShopBridge(director.session(), director);
    this.shopScreen = new ShopScreen(shopModel, shopBridge);
    wireBridgeListeners();
  }

  public Chapter1Director director() {
    return director;
  }

  public ShopModel shopModel() {
    return shopModel;
  }

  public void beginAfterIntro() {
    director.beginAfterIntro();
    startCutsceneIfNeeded();
  }

  public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
    switch (director.phase()) {
      case CUTSCENE -> updateCutscene(clicked);
      case SHOP -> {
        if (dukeDialog.isActive()) {
          updateDukeDialog(mouseX, mouseY, clicked);
        } else {
          shopScreen.update(mouseX, mouseY, clicked, escPressed, wheelNotches);
          shopBridge.fireBattleIfPendingAndIdle(shopScreen.presenter().isChapterEventIdle());
          maybeStartDukeDialog();
        }
      }
      case VN_BATTLE -> updateBattle(mouseX, mouseY, clicked);
      case VN_DIALOG -> updateDukeDialog(mouseX, mouseY, clicked);
      case HACK -> updateHack(escPressed);
      case ENDING -> updateEnding(mouseX, mouseY, clicked);
    }
    if (escPressed && director.phase() == Chapter1Phase.SHOP) {
      exitRequested = shopScreen.isExitRequested();
    }
  }

  public void keyPressed(KeyEvent e) {
    if (e == null) {
      return;
    }
    int code = e.getKeyCode();
    if (director.phase() == Chapter1Phase.SHOP) {
      if (isDukeDialogActive() && dukeDialog.scene().waitingForChoice()) {
        int choice = keyToChoiceIndex(code);
        if (choice >= 0) {
          applyDukeChoice(choice);
        }
        return;
      }
      if (code == KeyEvent.VK_BACK_QUOTE || code == KeyEvent.VK_DEAD_GRAVE) {
        shopBridge.tryOpenTerminal();
      } else if (code == KeyEvent.VK_B && e.isControlDown()) {
        director.requestBattle();
        startCutsceneIfNeeded();
      }
      return;
    }
    if (director.phase() == Chapter1Phase.VN_BATTLE && battle != null && battle.scene().waitingForChoice()) {
      int choice = keyToChoiceIndex(code);
      if (choice >= 0) {
        applyBattleChoice(choice);
      } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        advanceBattleNarration();
      }
      return;
    }
    if (isDukeDialogActive() && dukeDialog.scene().waitingForChoice()) {
      int choice = keyToChoiceIndex(code);
      if (choice >= 0) {
        applyDukeChoice(choice);
      }
      return;
    }
    if (director.phase() == Chapter1Phase.ENDING && ending != null) {
      if (ending.scene().waitingForChoice()) {
        int choice = keyToChoiceIndex(code);
        if (choice >= 0) {
          applyEndingChoice(choice);
        }
      } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        advanceEnding();
      }
      return;
    }
    if (director.phase() == Chapter1Phase.VN_BATTLE
        && (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE)) {
      advanceBattleNarration();
      return;
    }
    if (director.phase() == Chapter1Phase.HACK && hack != null) {
      if (code == KeyEvent.VK_ENTER) {
        handleHackSubmit();
      } else if (code == KeyEvent.VK_BACK_SPACE) {
        hack.backspace();
      }
    }
  }

  public void keyTyped(KeyEvent e) {
    if (e == null || director.phase() != Chapter1Phase.HACK || hack == null) {
      return;
    }
    char c = e.getKeyChar();
    if (c == '\b' || c == KeyEvent.CHAR_UNDEFINED) {
      return;
    }
    if (c == '\n' || c == '\r') {
      handleHackSubmit();
      return;
    }
    hack.appendChar(c);
  }

  public void render(BufferedImage screen, int mouseX, int mouseY) {
    int sw = screen.getWidth();
    int sh = screen.getHeight();

    switch (director.phase()) {
      case CUTSCENE -> {
        Graphics2D g = screen.createGraphics();
        try {
          g.setColor(Color.BLACK);
          g.fillRect(0, 0, sw, sh);
          cutscenePlayer.render(g, sw, sh);
        } finally {
          g.dispose();
        }
      }
      case SHOP -> {
        shopScreen.render(screen, mouseX, mouseY);
        if (isDukeDialogActive()) {
          Graphics2D g = screen.createGraphics();
          try {
            renderVnOverlay(g, screen.getWidth(), screen.getHeight());
          } finally {
            g.dispose();
          }
        }
      }
      case HACK -> shopScreen.render(screen, mouseX, mouseY);
      case VN_BATTLE, VN_DIALOG, ENDING -> {
        Graphics2D g = screen.createGraphics();
        try {
          renderVnScene(g, sw, sh);
        } finally {
          g.dispose();
        }
      }
    }

    if (director.phase() != Chapter1Phase.CUTSCENE) {
      Graphics2D overlay = screen.createGraphics();
      try {
        GlitchOverlayRenderer.draw(overlay, sw, sh, director.session());
        if (director.phase() == Chapter1Phase.HACK) {
          doorLoopPlayer.render(overlay, sw, sh);
          renderHackOverlay(overlay, sw, sh);
        }
        if (director.phase() == Chapter1Phase.SHOP || director.phase() == Chapter1Phase.HACK) {
          Chapter1SessionHud.draw(overlay, sw, director.session());
        }
      } finally {
        overlay.dispose();
      }
    }
  }

  public void renderTextOverlay(Graphics2D g, int mouseX, int mouseY) {
    if (director.phase() == Chapter1Phase.SHOP || director.phase() == Chapter1Phase.HACK) {
      shopScreen.renderTextOverlay(g, mouseX, mouseY);
    }
    if (hasActiveChoices()) {
      renderVnChoices(g);
    }
  }

  public boolean isExitRequested() {
    return exitRequested;
  }

  public void clearExitRequest() {
    exitRequested = false;
    shopScreen.clearExitRequest();
  }

  public boolean isChapterComplete() {
    return director.isChapterComplete();
  }

  private void wireBridgeListeners() {
    shopBridge.setOnBattleRequested(() -> {
      director.requestBattle();
      startCutsceneIfNeeded();
    });
    shopBridge.setOnTerminalRequested(() -> {
      director.requestHackTerminal();
      onPhaseEntered();
    });
    shopBridge.setOnPurchaseHook(() -> dukeDialog.onPrisonIncreased(director.session()));
  }

  private void startCutsceneIfNeeded() {
    if (director.phase() != Chapter1Phase.CUTSCENE) {
      return;
    }
    CutsceneId id = director.pendingCutscene();
    director.skipCutsceneIfMissing();
    if (director.phase() != Chapter1Phase.CUTSCENE) {
      onPhaseEntered();
      return;
    }
    cutscenePlayer.start(id);
    if (cutscenePlayer.isFinished()) {
      director.onCutsceneFinished();
      onPhaseEntered();
    }
  }

  private void updateCutscene(boolean clicked) {
    if (cutscenePlayer.isFinished()) {
      director.onCutsceneFinished();
      onPhaseEntered();
      return;
    }
    cutscenePlayer.tick();
    if (clicked) {
      cutscenePlayer.stop();
      director.onCutsceneFinished();
      onPhaseEntered();
    }
  }

  private void onPhaseEntered() {
    if (director.phase() == Chapter1Phase.CUTSCENE) {
      startCutsceneIfNeeded();
    } else if (director.phase() == Chapter1Phase.VN_BATTLE) {
      battle = new BattleVnController(director.session(), shopModel);
      battle.advanceIntro();
      refreshChoiceRects();
    } else if (director.phase() == Chapter1Phase.ENDING) {
      ending = new EndingVnController(director.session());
      refreshChoiceRects();
    } else if (director.phase() == Chapter1Phase.HACK) {
      hack = new HackConsoleModel(director.session());
      director.session().registerHackAttempt();
      String doorPath = CutsceneCatalog.resourcePath(CutsceneId.HACK_DOOR_POUND);
      if (doorPath != null && Chapter1Director.class.getResource(doorPath) != null) {
        doorLoopPlayer.start(CutsceneId.HACK_DOOR_POUND);
      } else {
        doorLoopPlayer.stop();
      }
    } else if (director.phase() == Chapter1Phase.SHOP) {
      doorLoopPlayer.stop();
      maybeStartDukeDialog();
    } else {
      doorLoopPlayer.stop();
    }
  }

  private void updateBattle(int mouseX, int mouseY, boolean clicked) {
    if (battle == null) {
      battle = new BattleVnController(director.session(), shopModel);
      battle.advanceIntro();
      refreshChoiceRects();
      return;
    }
    if (!clicked) {
      return;
    }
    if (battle.scene().waitingForChoice()) {
      int index = VnChoiceLayout.hitIndex(choiceRects, mouseX, mouseY);
      if (index >= 0) {
        applyBattleChoice(index);
      }
      return;
    }
    advanceBattleNarration();
  }

  private void updateDukeDialog(int mouseX, int mouseY, boolean clicked) {
    if (!dukeDialog.isActive() || !clicked) {
      return;
    }
    if (dukeDialog.scene().waitingForChoice()) {
      int index = VnChoiceLayout.hitIndex(choiceRects, mouseX, mouseY);
      if (index >= 0) {
        applyDukeChoice(index);
      }
    }
  }

  private void updateEnding(int mouseX, int mouseY, boolean clicked) {
    if (ending == null) {
      ending = new EndingVnController(director.session());
      refreshChoiceRects();
      return;
    }
    if (!clicked) {
      return;
    }
    if (ending.scene().waitingForChoice()) {
      int index = VnChoiceLayout.hitIndex(choiceRects, mouseX, mouseY);
      if (index >= 0) {
        applyEndingChoice(index);
      }
      return;
    }
    advanceEnding();
  }

  private void maybeStartDukeDialog() {
    if (dukeDialog.isActive()) {
      return;
    }
    if (dukeDialog.pollPending(director.session()) != null) {
      refreshChoiceRects();
    }
  }

  private void applyDukeChoice(int index) {
    dukeDialog.choose(index, director.session());
    choiceRects = List.of();
    if (director.phase() == Chapter1Phase.VN_DIALOG) {
      director.exitDukeDialog();
    }
  }

  private void applyEndingChoice(int index) {
    ending.choose(index);
    refreshChoiceRects();
  }

  private void advanceEnding() {
    if (ending == null) {
      return;
    }
    if (ending.isDone()) {
      finishEnding();
      return;
    }
    ending.advance();
    refreshChoiceRects();
    if (ending.isDone()) {
      finishEnding();
    }
  }

  private void finishEnding() {
    EscapeEnding result = ending.resolvedEnding();
    ending = null;
    choiceRects = List.of();
    if (result == EscapeEnding.LOCKED) {
      director.enterShop();
      return;
    }
    director.resolveEscapeEnding();
    startCutsceneIfNeeded();
  }

  private void applyBattleChoice(int index) {
    battle.choose(index);
    refreshChoiceRects();
    if (battle.isFinished()) {
      onBattleFinished();
    }
  }

  private void advanceBattleNarration() {
    if (battle == null || battle.scene().waitingForChoice()) {
      return;
    }
    if (battle.isFinished()) {
      onBattleFinished();
      return;
    }
    battle.advanceAfterNarration();
    refreshChoiceRects();
  }

  private void refreshChoiceRects() {
    VnSceneState scene = activeScene();
    if (scene == null || !scene.waitingForChoice()) {
      choiceRects = List.of();
      return;
    }
    choiceRects = VnChoiceLayout.layout(480, 360, scene.choices());
  }

  private VnSceneState activeScene() {
    if (director.phase() == Chapter1Phase.VN_BATTLE && battle != null) {
      return battle.scene();
    }
    if (director.phase() == Chapter1Phase.ENDING && ending != null) {
      return ending.scene();
    }
    if (isDukeDialogActive()) {
      return dukeDialog.scene();
    }
    return null;
  }

  private boolean isDukeDialogActive() {
    return dukeDialog.isActive();
  }

  private boolean hasActiveChoices() {
    VnSceneState scene = activeScene();
    return scene != null && scene.waitingForChoice();
  }

  private static int keyToChoiceIndex(int keyCode) {
    return switch (keyCode) {
      case KeyEvent.VK_1 -> 0;
      case KeyEvent.VK_2 -> 1;
      case KeyEvent.VK_3 -> 2;
      case KeyEvent.VK_4 -> 3;
      default -> -1;
    };
  }

  private void onBattleFinished() {
    BattleOutcome outcome = battle.outcome();
    switch (outcome) {
      case PLAYER_DEFEAT, IMPOSSIBLE_WIN -> director.onBattleDefeat();
      default -> director.onBattleStunReturnToShop();
    }
    battle = null;
    choiceRects = List.of();
    startCutsceneIfNeeded();
  }

  private void updateHack(boolean esc) {
    if (hack == null) {
      hack = new HackConsoleModel(director.session());
    }
    doorLoopPlayer.tick();
    if (hack.tick()) {
      director.onHackTimeout();
      hack = null;
      doorLoopPlayer.stop();
      return;
    }
    if (esc) {
      director.onHackTimeout();
      hack = null;
      doorLoopPlayer.stop();
    }
  }

  private void handleHackSubmit() {
    if (hack == null) {
      return;
    }
    HackConsoleModel.HackResult result = hack.submitLine();
    if (result.type() == HackConsoleModel.HackResultType.SUCCESS) {
      director.onHackSuccess();
      hack = null;
      doorLoopPlayer.stop();
      startCutsceneIfNeeded();
    } else if (result.type() == HackConsoleModel.HackResultType.EXIT) {
      director.onHackTimeout();
      hack = null;
      doorLoopPlayer.stop();
    }
  }

  private void renderVnScene(Graphics2D g, int sw, int sh) {
    g.setColor(new Color(12, 8, 6));
    g.fillRect(0, 0, sw, sh);
    VnSceneState scene = activeScene();
    if (scene == null) {
      return;
    }
    DialogBoxRenderer.drawCompactFramedSpeakerText(
        g, sw, sh, scene.speaker(), scene.body(), new Color(218, 165, 32), 1f);
  }

  private void renderVnOverlay(Graphics2D g, int sw, int sh) {
    g.setColor(new Color(0, 0, 0, 140));
    g.fillRect(0, 0, sw, sh);
    VnSceneState scene = activeScene();
    if (scene == null) {
      return;
    }
    DialogBoxRenderer.drawCompactFramedSpeakerText(
        g, sw, sh, scene.speaker(), scene.body(), new Color(218, 165, 32), 1f);
  }

  private void renderVnChoices(Graphics2D g) {
    VnSceneState scene = activeScene();
    if (scene == null || !scene.waitingForChoice()) {
      return;
    }
    var choices = scene.choices();
    g.setFont(GameFonts.get().uiBold(10));
    for (VnChoiceLayout.ChoiceRect rect : choiceRects) {
      g.setColor(new Color(255, 220, 140));
      String label = (rect.index() + 1) + ". " + choices.get(rect.index()).label();
      g.drawString(label, (int) rect.x(), (int) (rect.y() + 12));
    }
  }

  private void renderHackOverlay(Graphics2D g, int sw, int sh) {
    g.setColor(new Color(0, 0, 0, 160));
    g.fillRect(0, 0, sw, sh);
    if (hack == null) {
      return;
    }
    g.setFont(GameFonts.get().uiPlain(9));
    g.setColor(new Color(120, 255, 120));
    int y = 40;
    for (String line : hack.logText().split("\n")) {
      if (line.isEmpty()) {
        continue;
      }
      g.drawString(line, 20, y);
      y += 12;
    }
    g.drawString("> " + hack.inputLine() + "_", 20, sh - 40);
    g.drawString("TIME: " + hack.ticksRemaining(), sw - 80, 20);
    g.drawString("ENTER — выполнить, ESC — выход", 20, sh - 20);
  }
}
