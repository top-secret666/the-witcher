package main.java.com.witcher.chapter1.shop;

import main.java.com.witcher.chapter1.Chapter1Director;
import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.shop.DukeLines;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.shop.EquipSlot;

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
  private Runnable onBossMapOpen;
  private Runnable onEquipmentBack;
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

  public void setOnBossMapOpen(Runnable onBossMapOpen) {
    this.onBossMapOpen = onBossMapOpen;
  }

  public void setOnEquipmentBack(Runnable onEquipmentBack) {
    this.onEquipmentBack = onEquipmentBack;
  }

  public boolean battleCardInInventory() {
    return session != null && session.battleCardIconVisible();
  }

  public boolean battleMapPending() {
    return session != null && session.battleMapPending();
  }

  public void markBattleMapPending() {
    if (session != null) {
      session.markBattleMapPending();
    }
  }

  /** «Назад» из экрана экипировки — лавка; карта только отсюда. */
  public void onEquipmentBack() {
    if (onEquipmentBack != null) {
      onEquipmentBack.run();
    }
  }

  public void openBossMapIfPending() {
    if (session == null || !session.battleMapPending()) {
      return;
    }
    session.clearBattleMapPending();
    if (onBossMapOpen != null) {
      onBossMapOpen.run();
    }
  }

  public void useBattleCard() {
    if (!battleCardInInventory()) {
      return;
    }
    if (onBossMapOpen != null) {
      onBossMapOpen.run();
    }
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
    EquipSlot slot = slotForInspect(entry, category);
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
    EquipSlot slot = EquipSlot.forArmour(armour);
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

  private static EquipSlot slotForInspect(ShopCatalogEntry entry, ShopCategory category) {
    if (entry.armour != null) {
      EquipSlot fromArmour = EquipSlot.forArmour(entry.armour);
      if (fromArmour != null) {
        return fromArmour;
      }
    }
    return switch (category) {
      case CHEST -> EquipSlot.CHEST;
      case LEGS -> EquipSlot.LEGS;
      case GLOVES -> EquipSlot.GLOVES;
      case BOOTS -> EquipSlot.BOOTS;
      default -> null;
    };
  }
}
