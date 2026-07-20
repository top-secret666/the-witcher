package main.java.com.witcher.chapter1;

import main.java.com.witcher.chapter1.cutscene.CutsceneCatalog;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.chapter1.loop.LoopRules;

/**
 * State machine главы 1: какая фаза активна и что делать после событий.
 * Отрисовка — в {@code ui.chapter1.swing}; здесь только переходы.
 *
 * <p>Канонический cinematic-путь:
 * SHOP → BOSS_MAP → BOSS_QUEST_BRIEFING → LOOP_SEQUENCE → BOSS_ENCOUNTER → ...
 * Выдача карты — shop state {@code BATTLE_CARD_REVEAL}, не фаза директора.
 */
public final class Chapter1Director {

  private final Chapter1Session session;
  private Chapter1Phase phase = Chapter1Phase.CUTSCENE;
  private CutsceneId pendingCutscene = CutsceneId.LOOP_WAKE;
  private boolean cutsceneFinished;
  private boolean chapterComplete;
  private boolean loopEyesPrelude;

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

  /** Старт главы после интро: сразу лавка (loop_wake — только после выбора босса). */
  public void beginAfterIntro() {
    phase = Chapter1Phase.SHOP;
    pendingCutscene = null;
    cutsceneFinished = false;
    chapterComplete = false;
  }

  public void tickCutscene() {
    // Пока нет плеера — сразу завершаем; CutscenePlayer вызовет onCutsceneFinished().
  }

  public void onCutsceneFinished() {
    cutsceneFinished = true;
    switch (pendingCutscene) {
      case LOOP_WAKE, ILLUSION_WRONG -> {
        if (phase != Chapter1Phase.LOOP_HOLD) {
          enterShop();
        }
      }
      case BATTLE_INTRO -> enterBattleVn();
      case BATTLE_DEFEAT -> {
        LoopRules.onBattleDefeat(session);
        enterShop();
      }
      case HACK_UNLOCK -> enterEndingDialog();
      case ESCAPE_TRUE -> chapterComplete = true;
      case ESCAPE_FALSE -> {
        LoopRules.onFalseEscape(session);
        enterShop();
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
    if (!CutsceneCatalog.isAvailable(pendingCutscene)) {
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
    beginLoopSequence(true);
  }

  /** Канон: карта боссов после клика по карте в сумке. */
  public void enterBossMap() {
    phase = Chapter1Phase.BOSS_MAP;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  /** Брифинг с ложным контрактом в лавке после выбора босса на карте. */
  public void enterBossQuestBriefing() {
    phase = Chapter1Phase.BOSS_QUEST_BRIEFING;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  /** Канон: появление злодея после loop_wake. */
  public void enterBossEncounter() {
    phase = Chapter1Phase.BOSS_ENCOUNTER;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  /** Глитч-пробуждение Волка — только на ветке «вслушаться». */
  public void enterBossGlitchReveal() {
    phase = Chapter1Phase.BOSS_GLITCH_REVEAL;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  /** Финальная развилка: выбор №2. */
  public void enterBossFinale() {
    phase = Chapter1Phase.BOSS_FINALE;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  /** Экран итога первого босса (петля или осколок). */
  public void enterWolfEnding() {
    phase = Chapter1Phase.WOLF_ENDING;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  /** Канон: победа/поражение → клик возвращает в лавку. */
  public void enterBattleResult() {
    phase = Chapter1Phase.BATTLE_RESULT;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  /** Канон: loop_wake после выбора босса на карте. */
  public void beginLoopSequence(boolean eyesPrelude) {
    phase = Chapter1Phase.LOOP_SEQUENCE;
    loopEyesPrelude = eyesPrelude;
    pendingCutscene = null;
    cutsceneFinished = false;
  }

  public boolean loopEyesPrelude() {
    return loopEyesPrelude;
  }

  public void enterLoopHold() {
    phase = Chapter1Phase.LOOP_HOLD;
    pendingCutscene = null;
    cutsceneFinished = false;
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
