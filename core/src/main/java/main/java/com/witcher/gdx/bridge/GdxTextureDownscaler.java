package main.java.com.witcher.gdx.bridge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import main.java.com.witcher.gdx.graphics.PixelTextures;

import java.awt.image.BufferedImage;

/** Ступенчатый GPU-даунскейл (FBO /2) → чёткий BufferedImage для Swing. */
final class GdxTextureDownscaler {

    private GdxTextureDownscaler() {
    }

    static BufferedImage bake(SpriteBatch batch, TextureRegion region, int dstW, int dstH) {
        if (region == null || dstW <= 0 || dstH <= 0) {
            return null;
        }
        region.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

        TextureRegion src = region;
        int cw = region.getRegionWidth();
        int ch = region.getRegionHeight();
        FrameBuffer ping = null;

        try {
            while (cw > dstW * 2 || ch > dstH * 2) {
                int nw = Math.max(1, cw / 2);
                int nh = Math.max(1, ch / 2);
                if (ping != null) {
                    ping.dispose();
                }
                ping = new FrameBuffer(Pixmap.Format.RGBA8888, nw, nh, false);
                drawToFbo(batch, ping, src, nw, nh);
                Texture tex = ping.getColorBufferTexture();
                tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
                src = new TextureRegion(tex);
                cw = nw;
                ch = nh;
            }

            FrameBuffer out = new FrameBuffer(Pixmap.Format.RGBA8888, dstW, dstH, false);
            try {
                drawToFbo(batch, out, src, dstW, dstH);
                return readFbo(out, dstW, dstH);
            } finally {
                out.dispose();
            }
        } finally {
            if (ping != null) {
                ping.dispose();
            }
        }
    }

    private static void drawToFbo(SpriteBatch batch, FrameBuffer fbo, TextureRegion region, int w, int h) {
        fbo.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(region, 0f, 0f, w, h);
        batch.end();
        fbo.end();
    }

    private static BufferedImage readFbo(FrameBuffer fbo, int w, int h) {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, w, h);
        try {
            PixelTextures.flipPixmapVertical(pixmap);
            return PixelTextures.pixmapToBufferedImage(pixmap);
        } finally {
            pixmap.dispose();
        }
    }
}
