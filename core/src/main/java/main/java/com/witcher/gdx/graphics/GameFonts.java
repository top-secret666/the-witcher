package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

/** Шрифты с кириллицей: проектный TTF → Windows → запасной BitmapFont. */
public final class GameFonts implements Disposable {

    private static final String CYRILLIC =
        "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "—«»…⚠";

    public BitmapFont title;
    public BitmapFont ui;
    public BitmapFont uiSmall;
    public BitmapFont dialog;
    /** Готический жирный для кнопок меню (Philosopher-Bold / MedievalSharp). */
    public BitmapFont menuBold;

    private static final int MENU_BOLD_BASE_SIZE = 18;

    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator menuGenerator;

    public void load() {
        loadMenuBold();
        FileHandle fontFile = resolveFontFile();
        if (fontFile != null) {
            try {
                generator = new FreeTypeFontGenerator(fontFile);
                title = generate(15, true);
                ui = generate(13, false);
                uiSmall = generate(10, false);
                dialog = generate(12, false);
                Gdx.app.log("GameFonts", "Shrift: " + fontFile.path());
                if (menuBold == null) {
                    menuBold = title;
                }
                return;
            } catch (Exception e) {
                Gdx.app.error("GameFonts", "FreeType ne smog zagruzit shrift", e);
                disposeGenerator();
            }
        }
        title = fallback(1.15f);
        ui = fallback(1.05f);
        uiSmall = fallback(0.9f);
        dialog = fallback(1.0f);
        if (menuBold == null) {
            menuBold = title;
        }
        Gdx.app.log("GameFonts", "Zapasnoj shrift (latinitca)");
    }

    private void loadMenuBold() {
        FileHandle gothic = resolveGothicBoldFont();
        if (gothic == null) {
            return;
        }
        try {
            menuGenerator = new FreeTypeFontGenerator(gothic);
            menuBold = generateMenuBold(MENU_BOLD_BASE_SIZE);
            Gdx.app.log("GameFonts", "Menu gothic: " + gothic.path());
        } catch (Exception e) {
            Gdx.app.error("GameFonts", "Menu gothic ne zagruzhen", e);
            disposeMenuGenerator();
        }
    }

    private static FileHandle resolveGothicBoldFont() {
        String[] gothicBold = {
            "fonts/Philosopher-Bold.ttf",
            "fonts/lavka/MedievalSharp-Regular.ttf",
            "fonts/MedievalSharp-Regular.ttf",
            "fonts/gothic-bold.ttf",
            "fonts/gothic.ttf"
        };
        for (String path : gothicBold) {
            FileHandle handle = resolveProjectFont(path);
            if (handle != null) {
                return handle;
            }
        }
        return null;
    }

    private static FileHandle resolveFontFile() {
        String[] projectFonts = {
            "fonts/Philosopher-Regular.ttf",
            "fonts/Philosopher-Bold.ttf",
            "fonts/game.ttf",
            "fonts/noto-sans.ttf"
        };
        for (String path : projectFonts) {
            FileHandle handle = resolveProjectFont(path);
            if (handle != null) {
                return handle;
            }
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String[] winFonts = {
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/times.ttf",
                "C:/Windows/Fonts/segoeui.ttf",
                "C:/Windows/Fonts/trebuc.ttf"
            };
            for (String path : winFonts) {
                FileHandle handle = Gdx.files.absolute(path);
                if (handle.exists()) {
                    return handle;
                }
            }
        }
        return null;
    }

    private static FileHandle resolveProjectFont(String path) {
        FileHandle fromPixel = PixelTextures.resolve(path);
        if (fromPixel != null && fromPixel.exists()) {
            return fromPixel;
        }
        if (Gdx.files.internal(path).exists()) {
            return Gdx.files.internal(path);
        }
        if (Gdx.files.internal("assets/" + path).exists()) {
            return Gdx.files.internal("assets/" + path);
        }
        return null;
    }

    private BitmapFont generate(int size, boolean bold) {
        return generateFrom(generator, size, bold, 0.5f);
    }

    private BitmapFont generateMenuBold(int size) {
        return generateFrom(menuGenerator, size, true, 1.0f);
    }

    private static BitmapFont generateFrom(
        FreeTypeFontGenerator gen, int size, boolean bold, float borderWidth) {
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = size;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + CYRILLIC;
        p.hinting = FreeTypeFontGenerator.Hinting.Full;
        p.magFilter = RenderQuality.MAG;
        p.minFilter = RenderQuality.MIN;
        if (bold && borderWidth > 0f) {
            p.borderWidth = borderWidth;
            p.borderColor = new com.badlogic.gdx.graphics.Color(0.08f, 0.05f, 0.02f, 0.85f);
        }
        BitmapFont font = gen.generateFont(p);
        font.getRegion().getTexture().setFilter(RenderQuality.MIN, RenderQuality.MAG);
        return font;
    }

    public float menuBoldBaseSize() {
        return MENU_BOLD_BASE_SIZE;
    }

    private static BitmapFont fallback(float scale) {
        BitmapFont font = new BitmapFont();
        font.getData().setScale(scale);
        font.getRegion().getTexture().setFilter(RenderQuality.MIN, RenderQuality.MAG);
        return font;
    }

    @Override
    public void dispose() {
        disposeFont(title);
        disposeFont(ui);
        disposeFont(uiSmall);
        disposeFont(dialog);
        if (menuBold != null && menuBold != title) {
            disposeFont(menuBold);
        }
        menuBold = null;
        disposeGenerator();
        disposeMenuGenerator();
    }

    private void disposeGenerator() {
        if (generator != null) {
            generator.dispose();
            generator = null;
        }
    }

    private void disposeMenuGenerator() {
        if (menuGenerator != null) {
            menuGenerator.dispose();
            menuGenerator = null;
        }
    }

    private static void disposeFont(BitmapFont font) {
        if (font != null) {
            font.dispose();
        }
    }
}
