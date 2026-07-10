package main.java.com.witcher.chapter1;

import main.java.com.witcher.chapter1.cutscene.CutsceneCatalog;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.chapter1.loop.LoopRules;

/**
 * State machine главы 1: какая фаза активна и что делать после событий.
 * Отрисовка — в {@code ui.chapter1.swing}; здесь только переходы.
 */
public final class Chapter1Director {

  private final Chapter1Session session;
  private Chapter1Phase phase = Chapter1Phase.CUTSCENE;
  private CutsceneId pendingCutscene = CutsceneId.LOOP_WAKE;
  private boolean cutsceneFinished;
  private boolean chapterComplete;

  public Chapter1Director(Chapter1Session session) {
    this.session = session != null ? session : Chapter1Session.newGame();
  }

  public static Chapter1Director loadOrNew() {
    return new Chapter1Director(Chapter1Save.loadOrNew());
  }

  public Chapter1Session session() {
    return session;
  }

  public Chapter1Phase phase() {
    return phase;
  }

  public CutsceneId pendingCutscene() {
    return pendingCutscene;
  }

  public boolean isCutsceneFinished() {
    return cutsceneFinished;
  }

  public boolean isChapterComplete() {
    return chapterComplete;
  }

  /** Старт главы после интро: катсцена пробуждения → лавка. */
  public void beginAfterIntro() {
    phase = Chapter1Phase.CUTSCENE;
    pendingCutscene = CutsceneId.LOOP_WAKE;
    cutsceneFinished = false;
    chapterComplete = false;
  }

  public void tickCutscene() {
    // Пока нет плеера — сразу завершаем; CutscenePlayer вызовет onCutsceneFinished().
  }

  public void onCutsceneFinished() {
    cutsceneFinished = true;
    switch (pendingCutscene) {
      case LOOP_WAKE, ILLUSION_WRONG -> enterShop();
      case BATTLE_INTRO -> enterBattleVn();
      case BATTLE_DEFEAT -> {
        LoopRules.onBattleDefeat(session);
        queueCutscene(CutsceneId.LOOP_WAKE);
      }
      case HACK_UNLOCK -> enterEndingDialog();
      case ESCAPE_TRUE -> chapterComplete = true;
      case ESCAPE_FALSE -> {
        LoopRules.onFalseEscape(session);
        queueCutscene(CutsceneId.LOOP_WAKE);
      }
      default -> enterShop();
    }
  }

  /** Пропуск катсцены, если GIF ещё нет на диске. */
  public void skipCutsceneIfMissing() {
    if (pendingCutscene == null) {
      onCutsceneFinished();
      return;
    }
    String path = CutsceneCatalog.resourcePath(pendingCutscene);
    if (path == null || Chapter1Director.class.getResource(path) == null) {
      onCutsceneFinished();
    }
  }

  public void enterShop() {
    phase = Chapter1Phase.SHOP;
    pendingCutscene = null;
    cutsceneFinished = false;
    LoopRules.persist(session);
  }

  public void enterDukeDialog() {
    phase = Chapter1Phase.VN_DIALOG;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  public void exitDukeDialog() {
    enterShop();
  }

  /** Герцог зовёт в бой (триггер из лавки — позже). */
  public void requestBattle() {
    queueCutscene(CutsceneId.BATTLE_INTRO);
  }

  public void enterBattleVn() {
    phase = Chapter1Phase.VN_BATTLE;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  public void onBattleDefeat() {
    queueCutscene(CutsceneId.BATTLE_DEFEAT);
  }

  public void onBattleStunReturnToShop() {
    enterShop();
  }

  public void requestHackTerminal() {
    if (!session.terminalAccessGranted()) {
      return;
    }
    phase = Chapter1Phase.HACK;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  public void onHackSuccess() {
    session.markCipherSolved();
    LoopRules.persist(session);
    queueCutscene(CutsceneId.HACK_UNLOCK);
  }

  public void onHackTimeout() {
    enterShop();
  }

  public void enterEndingDialog() {
    phase = Chapter1Phase.ENDING;
    pendingCutscene = null;
  }

  public void resolveEscapeEnding() {
    if (session.suspicionDominates()) {
      queueCutscene(CutsceneId.ESCAPE_TRUE);
    } else {
      queueCutscene(CutsceneId.ESCAPE_FALSE);
    }
  }

  private void queueCutscene(CutsceneId id) {
    phase = Chapter1Phase.CUTSCENE;
    pendingCutscene = id;
    cutsceneFinished = false;
  }
}
