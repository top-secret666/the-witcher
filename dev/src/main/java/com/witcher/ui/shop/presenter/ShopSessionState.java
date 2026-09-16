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
    /** Спикер нижней реплики (Герцог / Геральт). */
    public String dialogSpeaker = "Герцог";
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
    /** Кэш обрезки иконки purchase-reveal — без повторного scan пикселей каждый кадр. */
    public Rectangle purchaseRevealCrop;
    public ShopCategory purchaseRevealCategory;
    public int purchaseRevealKeepRow = -1;
    /** Первая покупка: показать «щелчок — пропустить». */
    public boolean purchaseRevealShowSkipHint;
    /** Подсказку пропуска уже показали (только один раз за сессию). */
    public boolean purchaseSkipHintUsed;
    /** «Вам помочь подобрать броню?» — после нескольких заходов в категории без покупок. */
    public boolean armorHelpActive;
    /** Сколько раз открыли категорию, пока инвентарь пуст (сбрасывается после оффера / покупки). */
    public int armorHelpEmptyCategoryCount;
    public final java.util.List<main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.ChoiceRect> armorHelpChoiceBounds =
        new java.util.ArrayList<>();
    public int armorHelpHovered = -1;
    /** После примерки: «приобрести всё / купите сами» с Да/Нет. */
    public boolean outfitConfirmActive;
    public boolean inventoryOpen;
    public boolean equipmentOpen;
    public boolean equipmentBackHovered;
    public boolean inventoryPouchFocused = true;
    public int inventoryFocusedIndex;
    public int inventoryHoveredIndex = -1;
    public final java.util.List<Rectangle> inventorySlotBounds = new java.util.ArrayList<>();
    /** Скролл верхней сетки (особые предметы) в рядах. */
    public int inventorySpecialScroll;
    /** Скролл нижней сетки (броня) в рядах. */
    public int inventoryArmourScroll;
    public final Rectangle inventorySpecialGridBounds = new Rectangle();
    public final Rectangle inventoryArmourGridBounds = new Rectangle();
    public boolean inventoryBagHovered;
    public boolean inventoryPouchIconHovered;
    public final Rectangle inventoryBagBounds = new Rectangle();
    /** Мигает сумка (closed↔hover): после покупки — «зайди в инвентарь». */
    public boolean inventoryAttention;
    /** После кошелька — подсветить «Купить» / заставить взять товар. */
    public boolean buyAttention;
    /** После покупки — при открытии сумки мигает «Экипировка». */
    public boolean guideToEquipment;
    /** После брифинга карта уже выдана — не вести снова в экипировку. */
    public boolean skipEquipmentGuide;
    /** Мигает «В БОЙ» в инвентаре — только после того, как надели броню/оружие. */
    public boolean toBattleAttention;
    public final Rectangle toBattleButtonBounds = new Rectangle();
    public boolean toBattleHovered;
    /** Центр инвентаря: подтверждение ухода в бой. */
    public boolean battleConfirmActive;
    /** Сколько раз уже проигрывали анимацию покупки (подсказка пропуска со 2-й). */
    public int purchaseRevealCount;
    /** Карта ждёт очереди после кнопки «Экипировка». */
    public boolean battleCardAttentionPending;
    /** Мигает кнопка «Надеть» (после покупки брони/оружия). */
    public boolean equipmentAttention;
    /** Мигает «Выпить» после покупки зелья. */
    public boolean potionDrinkAttention;
    /** Мигает карта (иконка) — после выхода из экипировки. */
    public boolean battleCardAttention;
    /** Мигает только «Открыть» — после фокуса на иконке карты. */
    public boolean battleCardOpenAttention;
    public final Rectangle inventoryPanelBounds = new Rectangle();
    public final Rectangle inventoryPouchIconBounds = new Rectangle();
    public final Rectangle inventoryEquipButtonBounds = new Rectangle();
    public final Rectangle inventoryGearButtonBounds = new Rectangle();
    public final Rectangle inventoryCloseBounds = new Rectangle();
    public boolean inventoryCloseHovered;
    public final Rectangle equipmentPanelBounds = new Rectangle();
    public final Rectangle equipmentBackButtonBounds = new Rectangle();
    public final Rectangle[] equipmentSlotBounds = new Rectangle[EquipSlot.values().length];
    public final Rectangle equipmentWeaponSlotBounds = new Rectangle();
    public boolean equipmentWeaponHovered;
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
    /** Esc на корне лавки → пауза. */
    public boolean pauseRequested;

}
