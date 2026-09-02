package main.java.com.witcher.chapter1.battle;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Расчёт VN-боя: формула из дизайн-дока, без отдельного RPG-UI.
 */
public final class BattleResolver {

  public static final int ROUNDS_PER_FIGHT = 3;
  private static final int BASE_DUKE_ATTACK = 12;

  public record LoadoutStats(int defense, int stamina, int signs) {
  }

  public record RoundResult(
      int playerScore,
      int dukeScore,
      boolean playerWinsRound,
      String narrativeKey
  ) {
  }

  private BattleResolver() {
  }

  public static BattleCounter detectCounter(LoadoutStats stats) {
    if (stats == null) {
      return BattleCounter.STANDARD;
    }
    int def = stats.defense();
    int sta = stats.stamina();
    if (def > sta + 2) {
      return BattleCounter.TRUE_DAMAGE;
    }
    if (sta > def + 2) {
      return BattleCounter.STAMINA_DRAIN;
    }
    return BattleCounter.STANDARD;
  }

  public static RoundResult resolveRound(
      PlayerAction action,
      LoadoutStats stats,
      BattleCounter counter,
      BattleTier tier,
      int loop
  ) {
    int baseStat = pickStat(action, stats);
    int roll = ThreadLocalRandom.current().nextInt(1, 7);
    int playerScore = Math.round(baseStat * action.modifier) + roll;

    float dukeMult = tier.dukeAttackMultiplier * (1f + (loop - 1) * 0.08f);
    int dukeRoll = ThreadLocalRandom.current().nextInt(1, 7);
    int dukeScore = Math.round(BASE_DUKE_ATTACK * dukeMult) + dukeRoll;

    playerScore = applyCounterToPlayer(action, counter, playerScore);
    boolean playerWins = playerScore >= dukeScore;
    String key = narrativeKey(action, counter, playerWins);
    return new RoundResult(playerScore, dukeScore, playerWins, key);
  }

  /**
   * Порог «уверенного» клинка для cinematic-намёка на экране итога:
   * сумма defense + stamina + signs ≥ {@link #SWORD_VICTORY_STAT_SUM}.
   */
  public static final int SWORD_VICTORY_STAT_SUM = 6;

  public static boolean meetsSwordCutsceneVictory(LoadoutStats stats) {
    if (stats == null) {
      return false;
    }
    return stats.defense() + stats.stamina() + stats.signs() >= SWORD_VICTORY_STAT_SUM;
  }

  public static int loadoutStatSum(LoadoutStats stats) {
    if (stats == null) {
      return 0;
    }
    return stats.defense() + stats.stamina() + stats.signs();
  }

  public static BattleOutcome resolveFight(
      int roundsWon,
      BattleTier tier,
      boolean prisonBlocksVictory
  ) {
    if (prisonBlocksVictory) {
      return BattleOutcome.IMPOSSIBLE_WIN;
    }
    if (roundsWon >= 2 && tier.canBeDefeated) {
      return BattleOutcome.PLAYER_WIN;
    }
    if (roundsWon >= 1 && !tier.canBeDefeated) {
      return BattleOutcome.PLAYER_STUN;
    }
    return BattleOutcome.PLAYER_DEFEAT;
  }

  private static int pickStat(PlayerAction action, LoadoutStats stats) {
    if (action.usesDefense) {
      return stats.defense();
    }
    if (action.usesStamina) {
      return stats.stamina();
    }
    return Math.max(stats.signs(), 1);
  }

  private static int applyCounterToPlayer(PlayerAction action, BattleCounter counter, int score) {
    return switch (counter) {
      case TRUE_DAMAGE -> action.usesDefense ? score / 2 : score;
      case STAMINA_DRAIN -> action == PlayerAction.POTION ? score / 3 : score;
      case STANDARD -> score;
    };
  }

  private static String narrativeKey(PlayerAction action, BattleCounter counter, boolean playerWins) {
    if (!playerWins) {
      return switch (counter) {
        case TRUE_DAMAGE -> "round_fail_true_damage";
        case STAMINA_DRAIN -> "round_fail_stamina";
        case STANDARD -> "round_fail_standard";
      };
    }
    return "round_ok_" + action.name().toLowerCase();
  }
}
