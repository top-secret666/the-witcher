package main.java.com.witcher.ui.shop;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Реплики герцога в лавке — по docs/dialogues.md.
 */
public final class DukeLines {

    private DukeLines() {
    }

    public static final String IDLE = "Всё это - инвестиции, Геральт.";

    /** Приветствие убрано — после интро сразу idle / витрина. */
    public static final String WELCOME = IDLE;

    public static String forCategory(ShopCategory category) {
        return switch (category) {
            case CHEST -> pick(
                "С превеликим удовольствием!",
                "Я расширил ассортимент услуг! Прошу, взгляните!",
                "Понятно. Что-нибудь ещё?",
                "Ха-ха-ха!");
            case LEGS -> pick(
                "Хорошо.",
                "Ага!",
                "Хо-хо-хо",
                "И я непременно рекомендую это купить.");
            case GLOVES -> pick(
                "Эти руки куда проворнее, чем может показаться.",
                "Я бы не отказался от такой покупки.",
                "Хе-хе-хе",
                "О! Превосходный выбор.");
            case BOOTS -> pick(
                "Дайте мне знать, если захотите купить это.",
                "Разумеется.",
                "Ах да, у меня есть для вас кое-что!",
                "Кхе-кхе-кхе");
            case POTION -> pick(
                "Пейте аккуратно. Я продаю средство, не последствия.",
                "Я надеюсь, вы не страдаете аллергией...",
                "Не переживайте насчет побочных эффектов — я продаю исключительно качественный товар...");
            case SETS -> pick(
                "Для меня будет величайшая радость — увидеть вас в этих легендарных доспехах.",
                "Благодаря тому, что вы неизменно пользуетесь моими услугами, я смог расширить их перечень.",
                "Нет-нет, я был бы более чем счастлив приобрести такую вещь.");
            case WEAPON -> pick(
                "С этим вы будете косить врагов целыми толпами!",
                "В Империи Герцога найдётся всё необходимое для вашей решающей битвы!",
                "Хи-хи-хи");
        };
    }

    public static String rowInspect(String itemName, int price) {
        return pick(
            "Любопытный выбор!",
            "Великолепно! Вы хоть знаете, сколько это стоит?!",
            "Ах, " + itemName + ". У вас хороший вкус.",
            "Запастись всем необходимым важнее всего. Абсолютно всего, друг мой."
        );
    }

    public static String purchaseOk(String itemName, int price) {
        return pick(
            "Благодарю за покупку.",
            "Поздравляю. Теперь вы можете не бояться что-нибудь упустить.",
            "Уже не терпится испытать это в деле?",
            "Я заметил, что вы на это поглядывали!",
            "Да, я понимаю, почему вас это заинтересовало.",
            "Выгодная сделка, если позволите мне самому так сказать."
        );
    }

    public static String purchaseFailMoney() {
        return pick(
            "Тяжёлые времена, Геральт?",
            "Ах, будь у вас только достаточно крон...",
            "Ах... прошу прощения."
        );
    }

    public static String purchaseFailSold() {
        return pick(
            "Увы. Этого больше нет.",
            "Опоздали, господин Геральт. Ничего личного.",
            "На этой полке пусто."
        );
    }

    public static String purchaseFailGeneric() {
        return pick(
            "Простите?",
            "Боюсь, я вас не понимаю. Попробуйте ещё раз.",
            "Что-что? Не расслышал..."
        );
    }

    public static String walletReveal() {
        return "Копейка к копейке — вот и полон кошелёк, как говорится. "
            + "Вам попросили передать гонорар за прошлый заказ.";
    }

    public static String walletRevealAfter() {
        return "";
    }

    /** Напоминание, если ушли с витрины без покупки. */
    public static String forceBuyReminder() {
        return armorHelpOffer();
    }

    /** Если так и ничего не купил. */
    public static String armorHelpOffer() {
        return "Вам помочь подобрать броню?";
    }

    /** Отказ от помощи герцога. */
    public static String geraltBuyMyself() {
        return "Я сам всё «куплю».";
    }

    /** После авто-подбора комплекта. */
    public static String dukeHowDoYouLike() {
        return dukeOutfitPurchaseOrSelf();
    }

    /** В экипировке после подбора герцога. */
    public static String dukeOutfitPurchaseOrSelf() {
        return "Желаете это всё приобрести — или всё-таки сами что-то купите?";
    }

    /** Подтвердили покупку подобранного. */
    public static String dukeOutfitKept() {
        return "Отлично. Тогда ваше.";
    }

    /** Отказались от подобранного — покупают сами. */
    public static String dukeOutfitShopYourself() {
        return "Как угодно.";
    }

    /** Подтверждение «В бой» из инвентаря. */
    public static String battleConfirm() {
        return "Вы уверены, что хотите в бой? Или ещё что-нибудь купите?";
    }

    /** «В бой» нажали, но ничего не надето. */
    public static String equipBeforeBattle() {
        return "Не спешите, друг мой. Для начала «наденьте» свою новую броню.";
    }

    /** После покупки — вести в инвентарь. */
    public static String goToInventory() {
        return "Отлично. Теперь вы можете осмотреть свои покупки в «инвентаре».";
    }

    public static String battleCardReveal() {
        return pick(
            "Я окажу вам небольшую личную услугу. Я отметил местонахождение врагов на вашей карте.",
            "Ваша карта. Ассортимент квестов. Выберите сами, куда ступить первым."
        );
    }

    /** Карта без покупки — legacy; в каноне = force buy. */
    public static String battleCardRevealNoPurchase() {
        return forceBuyReminder();
    }

    public static String battleCardRevealAfter() {
        return "";
    }

    public static String potionDrunk(String name) {
        return "Эликсир сделал своё дело...";
    }

    private static String pick(String... lines) {
        return lines[ThreadLocalRandom.current().nextInt(lines.length)];
    }
}
