package main.java.com.witcher.ui.chapter1.presenter;

import main.java.com.witcher.chapter1.Chapter1Director;
import main.java.com.witcher.chapter1.Chapter1Phase;
import main.java.com.witcher.chapter1.Chapter1Save;
import main.java.com.witcher.chapter1.battle.BossCatalog;
import main.java.com.witcher.chapter1.battle.glitch.BossGlitchRevealController;
import main.java.com.witcher.chapter1.battle.BossEncounterController;
import main.java.com.witcher.chapter1.battle.BossQuestBriefingController;
import main.java.com.witcher.chapter1.battle.WolfBossFinaleController;
import main.java.com.witcher.chapter1.ending.WolfEndingType;
import main.java.com.witcher.chapter1.loop.LoopRules;
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
import main.java.com.witcher.chapter1.view.Chapter1Layout;
import main.java.com.witcher.ui.chapter1.view.BossMapLayout;
import main.java.com.witcher.ui.chapter1.view.VnChoiceLayout;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.chapter1.swing.Chapter1AssetPrewarm;
import main.java.com.witcher.ui.chapter1.swing.Chapter1SessionHud;
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
  private final BossGlitchRevealController bossGlitchReveal = new BossGlitchRevealController();

  private BattleVnController battle;
  private BossEncounterController encounter;
  private BossQuestBriefingController questBriefing;
  private WolfBossFinaleController wolfFinale;
  private EndingVnController ending;
  private DukeDialogController dukeDialog = new DukeDialogController();
  private HackConsoleModel hack;
  private List<VnChoiceLayout.ChoiceRect> choiceRects = List.of();
  private List<BossMapLayout.BossHit> bossHits = List.of();
  private BossEntry hoveredBoss;
  private BossEntry selectedBoss;
  private boolean bossMapBackHovered;
  private boolean battleVictory;
  private WolfEndingType wolfEndingType = WolfEndingType.BAD_LOOP;
  private int hackShakeTick;
  private boolean exitRequested;
  /** Не стартовать VN «тюрьмы» посреди fly-in покупки. */
  private boolean prisonDialogPending;

  public Chapter1Presenter() {
    this(Chapter1Director.loadOrNew(), ShopModel.createNewSession());
  }

  public Chapter1Presenter(Chapter1Director director, ShopModel shopModel) {
    this.director = director;
    this.shopModel = shopModel;
    this.shopBridge = new Chapter1ShopBridge(director.session(), director);
    this.shopScreen = new ShopScreen(shopModel, shopBridge);
    wireBridgeListeners();
    // ShopModel.createNewSession() всегда пустой — не тащим иконку карты из старого save.
    director.session().resetBattleCardUntilEquipReveal();
    Chapter1Save.save(director.session());
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

  public BossEncounterController encounter() {
    return encounter;
  }

  public BossQuestBriefingController questBriefing() {
    return questBriefing;
  }

  public BossGlitchRevealController bossGlitchReveal() {
    return bossGlitchReveal;
  }

  public boolean battleVictory() {
    return battleVictory;
  }

  public WolfBossFinaleController wolfFinale() {
    return wolfFinale;
  }

  public WolfEndingType wolfEndingType() {
    return wolfEndingType;
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

  public boolean bossMapBackHovered() {
    return bossMapBackHovered;
  }

  public List<VnChoiceLayout.ChoiceRect> choiceRects() {
    return choiceRects;
  }

  public VnSceneState activeScene() {
    if (director.phase() == Chapter1Phase.BOSS_ENCOUNTER
        && encounter != null && encounter.waitingForChoice()) {
      return encounter.choiceScene();
    }
    if (director.phase() == Chapter1Phase.BOSS_FINALE && wolfFinale != null) {
      return wolfFinale.scene();
    }
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
        if (tryAdminOpenBossMap(mouseX, mouseY, clicked)) {
          return;
        }
        if (dukeDialog.isActive()) {
          // Иначе PURCHASE_REVEAL зависает навечно под VN «тюрьмы».
          shopScreen.tickTimedScenes();
          updateDukeDialog(mouseX, mouseY, clicked);
        } else {
          shopScreen.update(mouseX, mouseY, clicked, escPressed, wheelNotches);
          flushPendingDukeDialog();
          maybeStartDukeDialog();
        }
      }
      case BOSS_MAP -> updateBossMap(mouseX, mouseY, clicked);
      case BOSS_QUEST_BRIEFING -> updateBossQuestBriefing(mouseX, mouseY, clicked, wheelNotches);
      case LOOP_SEQUENCE -> updateLoopSequence();
      case LOOP_HOLD -> { }
      case BOSS_ENCOUNTER -> updateBossEncounter(mouseX, mouseY, clicked, wheelNotches);
      case BOSS_GLITCH_REVEAL -> updateBossGlitchReveal();
      case BOSS_FINALE -> updateBossFinale(mouseX, mouseY, clicked);
      case WOLF_ENDING -> updateWolfEnding(clicked);
      case BATTLE_RESULT -> updateBattleResult(clicked);
      case VN_BATTLE -> updateBattle(mouseX, mouseY, clicked);
      case VN_DIALOG -> updateDukeDialog(mouseX, mouseY, clicked);
      case HACK -> {
        if (tryAdminOpenBossMap(mouseX, mouseY, clicked)) {
          return;
        }
        updateHack(escPressed);
      }
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
    if (director.phase() == Chapter1Phase.LOOP_SEQUENCE) {
      if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
        skipLoopAwakening();
      }
      return;
    }
    if (director.phase() == Chapter1Phase.BOSS_QUEST_BRIEFING && questBriefing != null) {
      if (questBriefing.inTransition()) {
        return;
      }
      if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        questBriefing.updateDialog(0, 0, false, 0, true);
      }
      return;
    }
    if (director.phase() == Chapter1Phase.BOSS_ENCOUNTER && encounter != null) {
      if (encounter.waitingForChoice()) {
        int choice = keyToChoiceIndex(code);
        if (choice >= 0) {
          applyEncounterChoice(choice);
        }
        return;
      }
      if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        encounter.updateDialog(0, 0, false, 0, true);
      }
      return;
    }
    if (director.phase() == Chapter1Phase.BOSS_FINALE && wolfFinale != null) {
      if (wolfFinale.scene().waitingForChoice()) {
        int choice = keyToChoiceIndex(code);
        if (choice >= 0) {
          applyWolfFinaleChoice(choice);
        }
      } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        advanceWolfFinale();
      }
      return;
    }
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
    shopBridge.setOnPurchaseHook(() -> prisonDialogPending = true);
    shopBridge.setOnEquipHook(this::tryGrantBattleCard);
    shopBridge.setOnBossMapOpen(this::openBossMap);
    shopBridge.setOnEquipmentBack(this::onEquipmentBackToLavka);
  }

  private void openBossMap() {
    director.enterBossMap();
    bossHits = BossMapLayout.layoutHits(Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
    hoveredBoss = null;
    selectedBoss = null;
    bossMapBackHovered = false;
    Chapter1AssetPrewarm.warmBossMapDrawables();
    Chapter1AssetPrewarm.warmCutscenesAsync();
  }

  /** Админ-кнопка hack_hidden_hint → сразу открытая карта боссов. */
  private boolean tryAdminOpenBossMap(int mouseX, int mouseY, boolean clicked) {
    if (!clicked) {
      return false;
    }
    if (!Chapter1SessionHud.hitAdminMapButton(mouseX, mouseY, Chapter1Layout.VIRTUAL_W)) {
      return false;
    }
    openBossMap();
    return true;
  }

  private void tryGrantBattleCard() {
    battleCard.tryGrantAfterEquip(director.session(), shopModel);
  }

  private void onEquipmentBackToLavka() {
    var session = director.session();
    if (session.battleCardRevealPending()
        || (session.battleCardGranted() && !session.battleCardIconVisible())) {
      session.clearBattleCardRevealPending();
      shopScreen.presenter().beginBattleCardReveal(this::onBattleCardRevealAfterBack);
    }
  }

  private void onBattleCardRevealAfterBack() {
    battleCard.finishReveal(director.session());
    Chapter1Save.save(director.session());
    Chapter1AssetPrewarm.warmAllAsync();
  }

  private void updateBossMap(int mouseX, int mouseY, boolean clicked) {
    var back = BossMapLayout.backButton(Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
    bossMapBackHovered = back.contains(mouseX, mouseY);
    if (clicked && bossMapBackHovered) {
      hoveredBoss = null;
      selectedBoss = null;
      director.enterShop();
      onPhaseEntered();
      return;
    }
    hoveredBoss = bossMapBackHovered ? null : BossMapLayout.hitBoss(bossHits, mouseX, mouseY);
    if (!clicked || hoveredBoss == null) {
      return;
    }
    selectedBoss = hoveredBoss;
    director.enterBossQuestBriefing();
    onPhaseEntered();
  }

  private void updateBossQuestBriefing(int mouseX, int mouseY, boolean clicked, int wheelNotches) {
    if (questBriefing == null) {
      questBriefing = new BossQuestBriefingController(selectedBoss);
    }
    questBriefing.setLayoutSize(Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
    questBriefing.tick();

    if (questBriefing.inTransition()) {
      if (questBriefing.isComplete()) {
        questBriefing = null;
        director.beginLoopSequence(false);
        onPhaseEntered();
      }
      return;
    }

    questBriefing.updateDialog(mouseX, mouseY, clicked, wheelNotches, false);
  }

  private void updateLoopSequence() {
    loopSequence.tick();
    loopCutscenePlayer.tick();
    if (loopSequence.showEyes()) {
      eyesEffect.tick();
    }

    if (loopSequence.step() == LoopSequenceController.Step.LOOP_WAKE && eyesEffect.isDone()) {
      director.enterBossEncounter();
      onPhaseEntered();
    }
  }

  private void skipLoopAwakening() {
    if (director.phase() != Chapter1Phase.LOOP_SEQUENCE) {
      return;
    }
    eyesEffect.skip();
    loopCutscenePlayer.stop();
    director.enterBossEncounter();
    onPhaseEntered();
  }

  private void updateBossEncounter(int mouseX, int mouseY, boolean clicked, int wheelNotches) {
    if (encounter == null) {
      encounter = new BossEncounterController(selectedBoss);
    }
    encounter.setLayoutSize(Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
    encounter.tick();

    if (encounter.waitingForChoice()) {
      refreshChoiceRects();
      if (clicked) {
        int index = VnChoiceLayout.hitIndex(choiceRects, mouseX, mouseY);
        if (index >= 0) {
          applyEncounterChoice(index);
        }
      }
      return;
    }

    encounter.updateDialog(mouseX, mouseY, clicked, wheelNotches, false);

    if (encounter.waitingForChoice()) {
      refreshChoiceRects();
      return;
    }

    if (encounter.isDialogComplete()) {
      battleVictory = encounter.choseListenPath();
      if (encounter.choseListenPath()) {
        director.enterBossGlitchReveal();
      } else {
        director.enterBossFinale();
      }
      onPhaseEntered();
    }
  }

  private void applyEncounterChoice(int index) {
    if (encounter == null) {
      return;
    }
    encounter.choose(index, director.session());
    choiceRects = List.of();
  }

  private void updateBossGlitchReveal() {
    bossGlitchReveal.tick();
    if (bossGlitchReveal.isComplete()) {
      director.enterBossFinale();
      onPhaseEntered();
    }
  }

  private void updateBossFinale(int mouseX, int mouseY, boolean clicked) {
    if (wolfFinale == null) {
      wolfFinale = new WolfBossFinaleController(director.session());
      refreshChoiceRects();
      return;
    }
    if (!clicked) {
      return;
    }
    if (wolfFinale.scene().waitingForChoice()) {
      int index = VnChoiceLayout.hitIndex(choiceRects, mouseX, mouseY);
      if (index >= 0) {
        applyWolfFinaleChoice(index);
      }
      return;
    }
    advanceWolfFinale();
  }

  private void applyWolfFinaleChoice(int index) {
    if (wolfFinale == null) {
      return;
    }
    wolfFinale.choose(index);
    refreshChoiceRects();
  }

  private void advanceWolfFinale() {
    if (wolfFinale == null) {
      return;
    }
    if (wolfFinale.isDone()) {
      finishWolfFinale();
      return;
    }
    wolfFinale.advance();
    refreshChoiceRects();
    if (wolfFinale.isDone()) {
      finishWolfFinale();
    }
  }

  private void finishWolfFinale() {
    if (wolfFinale == null) {
      return;
    }
    wolfEndingType = wolfFinale.trueEnding()
        ? WolfEndingType.TRUE_SHARD
        : WolfEndingType.BAD_LOOP;
    if (wolfFinale.trueEnding()) {
      LoopRules.onWolfTrueShard(director.session());
    } else {
      LoopRules.onWolfBadLoop(director.session());
    }
    wolfFinale = null;
    choiceRects = List.of();
    director.enterWolfEnding();
    onPhaseEntered();
  }

  private void updateWolfEnding(boolean clicked) {
    if (clicked) {
      director.enterShop();
      onPhaseEntered();
    }
  }

  private void updateBattleResult(boolean clicked) {
    if (clicked) {
      director.enterShop();
      onPhaseEntered();
    }
  }

  private void startLoopCutscene(CutsceneId id) {
    loopCutscenePlayer.start(id, Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
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
    cutscenePlayer.start(id, Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
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
        doorLoopPlayer.start(CutsceneId.HACK_DOOR_POUND, Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
      } else {
        doorLoopPlayer.stop();
      }
    } else if (director.phase() == Chapter1Phase.BOSS_QUEST_BRIEFING) {
      questBriefing = new BossQuestBriefingController(selectedBoss);
    } else if (director.phase() == Chapter1Phase.LOOP_SEQUENCE) {
      loopSequence.start(director.loopEyesPrelude());
      eyesEffect.reset(EyesBlinkEffect.Mode.AWAKENING);
      startLoopCutscene(CutsceneId.LOOP_WAKE);
    } else if (director.phase() == Chapter1Phase.BOSS_ENCOUNTER) {
      encounter = new BossEncounterController(selectedBoss);
    } else if (director.phase() == Chapter1Phase.BOSS_GLITCH_REVEAL) {
      bossGlitchReveal.reset();
      battleVictory = true;
    } else if (director.phase() == Chapter1Phase.BOSS_FINALE) {
      wolfFinale = new WolfBossFinaleController(director.session());
      refreshChoiceRects();
    } else if (director.phase() == Chapter1Phase.WOLF_ENDING) {
      encounter = null;
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
    if (dukeDialog.isActive() || !shopScreen.isChapterEventIdle()) {
      return;
    }
    if (dukeDialog.pollPending(director.session()) != null) {
      refreshChoiceRects();
    }
  }

  private void flushPendingDukeDialog() {
    if (!prisonDialogPending || dukeDialog.isActive() || !shopScreen.isChapterEventIdle()) {
      return;
    }
    prisonDialogPending = false;
    dukeDialog.onPrisonIncreased(director.session());
    if (dukeDialog.isActive()) {
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
