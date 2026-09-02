package main.java.com.witcher.chapter1.vn;

import java.util.List;

/** Состояние одной VN-сцены (бой, финал, диалог). */
public final class VnSceneState {

  private final String speaker;
  private final String body;
  private final List<VnChoice> choices;
  private boolean waitingForChoice;
  private int selectedIndex = -1;

  public VnSceneState(String speaker, String body) {
    this(speaker, body, List.of());
  }

  public VnSceneState(String speaker, String body, List<VnChoice> choices) {
    this.speaker = speaker != null ? speaker : "";
    this.body = body != null ? body : "";
    this.choices = choices != null ? List.copyOf(choices) : List.of();
    this.waitingForChoice = !this.choices.isEmpty();
  }

  public String speaker() {
    return speaker;
  }

  public String body() {
    return body;
  }

  public List<VnChoice> choices() {
    return choices;
  }

  public boolean waitingForChoice() {
    return waitingForChoice;
  }

  public int selectedIndex() {
    return selectedIndex;
  }

  public VnChoice selectedChoice() {
    if (selectedIndex < 0 || selectedIndex >= choices.size()) {
      return null;
    }
    return choices.get(selectedIndex);
  }

  public void select(int index) {
    if (index < 0 || index >= choices.size()) {
      return;
    }
    selectedIndex = index;
    waitingForChoice = false;
  }
}
