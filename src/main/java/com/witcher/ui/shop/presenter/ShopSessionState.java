package main.java.com.witcher.ui.shop.presenter;

import main.java.com.witcher.ui.shop.EquipmentFilter;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.shop.EquipSlot;
import main.java.com.witcher.ui.shop.view.ShopShowcaseItem;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Мутабельное UI-состояние лавки — общее для presenter и view. */
public final class ShopSessionState {

    public final List<ShopShowcaseItem> showcaseItems = new ArrayList<>();
    public final List<ShopCatalogEntry> catalogEntries = new ArrayList<>();
    public final List<float[]> ashParticles = new ArrayList<>();
    public final Random rng = new Random();

    public ShopScreenState state = ShopScreenState.REVEAL;
    public String currentDialog;
    public int selectedIndex = -1;
    public int hoveredIndex = -1;
    public int hoveredRowIndex = -1;
    public int selectedRowIndex = -1;
    public int tick;
    public int revealTicks;
    public int categoryTicks;
    public boolean categoryClosing;
    public final Rectangle categoryFromRect = new Rectangle();
    public final Rectangle categoryBuyBounds = new Rectangle();
    public int walletRevealTicks;
    public boolean walletRevealFromCategory;
    public int battleCardRevealTicks;
    public int purchaseRevealTicks;
    public BufferedImage purchaseRevealIcon;
    public ShopCategory purchaseRevealCategory;
    public int purchaseRevealKeepRow = -1;
    public boolean inventoryOpen;
    public boolean equipmentOpen;
    public boolean equipmentBackHovered;
    public boolean inventoryPouchFocused = true;
    public int inventoryFocusedIndex;
    public int inventoryHoveredIndex = -1;
    public final java.util.List<Rectangle> inventorySlotBounds = new java.util.ArrayList<>();
    public boolean inventoryBagHovered;
    public boolean inventoryPouchIconHovered;
    public final Rectangle inventoryBagBounds = new Rectangle();
    public final Rectangle inventoryPanelBounds = new Rectangle();
    public final Rectangle inventoryPouchIconBounds = new Rectangle();
    public final Rectangle inventoryEquipButtonBounds = new Rectangle();
    public final Rectangle inventoryCloseBounds = new Rectangle();
    public boolean inventoryCloseHovered;
    public final Rectangle equipmentPanelBounds = new Rectangle();
    public final Rectangle equipmentBackButtonBounds = new Rectangle();
    public final Rectangle[] equipmentSlotBounds = new Rectangle[EquipSlot.values().length];
    public final List<Rectangle> equipmentRowBounds = new ArrayList<>();
    public int equipmentHoveredRow = -1;
    public int equipmentHoveredSlot = -1;
    public EquipmentFilter equipmentFilter = EquipmentFilter.ALL;
    public int equipmentHoveredFilter = -1;
    public final Rectangle[] equipmentFilterBounds = new Rectangle[EquipmentFilter.values().length];
    public boolean categoryBuyHovered;
    public final Rectangle categoryBackBounds = new Rectangle();
    public boolean categoryBackHovered;
    public int catalogScrollOffset;
    public boolean exitRequested;
}
