package main.java.com.witcher.chapter1.vn;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.vn.VnChoiceEffects;

/**
 * Одноразовые VN-реплики герцога в лавке.
 * Экран вызывает {@link #pollPending(Chapter1Session)} при входе в SHOP.
 */
public final class DukeDialogController {

  private boolean loopReturnShown;
  private boolean prisonPressureShown;
  private VnSceneState scene;
  private boolean active;

  public VnSceneState scene() {
    return scene;
  }

  public boolean isActive() {
    return active;
  }

  public VnSceneState pollPending(Chapter1Session session) {
    if (active || session == null) {
      return scene;
    }
    if (!loopReturnShown && DukeShopDialog.shouldShowLoopReturn(session)) {
      loopReturnShown = true;
      begin(DukeShopDialog.loopReturnGreeting(session));
      return scene;
    }
    if (!prisonPressureShown && DukeShopDialog.shouldShowPrisonPressure(session)) {
      prisonPressureShown = true;
      begin(DukeShopDialog.prisonPressure(session));
      return scene;
    }
    return null;
  }

  public void onPrisonIncreased(Chapter1Session session) {
    if (active || prisonPressureShown || session == null) {
      return;
    }
    if (DukeShopDialog.shouldShowPrisonPressure(session)) {
      prisonPressureShown = true;
      begin(DukeShopDialog.prisonPressure(session));
    }
  }

  public void choose(int index, Chapter1Session session) {
    if (!active || scene == null || !scene.waitingForChoice() || session == null) {
      return;
    }
    scene.select(index);
    VnChoice choice = scene.selectedChoice();
    if (choice != null) {
      VnChoiceEffects.apply(session, choice);
    }
    active = false;
    scene = null;
  }

  public void dismiss() {
    if (active && scene != null && !scene.waitingForChoice()) {
      active = false;
      scene = null;
    }
  }

  private void begin(VnSceneState next) {
    scene = next;
    active = next != null;
  }
}
