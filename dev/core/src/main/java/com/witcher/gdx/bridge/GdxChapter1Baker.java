package com.witcher.gdx.bridge;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.witcher.gdx.graphics.PixelTextures;
import com.witcher.ui.chapter1.bridge.Chapter1BakeKeys;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * LibGDX-запекание спрайтов вспышки Весемира / Геральта для Swing.
 * Фон не заливается — FBO clear с alpha=0, исходники не трогаем на диске.
 */
public final class GdxChapter1Baker {

  /** Высота под IntroCharacterLayout (0.85 × 360), без лишнего даунскейла в Swing. */
  public static final int CHAR_H = 306;

  private GdxChapter1Baker() {
  }

  public static Map<String, BufferedImage> bake() {
    Map<String, BufferedImage> out = new HashMap<>();
    SpriteBatch batch = new SpriteBatch();
    try {
      put(out, Chapter1BakeKeys.VESEMIR_IDLE, bakeChar(batch, "vesemir_idle.png"));
      put(out, Chapter1BakeKeys.VESEMIR_SPEAK, bakeChar(batch, "vesemir_speak.png"));
      put(out, Chapter1BakeKeys.YOUNG_GERALT_IDLE, bakeChar(batch, "young_geralt_idle.png"));
      put(out, Chapter1BakeKeys.YOUNG_GERALT_SPEAK, bakeChar(batch, "young_geralt_speak.png"));
    } finally {
      batch.dispose();
    }
    return out;
  }

  private static void put(Map<String, BufferedImage> out, String key, BufferedImage image) {
    if (GdxIconBaker.isUsable(image)) {
      out.put(key, image);
    }
  }

  private static BufferedImage bakeChar(SpriteBatch batch, String fileName) {
    String path = "sprites/chapter1/battle/" + fileName;
    PixelTextures.LoadedTexture loaded = PixelTextures.loadFirstMeta(path);
    if (loaded == null || loaded.texture == null) {
      return null;
    }
    try {
      TextureRegion region = new TextureRegion(loaded.texture);
      int srcW = Math.max(1, region.getRegionWidth());
      int srcH = Math.max(1, region.getRegionHeight());
      int dstH = CHAR_H;
      int dstW = Math.max(1, Math.round(srcW * (dstH / (float) srcH)));
      return GdxTextureDownscaler.bake(batch, region, dstW, dstH);
    } finally {
      PixelTextures.dispose(loaded.texture);
    }
  }
}
