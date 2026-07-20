package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;

import java.util.List;

/** Финальная развилка и исходы первого босса — Волк в лесу, Герцог только после. */
public final class WolfBossFinaleScript {

  private WolfBossFinaleScript() {
  }

  public static VnSceneState finalChoice() {
    return new VnSceneState(
        "Геральт",
        "Сталь уже в руке. Ответить можно только одному.",
        List.of(
            new VnChoice(
                "forget",
                "«Я не помню. И, наверное, не хочу вспоминать.»",
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
        "Тогда покажи.");
  }

  public static VnSceneState badEndingLine() {
    return new VnSceneState(
        "Рассказчик",
        "Лес растворяется вместе с ним. Вы моргаете —\n"
            + "и снова стоите перед прилавком.\n\n"
            + "ГЕРЦОГ: Ну как, я же говорил — пустая формальность.\n"
            + "Взгляните лучше сюда, свежий товар для вас, Белый Волк.\n\n"
            + "Петля сомкнулась. Попытка не засчиталась.");
  }

  public static VnSceneState trueEndingLine(String fragmentCode) {
    return new VnSceneState(
        "Рассказчик",
        "Впервые лес не отпустил вас пустыми руками.\n\n"
            + "Волк не исчезает — он возвращается домой.\n"
            + "В голове всплывает обрывок шифра: " + fragmentCode + ".\n\n"
            + "ГЕРЦОГ: Э—это не должно было… Вы ведь просто взглянули,\n"
            + "я же говорил — пустая формальность —\n\n"
            + "Что-то внутри иллюзии треснуло.\n\n"
            + "…ПРОДОЛЖЕНИЕ СЛЕДУЕТ…");
  }
}
