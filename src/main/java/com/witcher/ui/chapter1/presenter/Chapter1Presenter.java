package main.java.com.witcher.ui.chapter1.presenter;

import main.java.com.witcher.chapter1.Chapter1Director;
import main.java.com.witcher.chapter1.Chapter1Phase;
import main.java.com.witcher.chapter1.battle.BattleCardController;
import main.java.com.witcher.chapter1.battle.BattleOutcome;
import main.java.com.witcher.chapter1.battle.BattleResolver;
import main.java.com.witcher.chapter1.battle.BattleVnController;
import main.java.com.witcher.chapter1.battle.BossEntry;
import main.java.com.witcher.chapter1.loop.LoopSequenceController;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.chapter1.cutscene.CutsceneCatalog;
import main.java.com.witcher.chapter1.ending.EscapeEnding;
import main.java.com.witcher.chapter1.hack.HackConsoleModel;
import main.java.com.witcher.chapter1.shop.Chapter1ShopBridge;
import main.java.com.witcher.chapter1.vn.DukeDialogController;
import main.java.com.witcher.chapter1.vn.EndingVnController;
import main.java.com.witcher.ui.chapter1.view.BossMapLayout;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;
import main.java.com.witcher.ui.chapter1.view.VnChoiceLayout;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.chapter1.swing.Chapter1AssetPrewarm;
import main.java.com.witcher.ui.chapter1.swing.CutscenePlayer;
import main.java.com.witcher.ui.chapter1.swing.EyesBlinkEffect;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.swing.ShopScreen;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Логика и ввод главы 1 (фазы, VN, карта, терминал).
 * Отрисовка — {@link main.java.com.witcher.ui.chapter1.swing.Chapter1SwingView}.
 */
public final class Chapter1Presenter {

  private final Chapter1Director director;
  private final Chapter1ShopBridge shopBridge;
  private final ShopModel shopModel;
  private final ShopScreen shopScreen;

  private final CutscenePlayer cutscenePlayer = new CutscenePlayer();
  private final CutscenePlayer doorLoopPlayer = new CutscenePlayer();
  private final CutscenePlayer loopCutscenePlayer = new CutscenePlayer();

  private final BattleCardController battleCard = new BattleCardController();
  private final LoopSequenceController loopSequence = new LoopSequenceController();
  private final EyesBlinkEffect eyesEffect = new EyesBlinkEffect();

  private BattleVnController battle;
  private EndingVnController ending;
  private DukeDialogController dukeDialog = new DukeDialogController();
  private HackConsoleModel hack;
  private List<VnChoiceLayout.ChoiceRect> choiceRects = List.of();
  private List<BossMapLayout.BossHit> bossHits = List.of();
  private BossEntry hoveredBoss;
  private BossEntry selectedBoss;
  private int hackShakeTick;
  private boolean exitRequested;

  public Chapter1Presenter() {
    this(Chapter1Director.loadOrNew(), ShopModel.createNewSession());
  }

  public Chapter1Presenter(Chapter1Director director, ShopModel shopModel) {
    this.director = director;
    this.shopModel = shopModel;
    this.shopBridge = new Chapter1ShopBridge(director.session(), director);
    this.shopScreen = new ShopScreen(shopModel, shopBridge);
    wireBridgeListeners();
    if (director.session().battleCardIconVisible()) {
      Chapter1AssetPrewarm.warmAllAsync();
    }
  }

  public Chapter1Director director() {
    return director;
  }

  public ShopModel shopModel() {
    return shopModel;
  }

  public ShopScreen shopScreen() {
    return shopScreen;
  }

  public CutscenePlayer cutscenePlayer() {
    return cutscenePlayer;
  }

  public CutscenePlayer doorLoopPlayer() {
    return doorLoopPlayer;
  }

  public CutscenePlayer loopCutscenePlayer() {
    return loopCutscenePlayer;
  }

  public LoopSequenceController loopSequence() {
    return loopSequence;
  }

  public EyesBlinkEffect eyesEffect() {
    return eyesEffect;
  }

  public BattleCardController battleCard() {
    return battleCard;
  }

  public HackConsoleModel hack() {
    return hack;
  }

  public int hackShakeTick() {
    return hackShakeTick;
  }

  public BossEntry hoveredBoss() {
    return hoveredBoss;
  }

  public BossEntry selectedBoss() {
    return selectedBoss;
  }

  public List<VnChoiceLayout.ChoiceRect> choiceRects() {
    return choiceRects;
  }

