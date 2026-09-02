package main.java.com.witcher.ui.intro.view;

/** Раскладка окна истории — общая геометрия Swing / LibGDX. */
public final class IntroHistoryLayout {

    public static final int SCROLL_STEP_PX = 18;

    public static final class Metrics {
        public final int fontSize;
        public final int titleSize;
        public final int hintSize;
        public final int lineH;
        public final int pad;
        public final float textX;
        public final float textMaxW;
        public final int titleBaseline;
        public final int headerBottom;
        public final int hintBaseline;
        public final int footerTop;
        public final int contentTop;
        public final int contentBottom;
        public final int contentH;
        public final int bodyAscent;
        public final int bodyDescent;
        public final int hintHeight;

        Metrics(int fontSize, int titleSize, int hintSize, int lineH, int pad,
                float textX, float textMaxW, int titleBaseline, int headerBottom,
                int hintBaseline, int footerTop, int contentTop, int contentBottom,
                int contentH, int bodyAscent, int bodyDescent, int hintHeight) {
            this.fontSize = fontSize;
            this.titleSize = titleSize;
            this.hintSize = hintSize;
            this.lineH = lineH;
            this.pad = pad;
            this.textX = textX;
            this.textMaxW = textMaxW;
            this.titleBaseline = titleBaseline;
            this.headerBottom = headerBottom;
            this.hintBaseline = hintBaseline;
            this.footerTop = footerTop;
            this.contentTop = contentTop;
            this.contentBottom = contentBottom;
            this.contentH = contentH;
            this.bodyAscent = bodyAscent;
            this.bodyDescent = bodyDescent;
            this.hintHeight = hintHeight;
        }
    }

    private IntroHistoryLayout() {
    }

    public static Metrics compute(int sw, int sh, float panelX, float panelY,
                                  float panelW, float panelH) {
        int fontSize = IntroHistoryTheme.fontSize(sh);
        int titleSize = fontSize + IntroHistoryTheme.TITLE_SIZE_DELTA;
        int hintSize = Math.max(IntroHistoryTheme.HINT_SIZE_MIN, fontSize + IntroHistoryTheme.HINT_SIZE_DELTA);
        int lineH = fontSize + IntroHistoryTheme.LINE_GAP;
        int pad = IntroHistoryTheme.pad(sw);
        float textX = panelX + pad;
        float textMaxW = panelW - pad * 2f;

        int titleAscent = IntroTextLayout.fontAscent(titleSize);
        int titleDescent = IntroTextLayout.fontDescent(titleSize);
        int hintAscent = IntroTextLayout.fontAscent(hintSize);
        int hintDescent = IntroTextLayout.fontDescent(hintSize);
        int bodyAscent = IntroTextLayout.fontAscent(fontSize);
        int bodyDescent = IntroTextLayout.fontDescent(fontSize);
        int hintHeight = hintAscent + hintDescent;

        int py = Math.round(panelY);
        int ph = Math.round(panelH);
        int titleBaseline = py + pad + titleAscent;
        int headerBottom = titleBaseline + titleDescent + IntroHistoryTheme.HEADER_GAP;
        int hintBaseline = py + ph - pad;
        int footerTop = hintBaseline - hintHeight - IntroHistoryTheme.FOOTER_GAP;
        int contentTop = headerBottom;
        int contentBottom = footerTop;
        int contentH = Math.max(0, contentBottom - contentTop);

        return new Metrics(fontSize, titleSize, hintSize, lineH, pad, textX, textMaxW,
            titleBaseline, headerBottom, hintBaseline, footerTop, contentTop, contentBottom,
            contentH, bodyAscent, bodyDescent, hintHeight);
    }

    public static int maxScroll(int renderedLineCount, int lineH, int contentH) {
        return Math.max(0, renderedLineCount * lineH - contentH);
    }

    public static int clampScroll(int scroll, int maxScroll) {
        return Math.min(Math.max(0, scroll), maxScroll);
    }
}
