package main.java.com.witcher.ui.intro;

import main.java.com.witcher.ui.intro.view.IntroCharacterLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Частицы и вспышка при смене правого персонажа (не stranger→duke).
 */
public final class IntroSwitchAnimation {

    private final List<float[]> switchParticles = new ArrayList<>();
    private final List<float[]> rightSwitchParticles = new ArrayList<>();
    private final Random rng = new Random();

    private float switchFlash;

    public float getSwitchFlash() {
        return switchFlash;
    }

    public List<float[]> getSwitchParticles() {
        return switchParticles;
    }

    public List<float[]> getRightSwitchParticles() {
        return rightSwitchParticles;
    }

    public void clearFlashAndParticles() {
        switchFlash = 0f;
        switchParticles.clear();
        rightSwitchParticles.clear();
    }

    public void reset() {
        clearFlashAndParticles();
    }

    public void spawnSwitchEffect(IntroCharacterLayout.Rect rightCharacterBounds, int refW, int refH) {
        switchFlash = 1.0f;
        for (int i = 0; i < 30; i++) {
            float px = 0.75f * refW + (rng.nextFloat() - 0.5f) * 90;
            float py = 0.35f * refH + (rng.nextFloat() - 0.5f) * 90;
            float vx = (rng.nextFloat() - 0.5f) * 2.2f;
            float vy = (rng.nextFloat() - 0.5f) * 2.2f;
            float cr = 210 + rng.nextInt(46);
            float cg = 140 + rng.nextInt(80);
            float cb = 30 + rng.nextInt(50);
            switchParticles.add(new float[]{px, py, vx, vy, 0, 28 + rng.nextInt(26), cr, cg, cb});
        }

        rightSwitchParticles.clear();
        if (rightCharacterBounds != null) {
            for (int i = 0; i < 28; i++) {
                float angle = (float) (rng.nextFloat() * Math.PI * 2);
                float radius = rightCharacterBounds.width * 0.5f + rng.nextFloat() * 16;
                float px = rightCharacterBounds.x + rightCharacterBounds.width / 2f
                    + (float) Math.cos(angle) * radius;
                float py = rightCharacterBounds.y + rightCharacterBounds.height / 2f
                    + (float) Math.sin(angle) * radius;
                float vx = (float) Math.cos(angle) * (0.8f + rng.nextFloat() * 1.2f);
                float vy = (float) Math.sin(angle) * (0.8f + rng.nextFloat() * 1.2f);
                rightSwitchParticles.add(new float[]{px, py, vx, vy, 0, 25 + rng.nextInt(25), 1f});
            }
        }
    }

    public void tick() {
        if (switchFlash > 0) {
            switchFlash = Math.max(0f, switchFlash - 0.015f);
        }

        switchParticles.removeIf(p -> p[4] >= p[5]);
        for (float[] p : switchParticles) {
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.96f;
            p[3] *= 0.96f;
            p[4]++;
        }

        rightSwitchParticles.removeIf(p -> p[4] >= p[5]);
        for (float[] p : rightSwitchParticles) {
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.94f;
            p[3] *= 0.94f;
            p[4]++;
        }
    }
}
