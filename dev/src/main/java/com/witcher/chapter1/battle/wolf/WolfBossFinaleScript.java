package com.witcher.chapter1.battle.wolf;

import com.witcher.chapter1.vn.VnChoice;
import com.witcher.chapter1.vn.VnSceneState;

import java.util.List;

/** Финальная развилка и исходы первого босса. */
public final class WolfBossFinaleScript {

  private WolfBossFinaleScript() {
  }

  public static VnSceneState finalChoice() {
    return new VnSceneState(
        "Волк",
        "Момент истины.",
        List.of(
            new VnChoice(
                "forget",
                "«Не помню. И не хочу копаться в этой грязи.»",
                0, 2),
            new VnChoice(
                "remember",
                "«Помню. Каждую трещину на рукояти.»",
                2, 0)
        ));
  }

  public static VnSceneState wolfClashLine() {
    return new VnSceneState(
        "Волк",
        "Тогда доставай меч.\nИ постарайся не делать вид, что тебе всё равно.\n"
            + "У тебя это всегда паршиво выходило.");
  }

  public static VnSceneState badEndingLine() {
    return new VnSceneState(
        "Рассказчик",
        "Удар проходит точно.\nНо вместо плоти сталь рассекает морозный воздух.\n"
            + "Волк не падает. Он просто тускнеет,\n"
            + "словно нарисованную фигуру смывает серым дождём.\n\n"
            + "ВОЛК: Ну вот... Опять чисто сработано. Без грязи.\n"
            + "Увидимся на следующем круге, Белый Волк.\n"
            + "Купи себе ещё пару красивых перчаток.\n\n"
            + "Лес схлопывается в точку.\n"
            + "Моргание — и Геральт снова стоит у полированного дубового прилавка.\n\n"
            + "ГЕРЦОГ: С возвращением, мой друг!\n"
            + "Ну вот, а вы беспокоились. Пустая тень, пережиток прошлого.\n"
            + "Главное — вы целы.\n"
            + "Взгляните-ка лучше сюда: только что поступила превосходная кольчуга...\n\n"
            + "[ СИСТЕМА: Петля замкнулась. Память подавлена. ]");
  }

  public static VnSceneState trueEndingLine(String fragmentCode) {
    // Монтаж/эпилог убран по docs/dialogues.md — сразу к глитчу/титрам.
    return new VnSceneState("", "");
  }
}
