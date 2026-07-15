package main.java.com.witcher.ui.shop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Общие правила сопоставления названий доспехов → файлов иконок. */
public final class ArmourIconMap {

    public static final String MAP_PATH = "/armor_icon_map.properties";

    public record Rule(String keyword, String fileName) {
    }

    private ArmourIconMap() {
    }

    public static List<Rule> loadRules() {
        List<Rule> out = new ArrayList<>();
        try (InputStream in = ArmourIconMap.class.getResourceAsStream(MAP_PATH)) {
            if (in == null) {
                System.err.println("Armour icons: map not found at " + MAP_PATH);
                return out;
            }
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.strip();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int sep = line.indexOf('=');
                    if (sep <= 0) {
                        continue;
                    }
                    String keyword = line.substring(0, sep).strip().toLowerCase(Locale.ROOT);
                    String file = line.substring(sep + 1).strip();
                    if (!keyword.isEmpty() && !file.isEmpty()) {
                        out.add(new Rule(keyword, file));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load armour icon map", e);
        }
        out.sort(Comparator.comparingInt((Rule rule) -> rule.keyword.length()).reversed());
        return out;
    }

    public static Set<String> distinctIconFiles() {
        Set<String> files = new HashSet<>();
        for (Rule rule : loadRules()) {
            files.add(rule.fileName);
        }
        return files;
    }

    public static String resolveFile(String armourName, List<Rule> rules) {
        if (armourName == null || armourName.isBlank()) {
            return null;
        }
        String lower = armourName.toLowerCase(Locale.ROOT);
        for (Rule rule : rules) {
            if (lower.contains(rule.keyword)) {
                return rule.fileName;
            }
        }
        return null;
    }

    public static boolean matchesCategory(String fileName, ShopCategory category) {
        if (category == null) {
            return true;
        }
        return switch (category) {
            case CHEST -> isChestIcon(fileName);
            case LEGS -> isLegsIcon(fileName);
            case GLOVES -> isGlovesIcon(fileName);
            case BOOTS -> isBootsIcon(fileName);
            case SETS -> isSetIcon(fileName);
            case POTION -> isPotionIcon(fileName);
            case WEAPON -> isWeaponIcon(fileName);
        };
    }

    private static boolean isGlovesIcon(String fileName) {
        Integer kitSlot = kitPieceIndex(fileName);
        if (kitSlot != null) {
            return kitSlot == 3;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("gauntlets") || lower.contains("gloves");
    }

    private static boolean isBootsIcon(String fileName) {
        Integer kitSlot = kitPieceIndex(fileName);
        if (kitSlot != null) {
            return kitSlot == 4;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("boots") || lower.contains("shoes") || lower.contains("slippers");
    }

    private static boolean isSetIcon(String fileName) {
        if (kitPieceIndex(fileName) != null) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("school") || lower.contains("szkola") || lower.contains("szkoła")
            || lower.contains("cechu") || lower.contains("kit") || lower.contains("set_of")
            || lower.contains("the_set");
    }

    /** kits/Name-01..04 → 1 кираса, 2 штаны, 3 перчатки, 4 сапоги. */
    private static Integer kitPieceIndex(String fileName) {
        if (fileName == null) {
            return null;
        }
        String lower = fileName.toLowerCase(Locale.ROOT).replace('\\', '/');
        if (!lower.contains("kits/")) {
            return null;
        }
        if (lower.endsWith("-01.png")) {
            return 1;
        }
        if (lower.endsWith("-02.png")) {
            return 2;
        }
        if (lower.endsWith("-03.png")) {
            return 3;
        }
        if (lower.endsWith("-04.png")) {
            return 4;
        }
        return null;
    }

    private static boolean isPotionIcon(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("potion") || lower.contains("elixir") || lower.contains("decoction");
    }

    private static boolean isWeaponIcon(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("sword") || lower.contains("crossbow") || lower.contains("laymore");
    }

    private static boolean isLegsIcon(String fileName) {
        Integer kitSlot = kitPieceIndex(fileName);
        if (kitSlot != null) {
            return kitSlot == 2;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("trousers")
            || lower.contains("breeches")
            || lower.contains("sharovary");
    }

    private static boolean isChestIcon(String fileName) {
        Integer kitSlot = kitPieceIndex(fileName);
        if (kitSlot != null) {
            return kitSlot == 1;
        }
        if (isLegsIcon(fileName)) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.startsWith("chest_")
            || lower.contains("armor")
            || lower.contains("cuirass")
            || lower.contains("aketon")
            || lower.contains("gambeson")
            || lower.contains("halberdier");
    }
}
