package main.java.com.witcher.chapter1.shop;

import main.java.com.witcher.chapter1.Chapter1Director;
import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.shop.DukeLines;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEquipSlot;

/**
 * Связка лавки с мета-прогрессом главы 1.
 * {@link main.java.com.witcher.ui.shop.presenter.ShopPresenter} вызывает хуки; экран — слушатели.
 */
public final class Chapter1ShopBridge {

  private final Chapter1Session session;
  private final Chapter1Director director;
  private Runnable onBattleRequested;
  private Runnable onTerminalRequested;
  private Runnable onPurchaseHook;
  private Runnable onEquipHook;
  private boolean battlePending;

  public Chapter1ShopBridge(Chapter1Session session, Chapter1Director director) {
    this.session = session;
    this.director = director;
  }

  public Chapter1Session session() {
    return session;
  }

  public Chapter1Director director() {
    return director;
  }

  public void setOnBattleRequested(Runnable onBattleRequested) {
    this.onBattleRequested = onBattleRequested;
  }

  public void setOnTerminalRequested(Runnable onTerminalRequested) {
    this.onTerminalRequested = onTerminalRequested;
  }

  public void setOnPurchaseHook(Runnable onPurchaseHook) {
    this.onPurchaseHook = onPurchaseHook;
  }

  public void setOnEquipHook(Runnable onEquipHook) {
    this.onEquipHook = onEquipHook;
  }

  public void onPurchase() {
    PrisonTracker.onPurchase(session);
    if (onPurchaseHook != null) {
      onPurchaseHook.run();
    }
  }

  public void onEquip() {
    PrisonTracker.onEquip(session);
    if (onEquipHook != null) {
      onEquipHook.run();
    }
  }

  public String inspectCatalogRow(ShopCatalogEntry entry, ShopCategory category) {
    if (entry == null) {
      return "";
    }
    ShopEquipSlot slot = slotForInspect(entry, category);
    if (slot != null) {
      InspectAction result = InspectAction.inspect(session, slot, entry.name);
      return result.dukeLine();
    }
    return DukeLines.rowInspect(entry.name, entry.price);
  }

  public String inspectOwnedArmour(Armour armour) {
    if (armour == null) {
      return "";
    }
    ShopEquipSlot slot = ShopEquipSlot.forArmour(armour);
    if (slot == null) {
      return DukeLines.rowInspect(armour.getName(), 0);
    }
    InspectAction result = InspectAction.inspect(session, slot, armour.getName());
    return result.dukeLine();
  }

  public boolean tryOpenTerminal() {
    if (!session.terminalAccessGranted()) {
      return false;
    }
    if (onTerminalRequested != null) {
      onTerminalRequested.run();
    }
    return true;
  }

  public boolean consumeBattlePending() {
    if (!battlePending) {
      return false;
    }
    battlePending = false;
    return true;
  }

  private void queueBattleIfNeeded() {
    if (session.prison() >= Chapter1Session.PRISON_COUNTER_THRESHOLD) {
      battlePending = true;
    }
  }

  public void fireBattleIfPendingAndIdle(boolean shopIdle) {
    if (!shopIdle || !consumeBattlePending()) {
      return;
    }
    if (onBattleRequested != null) {
      onBattleRequested.run();
    }
  }

  private static ShopEquipSlot slotForInspect(ShopCatalogEntry entry, ShopCategory category) {
    if (entry.armour != null) {
      ShopEquipSlot fromArmour = ShopEquipSlot.forArmour(entry.armour);
      if (fromArmour != null) {
        return fromArmour;
      }
    }
    return switch (category) {
      case CHEST -> ShopEquipSlot.CHEST;
      case LEGS -> ShopEquipSlot.LEGS;
      case GLOVES -> ShopEquipSlot.GLOVES;
      case BOOTS -> ShopEquipSlot.BOOTS;
      default -> null;
    };
  }
}
