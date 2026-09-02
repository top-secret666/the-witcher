package main.java.com.witcher.ui.intro;

import main.java.com.witcher.ui.intro.view.IntroCharacterLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Анимация stranger → duke: дым и золотые искры.
 */
public final class IntroMorphAnimation {

    /** ~0.7 с при 60 FPS (раньше 84 ≈ 1.4 с). */
    public static final int VN_RIGHT_MORPH_TICKS = 42;

    public static final class IntroRect {
        public float x;
        public float y;
        public float width;
        public float height;

        public void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void copyFrom(IntroRect other) {
            set(other.x, other.y, other.width, other.height);
        }

        public void copyFrom(IntroCharacterLayout.Rect other) {
            set(other.x, other.y, other.width, other.height);
        }
    }

    private final List<float[]> morphSmoke = new ArrayList<>();
    private final List<float[]> morphSparks = new ArrayList<>();
    private final Random rng = new Random();

    private boolean rightMorphActive;
    private float rightMorphT;
    private IntroRect morphAnchorBounds;

    public static boolean isStrangerToDukeReveal(String from, String to) {
        return "stranger".equals(from) && "duke".equals(to);
    }

    public boolean isActive() {
        return rightMorphActive;
    }

    public float getMorphT() {
        return rightMorphT;
    }

    public IntroRect getMorphAnchorBounds() {
        return morphAnchorBounds;
    }

    public List<float[]> getMorphSmoke() {
        return morphSmoke;
    }

    public List<float[]> getMorphSparks() {
        return morphSparks;
    }

    public void begin(IntroRect rightCharacterBounds, int sw, int sh, int strangerW, int strangerH) {
        rightMorphActive = true;
        rightMorphT = 0f;
        morphSmoke.clear();
        morphSparks.clear();

        if (rightCharacterBounds != null) {
            morphAnchorBounds = new IntroRect();
            morphAnchorBounds.copyFrom(rightCharacterBounds);
        } else {
            IntroCharacterLayout.Rect estimated =
                IntroCharacterLayout.estimateRightCharacterBounds(sw, sh, strangerW, strangerH);
            morphAnchorBounds = new IntroRect();
            morphAnchorBounds.copyFrom(estimated);
        }

        spawnMorphSmoke(morphAnchorBounds);
        spawnMorphSparks(morphAnchorBounds, 160);
    }

    public void reset() {
        rightMorphActive = false;
        rightMorphT = 0f;
        morphSmoke.clear();
        morphSparks.clear();
        morphAnchorBounds = null;
    }

    /**
     * @return true если морф завершён в этом тике
     */
    public boolean tick(int globalTick) {
        if (!rightMorphActive) {
            return false;
        }

        rightMorphT = Math.min(1f, rightMorphT + 1f / VN_RIGHT_MORPH_TICKS);
        updateMorphSmoke(rightMorphT, globalTick);

        if (rightMorphT >= 1f) {
            rightMorphActive = false;
            morphSmoke.clear();
            morphSparks.clear();
            morphAnchorBounds = null;
            return true;
        }
        return false;
    }

    private void spawnMorphSparks(IntroRect bounds, int count) {
        float cx = bounds.x + bounds.width / 2f;
        float cy = bounds.y + bounds.height / 2f;
        for (int i = 0; i < count; i++) {
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            float speed = 0.8f + rng.nextFloat() * 4.2f;
            float px = cx + (rng.nextFloat() - 0.5f) * bounds.width * 0.75f;
            float py = cy + (rng.nextFloat() - 0.5f) * bounds.height * 0.65f;
            morphSparks.add(new float[]{
                px, py,
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed - 1.2f,
                0,
                22 + rng.nextInt(28),
                1.5f + rng.nextFloat() * 2.5f
            });
        }
    }

    private void spawnMorphSparkBurst(IntroRect bounds, int count) {
        float cx = bounds.x + bounds.width / 2f;
        float cy = bounds.y + bounds.height / 2f;
        for (int i = 0; i < count; i++) {
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            float speed = 1.8f + rng.nextFloat() * 5f;
            morphSparks.add(new float[]{
                cx + (rng.nextFloat() - 0.5f) * bounds.width * 0.35f,
                cy + (rng.nextFloat() - 0.5f) * bounds.height * 0.25f,
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed - 2f,
                0,
                16 + rng.nextInt(20),
                2f + rng.nextFloat() * 3f
            });
        }
    }

