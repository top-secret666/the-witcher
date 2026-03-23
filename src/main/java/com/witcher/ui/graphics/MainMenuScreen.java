package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;

// Для поддержки спрайт-листа фона
import main.java.com.witcher.ui.graphics.SpriteSheet;

public class MainMenuScreen {
        // Табличка-борд для кнопок
        private final BufferedImage boardFrame;
    public enum Action {
        NONE,
        START,
        SETTINGS,
        EXIT
    }

    private final Sprite background;
    private final SpriteSheet boardSheet;
    private final int boardSheetCols = 8; // по картинке 8 кадров
    private final int boardSheetRows = 1;
    private final BufferedImage[][] buttons; // [buttonRow][stateCol]
    private final BufferedImage[] smokeFrames;
    private final BufferedImage[] dustFrames;
    private final BufferedImage[] transitionFrames;
    private final BufferedImage cursor;
    private final BufferedImage titleLogo;
    private final BufferedImage logoSignData;

    private final String[] buttonLabels = new String[]{"Играть", "Настройки", "Выход"};

    // buttonRects — прямоугольники для размещения кнопок меню (Играть, Настройки, Выход)
    private final Rectangle[] buttonRects = new Rectangle[]{new Rectangle(), new Rectangle(), new Rectangle()};

    private int selectedIndex = 0;
    private int pressedIndex = -1;
    private int pressedTicks = 0;
    private int tick = 0;
    private int transitionTick = 0;
    private Action pendingAction = Action.NONE;

    public MainMenuScreen() {
            boardFrame = loadTrimmed("/assets/sprites/menu/menu_board_single.png"); // путь к вашей табличке
        background = Sprite.load("/assets/sprites/menu/menu_bg_custom.jpg");
        boardSheet = SpriteSheet.load("/assets/sprites/menu/menu_board_sheet.png", boardSheetCols, boardSheetRows, 1, false);
        buttons = loadButtonGrid("/assets/sprites/menu/menu_buttons_sheet.png", 3, 3);
        titleLogo = loadFirstFrame("/assets/sprites/witcher_logo_new.png", 2, 3, true);
        logoSignData = loadTrimmed("/assets/sprites/menu/menu_logo_sign.png");

        // smokeFrames removed per user request (no smoke)
        smokeFrames = new BufferedImage[0];
        dustFrames = loadFrames("/assets/sprites/menu/menu_dust_sheet.png", 8, 1, true);
        transitionFrames = loadFramesRaw("/assets/sprites/menu/menu_transition_sheet.png", 4, 3);

        cursor = loadTrimmed("/assets/sprites/menu/menu_cursor.png");
    }

    public void update(int mouseX, int mouseY, boolean mouseClicked, int navDir, boolean activate) {
        tick++;

        if (pressedTicks > 0) {
            pressedTicks--;
        }
        transitionTick++;

        // Mouse hover chooses currently focused button.
        for (int i = 0; i < buttonRects.length; i++) {
            if (buttonRects[i].contains(mouseX, mouseY)) {
                selectedIndex = i;
                break;
            }
        }

        if (navDir != 0) {
            selectedIndex = (selectedIndex + navDir) % buttonRects.length;
            if (selectedIndex < 0) selectedIndex += buttonRects.length;
        }

        if (mouseClicked && buttonRects[selectedIndex].contains(mouseX, mouseY)) {
            pressSelected();
        } else if (activate) {
            pressSelected();
        }
    }

    private void pressSelected() {
        pressedIndex = selectedIndex;
        pressedTicks = 6;
        if (selectedIndex == 0) {
            pendingAction = Action.START;
        } else if (selectedIndex == 1) {
            pendingAction = Action.SETTINGS;
        } else if (selectedIndex == 2) {
            pendingAction = Action.EXIT;
        }
    }

    public Action consumeAction() {
        Action out = pendingAction;
        pendingAction = Action.NONE;
        return out;
    }

