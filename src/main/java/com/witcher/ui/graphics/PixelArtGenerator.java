package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Программная генерация пиксель-арта для заставки.
 * Рисует ведьмака за барной стойкой (спиной), монстров по бокам.
 */
public class PixelArtGenerator {

    // ========== ВЕДЬМАК ЗА БАРНОЙ СТОЙКОЙ (вид со спины, сидит, пьёт зелье) ==========
    public static BufferedImage generateWitcherBack() {
        int W = 48, H = 64;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Палитра
        Color hair = new Color(210, 210, 220);       // белые волосы
        Color hairDark = new Color(160, 160, 175);
        Color skin = new Color(200, 170, 140);
        Color armor = new Color(50, 45, 42);          // тёмная броня
        Color armorLight = new Color(75, 68, 62);
        Color armorDetail = new Color(90, 80, 70);
        Color medallion = new Color(180, 180, 190);   // медальон волка
        Color medallionGlow = new Color(200, 200, 220);
        Color swordHandle = new Color(120, 100, 80);
        Color swordBlade = new Color(190, 200, 210);
        Color potionGlow = new Color(80, 220, 120);   // зелёное зелье
        Color potionDark = new Color(50, 160, 80);
        Color potionBottle = new Color(100, 90, 80);
        Color stool = new Color(100, 70, 45);
        Color stoolDark = new Color(75, 50, 30);
        Color belt = new Color(90, 60, 35);
        Color pants = new Color(45, 40, 38);

        // Табурет (ноги)
        px(g, stoolDark, 15, 56, 2, 8);
        px(g, stoolDark, 31, 56, 2, 8);
        px(g, stool, 14, 52, 20, 4); // сиденье

        // Ноги (тёмные штаны, расслабленная поза)
        px(g, pants, 17, 46, 6, 10);
        px(g, pants, 25, 46, 6, 10);
        // Ботинки
        px(g, armorDetail, 16, 54, 8, 3);
        px(g, armorDetail, 24, 54, 8, 3);

        // Тело (броня со спины)
        px(g, armor, 16, 22, 16, 24);
        px(g, armorLight, 18, 24, 12, 20);
        // Плечи
        px(g, armor, 12, 22, 6, 6);
        px(g, armor, 30, 22, 6, 6);
        px(g, armorDetail, 13, 23, 4, 4);
        px(g, armorDetail, 31, 23, 4, 4);

        // Пояс
        px(g, belt, 15, 42, 18, 3);
        px(g, new Color(160, 140, 60), 23, 42, 3, 3); // пряжка

        // Медальон-волка на спине (маленький)
        px(g, medallion, 22, 32, 4, 4);
        px(g, medallionGlow, 23, 33, 2, 2);

        // Шея
        px(g, skin, 20, 18, 8, 5);

        // Голова (сзади — волосы)
        px(g, hairDark, 18, 6, 12, 14);
        px(g, hair, 19, 7, 10, 12);
        px(g, hair, 17, 10, 2, 8);  // волосы слева
        px(g, hair, 29, 10, 2, 8);  // волосы справа
        // Завязка/хвост
        px(g, hairDark, 22, 4, 4, 3);
        px(g, hair, 23, 2, 2, 3);   // кончик хвоста

        // Левая рука (поднята — держит бутылку с зельем)
        px(g, armorLight, 10, 24, 4, 6);
        px(g, skin, 8, 22, 4, 4);    // кисть
        px(g, skin, 7, 18, 3, 5);    // кисть поднята

        // Бутылка зелья в руке
        px(g, potionBottle, 6, 12, 4, 7);
        px(g, potionGlow, 7, 13, 2, 4);
        // Свечение зелья (1px ореол)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        px(g, potionGlow, 5, 11, 6, 9);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        px(g, potionGlow, 4, 10, 8, 11);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Правая рука (опущена на стойку)
        px(g, armorLight, 34, 24, 4, 10);
        px(g, skin, 35, 33, 3, 4);

        // Меч за спиной (торчит вверх-вправо по диагонали)
        px(g, swordHandle, 28, 16, 2, 10);
        px(g, swordBlade, 29, 4, 2, 13);
        px(g, new Color(220, 225, 235), 30, 2, 1, 3); // кончик

        // Перекрестье
        px(g, new Color(160, 140, 60), 27, 15, 6, 2);

        g.dispose();
        return img;
    }

