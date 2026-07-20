package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.chapter1.battle.BossQuestBriefingController;
import main.java.com.witcher.chapter1.battle.BossQuestBriefingScript;
import main.java.com.witcher.ui.intro.view.IntroCharacterLayout;
import main.java.com.witcher.ui.shop.swing.ShopAssetCache;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Портреты в брифинге — slide-in и pop активного спикера, как в интро (shop-спрайты). */
public final class BriefingCharacterRenderer {

  private BriefingCharacterRenderer() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossQuestBriefingController ctrl) {
    if (ctrl == null) {
      return;
    }

    ShopAssetCache assets = ShopAssetCache.get();
    BossQuestBriefingScript.DialogLine line = ctrl.currentLine();
    boolean dukeActive = line != null && "Герцог".equals(line.speaker());
    boolean dukeEmotion = dukeActive && ctrl.rightActiveAnim() > 0.35f;

    BufferedImage geralt = assets.geraltPortrait();
    BufferedImage dukeBase = assets.dukePortrait();
    BufferedImage dukeSprite = dukeEmotion && assets.dukeLaughPortrait() != null
        ? assets.dukeLaughPortrait()
        : dukeBase;

    drawCharacter(g, sw, sh, geralt, ctrl.geraltSlide(), true,
        false, ctrl.leftActiveAnim(), ctrl.tickCount());
    drawCharacter(g, sw, sh, dukeSprite, ctrl.dukeSlide(), false,
        dukeActive, ctrl.rightActiveAnim(), ctrl.tickCount(),
        dukeEmotion, dukeEmotion);
  }

  private static void drawCharacter(Graphics2D g, int sw, int sh, BufferedImage sprite,
                                    float slide, boolean isLeft, boolean isActive,
                                    float activeAnim, int tick) {
    drawCharacter(g, sw, sh, sprite, slide, isLeft, isActive, activeAnim, tick, false, false);
  }

  private static void drawCharacter(Graphics2D g, int sw, int sh, BufferedImage sprite,
                                    float slide, boolean isLeft, boolean isActive,
                                    float activeAnim, int tick,
                                    boolean liftForShop, boolean raiseAboveOthers) {
    if (sprite == null || slide <= 0.001f) {
      return;
    }

    IntroCharacterLayout.Rect rect = IntroCharacterLayout.computeCharacterRect(
        sw, sh, sprite.getWidth(), sprite.getHeight(),
        slide, isLeft, isActive, activeAnim, tick, liftForShop, raiseAboveOthers);

    Composite prev = g.getComposite();
    float alpha = Math.min(1f, 0.2f + slide * 0.9f);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    Object render = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(sprite, rect.x, rect.y, rect.width, rect.height, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    if (render != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, render);
    }
    g.setComposite(prev);
  }
}