    private void spawnMorphSmoke(IntroRect bounds) {
        float cx = bounds.x + bounds.width / 2f;
        float cy = bounds.y + bounds.height / 2f;
        for (int i = 0; i < 520; i++) {
            float px = bounds.x + rng.nextFloat() * bounds.width;
            float py = bounds.y + rng.nextFloat() * bounds.height;
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            float speed = 0.5f + rng.nextFloat() * 3.2f;
            float vx = (float) Math.cos(angle) * speed * 0.65f;
            float vy = -speed * (0.8f + rng.nextFloat() * 1.1f);
            float size = 2f + rng.nextFloat() * 5f;
            int maxLife = 45 + rng.nextInt(55);
            boolean warm = rng.nextFloat() < 0.22f;
            float r = warm ? 120 + rng.nextInt(80) : 38 + rng.nextInt(42);
            float g = warm ? 85 + rng.nextInt(55) : 32 + rng.nextInt(38);
            float b = warm ? 35 + rng.nextInt(35) : 48 + rng.nextInt(55);
            morphSmoke.add(new float[]{px, py, vx, vy, size, 0, maxLife, r, g, b});
        }
        for (int i = 0; i < 90; i++) {
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            float speed = 1.4f + rng.nextFloat() * 3.8f;
            float px = cx + (rng.nextFloat() - 0.5f) * bounds.width * 0.25f;
            float py = cy + (rng.nextFloat() - 0.5f) * bounds.height * 0.2f;
            morphSmoke.add(new float[]{
                px, py,
                (float) Math.cos(angle) * speed * 0.7f,
                (float) Math.sin(angle) * speed * 0.5f - 2f,
                3f + rng.nextFloat() * 4f,
                0, 38 + rng.nextInt(32),
                28 + rng.nextInt(30), 22 + rng.nextInt(24), 38 + rng.nextInt(35)
            });
        }
    }

    private void updateMorphSmoke(float morphT, int tick) {
        if (morphAnchorBounds == null) {
            return;
        }
        float cx = morphAnchorBounds.x + morphAnchorBounds.width / 2f;
        float cy = morphAnchorBounds.y + morphAnchorBounds.height / 2f;
        float converge = IntroEasing.smoothstep(0.42f, 0.92f, morphT);

        morphSmoke.removeIf(p -> p[5] >= p[6]);
        for (float[] p : morphSmoke) {
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.96f;
            p[3] *= 0.96f;
            p[3] -= 0.045f;
            if (converge > 0.01f) {
                p[2] += (cx - p[0]) * converge * 0.04f;
                p[3] += (cy - p[1]) * converge * 0.032f;
            }
            p[5]++;
        }

        morphSparks.removeIf(p -> p[4] >= p[5]);
        for (float[] p : morphSparks) {
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.94f;
            p[3] = p[3] * 0.94f - 0.06f;
            p[4]++;
        }

        if (morphT > 0.1f && morphT < 0.82f && tick % 2 == 0 && morphSmoke.size() < 680) {
            IntroRect b = morphAnchorBounds;
            float px = b.x + rng.nextFloat() * b.width;
            float py = b.y + b.height * (0.45f + rng.nextFloat() * 0.4f);
            morphSmoke.add(new float[]{
                px, py,
                (rng.nextFloat() - 0.5f) * 1.8f,
                -1.8f - rng.nextFloat() * 2.2f,
                2.5f + rng.nextFloat() * 4f,
                0, 40 + rng.nextInt(35),
                32 + rng.nextInt(35), 26 + rng.nextInt(28), 42 + rng.nextInt(40)
            });
        }
        if (morphT > 0.18f && morphT < 0.78f && tick % 2 == 0 && morphSparks.size() < 320) {
            spawnMorphSparkBurst(morphAnchorBounds, 6 + rng.nextInt(6));
        }
    }
}
