package main.java.com.witcher.chapter1.ending;

/** Строки быстрых титров демо — лесенка + контакты на финальном экране. */
public final class DemoEndingCredits {

  public static final String DIRECTOR_NICK = "TOP-SECRET666";
  public static final String GITHUB_URL = "https://github.com/top-secret666/the-witcher";
  public static final String LINKEDIN_URL =
      "https://www.linkedin.com/in/dana-stukalova-1b7061330/?isSelfProfile=true";
  public static final String TELEGRAM_LABEL = "Telegram: @IKnowAllYourSecretss";

  private static final String[] LINES = {
      "THE WITCHER",
      "CORE LOGIC ENGINE",
      "",
      "A DEMO BY",
      DIRECTOR_NICK,
      "",
      "Directed by",
      DIRECTOR_NICK,
      "",
      "Written by",
      DIRECTOR_NICK,
      "",
      "Game Design",
      DIRECTOR_NICK,
      "",
      "Programming",
      DIRECTOR_NICK,
      "",
      "UI / UX",
      DIRECTOR_NICK,
      "",
      "Combat Systems",
      DIRECTOR_NICK,
      "",
      "Architecture",
      DIRECTOR_NICK,
      "",
      "Art Direction",
      DIRECTOR_NICK,
      "",
      "QA",
      DIRECTOR_NICK,
      "",
      "Special Thanks",
      "You",
      "",
      GITHUB_URL,
      "",
      "Thanks for playing"
  };

  private DemoEndingCredits() {
  }

  public static String[] lines() {
    return LINES;
  }
}
