package com.witcher.ui.chapter1.presenter;

import com.witcher.chapter1.Chapter1Director;
import com.witcher.chapter1.Chapter1Phase;
import com.witcher.chapter1.Chapter1Save;
import com.witcher.chapter1.battle.BossCatalog;
import com.witcher.chapter1.battle.glitch.BossGlitchRevealController;
import com.witcher.chapter1.battle.briefing.BossQuestBriefingController;
import com.witcher.chapter1.battle.encounter.BossEncounterController;
import com.witcher.chapter1.battle.wolf.WolfBossFinaleController;
import com.witcher.ui.chapter1.presenter.wolf.WolfBossPhaseHandler;
import com.witcher.chapter1.ending.DemoEndingController;
import com.witcher.chapter1.ending.WolfEndingType;
import com.witcher.chapter1.battle.BattleCardController;
import com.witcher.chapter1.battle.BattleOutcome;
import com.witcher.chapter1.battle.BattleResolver;
import com.witcher.chapter1.battle.BattleVnController;
import com.witcher.chapter1.battle.BossEntry;
import com.witcher.chapter1.battle.ScreenDissolveController;
import com.witcher.chapter1.battle.briefing.BossQuestBriefingConstants;
import com.witcher.chapter1.battle.glitch.BossGlitchRevealTimeline;
import com.witcher.chapter1.loop.LoopSequenceController;
import com.witcher.chapter1.cutscene.CutsceneId;
import com.witcher.chapter1.cutscene.CutsceneCatalog;
import com.witcher.chapter1.ending.EscapeEnding;
import com.witcher.chapter1.hack.HackConsoleModel;
import com.witcher.chapter1.shop.Chapter1ShopBridge;
import com.witcher.chapter1.vn.DukeDialogController;
import com.witcher.chapter1.vn.EndingVnController;
import com.witcher.chapter1.view.Chapter1Layout;
import com.witcher.ui.chapter1.view.BossMapLayout;
import com.witcher.ui.chapter1.view.VnChoiceLayout;
import com.witcher.chapter1.vn.VnSceneState;
import com.witcher.ui.chapter1.swing.Chapter1AssetPrewarm;
import com.witcher.ui.chapter1.swing.Chapter1SessionHud;
import com.witcher.ui.chapter1.swing.CutscenePlayer;
import com.witcher.ui.chapter1.swing.EyesBlinkEffect;
import com.witcher.ui.chapter1.swing.SwordGlintOverlay;
import com.witcher.ui.shop.ShopModel;
import com.witcher.ui.shop.swing.ShopScreen;
import com.witcher.ui.audio.GameAudio;
import com.witcher.ui.pause.PauseCornerButton;
import com.witcher.ui.shop.view.ShopViewConstants;
import com.witcher.chapter1.battle.SwordCutsceneTiming;
import com.witcher.chapter1.loop.WakeAwakeningTimeline;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Логика и ввод главы 1 (фазы, VN, карта, терминал).
 * Отрисовка — {@link com.witcher.ui.chapter1.swing.Chapter1SwingView}.
 */
public final class Chapter1Presenter implements WolfBossPhaseHandler.Host {

  private final Chapter1Director director;
  private final Chapter1ShopBridge shopBridge;
  private final ShopModel shopModel;
  private final ShopScreen shopScreen;

  private final CutscenePlayer cutscenePlayer = new CutscenePlayer();
  private final CutscenePlayer doorLoopPlayer = new CutscenePlayer();
  private final CutscenePlayer loopCutscenePlayer = new CutscenePlayer();

  private final BattleCardController battleCard = new BattleCardController();
  private final ScreenDissolveController mapDepartDissolve = new ScreenDissolveController();
  private final LoopSequenceController loopSequence = new LoopSequenceController();
  private final EyesBlinkEffect eyesEffect = new EyesBlinkEffect();
  private final BossGlitchRevealController bossGlitchReveal = new BossGlitchRevealController();
  private final SwordGlintOverlay swordGlint = new SwordGlintOverlay();

  private final WolfBossPhaseHandler wolfBoss;

