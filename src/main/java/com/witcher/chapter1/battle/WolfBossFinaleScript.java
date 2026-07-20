package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;

import java.util.List;

/** Финальная развилка после глитч-пробуждения (или короткого пути доверия). */
public final class WolfBossFinaleScript {

  private WolfBossFinaleScript() {
  }

  public static VnSceneState prelude() {
    return new VnSceneState(
        "Герцог",
        "Ну вот и всё. Что бы он ни шептал — здесь решаю только я.\n"
            + "Скажи мне то, что я хочу услышать, ведьмак.");
  }

  public static VnSceneState wolfPrompt() {
    return new VnSceneState(
        "Волк",
        "Ты уже знаешь ответ. Просто произнеси его вслух —\n"
            + "пока ещё помнишь собственный голос.");
  }

  public static VnSceneState finalChoice() {
    return new VnSceneState(
        "Геральт",
        "Кому верить?",
        List.of(
            new VnChoice(
                "trust",
                "«Ты прав, Герцог. Это иллюзия — ничего не значащая.»",
                0, 2),
            new VnChoice(
                "suspicion",
                "«Нет. Ты — это я. И я забираю себя обратно.»",
                2, 0)
        ));
  }

  public static VnSceneState badEndingLine() {
    return new VnSceneState(
        "Рассказчик",
        "Слова растворяются вместе с фигурой. Вы моргаете —\n"
            + "и снова стоите перед прилавком. Герцог улыбается,\n"
            + "будто ничего не произошло.\n\n"
            + "«Как я и говорил. Свежий товар для вас, Белый Волк.»\n\n"
            + "Петля сомкнулась. Попытка не засчиталась.");
  }

  public static VnSceneState trueEndingLine(String fragmentCode) {
    return new VnSceneState(
        "Рассказчик",
        "Волк не исчезает — он возвращается домой.\n"
            + "Медальон вспыхивает, и в голове всплывает обрывок шифра: "
            + fragmentCode + ".\n\n"
            + "«Вот. Теперь ты помнишь, каково это — быть собой.»\n\n"
            + "Герцог дёргается, голос ломается: «Этого не должно было…»\n\n"
            + "Впервые за все витки петля не замкнулась заново.\n"
            + "Что-то внутри иллюзии треснуло.");
  }
}