    public void render(BufferedImage screen, int mouseX, int mouseY) {
        int sw = screen.getWidth();
        int sh = screen.getHeight();

        Graphics2D g = screen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // Очищаем экран чёрным цветом
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        drawBackground(g, sw, sh); // Рисуем фон
        drawAtmosphere(g, sw, sh);

        drawTitle(g, sw, sh);

        layoutButtons(sw, sh);
        drawButtons(g);

        drawTransition(g, sw, sh);

        // Small menu hint for keyboard navigation.
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(new Color(230, 195, 120));
        String help = "W/S or arrows - Enter - Esc";
        int hw = g.getFontMetrics().stringWidth(help);
        g.drawString(help, (sw - hw) / 2, sh - 8);

        drawCursor(g, mouseX, mouseY);
        g.dispose();
    }

    private void drawBackground(Graphics2D g, int sw, int sh) {
        // If a full-size background sprite is provided, draw it scaled to cover the screen.
        if (background != null && background.getImage() != null) {
            BufferedImage bg = background.getImage();
            int srcW = bg.getWidth();
            int srcH = bg.getHeight();
            if (srcW > 0 && srcH > 0) {
                float scale = Math.max((float) sw / srcW, (float) sh / srcH);
                int w = Math.round(srcW * scale);
                int h = Math.round(srcH * scale);
                int x = (sw - w) / 2;
                int y = (sh - h) / 2;
                g.drawImage(bg, x, y, w, h, null);
                return;
            }
        }

        // Fallback: use boardSheet frames if available (keeps previous behaviour)
        if (boardSheet == null) return;
        int frameIdx = Math.min(boardSheetCols - 1, Math.max(0, (int) Math.round((double) (sw - 400) / 200)));
        BufferedImage frame = boardSheet.getFrame(frameIdx);
        if (frame == null) return;
        int srcW = boardSheet.getFrameWidth();
        int srcH = boardSheet.getFrameHeight();
        float scale = Math.max((float) sw / srcW, (float) sh / srcH);
        int w = Math.round(srcW * scale);
        int h = Math.round(srcH * scale);
        int x = (sw - w) / 2;
        int y = (sh - h) / 2;
        g.drawImage(frame, x, y, w, h, null);
    }

    private void layoutButtons(int sw, int sh) {
        // layoutButtons — рассчитывает размеры и положение кнопок так,
        // чтобы они четко помещались на доске с квестами и не налезали
        // на логотип или выходили за пределы доски.
        BufferedImage ref = getButtonFrame(0, 0);
        // Вычисляем нижнюю границу логотипа
        int logoY = (int)(sh * 0.035f);
        int logoReservedBottom = logoY;
        if (logoSignData != null) {
            int signW = (int)(sw * 0.45f);
            int signH = Math.max(1, signW * logoSignData.getHeight() / logoSignData.getWidth());
            logoReservedBottom = logoY + signH;
        } else if (titleLogo != null) {
            int logoW = (int)(sw * 0.31f);
            int logoH = Math.max(1, logoW * titleLogo.getHeight() / titleLogo.getWidth());
            logoReservedBottom = logoY + logoH;
        }
        logoReservedBottom += 16;

        // Высота доступной области под кнопки
        int availableH = sh - logoReservedBottom - 16;
        int gap = (int)(availableH * 0.04f); // 4% от доступной высоты
        int bh = (int)((availableH - gap * (buttonRects.length - 1)) / buttonRects.length);
        int bw = (int)(sw * 0.98f); // почти на всю ширину
        int startX = (sw - bw) / 2;
        int startY = logoReservedBottom;

        for (int i = 0; i < buttonRects.length; i++) {
            buttonRects[i].setBounds(startX, startY + i * (bh + gap), bw, bh);
        }
    }