  private BattleVnController battle;
  private EndingVnController ending;
  private DukeDialogController dukeDialog = new DukeDialogController();
  private HackConsoleModel hack;
  private List<VnChoiceLayout.ChoiceRect> choiceRects = List.of();
  private List<BossMapLayout.BossHit> bossHits = List.of();
  private BossEntry hoveredBoss;
  private BossEntry selectedBoss;
  private boolean bossMapBackHovered;
  /** Мигает медальон на карте (idle↔hover), пока не наведут курсор. */
  private boolean bossMapAttention;
  private int bossMapAttentionTick;
  private boolean battleVictory;
  private boolean finaleSwordPlaying;
  private int hackShakeTick;
  private boolean exitRequested;
  /** Не стартовать VN «тюрьмы» посреди fly-in покупки. */
  private boolean prisonDialogPending;
  private DemoEndingController demoEnding;
  private boolean pauseRequested;
  /** Esc ушёл на закрытие UI (инвентарь/хак/титры) — GameWindow не должен форсить паузу. */
  private boolean blocksPauseOnEsc;

  public Chapter1Presenter() {
    this(Chapter1Director.loadOrNew(), ShopModel.createNewSession());
  }

  public Chapter1Presenter(Chapter1Director director, ShopModel shopModel) {
    this.director = director;
    this.shopModel = shopModel;
    this.shopBridge = new Chapter1ShopBridge(director.session(), director);
    this.shopScreen = new ShopScreen(shopModel, shopBridge);
    this.wolfBoss = new WolfBossPhaseHandler(this);
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
    return wolfBoss.encounter();
  }

  public BossQuestBriefingController questBriefing() {
    return wolfBoss.questBriefing();
  }

  public BossGlitchRevealController bossGlitchReveal() {
    return bossGlitchReveal;
  }

  public boolean battleVictory() {
    return battleVictory;
  }

  public SwordGlintOverlay swordGlint() {
    return swordGlint;
  }

  public boolean isFinaleSwordClashPlaying() {
    return finaleSwordPlaying;
  }

  @Override
  public void beginFinaleSwordClash() {
    swordGlint.reset();
    battleVictory = BattleResolver.meetsSwordCutsceneVictory(loadoutStats());
    finaleSwordPlaying = true;
  }

  @Override
  public boolean tickFinaleSwordClash() {
    if (!finaleSwordPlaying) {
      return true;
    }
    swordGlint.update(
        WakeAwakeningTimeline.MS_PER_TICK,
        Chapter1Layout.VIRTUAL_W,
        Chapter1Layout.VIRTUAL_H);
    if (swordGlint.elapsedMs() >= SwordCutsceneTiming.TOTAL_MS) {
      finaleSwordPlaying = false;
      return true;
    }
    return false;
  }

  @Override
  public void skipFinaleSwordClash() {
    finaleSwordPlaying = false;
  }

  public WolfBossFinaleController wolfFinale() {
    return wolfBoss.wolfFinale();
  }

  public WolfEndingType wolfEndingType() {
    return wolfBoss.wolfEndingType();
  }

