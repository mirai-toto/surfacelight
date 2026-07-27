# Surface Light

A Fabric mod for Minecraft **1.21.11**: surface (sky) light that follows the world.

Right now that means **moonlight** — nights get brighter or darker with the moon phase — plus a small dynamic layer for things data can't express (currently: extra darkness during night thunderstorms). Weather, advancements and an in-game settings screen are on the roadmap.

Night surface light by phase: **full 6 · gibbous 5 · quarter 4 (vanilla) · crescent 3 · new 2**. Days are untouched.

## Requirements

- JDK 21 (Temurin works well)
- Gradle comes with the wrapper — no separate install

The first build downloads Minecraft, mappings and Fabric from Mojang/FabricMC. Behind a corporate proxy (e.g. Zscaler), set `systemProp.https.proxyHost/Port` in `~/.gradle/gradle.properties`.

## Build & run

```bash
./gradlew runClient   # dev client with the mod loaded
./gradlew build       # mod jar in build/libs/
./gradlew genSources  # decompiled, Mojang-mapped Minecraft sources for browsing
```

## Config

`config/surfacelight.json` (created on first run):

- `dynamicLayer` — master switch for the code-driven layer
- `thunderExtraDarken` — extra sky darkness during a night thunderstorm

## Testing in game

```
/time set night     # jump to night
/time add 24000     # advance a day = next moon phase
/weather thunder    # exercise the dynamic layer
```

F3 → "Client Light" sky value at your feet. Hostile spawns follow it (`monster_spawn_light_level` is 0–7 in the overworld).

## How it works & roadmap

See **[DESIGN.md](DESIGN.md)** for the 1.21.11 lighting pipeline this is built on, the full feature map, and the multi-version strategy.

## License

[MIT](LICENSE).
