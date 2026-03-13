package main.java.com.witcher.ui.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import javax.swing.border.AbstractBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class GameWindow {
    private final JFrame frame;
    private final Renderer renderer;
    private JComponent titleBar;
    private Sprite sprite;
    private SplashScreen splashScreen;
    private MainMenuScreen mainMenu;
    private boolean splashActive = true;
    private boolean menuActive = false;

    // Ввод для меню в координатах виртуального экрана
    private int mouseVX = 0;
    private int mouseVY = 0;
    private boolean mouseClickPending = false;
    private int menuNavDir = 0;
    private boolean menuActivate = false;
    private boolean menuExitRequested = false;

    // Цвета шапки окна (в том же духе, что и сплэш)
    private static final Color TITLE_BG = new Color(30, 22, 12);
    private static final Color TITLE_FG = new Color(218, 165, 32);
    private static final Color TITLE_BORDER = new Color(140, 100, 35);
    private static final Color TITLE_HOVER = new Color(230, 180, 60);

    public GameWindow() {
        frame = new JFrame("Witcher - Pixel Prototype");
        // Загрузочное/прототип-окно: убираем системную рамку/заголовок (никакого fullscreen/maximize)
        frame.setUndecorated(true);
        renderer = new Renderer(480, 360, 2); // virtual res 480x360 scaled x2 = окно 960x720

        // Иконка окна/приложения
        Sprite appIcon = Sprite.load("/assets/sprites/app_icon.png");
        if (appIcon == null) {
            appIcon = Sprite.load("/assets/sprites/app_icon.png");
        }
        Image iconImg = null;
        if (appIcon != null) {
            iconImg = appIcon.getImage();
            frame.setIconImage(iconImg);
            try {
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar.getTaskbar().setIconImage(iconImg);
                }
            } catch (Exception ignored) {
                // ignore (not supported on some platforms)
            }
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Пиксельная шапка окна (иконка + заголовок + кнопки)
        titleBar = new PixelTitleBar(frame, iconImg);
        frame.add(titleBar, BorderLayout.NORTH);

        frame.add(renderer, BorderLayout.CENTER);
        frame.pack();

        // Загрузочное/прототип-окно: без ресайза и без разворачивания на весь экран
        frame.setResizable(false);
        Dimension fixedSize = frame.getSize();
        frame.setMinimumSize(fixedSize);
        frame.setMaximumSize(fixedSize);
        frame.addWindowStateListener(e -> {
            if ((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                frame.setExtendedState(Frame.NORMAL);
            }
        });

        // Тонкая рамка вокруг окна (раз уж системную убрали)
        frame.getRootPane().setOpaque(true);
        frame.getRootPane().setBackground(Color.BLACK);
        frame.getRootPane().setBorder(new PixelBorder(TITLE_BORDER, TITLE_FG, TITLE_BG, 10));

        frame.setLocationRelativeTo(null);
        splashScreen = new SplashScreen();

        setupInput();
    }

    private void setupInput() {
        renderer.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateVirtualMouse(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateVirtualMouse(e);
            }
        });

        renderer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updateVirtualMouse(e);
                mouseClickPending = true;
            }
        });

        renderer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!menuActive) return;

                int code = e.getKeyCode();
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    menuNavDir = -1;
                } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    menuNavDir = 1;
                } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                    menuActivate = true;
                } else if (code == KeyEvent.VK_ESCAPE) {
                    menuExitRequested = true;
                }
            }
        });
    }

    private void updateVirtualMouse(MouseEvent e) {
        int rw = Math.max(1, renderer.getWidth());
        int rh = Math.max(1, renderer.getHeight());
        mouseVX = e.getX() * renderer.getVirtualW() / rw;
        mouseVY = e.getY() * renderer.getVirtualH() / rh;
    }

    private void enterMainMenuMode() {
        splashActive = false;
        menuActive = true;
        mainMenu = new MainMenuScreen();

        // Меню должно быть как игровая сцена: без шапки и рамки окна.
        if (titleBar != null) {
            frame.remove(titleBar);
            titleBar = null;
        }
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder());

        // Меню должно быть уже не "окошком", а полноценной fullscreen-сценой.
        frame.setMinimumSize(new Dimension(0, 0));
        frame.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        frame.revalidate();

        GraphicsDevice device = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();
        device.setFullScreenWindow(frame);
        frame.validate();

        // Скрываем системный курсор, рисуем кастомный курсор внутри сцены.
        BufferedImage blank = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor invisible = Toolkit.getDefaultToolkit().createCustomCursor(blank, new Point(0, 0), "menu_blank_cursor");
        renderer.setCursor(invisible);
        renderer.requestFocusInWindow();
    }

    private static final class PixelTitleBar extends JComponent {
        private final JFrame frame;
        private final BufferedImage icon16;
        private final PixelTitleButton minimize;
        private final PixelTitleButton close;

        private PixelTitleBar(JFrame frame, Image iconImg) {
            this.frame = frame;
            this.icon16 = toNearestIcon16(iconImg);

            // Чуть выше шапка, чтобы гармонировала с толстой рамкой
            setPreferredSize(new Dimension(10, 30));

            setLayout(null);
            minimize = new PixelTitleButton("-");
            minimize.addActionListener(() -> frame.setState(Frame.ICONIFIED));
            close = new PixelTitleButton("X");
            close.addActionListener(() -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
            add(minimize);
            add(close);

            // Перетаскивание окна за шапку
            Point[] dragOffset = new Point[1];
            MouseAdapter drag = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragOffset[0] = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragOffset[0] == null) return;
                    Point p = e.getLocationOnScreen();
                    frame.setLocation(p.x - dragOffset[0].x, p.y - dragOffset[0].y);
                }
            };
            addMouseListener(drag);
            addMouseMotionListener(drag);
        }

        @Override
        public void doLayout() {
            int h = getHeight();
            int btn = h - 8;
            int pad = 6;
            int x = getWidth() - pad - btn;
            int y = (h - btn) / 2;
            close.setBounds(x, y, btn, btn);
            minimize.setBounds(x - 4 - btn, y, btn, btn);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            int w = getWidth();
            int h = getHeight();

            // фон шапки
            g2.setColor(TITLE_BG);
            g2.fillRect(0, 0, w, h);

            // нижняя линия (толще для пиксельного вида)
            g2.setColor(TITLE_BORDER);
            int bottomLineH = 4;
            g2.fillRect(0, h - bottomLineH, w, bottomLineH);

            int x = 14; // подвинуть заголовок вправо, чтобы не заходил на кнопки
            if (icon16 != null) {
                int iy = (h - 16) / 2;
                g2.drawImage(icon16, x, iy, null);
                x += 16 + 6;
            }

            // заголовок
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(TITLE_FG);
            // Лёгкая подстройка по пикселям — так визуально ровнее
            int ty = (h + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2 + 1;
            String t = frame.getTitle();
            int maxRight = Math.min(minimize.getX() - 12, w - 12);
            if (maxRight > x) {
                // грубая обрезка по ширине (без троеточий) — для простоты
                while (t.length() > 0 && x + g2.getFontMetrics().stringWidth(t) > maxRight) {
                    t = t.substring(0, t.length() - 1);
                }
                g2.drawString(t, x, ty);
            }

            g2.dispose();
        }
    }

    private interface SimpleActionListener {
        void run();
    }

    private static final class PixelTitleButton extends JComponent {
        private final String label;
        private boolean hover;
        private boolean pressed;
        private SimpleActionListener action;

        private PixelTitleButton(String label) {
            this.label = label;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    boolean wasPressed = pressed;
                    pressed = false;
                    repaint();
                    if (wasPressed && contains(e.getPoint()) && action != null) action.run();
                }
            };
            addMouseListener(m);
        }

        void addActionListener(SimpleActionListener a) {
            this.action = a;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            int w = getWidth();
            int h = getHeight();

            // фон: hover/pressed делаем на контрасте существующих цветов
            Color fill = hover ? TITLE_HOVER : TITLE_BG;
            if (pressed) fill = TITLE_FG;

            g2.setColor(fill);
            g2.fillRect(0, 0, w, h);

            // пиксельная рамка кнопки
            g2.setColor(TITLE_FG);
            g2.fillRect(0, 0, w, 1);
            g2.fillRect(0, h - 1, w, 1);
            g2.fillRect(0, 0, 1, h);
            g2.fillRect(w - 1, 0, 1, h);
            g2.setColor(TITLE_BG);
            g2.fillRect(1, 1, w - 2, 1);
            g2.fillRect(1, h - 2, w - 2, 1);
            g2.fillRect(1, 1, 1, h - 2);
            g2.fillRect(w - 2, 1, 1, h - 2);

            // Символ: рисуем пиксельный крест для кнопки "X", пиксельную полоску для "-", иначе текст
            if ("X".equals(label)) {
                g2.setColor(pressed ? TITLE_BG : TITLE_FG);
                int pad = Math.max(2, w / 6);
                int len = Math.max(1, Math.min(w, h) - pad * 2);
                for (int i = 0; i < len; i++) {
                    g2.fillRect(pad + i, pad + i, 1, 1);
                    g2.fillRect(w - 1 - pad - i, pad + i, 1, 1);
                }
                // дополнительная толщина крестика
                if (len >= 3) {
                    for (int i = 0; i < len; i++) {
                        g2.fillRect(pad + i, pad + i + 1, 1, 1);
                        g2.fillRect(w - 1 - pad - i, pad + i + 1, 1, 1);
                    }
                }
            } else if ("-".equals(label)) {
                g2.setColor(pressed ? TITLE_BG : TITLE_FG);
                int barH = 5; // увеличенная толщина полоски в пикселях
                int barW = Math.max(8, w - 10);
                int bx = (w - barW) / 2;
                int by = (h - barH) / 2 + 1;
                g2.fillRect(bx, by, barW, barH);
                // лёгкий внутренний тон (чтобы не было плоской поверхности)
                g2.setColor(TITLE_BG);
                if (barH > 2) g2.fillRect(bx + 1, by + 1, barW - 2, barH - 2);
            } else {
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.setColor(pressed ? TITLE_BG : TITLE_FG);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(label)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2 + ("-".equals(label) ? 1 : 0);
                g2.drawString(label, tx, ty);
            }

            g2.dispose();
        }
    }

    private static BufferedImage toNearestIcon16(Image iconImg) {
        if (iconImg == null) return null;
        BufferedImage src;
        if (iconImg instanceof BufferedImage) {
            src = (BufferedImage) iconImg;
        } else {
            src = new BufferedImage(iconImg.getWidth(null), iconImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = src.createGraphics();
            g.drawImage(iconImg, 0, 0, null);
            g.dispose();
        }

        BufferedImage dst = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(src, 0, 0, 16, 16, null);
        g2.dispose();
        return dst;
    }

    private static final class PixelBorder extends AbstractBorder {
        private final Color outer;
        private final Color inner;
        private final Color bg;
        private final int thickness;

        private PixelBorder(Color outer, Color inner, Color bg, int thickness) {
            this.outer = outer;
            this.inner = inner;
            this.bg = bg;
            this.thickness = Math.max(3, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = thickness;
            insets.left = thickness;
            insets.bottom = thickness;
            insets.right = thickness;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int t = thickness;
            int w = width;
            int h = height;

            // Внешняя рамка (пиксельные полосы)
            g2.setColor(outer);
            g2.fillRect(x, y, w, t);                 // top
            g2.fillRect(x, y + h - t, w, t);         // bottom
            g2.fillRect(x, y, t, h);                 // left
            g2.fillRect(x + w - t, y, t, h);         // right

            // Внутренняя рамка (второй контур)
            g2.setColor(inner);
            g2.fillRect(x + 1, y + 1, w - 2, 1);                 // top
            g2.fillRect(x + 1, y + h - 2, w - 2, 1);             // bottom
            g2.fillRect(x + 1, y + 1, 1, h - 2);                 // left
            g2.fillRect(x + w - 2, y + 1, 1, h - 2);             // right

            // "Ступеньки" в углах (пиксельная фаска), масштабируется с толщиной
            g2.setColor(bg);
            int steps = Math.max(2, t - 1);
            for (int i = 0; i < steps; i++) {
                int len = steps - i;

                // top-left
                g2.fillRect(x, y + i, len, 1);
                // top-right
                g2.fillRect(x + w - len, y + i, len, 1);

                // bottom-left
                g2.fillRect(x, y + h - 1 - i, len, 1);
                // bottom-right
                g2.fillRect(x + w - len, y + h - 1 - i, len, 1);
            }

            g2.dispose();
        }
    }

    private void initSprite() {
        // После заставки пока показываем тот же логотип (позже заменим на меню/игру)
        sprite = Sprite.load("/assets/sprites/witcher_logo.png");
        if (sprite == null) {
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int col;
                    if (x == 0 || y == 0 || x == 15 || y == 15) {
                        col = 0xFF000000;
                    } else {
                        col = ((x + y) % 2 == 0) ? 0xFFFF00FF : 0xFF00FFFF;
                    }
                    img.setRGB(x, y, col);
                }
            }
            sprite = new Sprite(img);
        }
        renderer.setSprite(sprite);
    }

    public void start() {
        frame.setVisible(true);
        renderer.requestFocusInWindow();
        Timer timer = new Timer(16, e -> {
            if (splashActive) {
                splashScreen.update();
                splashScreen.render(renderer.screen);
                renderer.repaint();
                if (splashScreen.isFinished()) {
                    enterMainMenuMode();
                }
            } else if (menuActive) {
                mainMenu.update(mouseVX, mouseVY, mouseClickPending, menuNavDir, menuActivate);
                mainMenu.render(renderer.screen, mouseVX, mouseVY);
                renderer.repaint();

                MainMenuScreen.Action action = mainMenu.consumeAction();
                if (menuExitRequested || action == MainMenuScreen.Action.EXIT) {
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                } else if (action == MainMenuScreen.Action.START) {
                    System.out.println("[MENU] Start pressed (scene not implemented yet)");
                } else if (action == MainMenuScreen.Action.SETTINGS) {
                    System.out.println("[MENU] Settings pressed (scene not implemented yet)");
                }

                mouseClickPending = false;
                menuNavDir = 0;
                menuActivate = false;
                menuExitRequested = false;
            } else {
                renderer.update();
                renderer.repaint();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow().start());
    }
}
