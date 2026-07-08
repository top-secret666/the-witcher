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

    private FreeTypeFontGenerator generator;

    public void load() {
        FileHandle fontFile = resolveFontFile();
        if (fontFile != null) {
            try {
                generator = new FreeTypeFontGenerator(fontFile);
                title = generate(15, true);
                ui = generate(13, false);
                uiSmall = generate(10, false);
                dialog = generate(12, false);
                Gdx.app.log("GameFonts", "Shrift: " + fontFile.path());
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
        Gdx.app.log("GameFonts", "Zapasnoj shrift (latinitca)");
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
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = size;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + CYRILLIC;
        p.hinting = FreeTypeFontGenerator.Hinting.Slight;
        p.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        p.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        if (bold) {
            p.borderWidth = 0.5f;
            p.borderColor = new com.badlogic.gdx.graphics.Color(0.08f, 0.05f, 0.02f, 0.85f);
        }
        BitmapFont font = generator.generateFont(p);
        font.getRegion().getTexture().setFilter(
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        return font;
    }

    private static BitmapFont fallback(float scale) {
        BitmapFont font = new BitmapFont();
        font.getData().setScale(scale);
        font.getRegion().getTexture().setFilter(
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        return font;
    }

    @Override
    public void dispose() {
        disposeFont(title);
        disposeFont(ui);
        disposeFont(uiSmall);
        disposeFont(dialog);
        disposeGenerator();
    }

    private void disposeGenerator() {
        if (generator != null) {
            generator.dispose();
            generator = null;
        }
    }

    private static void disposeFont(BitmapFont font) {
        if (font != null) {
            font.dispose();
        }
    }
}
