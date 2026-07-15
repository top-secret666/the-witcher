# Chapter 1 battle card — asset prompts

Place generated files in `src/main/resources/assets/sprites/chapter1/battle/`.

Paths are defined in `Chapter1AssetPaths.java`.

| File | Size | Prompt |
|------|------|--------|
| `card_icon.png` | 32×32 | Small pixel art folded battle map icon, dark red wax seal, gold border, Witcher medieval shop UI style, transparent background, no text |
| `card_closed.png` | 128×168 | Pixel art closed treasure map card, aged parchment, red wax seal, gold filigree corners, top-down view, no text |
| `card_map_open.png` | 480×360 | Pixel art opened medieval battle map fullscreen, dark fantasy castle region, ink paths, marked X spots for bosses, muted browns and gold, no text no labels |
| `boss_duke_map.png` | 40×40 | Tiny pixel art boss marker icon, armored duke silhouette with crown, red and gold, transparent background |
| `boss_duke_portrait.png` | 120×140 | Pixel art portrait bust of medieval duke lord, unsettling polite smile, dark fantasy Witcher style, gold frame crop, no text |

**Negative prompt for all:** no text, no letters, no watermark, crisp pixel edges, no photorealistic.

## Code wiring

| Asset | Used in |
|-------|---------|
| `card_icon.png` | `BattleCardRevealView.drawCardIcon` — HUD after card reveal (`Chapter1Screen`) |
| `card_closed.png` | `BattleCardRevealView.draw` — Duke grant animation |
| `card_map_open.png` | `BossMapView.draw` — fullscreen boss map |
| `boss_duke_map.png` | `BossCatalog` → `BossMapView` marker |
| `boss_duke_portrait.png` | `BossMapView.drawBossPanel` hover/selected panel |

**Flow (canon):** equip → shop `BATTLE_CARD_REVEAL` (like wallet) → card icon in bag → click → `BOSS_MAP` → click Duke → `LOOP_SEQUENCE` (loop_wake) → `BOSS_ENCOUNTER` → `SWORD_CUTSCENE` → `BATTLE_RESULT` → `SHOP`.

Hack `BREAK_LOOP` success → `LOOP_SEQUENCE` (legacy prelude; not the cinematic bag path).
