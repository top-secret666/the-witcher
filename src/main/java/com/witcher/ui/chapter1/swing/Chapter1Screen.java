package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Director;
import main.java.com.witcher.chapter1.Chapter1Phase;
import main.java.com.witcher.chapter1.battle.BattleOutcome;
import main.java.com.witcher.chapter1.battle.BattleVnController;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.chapter1.ending.EscapeEnding;
import main.java.com.witcher.chapter1.ending.EscapeResolver;
import main.java.com.witcher.chapter1.hack.HackConsoleModel;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.swing.ShopScreen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Фасад главы 1 для {@link main.java.com.witcher.ui.graphics.GameWindow}.
 * Логика — {@link Chapter1Director}; лавка — {@link ShopScreen}.
 */
public final class Chapter1Screen {

  private final Chapter1Director director;
  private final ShopModel shopModel;
  private final ShopScreen shopScreen;
  private final CutscenePlayer cutscenePlayer = new CutscenePlayer();

  private BattleVnController battle;
  private HackConsoleModel hack;
  private boolean exitRequested;

  public Chapter1Screen() {
    this(Chapter1Director.loadOrNew(), ShopModel.createNewSession());
  }

  public Chapter1Screen(Chapter1Director director, ShopModel shopModel) {
    this.director = director;
    this.shopModel = shopModel;
    this.shopScreen = new ShopScreen(shopModel);
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
      case SHOP -> shopScreen.update(mouseX, mouseY, clicked, escPressed, wheelNotches);
      case VN_BATTLE -> updateBattle(clicked);
      case HACK -> updateHack(escPressed);
      case ENDING -> updateEnding(clicked);
      case VN_DIALOG -> { }
    }
    if (escPressed && director.phase() == Chapter1Phase.SHOP) {
      exitRequested = shopScreen.isExitRequested();
    }
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
      case SHOP, HACK -> shopScreen.render(screen, mouseX, mouseY);
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
          renderHackOverlay(overlay, sw, sh);
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
    if (director.phase() == Chapter1Phase.VN_BATTLE
        || director.phase() == Chapter1Phase.ENDING) {
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
    } else if (director.phase() == Chapter1Phase.HACK) {
      hack = new HackConsoleModel(director.session());
      director.session().registerHackAttempt();
    }
  }

  private void updateBattle(boolean clicked) {
    if (battle == null) {
      battle = new BattleVnController(director.session(), shopModel);
      battle.advanceIntro();
      return;
    }
    VnSceneState scene = battle.scene();
    if (!clicked) {
      return;
    }
    if (scene.waitingForChoice()) {
      battle.choose(0);
    }
    if (battle.isFinished()) {
      onBattleFinished();
      return;
    }
    if (!battle.scene().waitingForChoice()) {
      battle.advanceAfterNarration();
    }
  }

  private void onBattleFinished() {
    BattleOutcome outcome = battle.outcome();
    switch (outcome) {
      case PLAYER_DEFEAT, IMPOSSIBLE_WIN -> director.onBattleDefeat();
      default -> director.onBattleStunReturnToShop();
    }
    battle = null;
    startCutsceneIfNeeded();
  }

  private void updateHack(boolean esc) {
    if (hack == null) {
      hack = new HackConsoleModel(director.session());
    }
    if (hack.tick() || esc) {
      director.onHackTimeout();
      hack = null;
    }
  }

  private void updateEnding(boolean clicked) {
    if (!clicked) {
      return;
    }
    EscapeEnding ending = EscapeResolver.resolve(director.session());
    if (ending == EscapeEnding.LOCKED) {
      director.enterShop();
      return;
    }
    director.resolveEscapeEnding();
    startCutsceneIfNeeded();
  }

  private void renderVnScene(Graphics2D g, int sw, int sh) {
    g.setColor(new Color(12, 8, 6));
    g.fillRect(0, 0, sw, sh);
    if (battle == null) {
      return;
    }
    VnSceneState scene = battle.scene();
    DialogBoxRenderer.drawCompactFramedSpeakerText(
        g, sw, sh, scene.speaker(), scene.body(), new Color(218, 165, 32), 1f);
  }

  private void renderVnChoices(Graphics2D g) {
    if (battle == null || !battle.scene().waitingForChoice()) {
      return;
    }
    var choices = battle.scene().choices();
    g.setFont(GameFonts.get().uiBold(10));
    g.setColor(new Color(255, 220, 140));
    int y = g.getClipBounds().height - 24 - choices.size() * 16;
    for (int i = 0; i < choices.size(); i++) {
      g.drawString((i + 1) + ". " + choices.get(i).label(), 12, y + i * 16);
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
  }
}