    // ========== МОНСТР СЛЕВА (утопец/дровнер, подглядывает справа) ==========
    public static BufferedImage generateMonsterLeft() {
        int W = 32, H = 56;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Палитра
        Color skinDark = new Color(60, 80, 55);
        Color skin = new Color(80, 105, 70);
        Color skinLight = new Color(100, 130, 90);
        Color eyeGlow = new Color(240, 220, 50);
        Color eyePupil = new Color(30, 20, 10);
        Color slime = new Color(120, 160, 80, 120);
        Color teeth = new Color(200, 195, 170);
        Color mouth = new Color(100, 40, 35);
        Color claw = new Color(140, 130, 110);

        // Голова (вытянутая, рыбообразная)
        px(g, skinDark, 10, 4, 18, 22);
        px(g, skin, 12, 6, 14, 18);
        px(g, skinLight, 14, 8, 10, 10); // блик

        // Череп/лоб (выпуклый)
        px(g, skinDark, 14, 2, 12, 4);
        px(g, skin, 16, 3, 8, 2);

        // Глаз (светящийся жёлтый)
        px(g, eyeGlow, 18, 10, 6, 4);
        px(g, eyePupil, 20, 11, 2, 2);
        // Свечение глаза
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        px(g, eyeGlow, 16, 8, 10, 8);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Рот (оскалился)
        px(g, mouth, 16, 18, 10, 4);
        px(g, teeth, 17, 18, 2, 2);
        px(g, teeth, 21, 18, 2, 2);
        px(g, teeth, 17, 20, 2, 2);
        px(g, teeth, 21, 20, 2, 2);

        // Шея/тело
        px(g, skinDark, 14, 24, 12, 14);
        px(g, skin, 16, 26, 8, 10);

        // Когтистая лапа (вцепилась в край)
        px(g, skinDark, 22, 32, 8, 6);
        px(g, skin, 24, 33, 4, 4);
        // Когти
        px(g, claw, 26, 36, 2, 4);
        px(g, claw, 28, 35, 2, 5);
        px(g, claw, 30, 36, 2, 4);

        // Слизь (капли)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        px(g, slime, 20, 22, 3, 6);
        px(g, slime, 12, 16, 2, 4);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Плавники/наросты на голове
        px(g, skinDark, 8, 6, 4, 3);
        px(g, skinDark, 6, 8, 3, 2);
        px(g, skinDark, 12, 1, 3, 3);

        g.dispose();
        return img;
    }

    // ========== МОНСТР СПРАВА (грифон, подглядывает слева, отзеркаленный) ==========
    public static BufferedImage generateMonsterRight() {
        int W = 32, H = 56;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Палитра
        Color featherDark = new Color(80, 55, 40);
        Color feather = new Color(120, 85, 55);
        Color featherLight = new Color(155, 120, 75);
        Color eyeGlow = new Color(220, 60, 40);
        Color eyePupil = new Color(30, 10, 10);
        Color beak = new Color(180, 160, 60);
        Color beakDark = new Color(140, 120, 40);
        Color claw = new Color(160, 150, 100);

        // Голова (крупная, хищная)
        px(g, featherDark, 2, 4, 18, 20);
        px(g, feather, 4, 6, 14, 16);
        px(g, featherLight, 6, 8, 8, 8);

        // Ушные пучки / "рога"
        px(g, featherDark, 4, 0, 3, 5);
        px(g, featherDark, 14, 0, 3, 5);
        px(g, feather, 5, 1, 1, 3);
        px(g, feather, 15, 1, 1, 3);

        // Глаз (огненно-красный)
        px(g, eyeGlow, 6, 10, 5, 4);
        px(g, eyePupil, 8, 11, 2, 2);
        // Свечение
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        px(g, eyeGlow, 4, 8, 9, 8);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Клюв
        px(g, beakDark, 2, 16, 10, 4);
        px(g, beak, 0, 17, 4, 2);   // кончик
        px(g, beakDark, 4, 20, 6, 2); // нижняя часть

        // Шея/тело (оперение)
        px(g, featherDark, 6, 24, 12, 16);
        px(g, feather, 8, 26, 8, 12);
        // Перья-текстура
        px(g, featherLight, 9, 28, 2, 2);
        px(g, featherLight, 13, 30, 2, 2);
        px(g, featherLight, 10, 34, 2, 2);

        // Когтистая лапа
        px(g, featherDark, 0, 32, 8, 6);
        px(g, feather, 2, 33, 4, 4);
        // Когти
        px(g, claw, 0, 36, 2, 4);
        px(g, claw, 2, 35, 2, 5);
        px(g, claw, 4, 36, 2, 4);

        g.dispose();
        return img;
    }

    private static void px(Graphics2D g, Color c, int x, int y, int w, int h) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }
}
