package main.java.com.witcher.release;

import main.java.com.witcher.ui.graphics.GameWindow;

/** EXE entry point — enables release-only overlays without touching dev run.bat. */
public final class ReleaseBootstrap {

    static {
        System.setProperty("witcher.release.build", "true");
    }

    private ReleaseBootstrap() {
    }

    public static void main(String[] args) {
        GameWindow.main(args);
    }
}