  public DemoEndingController demoEnding() {
    return demoEnding;
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

  public boolean bossMapAttention() {
    return bossMapAttention;
  }

  public int bossMapAttentionTick() {
    return bossMapAttentionTick;
  }

  public ScreenDissolveController mapDepartDissolve() {
    return mapDepartDissolve;
  }

  public List<VnChoiceLayout.ChoiceRect> choiceRects() {
    return choiceRects;
  }

  public VnSceneState activeScene() {
    VnSceneState wolfScene = wolfBoss.activeChoiceScene(director.phase());
    if (wolfScene != null) {
      return wolfScene;
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

  /** Лавка — A затем B (B крутится); Волк / Весемир / финал — свои темы. */
  private String lastAudioKey = "";

  public void beginAfterIntro() {
    director.beginAfterIntro();
    lastAudioKey = "";
    syncSceneAudio();
  }

  public void update(Chapter1Input input) {
    update(input.mouseX(), input.mouseY(), input.clicked(), input.escPressed(), input.wheelNotches());
  }

  public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
    blocksPauseOnEsc = false;
    PauseCornerButton.Corner pauseCorner = pauseCornerForPhase(director.phase());
    int pauseTopY = pauseTopYForPhase(director.phase());
    if (PauseCornerButton.hit(ShopViewConstants.VIRTUAL_W, ShopViewConstants.VIRTUAL_H,
        mouseX, mouseY, pauseCorner, pauseTopY)) {
      if (clicked) {
        pauseRequested = true;
      }
      // Клик по кнопке паузы не уходит в сцену; наведение — ок.
      if (clicked) {
        return;
      }
    }
    switch (director.phase()) {
      case CUTSCENE -> updateCutscene(clicked);
      case SHOP -> {
        if (tryAdminOpenBossMap(mouseX, mouseY, clicked)) {
          return;
        }
        if (dukeDialog.isActive()) {
          if (escPressed) {
            pauseRequested = true;
            return;
          }
          // Иначе PURCHASE_REVEAL зависает навечно под VN «тюрьмы».
          shopScreen.tickTimedScenes();
          updateDukeDialog(mouseX, mouseY, clicked);
        } else {
          shopScreen.update(mouseX, mouseY, clicked, escPressed, wheelNotches);
          if (escPressed && shopScreen.escConsumedByUi()) {
            blocksPauseOnEsc = true;
          }
          flushPendingDukeDialog();
          maybeStartDukeDialog();
        }
      }
      case BOSS_MAP -> updateBossMap(mouseX, mouseY, clicked);
      case BOSS_QUEST_BRIEFING -> wolfBoss.updateBriefing(mouseX, mouseY, clicked, wheelNotches);
      case LOOP_SEQUENCE -> updateLoopSequence();
      case LOOP_HOLD -> { }
      case BOSS_ENCOUNTER -> wolfBoss.updateEncounter(mouseX, mouseY, clicked, wheelNotches);
      case BOSS_GLITCH_REVEAL -> wolfBoss.updateGlitchReveal();
      case BOSS_FINALE -> wolfBoss.updateFinale(mouseX, mouseY, clicked);
      case WOLF_ENDING -> wolfBoss.updateEnding(clicked);
      case DEMO_ENDING -> updateDemoEnding(clicked, escPressed);
      case BATTLE_RESULT -> updateBattleResult(clicked);
      case VN_BATTLE -> updateBattle(mouseX, mouseY, clicked);
      case VN_DIALOG -> updateDukeDialog(mouseX, mouseY, clicked);
      case HACK -> {
        if (tryAdminOpenBossMap(mouseX, mouseY, clicked)) {
          return;
        }
        if (escPressed) {
          blocksPauseOnEsc = true;
        }
        updateHack(escPressed);
      }
      case ENDING -> updateEnding(mouseX, mouseY, clicked);
    }
    if (escPressed && director.phase() == Chapter1Phase.SHOP) {
      if (shopScreen.isPauseRequested()) {
        pauseRequested = true;
        shopScreen.clearPauseRequest();
      }
      exitRequested = shopScreen.isExitRequested();
    } else if (escPressed && !blocksPauseOnEsc && canOpenPause(director.phase())) {
      pauseRequested = true;
    }
    if (escPressed && director.phase() == Chapter1Phase.DEMO_ENDING) {
      blocksPauseOnEsc = true;
      handleDemoEndingExitKey();
    }
    syncSceneAudio();
  }

  /** Лавка — тема + рынок; карта держит лавку до dissolve; Волк с пробуждения. */
  private void syncSceneAudio() {
    String key = audioKeyForPhase();
    if (key.equals(lastAudioKey)) {
      return;
    }
    lastAudioKey = key;
    switch (key) {
      case "shop" -> GameAudio.playShop();
      case "shop_fade" -> {
        if (!GameAudio.isFadingOut() && GameAudio.currentScene() == GameAudio.Scene.SHOP) {
          GameAudio.beginFadeOut(BossQuestBriefingConstants.DISSOLVE_RAMP_MS);
        }
      }
      case "wolf" -> GameAudio.playWolf();
      case "vesemir" -> GameAudio.playVesemir();
      case "finale" -> GameAudio.playFinale();
      case "silence" -> GameAudio.stopAll();
      case "credits" -> {
        // updateDemoEnding стартует титры.
      }
      default -> {
        GameAudio.Scene cur = GameAudio.currentScene();
        if (cur == GameAudio.Scene.SHOP
            || cur == GameAudio.Scene.INTRO
            || cur == GameAudio.Scene.MENU
            || cur == GameAudio.Scene.WOLF
            || cur == GameAudio.Scene.VESEMIR
            || cur == GameAudio.Scene.FINALE) {
          if (!GameAudio.isFadingOut()) {
            GameAudio.stopAll();
          }
        }
      }
    }
  }

  private String audioKeyForPhase() {
    Chapter1Phase phase = director.phase();
    return switch (phase) {
      case SHOP, BOSS_QUEST_BRIEFING -> "shop";
      case BOSS_MAP -> mapDepartDissolve.active() ? "shop_fade" : "shop";
      case LOOP_SEQUENCE, LOOP_HOLD -> "wolf";
      case BOSS_ENCOUNTER -> {
        var enc = encounter();
        if (enc != null && (enc.isVesemirScene() || enc.flashbackActive())) {
          yield "vesemir";
        }
        if (enc != null && !enc.wolfMusicActive()) {
          yield "silence";
        }
        yield "wolf";
      }
      case BOSS_GLITCH_REVEAL -> {
        if (bossGlitchReveal.stage().ordinal()
            >= BossGlitchRevealTimeline.Stage.SHARD_EMERGE.ordinal()) {
          yield "silence";
        }
        yield "finale";
      }
      case DEMO_ENDING -> "credits";
      default -> "none";
    };
  }

  private static boolean canOpenPause(Chapter1Phase phase) {
    return switch (phase) {
      case SHOP, CUTSCENE, LOOP_SEQUENCE, LOOP_HOLD,
           BOSS_MAP, BOSS_QUEST_BRIEFING, BOSS_ENCOUNTER,
           BOSS_FINALE, BOSS_GLITCH_REVEAL, WOLF_ENDING,
           VN_BATTLE, VN_DIALOG, BATTLE_RESULT, ENDING -> true;
      default -> false;
    };
  }

  /** Лавка / карта — пауза справа; остальное — слева. */
  private static PauseCornerButton.Corner pauseCornerForPhase(Chapter1Phase phase) {
    return switch (phase) {
      case SHOP, BOSS_MAP, BOSS_QUEST_BRIEFING -> PauseCornerButton.Corner.TOP_RIGHT;
      default -> PauseCornerButton.Corner.TOP_LEFT;
    };
  }

  private int pauseTopYForPhase(Chapter1Phase phase) {
    if (phase != Chapter1Phase.SHOP || shopScreen == null) {
      return PauseCornerButton.MARGIN;
    }
    var shop = shopScreen.presenter();
    if (shop.isInventoryOpen() || shop.isEquipmentOpen()) {
      return PauseCornerButton.MARGIN;
    }
    if (shop.isCategoryMode()) {
      return ShopViewConstants.PAUSE_BELOW_WALLET_TOP;
    }
    return PauseCornerButton.MARGIN;
  }

  private void updateDemoEnding(boolean clicked, boolean escPressed) {
    if (demoEnding == null) {
      demoEnding = new DemoEndingController();
      GameAudio.playCreditsTheme();
    }
    if (demoEnding.isDone()) {
      return;
    }
    if (escPressed) {
      handleDemoEndingExitKey();
      return;
    }
    if (clicked && demoEnding.step() == DemoEndingController.Step.CREDITS) {
      // Титры только автоскролл — щелчок не пропускает.
      return;
    }
    DemoEndingController.Step before = demoEnding.step();
    demoEnding.tick();
    if (before != DemoEndingController.Step.THANKS
        && demoEnding.step() == DemoEndingController.Step.THANKS) {
      GameAudio.stopCreditsTheme();
    }
  }

  private void handleDemoEndingExitKey() {
    if (demoEnding == null) {
      return;
    }
    if (demoEnding.acceptsExitKey()) {
      GameAudio.stopCreditsTheme();
      demoEnding.finishToMenu();
      exitRequested = true;
    }
  }

  public void keyPressed(KeyEvent e) {
    if (e == null) {
      return;
    }
    int code = e.getKeyCode();
    if (director.phase() == Chapter1Phase.LOOP_SEQUENCE) {
      if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
        if (eyesEffect.canSkip()) {
          skipLoopAwakening();
        }
      }
      return;
    }
    if (director.phase() == Chapter1Phase.BOSS_GLITCH_REVEAL) {
      if (code == KeyEvent.VK_SPACE && bossGlitchReveal.canSkip()) {
        wolfBoss.skipGlitchReveal();
      }
      return;
    }
    if (director.phase() == Chapter1Phase.CUTSCENE) {
      if ((code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) && cutscenePlayer.canSkip()) {
        skipActiveCutscene();
      }
      return;
    }
    if (director.phase() == Chapter1Phase.BOSS_QUEST_BRIEFING && wolfBoss.questBriefing() != null) {
      var briefing = wolfBoss.questBriefing();
      if (briefing.inTransition()) {
        return;
      }
      if (briefing.waitingForChoice()) {
        int choice = keyToChoiceIndex(code);
        if (choice >= 0) {
          wolfBoss.applyBriefingChoice(choice);
        }
        return;
      }
      if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        briefing.updateDialog(0, 0, false, 0, true);
      }
      return;
    }
    if (director.phase() == Chapter1Phase.BOSS_ENCOUNTER && wolfBoss.encounter() != null) {
      var enc = wolfBoss.encounter();
      if (enc.waitingForChoice()) {
        int choice = keyToChoiceIndex(code);
        if (choice >= 0) {
          wolfBoss.applyEncounterChoice(choice);
        }
        return;
      }
      if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        enc.updateDialog(0, 0, false, 0, true);
      }
      return;
    }
    if (director.phase() == Chapter1Phase.BOSS_FINALE && wolfBoss.wolfFinale() != null) {
      var finale = wolfBoss.wolfFinale();
      if (finale.scene().waitingForChoice()) {
        int choice = keyToChoiceIndex(code);
        if (choice >= 0) {
          wolfBoss.applyFinaleChoice(choice);
        }
      } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        // True ending: CLASH/RESOLVE только по таймеру.
        if (!finale.trueEnding()) {
          wolfBoss.advanceFinale();
        }
      }
      return;
    }
    if (director.phase() == Chapter1Phase.WOLF_ENDING) {
      if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
        wolfBoss.updateEnding(true);
      }
      return;
    }
    if (director.phase() == Chapter1Phase.DEMO_ENDING) {
      if (code == KeyEvent.VK_ESCAPE) {
        handleDemoEndingExitKey();
      }
      return;
    }
    if (director.phase() == Chapter1Phase.SHOP) {
      if (isDukeDialogActive()) {
        if (dukeDialog.scene() != null && dukeDialog.scene().waitingForChoice()) {
          int choice = keyToChoiceIndex(code);
          if (choice >= 0) {
            applyDukeChoice(choice);
          }
        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
          dukeDialog.dismiss();
          choiceRects = List.of();
        }
        return;
      }
      if (code == KeyEvent.VK_BACK_QUOTE || code == KeyEvent.VK_DEAD_GRAVE) {
        shopBridge.tryOpenTerminal();
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

  public boolean isPauseRequested() {
    return pauseRequested;
  }

  public void clearPauseRequest() {
    pauseRequested = false;
  }

  public boolean blocksPauseOnEsc() {
    return blocksPauseOnEsc;
  }

  public void clearExitRequest() {
    exitRequested = false;
    shopScreen.clearExitRequest();
    GameAudio.stopCreditsTheme();
  }

  public boolean isChapterComplete() {
    return director.isChapterComplete();
  }

  private void wireBridgeListeners() {
    shopBridge.setOnTerminalRequested(() -> {
      director.requestHackTerminal();
      onPhaseEntered();
    });
    shopBridge.setOnPurchaseHook(() -> {
      prisonDialogPending = true;
      // Карту выдаём только после брифинга («В БОЙ»), не сразу после покупки.
    });
    shopBridge.setOnEquipHook(() -> {
      // Экипировка больше не выдаёт карту — только «В БОЙ» → брифинг → карта.
    });
    shopBridge.setOnBossMapOpen(this::openBossMap);
    shopBridge.setOnQuestBriefingRequested(this::startQuestBriefingFromShop);
  }

  private void startQuestBriefingFromShop() {
    if (selectedBoss == null) {
      selectedBoss = BossCatalog.byId("duke");
    }
    director.enterBossQuestBriefing();
    onPhaseEntered();
  }

  private void openBossMap() {
    director.enterBossMap();
    bossHits = BossMapLayout.layoutHits(Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
    hoveredBoss = null;
    selectedBoss = null;
    bossMapBackHovered = false;
    bossMapAttention = true;
    bossMapAttentionTick = 0;
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

  private void tryGrantCardWithoutPurchase() {
    if (!battleCard.tryGrantAsBrowseConsolation(director.session())) {
      return;
    }
    var session = director.session();
    session.clearBattleCardRevealPending();
    shopScreen.presenter().beginBattleCardReveal(this::onBattleCardRevealAfterBack, true);
  }

  private void tryShowBattleCardReveal() {
    var session = director.session();
    if (session.battleCardRevealPending()
        || (session.battleCardGranted() && !session.battleCardIconVisible())) {
      session.clearBattleCardRevealPending();
      shopScreen.presenter().beginBattleCardReveal(this::onBattleCardRevealAfterBack, false);
    }
  }

  private void onBattleCardRevealAfterBack() {
    battleCard.finishReveal(director.session());
    Chapter1Save.save(director.session());
    Chapter1AssetPrewarm.warmAllAsync();
  }

  private void updateBossMap(int mouseX, int mouseY, boolean clicked) {
    if (mapDepartDissolve.active()) {
      mapDepartDissolve.tick();
      if (mapDepartDissolve.isComplete()) {
        mapDepartDissolve.clear();
        director.beginLoopSequence(false);
        onPhaseEntered();
      }
      return;
    }
    bossMapAttentionTick++;
    var back = BossMapLayout.backButton(Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
    bossMapBackHovered = back.contains(mouseX, mouseY);
    if (clicked && bossMapBackHovered) {
      hoveredBoss = null;
      selectedBoss = null;
      bossMapAttention = false;
      director.enterShop();
      onPhaseEntered();
      return;
    }
    hoveredBoss = bossMapBackHovered ? null : BossMapLayout.hitBoss(bossHits, mouseX, mouseY);
    if (bossMapAttention && hoveredBoss != null) {
      bossMapAttention = false;
    }
    if (!clicked || hoveredBoss == null) {
      return;
    }
    selectedBoss = hoveredBoss;
    bossMapAttention = false;
    // Dissolve / затемнение — при клике по боссу на карте (не в конце брифинга).
    mapDepartDissolve.begin();
  }

  /** Конец брифинга: назад в лавку + выдача карты. */
  @Override
  public void onQuestBriefingFinishedToShop() {
    director.enterShop();
    var session = director.session();
    if (!session.battleCardGranted() && !session.battleCardIconVisible()) {
      session.grantBattleCard();
    }
    session.clearBattleCardRevealPending();
    shopScreen.presenter().beginBattleCardReveal(this::onBattleCardRevealAfterBack, false);
    onPhaseEntered();
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
    if (director.phase() != Chapter1Phase.LOOP_SEQUENCE || !eyesEffect.canSkip()) {
      return;
    }
    eyesEffect.skip();
    loopCutscenePlayer.stop();
    director.enterBossEncounter();
    onPhaseEntered();
  }

  private void skipActiveCutscene() {
    if (director.phase() != Chapter1Phase.CUTSCENE || !cutscenePlayer.canSkip()) {
      return;
    }
    cutscenePlayer.stop();
    director.onCutsceneFinished();
    onPhaseEntered();
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
    if (clicked && cutscenePlayer.canSkip()) {
      skipActiveCutscene();
    }
  }

  private void onPhaseEntered() {
    wolfBoss.onPhaseEntered(director.phase());
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
    } else if (director.phase() == Chapter1Phase.LOOP_SEQUENCE) {
      loopSequence.start(director.loopEyesPrelude());
      eyesEffect.reset(EyesBlinkEffect.Mode.AWAKENING);
      startLoopCutscene(CutsceneId.LOOP_WAKE);
    } else if (director.phase() == Chapter1Phase.SHOP) {
      doorLoopPlayer.stop();
      maybeStartDukeDialog();
    } else if (director.phase() == Chapter1Phase.DEMO_ENDING) {
      demoEnding = new DemoEndingController();
      GameAudio.playCreditsTheme();
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
    if (dukeDialog.scene() != null && dukeDialog.scene().waitingForChoice()) {
      int index = VnChoiceLayout.hitIndex(choiceRects, mouseX, mouseY);
      if (index >= 0) {
        applyDukeChoice(index);
      }
      return;
    }
    dukeDialog.dismiss();
    choiceRects = List.of();
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

  @Override
  public void setChoiceRects(List<VnChoiceLayout.ChoiceRect> rects) {
    choiceRects = rects;
  }

  @Override
  public void notifyPhaseEntered() {
    onPhaseEntered();
  }

  @Override
  public void setBattleVictory(boolean value) {
    battleVictory = value;
  }

  @Override
  public void refreshChoiceRects() {
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
