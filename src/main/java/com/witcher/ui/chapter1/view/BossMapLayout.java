package main.java.com.witcher.ui.chapter1.view;

import main.java.com.witcher.chapter1.battle.BossCatalog;
import main.java.com.witcher.chapter1.battle.BossEntry;
import main.java.com.witcher.ui.graphics.UiChrome;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Геометрия иконок боссов на карте (без отрисовки). */
public final class BossMapLayout {

  public static final int BOSS_ICON = 40;

  public record BossHit(BossEntry boss, Rectangle bounds) {
  }

  private BossMapLayout() {
  }

  public static Rectangle backButton(int sw, int sh) {
    return new Rectangle(10, 10, UiChrome.BTN_SIZE, UiChrome.BTN_SIZE);
  }

  public static List<BossHit> layoutHits(int sw, int sh) {
    float sx = sw / (float) Chapter1ViewConstants.VIRTUAL_W;
    float sy = sh / (float) Chapter1ViewConstants.VIRTUAL_H;
    List<BossHit> hits = new ArrayList<>();
    for (BossEntry boss : BossCatalog.all()) {
      int x = Math.round(boss.mapX() * sx) - BOSS_ICON / 2;
      int y = Math.round(boss.mapY() * sy) - BOSS_ICON / 2;
      hits.add(new BossHit(boss, new Rectangle(x, y, BOSS_ICON, BOSS_ICON)));
    }
    return hits;
  }

  public static BossEntry hitBoss(List<BossHit> hits, int mx, int my) {
    if (hits == null) {
      return null;
    }
    for (BossHit hit : hits) {
      if (hit.bounds().contains(mx, my)) {
        return hit.boss();
      }
    }
    return null;
  }
}
