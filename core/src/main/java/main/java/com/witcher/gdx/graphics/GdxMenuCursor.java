package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;

/** Скрытие системного курсора — как Swing {@code GameWindow.useHiddenCursor}. */
public final class GdxMenuCursor {

    private static Cursor hidden;
    private static boolean menuHidden;

    private GdxMenuCursor() {
    }

    public static void hideForMenu() {
        if (hidden == null) {
            Pixmap blank = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            hidden = Gdx.graphics.newCursor(blank, 0, 0);
            blank.dispose();
        }
        Gdx.graphics.setCursor(hidden);
        menuHidden = true;
    }

    public static void restoreAfterMenu() {
        if (!menuHidden) {
            return;
        }
        try {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        } catch (Exception ignored) {
            Gdx.graphics.setCursor(null);
        }
        menuHidden = false;
    }

    public static void dispose() {
        if (hidden != null) {
            hidden.dispose();
            hidden = null;
        }
        menuHidden = false;
    }
}
