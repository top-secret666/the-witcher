package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnChoice;

import java.util.List;

/** Сценарий VN-боя: реплики и варианты по раундам. */
public final class BattleScript {

  public record RoundScript(String dukeLine, List<VnChoice> choices) {
  }

  private static final List<VnChoice> STANDARD_CHOICES = List.of(
      new VnChoice("attack", "Ударить сталью", PlayerAction.ATTACK, 0, 0),
      new VnChoice("dodge", "Отступить и выждать", PlayerAction.DODGE, 0, 0),
      new VnChoice("sign", "Применить знак", PlayerAction.SIGN, 0, 0),
      new VnChoice("potion", "Выпить зелье", PlayerAction.POTION, 0, 0)
  );

  private BattleScript() {
  }

  public static String introLine(BattleCounter counter) {
    return switch (counter) {
      case TRUE_DAMAGE -> "Крепкая броня… Я найду, куда ударить без неё.";
      case STAMINA_DRAIN -> "Выносливость — ваша слабость сегодня, Белый Волк.";
      case STANDARD -> "Покажите, чему вас учили в Каэр Морхене.";
    };
  }

  public static RoundScript round(int index) {
    String line = switch (index) {
      case 0 -> "Первый обмен. Не медлите.";
      case 1 -> "Интересно… Продолжим.";
      default -> "Последний шанс в этом витке.";
    };
    return new RoundScript(line, STANDARD_CHOICES);
  }

  public static String narrative(String key) {
    return switch (key) {
      case "round_fail_true_damage" -> "Удар проходит сквозь сталь, словно её нет.";
      case "round_fail_stamina" -> "Силы уходят быстрее, чем зелье успевает помочь.";
      case "round_fail_standard" -> "Герцог отвечает точнее. Вы уступаете в обмене.";
      case "round_ok_attack" -> "Сталь находит брешь. Вы держите инициативу.";
      case "round_ok_dodge" -> "Вы уходите из-под удара. Дыхание ровное.";
      case "round_ok_sign" -> "Знак вспыхивает — герцог на миг отступает.";
      case "round_ok_potion" -> "Горький отвар. Кровь бьётся спокойнее.";
      default -> "Раунд завершён.";
    };
  }

  public static String outcomeLine(BattleOutcome outcome) {
    return switch (outcome) {
      case PLAYER_WIN -> "Герцог рассыпается смехом… но петля не рвётся.";
      case PLAYER_STUN -> "Он шатается и исчезает в дыму. На миг — тишина.";
      case PLAYER_DEFEAT -> "Тьма. Снова начало.";
      case IMPOSSIBLE_WIN -> "Слишком много их снаряжения на вас. Он знал заранее.";
      case ONGOING -> "";
    };
  }
}
