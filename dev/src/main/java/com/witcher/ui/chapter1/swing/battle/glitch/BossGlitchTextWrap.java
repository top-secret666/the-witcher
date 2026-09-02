package main.java.com.witcher.ui.chapter1.swing.battle.glitch;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

/** Перенос строк для диалоговых блоков глитч-катсцены. */
public final class BossGlitchTextWrap {

  private BossGlitchTextWrap() {
  }

  public static List<String> wrap(String text, FontMetrics fm, int maxW) {
    List<String> out = new ArrayList<>();
    for (String paragraph : text.split("\n", -1)) {
      if (paragraph.isBlank()) {
        out.add("");
        continue;
      }
      String[] words = paragraph.split("\\s+");
      StringBuilder line = new StringBuilder();
      for (String word : words) {
        String trial = line.isEmpty() ? word : line + " " + word;
        if (fm.stringWidth(trial) > maxW && !line.isEmpty()) {
          out.add(line.toString());
          line = new StringBuilder(word);
        } else {
          line = new StringBuilder(trial);
        }
      }
      if (!line.isEmpty()) {
        out.add(line.toString());
      }
    }
    return out;
  }
}
