package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.shop.ShopModel;

/**
 * Контроллер VN-боя: раунды, выборы, итог.
 */
public final class BattleVnController {

  private final Chapter1Session session;
  private final BattleResolver.LoadoutStats stats;
  private final BattleCounter counter;
  private final BattleTier tier;

  private int round;
  private int roundsWon;
  private VnSceneState scene;
  private BattleOutcome outcome = BattleOutcome.ONGOING;
  private boolean finished;

  public BattleVnController(Chapter1Session session, ShopModel shop) {
    this.session = session;
    if (shop != null) {
      var gear = shop.equippedGearStats();
      this.stats = new BattleResolver.LoadoutStats(gear.protection(), gear.stamina(), gear.signs());
    } else {
      this.stats = new BattleResolver.LoadoutStats(3, 3, 2);
    }
    this.counter = BattleResolver.detectCounter(stats);
    this.tier = BattleTier.forLoop(session != null ? session.loop() : 1);
    this.scene = new VnSceneState("Герцог", BattleScript.introLine(counter));
  }

  public VnSceneState scene() {
    return scene;
  }

  public BattleOutcome outcome() {
    return outcome;
  }

  public boolean isFinished() {
    return finished;
  }

  public void advanceIntro() {
    if (round > 0) {
      return;
    }
    beginRound(0);
  }

  public void choose(int index) {
    if (finished || scene == null || !scene.waitingForChoice()) {
      return;
    }
    scene.select(index);
    VnChoice choice = scene.selectedChoice();
    if (choice == null || choice.battleAction() == null) {
      return;
    }
    BattleResolver.RoundResult result = BattleResolver.resolveRound(
        choice.battleAction(), stats, counter, tier, session.loop());
    if (result.playerWinsRound()) {
      roundsWon++;
    }
    scene = new VnSceneState("Рассказчик", BattleScript.narrative(result.narrativeKey()));
    round++;
    if (round >= BattleResolver.ROUNDS_PER_FIGHT) {
      finishFight();
    } else {
      // Следующий раунд — после клика игрока вызовут advanceAfterNarration()
    }
  }

  public void advanceAfterNarration() {
    if (finished || scene.waitingForChoice()) {
      return;
    }
    if (round < BattleResolver.ROUNDS_PER_FIGHT) {
      beginRound(round);
    }
  }

  private void beginRound(int roundIndex) {
    BattleScript.RoundScript script = BattleScript.round(roundIndex);
    scene = new VnSceneState("Герцог", script.dukeLine(), script.choices());
  }

  private void finishFight() {
    outcome = BattleResolver.resolveFight(
        roundsWon, tier, session.prisonBlocksVictory());
    finished = true;
    scene = new VnSceneState("Рассказчик", BattleScript.outcomeLine(outcome));
  }
}
