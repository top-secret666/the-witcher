package main.java.com.witcher.chapter1.vn;

import main.java.com.witcher.chapter1.battle.PlayerAction;

/** Вариант ответа в VN-сцене. */
public record VnChoice(
    String id,
    String label,
    PlayerAction battleAction,
    int suspicionDelta,
    int trustDelta
) {
  public VnChoice(String id, String label, int suspicionDelta, int trustDelta) {
    this(id, label, null, suspicionDelta, trustDelta);
  }
}
