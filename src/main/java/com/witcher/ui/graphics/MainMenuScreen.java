package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MainMenuScreen {
    public enum Action {
        NONE,
        START,
        SETTINGS,
        EXIT
    }

    private final Sprite background;
    private final BufferedImage logo;
    private final BufferedImage[][] buttons; // [buttonRow][stateCol]
    private final BufferedImage[] candleFrames;
    private final BufferedImage[] smokeFrames;
    private final BufferedImage[] dustFrames;
    private final BufferedImage cursor;

    private final Rectangle[] buttonRects = new Rectangle[]{new Rectangle(), new Rectangle(), new Rectangle()};

    private int selectedIndex = 0;
    private int pressedIndex = -1;
    private int pressedTicks = 0;
    private int tick = 0;
    private Action pendingAction = Action.NONE;

    public MainMenuScreen() {
        background = Sprite.load("/assets/sprites/menu/menu_bg.png");
        logo = loadTrimmed("/assets/sprites/menu/menu_logo_sign.png");
        buttons = loadButtonGrid("/assets/sprites/menu/menu_buttons_sheet.png", 3, 3);

        candleFrames = loadFrames("/assets/sprites/menu/menu_candle_flame_sheet.png", 8, 1, true);
        smokeFrames = loadFrames("/assets/sprites/menu/menu_smoke_sheet.png", 8, 1, true);
        dustFrames = loadFrames("/assets/sprites/menu/menu_dust_sheet.png", 8, 1, true);

        cursor = loadTrimmed("/assets/sprites/menu/menu_cursor.png");
    }

    public void update(int mouseX, int mouseY, boolean mouseClicked, int navDir, boolean activate) {
        tick++;

        if (pressedTicks > 0) {
            pressedTicks--;
        }

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

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        drawBackground(g, sw, sh);
        drawAtmosphere(g, sw, sh);

        int logoW = (int) (sw * 0.52f);
        int logoH = logo != null ? Math.max(1, logoW * logo.getHeight() / logo.getWidth()) : 0;
        int logoX = (sw - logoW) / 2;
        int logoY = (int) (sh * 0.05f);
        if (logo != null) {
            g.drawImage(logo, logoX, logoY, logoW, logoH, null);
        }

        layoutButtons(sw, sh);
        drawButtons(g);

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
        if (background == null) return;
        int srcW = background.getWidth();
        int srcH = background.getHeight();
        float scale = Math.max((float) sw / srcW, (float) sh / srcH);
        int w = Math.round(srcW * scale);
        int h = Math.round(srcH * scale);
        int x = (sw - w) / 2;
        int y = (sh - h) / 2;
        g.drawImage(background.getImage(), x, y, w, h, null);
    }

    private void layoutButtons(int sw, int sh) {
        BufferedImage ref = getButtonFrame(0, 0);
        int bw;
        int bh;
        if (ref != null) {
            bw = (int) (sw * 0.38f);
            bh = Math.max(1, bw * ref.getHeight() / ref.getWidth());
        } else {
            bw = (int) (sw * 0.34f);
            bh = (int) (sh * 0.10f);
        }

        int gap = Math.max(8, (int) (sh * 0.028f));
        int totalH = bh * 3 + gap * 2;
        int startX = (sw - bw) / 2;
        int startY = (sh - totalH) / 2 + (int) (sh * 0.15f);

        for (int i = 0; i < buttonRects.length; i++) {
            buttonRects[i].setBounds(startX, startY + i * (bh + gap), bw, bh);
        }
    }

    private void drawButtons(Graphics2D g) {
        for (int i = 0; i < buttonRects.length; i++) {
            Rectangle r = buttonRects[i];
            int state = 0;
            if (pressedIndex == i && pressedTicks > 0) {
                state = 2;
            } else if (selectedIndex == i) {
                state = 1;
            }

            BufferedImage frame = getButtonFrame(i, state);
            if (frame != null) {
                g.drawImage(frame, r.x, r.y, r.width, r.height, null);
            } else {
                g.setColor(new Color(130, 90, 35));
                g.fillRect(r.x, r.y, r.width, r.height);
                g.setColor(new Color(230, 185, 90));
                g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
            }
        }
    }

    private void drawAtmosphere(Graphics2D g, int sw, int sh) {
        int dustIdx = (tick / 5) % Math.max(1, dustFrames.length);
        if (dustFrames.length > 0) {
            BufferedImage dust = dustFrames[dustIdx];
            if (dust != null) {
                Composite prev = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));
                int dw = (int) (sw * 0.92f);
                int dh = Math.max(1, dw * dust.getHeight() / dust.getWidth());
                g.drawImage(dust, (sw - dw) / 2, (int) (sh * 0.14f), dw, dh, null);
                g.setComposite(prev);
            }
        }

        int candleIdx = (tick / 7) % Math.max(1, candleFrames.length);
        if (candleFrames.length > 0) {
            BufferedImage c = candleFrames[candleIdx];
            if (c != null) {
                int ch = (int) (sh * 0.18f);
                int cw = Math.max(1, ch * c.getWidth() / c.getHeight());

                // Свечи должны сидеть у нижних углов сцены, а не в центре меню.
                int leftX = (int) (sw * 0.015f);
                int rightX = sw - cw - (int) (sw * 0.015f);
                int candleY = (int) (sh * 0.70f);

                g.drawImage(c, leftX, candleY, cw, ch, null);
                g.drawImage(c, rightX, candleY, cw, ch, null);
            }
        }

        int smokeIdx = (tick / 8) % Math.max(1, smokeFrames.length);
        if (smokeFrames.length > 0) {
            BufferedImage s = smokeFrames[smokeIdx];
            if (s != null) {
                Composite prev = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                int shh = (int) (sh * 0.18f);
                int sww = Math.max(1, shh * s.getWidth() / s.getHeight());
                g.drawImage(s, (sw - sww) / 2, (int) (sh * 0.63f), sww, shh, null);
                g.setComposite(prev);
            }
        }
    }

    private void drawCursor(Graphics2D g, int mouseX, int mouseY) {
        if (cursor != null) {
            int cw = 18;
            int ch = Math.max(1, cw * cursor.getHeight() / cursor.getWidth());
            g.drawImage(cursor, mouseX - 3, mouseY - 2, cw, ch, null);
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

    private static BufferedImage loadTrimmed(String path) {
        Sprite s = Sprite.load(path);
        if (s == null) return null;
        return trimTransparent(removeNearBlack(s.getImage()));
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
