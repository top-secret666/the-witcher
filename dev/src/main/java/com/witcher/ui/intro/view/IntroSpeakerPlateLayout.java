package main.java.com.witcher.ui.intro.view;

/** Плашка имени говорящего над диалоговым окном — общая геометрия Swing / LibGDX. */
public final class IntroSpeakerPlateLayout {

    public static final class Plate {
        public int boxX;
        public int boxY;
        public int boxW;
        public int boxH;
        public int textX;
        /** Swing Y baseline текста имени. */
        public int textBaselineY;
    }

    private IntroSpeakerPlateLayout() {
    }

    public static int plateSwingTop(int dialogBoxY, int nameTextHeight) {
        return dialogBoxY - nameTextHeight
            - IntroDialogTheme.SPEAKER_NAME_OFFSET_Y
            - IntroDialogTheme.SPEAKER_NAME_LIFT_EXTRA
            - IntroDialogTheme.FRAME_OUTER_OFFSET;
    }

    public static Plate compute(int boxX, int boxY, int pad, int nameTextWidth, int nameTextHeight,
                                int textAscent) {
        Plate p = new Plate();
        p.boxX = boxX + pad - 4;
        p.boxY = plateSwingTop(boxY, nameTextHeight);
        p.boxW = nameTextWidth + IntroDialogTheme.SPEAKER_NAME_BOX_PAD;
        p.boxH = nameTextHeight + 4;
        p.textX = p.boxX + IntroDialogTheme.SPEAKER_NAME_PAD_H;
        p.textBaselineY = p.boxY + textAscent + IntroDialogTheme.SPEAKER_NAME_PAD_V;
        return p;
    }
}