    private void drawTitle(Graphics2D g, int sw, int sh) {
        // drawTitle — рисует табличку и логотип ведьмака сверху доски
        int logoY = (int) (sh * 0.035f);
        int signW = (int) (sw * 0.45f);
        int signX = (sw - signW) / 2;

        if (logoSignData != null) {
            int signH = Math.max(1, signW * logoSignData.getHeight() / logoSignData.getWidth());
            g.drawImage(logoSignData, signX, logoY, signW, signH, null);

            if (titleLogo != null) {
                int logoW = (int) (signW * 0.7f);
                int logoH = Math.max(1, logoW * titleLogo.getHeight() / titleLogo.getWidth());
                int logoX = (sw - logoW) / 2;
                int innerLogoY = logoY + (signH - logoH) / 2 + (int)(signH * 0.05f); // slightly offset down
                g.drawImage(titleLogo, logoX, innerLogoY, logoW, logoH, null);
            }
            return;
        }

        if (titleLogo != null) {
            int logoW = (int) (sw * 0.31f);
            int logoH = Math.max(1, logoW * titleLogo.getHeight() / titleLogo.getWidth());
            int logoX = (sw - logoW) / 2;

            Composite prev = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g.setColor(Color.BLACK);
            g.fillRoundRect(logoX - 4, logoY - 2, logoW + 8, logoH + 4, 4, 4);
            g.setComposite(prev);

            g.drawImage(titleLogo, logoX, logoY, logoW, logoH, null);
            return;
        }

        g.setFont(new Font("Serif", Font.BOLD, 26));
        g.setColor(new Color(230, 194, 92));
        String title = "WITCHER";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (sw - tw) / 2, (int) (sh * 0.12f));
    }

    private void drawButtons(Graphics2D g) {
        // drawButtons — рисует кнопки меню с нужным состоянием (обычная, hover, pressed)
        for (int i = 0; i < buttonRects.length; i++) {
            Rectangle r = buttonRects[i];
            int state = 0;
            if (pressedIndex == i && pressedTicks > 0) {
                state = 2;
            } else if (selectedIndex == i) {
                state = 1;
            }

            // Рисуем табличку под кнопкой (еще больше)
            if (boardFrame != null) {
                int boardW = (int)(r.width * 36.45); // еще больше ширина таблички
                int boardH = (int)(r.height * 36.45); // еще больше высота таблички
                int boardX = r.x - (int)((boardW - r.width) / 2);
                int boardY = r.y - (int)((boardH - r.height) / 2);
                g.drawImage(boardFrame, boardX, boardY, boardW, boardH, null);
            }

            // Рисуем спрайт кнопки, чуть уже
            BufferedImage frame = getButtonFrame(i, state);
            if (frame != null) {
                int spriteW = (int)(r.width * 0.25); // 40% ширины
                int spriteH = (int)(r.height * 0.7); // 70% высоты
                int spriteX = r.x + (r.width - spriteW) / 2;
                int spriteY = r.y + (r.height - spriteH) / 2;
                g.drawImage(frame, spriteX, spriteY, spriteW, spriteH, null);
            }

            // Draw label on top to ensure readability (shadow + main color)
            String label = buttonLabels.length > i ? buttonLabels[i] : "";
            if (!label.isEmpty()) {
                int fontSize = Math.max(18, (int) (r.height * 0.35f)); // увеличиваем размер текста
                Font font = new Font("Serif", Font.BOLD, fontSize);
                g.setFont(font);
                FontMetrics fm = g.getFontMetrics(font);
                int textW = fm.stringWidth(label);
                int textH = fm.getAscent() - fm.getDescent();
                int tx = r.x + (r.width - textW) / 2;
                int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;

                // Shadow
                Color shadow = new Color(0, 0, 0, 180);
                g.setColor(shadow);
                g.drawString(label, tx + 1, ty + 1);

                // Main text (bright)
                Color main = new Color(245, 220, 120);
                if (state == 2) main = main.darker();
                g.setColor(main);
                g.drawString(label, tx, ty);
            }
        }
    }

    private void drawAtmosphere(Graphics2D g, int sw, int sh) {
        // drawAtmosphere — рисует анимации атмосферы: пыль внизу и дым по бокам логотипа
        // Dust (bottom strip)
        int dustIdx = (tick / 5) % Math.max(1, dustFrames.length);
        if (dustFrames.length > 0) {
            BufferedImage dust = dustFrames[dustIdx];
            if (dust != null) {
                int dh = dust.getHeight();
                int dw = dust.getWidth();
                // draw dust smaller and slightly above the top edge
                float scale = 0.4f; // scale down to 40%
                int drawW = Math.max(1, (int) (dw * scale));
                int drawH = Math.max(1, (int) (dh * scale));
                int y = - (drawH / 3); // move slightly above top
                // tile horizontally using scaled width
                Composite prev = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                // top dust strip only
                for (int x = 0; x < sw; x += drawW) {
                    g.drawImage(dust, x, y, drawW, drawH, null);
                }
                g.setComposite(prev);
            }
        }

        // smoke removed — no smoke drawn
    }

    private void drawTransition(Graphics2D g, int sw, int sh) {
        // drawTransition отключён — трансформации экрана не нужны
        return;
    }

    private void drawCursor(Graphics2D g, int mouseX, int mouseY) {
        // drawCursor — рисует кастомный курсор в стиле ведьмака
        if (cursor != null) {
            int cw = 28;
            int ch = Math.max(1, cw * cursor.getHeight() / cursor.getWidth());
            g.drawImage(cursor, mouseX - 4, mouseY - 4, cw, ch, null);
        } else {
            g.setColor(Color.WHITE);
            g.drawLine(mouseX, mouseY, mouseX + 8, mouseY + 8);
        }
    }

    private BufferedImage getButtonFrame(int row, int state) {
        if (buttons == null || row < 0 || row >= buttons.length) return null;
        if (buttons[row] == null) return null;
        if (state < 0 || state >= buttons[row].length) return null;
        return buttons[row][state];
    }

    private static BufferedImage[][] loadButtonGrid(String path, int cols, int rows) {
        Sprite s = Sprite.load(path);
        if (s == null) return null;

        BufferedImage src = removeNearBlack(s.getImage());
        int cw = src.getWidth() / cols;
        int ch = src.getHeight() / rows;

        BufferedImage[][] out = new BufferedImage[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                BufferedImage frame = src.getSubimage(c * cw, r * ch, cw, ch);
                out[r][c] = trimTransparent(frame);
            }
        }
        return out;
    }

    private static BufferedImage[] loadFrames(String path, int cols, int rows, boolean trim) {
        Sprite s = Sprite.load(path);
        if (s == null) return new BufferedImage[0];

        BufferedImage src = removeNearBlack(s.getImage());
        int cw = src.getWidth() / cols;
        int ch = src.getHeight() / rows;
        BufferedImage[] out = new BufferedImage[cols * rows];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                BufferedImage frame = src.getSubimage(c * cw, r * ch, cw, ch);
                out[r * cols + c] = trim ? trimTransparent(frame) : frame;
            }
        }
        return out;
    }

    private static BufferedImage[] loadFramesRaw(String path, int cols, int rows) {
        Sprite s = Sprite.load(path);
        if (s == null) return new BufferedImage[0];

        BufferedImage src = s.getImage();
        int cw = src.getWidth() / cols;
        int ch = src.getHeight() / rows;
        BufferedImage[] out = new BufferedImage[cols * rows];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                BufferedImage frame = src.getSubimage(c * cw, r * ch, cw, ch);
                BufferedImage copy = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = copy.createGraphics();
                g.drawImage(frame, 0, 0, null);
                g.dispose();
                out[r * cols + c] = copy;
            }
        }
        return out;
    }

    private static BufferedImage loadTrimmed(String path) {
        Sprite s = Sprite.load(path);
        if (s == null) return null;
        return trimTransparent(removeNearBlack(s.getImage()));
    }

    private static BufferedImage loadFirstFrame(String path, int cols, int rows, boolean removeBlack) {
        Sprite s = Sprite.load(path);
        if (s == null) return null;

        BufferedImage src = removeBlack ? removeNearBlack(s.getImage()) : s.getImage();
        int fw = src.getWidth() / cols;
        int fh = src.getHeight() / rows;
        BufferedImage frame = src.getSubimage(0, 0, fw, fh);
        return trimTransparent(frame);
    }

    private static BufferedImage removeNearBlack(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;

                // У большинства новых ассетов чёрная подложка вместо прозрачности.
                if (a == 0 || (r < 18 && g < 18 && b < 18)) {
                    out.setRGB(x, y, 0x00000000);
                } else {
                    out.setRGB(x, y, argb | 0xFF000000);
                }
            }
        }
        return out;
    }

    private static BufferedImage trimTransparent(BufferedImage src) {
        int minX = src.getWidth();
        int minY = src.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                if (a > 4) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage cropped = src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
        BufferedImage copy = new BufferedImage(cropped.getWidth(), cropped.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(cropped, 0, 0, null);
        g.dispose();
        return copy;
    }
}