  public VnSceneState activeScene() {
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

  public boolean isDukeDialogActive() {
    return dukeDialog.isActive();
  }

  public boolean hasActiveChoices() {
    VnSceneState scene = activeScene();
    return scene != null && scene.waitingForChoice();
  }

  public void beginAfterIntro() {
    director.beginAfterIntro();
  }

  public void update(Chapter1Input input) {
    update(input.mouseX(), input.mouseY(), input.clicked(), input.escPressed(), input.wheelNotches());
  }

  public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
    switch (director.phase()) {
      case CUTSCENE -> updateCutscene(clicked);
      case SHOP -> {
        if (dukeDialog.isActive()) {
          updateDukeDialog(mouseX, mouseY, clicked);
        } else {
          shopScreen.update(mouseX, mouseY, clicked, escPressed, wheelNotches);
          maybeStartDukeDialog();
        }
      }
      case CARD_REVEAL -> { }
      case BOSS_MAP -> updateBossMap(mouseX, mouseY, clicked);
      case LOOP_SEQUENCE -> updateLoopSequence();
      case LOOP_HOLD -> { }
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
    shopBridge.setOnTerminalRequested(() -> {
      director.requestHackTerminal();
      onPhaseEntered();
    });
    shopBridge.setOnPurchaseHook(() -> dukeDialog.onPrisonIncreased(director.session()));
    shopBridge.setOnEquipHook(this::tryGrantBattleCard);
    shopBridge.setOnBattleCardUse(this::openBossMapFromInventory);
  }

  private void openBossMapFromInventory() {
    director.enterBossMap();
    bossHits = BossMapLayout.layoutHits(Chapter1ViewConstants.VIRTUAL_W, Chapter1ViewConstants.VIRTUAL_H);
    hoveredBoss = null;
    selectedBoss = null;
    Chapter1AssetPrewarm.warmBossMapDrawables();
    Chapter1AssetPrewarm.warmCutscenesAsync();
  }

  private void tryGrantBattleCard() {
    if (battleCard.tryGrantAfterEquip(director.session(), shopModel)) {
      shopScreen.presenter().beginBattleCardReveal(this::onBattleCardRevealFinished);
    }
  }

  private void onBattleCardRevealFinished() {
    battleCard.finishReveal(director.session());
    Chapter1AssetPrewarm.warmAllAsync();
  }

  private void updateBossMap(int mouseX, int mouseY, boolean clicked) {
    hoveredBoss = BossMapLayout.hitBoss(bossHits, mouseX, mouseY);
    if (!clicked || hoveredBoss == null) {
      return;
    }
    selectedBoss = hoveredBoss;
    director.beginLoopSequence(false);
    onPhaseEntered();
  }

  private void updateLoopSequence() {
    loopSequence.tick();
    loopCutscenePlayer.tick();
    if (loopSequence.showEyes()) {
      eyesEffect.tick();
    }

    if (loopSequence.step() == LoopSequenceController.Step.LOOP_WAKE && eyesEffect.isDone()) {
      director.enterLoopHold();
    }
  }

  private void startLoopCutscene(CutsceneId id) {
    loopCutscenePlayer.start(id, Chapter1ViewConstants.VIRTUAL_W, Chapter1ViewConstants.VIRTUAL_H);
    if (loopCutscenePlayer.isFinished()) {
      if (id == CutsceneId.ILLUSION_WRONG) {
        director.enterLoopHold();
      }
    }
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
    cutscenePlayer.start(id, Chapter1ViewConstants.VIRTUAL_W, Chapter1ViewConstants.VIRTUAL_H);
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
      battle = new BattleVnController(director.session(), loadoutStats());
      battle.advanceIntro();
      refreshChoiceRects();
    } else if (director.phase() == Chapter1Phase.ENDING) {
      ending = new EndingVnController(director.session());
      refreshChoiceRects();
    } else if (director.phase() == Chapter1Phase.HACK) {
      hack = new HackConsoleModel(director.session());
      director.session().registerHackAttempt();
      hackShakeTick = 0;
      String doorPath = CutsceneCatalog.resourcePath(CutsceneId.HACK_DOOR_POUND);
      if (doorPath != null && Chapter1Director.class.getResource(doorPath) != null) {
        doorLoopPlayer.start(CutsceneId.HACK_DOOR_POUND, Chapter1ViewConstants.VIRTUAL_W, Chapter1ViewConstants.VIRTUAL_H);
      } else {
        doorLoopPlayer.stop();
      }
    } else if (director.phase() == Chapter1Phase.LOOP_SEQUENCE) {
      loopSequence.start(director.loopEyesPrelude());
      eyesEffect.reset(EyesBlinkEffect.Mode.AWAKENING);
      startLoopCutscene(CutsceneId.LOOP_WAKE);
    } else if (director.phase() == Chapter1Phase.SHOP) {
      doorLoopPlayer.stop();
      maybeStartDukeDialog();
    } else {
      doorLoopPlayer.stop();
    }
  }

  private void updateBattle(int mouseX, int mouseY, boolean clicked) {
    if (battle == null) {
      battle = new BattleVnController(director.session(), loadoutStats());
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

  private BattleResolver.LoadoutStats loadoutStats() {
    var gear = shopModel.equippedGearStats();
    return new BattleResolver.LoadoutStats(gear.protection(), gear.stamina(), gear.signs());
  }

  private void updateHack(boolean esc) {
    if (hack == null) {
      hack = new HackConsoleModel(director.session());
    }
    hackShakeTick++;
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
      onPhaseEntered();
    } else if (result.type() == HackConsoleModel.HackResultType.EXIT) {
      director.onHackTimeout();
      hack = null;
      doorLoopPlayer.stop();
    }
  }
}
