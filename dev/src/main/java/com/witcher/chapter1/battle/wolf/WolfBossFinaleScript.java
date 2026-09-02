package main.java.com.witcher.chapter1.battle.wolf;

import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;

import java.util.List;

/** Финальная развилка и исходы первого босса — Волк в лесу, Герцог только после. */
public final class WolfBossFinaleScript {

  private WolfBossFinaleScript() {
  }

  public static VnSceneState finalChoice() {
    return new VnSceneState(
        "Волк",
        "Последний вопрос.\nТы ещё помнишь, каково это —\n"
            + "держать в руках что-то настоящее?\n"
            + "Не купленное. Не выданное. Не подсунутое.\n"
            + "Своё.",
        List.of(
            new VnChoice(
                "forget",
                "«Не помню. И не хочу.»",
                0, 2),
            new VnChoice(
                "remember",
                "«Помню. Каждую чёртову деталь.»",
                2, 0)
        ));
  }

  public static VnSceneState wolfClashLine() {
    return new VnSceneState(
        "Волк",
        "Тогда покажи.\nИ постарайся не делать вид, что тебе всё равно.\n"
            + "У тебя плохо выходит.");
  }

  public static VnSceneState badEndingLine() {
    return new VnSceneState(
        "Рассказчик",
        "Удар проходит.\nТуман на мгновение становится плотнее,\n"
            + "а потом Волк начинает тускнеть.\n"
            + "Не погибать. Не кричать.\n"
            + "Просто исчезать, будто его снова убрали туда,\n"
            + "где ему приказали молчать.\n\n"
            + "ВОЛК: Ну вот. Опять аккуратно.\n\n"
            + "Лес растворяется. Вы моргаете —\n"
            + "и снова стоите перед прилавком.\n\n"
            + "ГЕРЦОГ: Вот и вы. Видите? Пустая формальность.\n"
            + "Неприятная, возможно, но такие вещи лучше не растягивать.\n"
            + "Главное — вы вернулись.\n"
            + "А раз уж вернулись, позвольте показать вам свежий товар.\n\n"
            + "Петля сомкнулась. Попытка не засчиталась.");
  }

  public static VnSceneState trueEndingLine(String fragmentCode) {
    return new VnSceneState(
        "Рассказчик",
        "Клинки встречаются.\n"
            + "Звук не похож на удар металла.\n"
            + "Скорее — на трещину во льду, по которому слишком долго ходили.\n\n"
            + "ВОЛК: Вот. Слышишь?\n"
            + "ГЕРАЛЬТ: Медальон.\n"
            + "ВОЛК: Нет. Ты.\n\n"
            + "Волк не исчезает.\n"
            + "Фигура распадается не в пепел, а в свет.\n"
            + "Холод медальона уходит под кожу.\n"
            + "В памяти всплывает обрывок: "
            + fragmentCode + ".");
  }
}
