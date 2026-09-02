package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/** Сглаженная отрисовка: чёткие ассеты без «крупных пикселей». */
public final class RenderQuality {

    public static final TextureFilter MIN = TextureFilter.Linear;
    public static final TextureFilter MAG = TextureFilter.Linear;

    /** Множитель шрифтов относительно базового макета 480×360 (экран 960×720). */
    public static final float FONT_SCALE = 2f;

    private RenderQuality() {
    }

    public static void apply(Texture texture) {
        if (texture != null) {
            texture.setFilter(MIN, MAG);
        }
    }
}
