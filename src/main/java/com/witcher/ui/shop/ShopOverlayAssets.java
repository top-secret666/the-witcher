package main.java.com.witcher.ui.shop;

import java.awt.image.BufferedImage;

/** Текстуры оверлеев лавки (инвентарь / экипировка) — без привязки к загрузчику. */
public interface ShopOverlayAssets {

    BufferedImage geraltPortraitShop();

    BufferedImage equipSlotPlaceholder(int iconIndex);

    BufferedImage equipmentFilterIcon(EquipmentFilter filter);

    BufferedImage statVialEmpty();

    BufferedImage statVialOverlay();

    BufferedImage statVialEndCap();
}
