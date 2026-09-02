package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * Загрузка ассетов главного меню — baked {@code sprites/menu/1x/} как у лавки.
 */
public final class GdxMenuAssets implements Disposable {

    public Texture backgroundTex;
    public Texture buttonsSheetTex;
    public Texture cursorTex;
    public Texture logoSignTex;
    public Texture titleLogoSheetTex;

    /** [кнопка 0..2][состояние 0..2] */
    public TextureRegion[][] buttonFrames;
    public TextureRegion titleLogoFrame;

    /** Отдельные baked-текстуры кнопок (если есть). */
    public Texture[] bakedButtonTextures;

    public static GdxMenuAssets load() {
        GdxMenuAssets a = new GdxMenuAssets();
        a.backgroundTex = PixelTextures.loadMenu("menu_bg_custom.jpg");
        a.cursorTex = PixelTextures.loadMenu("menu_cursor.png");
        a.logoSignTex = PixelTextures.loadMenu("menu_logo_sign.png");
        a.titleLogoFrame = loadTitleLogo(a);
        a.buttonFrames = loadBakedButtons(a);
        if (a.buttonFrames == null || a.buttonFrames.length == 0) {
            a.buttonFrames = loadGrid("sprites/menu/menu_buttons_sheet.png", 3, 3, true, a);
        }
        if (a.titleLogoFrame == null) {
            a.titleLogoFrame = loadFirstFrame("sprites/witcher_logo_new.png", 2, 3, true, a);
        }
        return a;
    }

    private static TextureRegion[][] loadBakedButtons(GdxMenuAssets owner) {
        TextureRegion[][] grid = new TextureRegion[3][3];
        java.util.ArrayList<Texture> baked = new java.util.ArrayList<>();
        boolean any = false;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                Texture tex = PixelTextures.loadMenu("buttons/btn_" + r + "_" + c + ".png");
                if (tex != null) {
                    grid[r][c] = new TextureRegion(tex);
                    baked.add(tex);
                    any = true;
                }
            }
        }
        if (any) {
            owner.bakedButtonTextures = baked.toArray(new Texture[0]);
        }
        return any ? grid : new TextureRegion[0][0];
    }

    private static TextureRegion loadTitleLogo(GdxMenuAssets owner) {
        Texture tex = PixelTextures.loadMenu("witcher_logo_frame.png");
        if (tex == null) {
            return null;
        }
        owner.titleLogoSheetTex = tex;
        return new TextureRegion(tex);
    }

    private static TextureRegion[][] loadGrid(String path, int cols, int rows, boolean stripBlack,
                                              GdxMenuAssets owner) {
        FileHandle file = PixelTextures.resolve(path);
        if (file == null) {
            return new TextureRegion[0][0];
        }
        Pixmap pixmap = new Pixmap(file);
        try {
            pixmap = ensureRgba(pixmap);
            if (stripBlack) {
                stripNearBlack(pixmap);
            }
            Texture texture = new Texture(pixmap);
            RenderQuality.apply(texture);
            owner.buttonsSheetTex = texture;
            return splitGrid(texture, cols, rows);
        } finally {
            pixmap.dispose();
        }
    }

    private static TextureRegion loadFirstFrame(String path, int cols, int rows, boolean stripBlack,
                                                GdxMenuAssets owner) {
        FileHandle file = PixelTextures.resolve(path);
        if (file == null) {
            return null;
        }
        Pixmap pixmap = new Pixmap(file);
        try {
            pixmap = ensureRgba(pixmap);
            if (stripBlack) {
                stripNearBlack(pixmap);
            }
            Texture texture = new Texture(pixmap);
            RenderQuality.apply(texture);
            owner.titleLogoSheetTex = texture;
            int fw = texture.getWidth() / cols;
            int fh = texture.getHeight() / rows;
            int srcY = texture.getHeight() - fh;
            return new TextureRegion(texture, 0, srcY, fw, fh);
        } finally {
            pixmap.dispose();
        }
    }

    private static TextureRegion[][] splitGrid(Texture texture, int cols, int rows) {
        int fw = texture.getWidth() / cols;
        int fh = texture.getHeight() / rows;
        int texH = texture.getHeight();
        TextureRegion[][] grid = new TextureRegion[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int srcY = texH - (r + 1) * fh;
                grid[r][c] = new TextureRegion(texture, c * fw, srcY, fw, fh);
            }
        }
        return grid;
    }

    private static Pixmap ensureRgba(Pixmap pixmap) {
        if (pixmap.getFormat() == Pixmap.Format.RGBA8888) {
            return pixmap;
        }
        Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
        rgba.drawPixmap(pixmap, 0, 0);
        pixmap.dispose();
        return rgba;
    }

    private static void stripNearBlack(Pixmap pixmap) {
        int w = pixmap.getWidth();
        int h = pixmap.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = pixmap.getPixel(x, y);
                int a = rgba & 0xff;
                int r = (rgba >>> 24) & 0xff;
                int g = (rgba >>> 16) & 0xff;
                int b = (rgba >>> 8) & 0xff;
                if (a == 0 || (r < 18 && g < 18 && b < 18)) {
                    pixmap.drawPixel(x, y, 0);
                }
            }
        }
    }

    public TextureRegion buttonFrame(int row, int state) {
        if (buttonFrames == null || row < 0 || row >= buttonFrames.length) {
            return null;
        }
        if (state < 0 || state >= buttonFrames[row].length) {
            return null;
        }
        return buttonFrames[row][state];
    }

    @Override
    public void dispose() {
        disposeTex(backgroundTex);
        disposeTex(buttonsSheetTex);
        if (bakedButtonTextures != null) {
            for (Texture t : bakedButtonTextures) {
                disposeTex(t);
            }
        }
        disposeTex(cursorTex);
        disposeTex(logoSignTex);
        disposeTex(titleLogoSheetTex);
        backgroundTex = null;
        buttonsSheetTex = null;
        cursorTex = null;
        logoSignTex = null;
        titleLogoSheetTex = null;
        buttonFrames = null;
        titleLogoFrame = null;
        bakedButtonTextures = null;
    }

    private static void disposeTex(Texture t) {
        if (t != null) {
            t.dispose();
        }
    }
}
