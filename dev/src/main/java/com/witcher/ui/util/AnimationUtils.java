package main.java.com.witcher.ui.util;

import java.util.Random;

public class AnimationUtils {

    public static void animateWitcherLogoFade() {
        String[] logoLines = {

                "██╗    ██╗██╗████████╗ ██████╗██╗  ██╗███████╗██████╗",
                "██║    ██║██║╚══██╔══╝██╔════╝██║  ██║██╔════╝██╔══██╗",
                "██║ █╗ ██║██║   ██║   ██║     ███████║█████╗  ██████╔╝",
                "██║███╗██║██║   ██║   ██║     ██╔══██║██╔══╝  ██╔══██╗",
                "╚███╔███╔╝██║   ██║   ╚██████╗██║  ██║███████╗██║  ██║",
                "╚══╝╚══╝ ╚═╝   ╚═╝    ╚═════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝"
        };

        String[] glitchChars = {"█", "▓", "▒", "░"};
        Random random = new Random();

        for (String line : logoLines) {
            for (int glitch = 0; glitch < 3; glitch++) {
                StringBuilder glitched = new StringBuilder(line);
                for (int i = 0; i < 5; i++) {
                    int pos = random.nextInt(line.length());
                    glitched.setCharAt(pos, glitchChars[random.nextInt(glitchChars.length)].charAt(0));
                }
                System.out.print("\r" + glitched);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.print("\r" + line + "\n");
        }
    }

    public static void showProgressBar() {
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i <= 20; i++) {
            bar.append("█");
            System.out.print("\rЗагрузка: [" + bar + "] " + i * 5 + "%");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();
    }

    public static void displayCharacterSetup() {
        System.out.println("""
                ╔════════════[ ℕ𝔸𝕊𝕋ℝ𝕆𝕀𝕂𝔸 ℙ𝔼ℝ𝕊𝕆ℕ𝔸𝔾𝔸 ]════════════╗
                 ║                                             ║
                ║           ⚔️  𝕲𝕰𝕽𝕬𝕷𝕿 𝖅 𝕽𝕴𝖁𝕴𝕴  ⚔️           ║
                 ║                                             ║
                ║     Класс: Ведьмак                            ║
                 ║    Школа: Волка                             ║
                ║     Статус: Странствующий охотник             ║
                 ║                                             ║
                ╠════════════[ ℕ𝔸ℂℍ𝔸𝕃ℕ𝕆𝔼 𝕊ℕ𝔸ℝ𝕐𝔸𝕁𝔼ℕ𝕀𝔼 ]══════════╣
                 ║                                             ║
                ║    💰Кошелек: [?] крон💰                      ║
                 ║     Уровень: [?]                            ║
                ╚═══════════════════════════════════════════════╝
                """);
    }
}

