# Surface Light

Surface Light is a Fabric mod that varies night-time sky light by moon phase.

Nights get brighter or darker with the moon: a full moon lights the surface, a new moon leaves it dark. Because that changes the effective sky light, it also decides whether hostile mobs spawn on the surface. Everything is configurable in-game (Mod Menu + Cloth Config), with ready-made presets plus a weather rule that darkens night thunderstorms.

## Presets

Pick a rule set in the config screen, or choose Custom and set each phase yourself:

- **Safe Nights**: every phase is bright enough (>=8) that no hostiles spawn on the surface.
- **Full Moon Respite**: only the full moon is safe; nights darken to a pitch-black new moon.
- **New Moon Prowl**: every night is safe except the new moon.
- **Vanilla**: unchanged vanilla night light.
- **Custom**: eight per-phase sliders (0-15).

Spawn note: the overworld rolls a random 0-7 and spawns a mob when the light is `<=` that roll, so **sky light 8 or above is the "no surface spawns" line**. The sliders flag this live (green at 8+, red below).

## Requirements

- Minecraft 1.21.11 (Fabric) + Fabric API
- Optional, for the config screen: Mod Menu + Cloth Config

## In-game

`/surfacelight light` prints the raw sky/block, effective light, and current moon phase at your feet. The effective value is the one mob spawning uses; F3's sky number stays 15 outdoors and won't reflect the moon.

```
/time set night     # jump to night
/time add 24000     # advance one day = next moon phase
/weather thunder    # exercise the thunderstorm rule
```

## Config

Edit via the Mod Menu screen, or by hand in `config/surfacelight.json`:

- `enabled`: master switch; off = vanilla sky light
- `preset`: `SAFE_NIGHTS`, `FULL_MOON_RESPITE`, `NEW_MOON_PROWL`, `VANILLA`, or `CUSTOM`
- `moonPhaseLight`: eight per-phase levels used when preset is `CUSTOM` (index 0 = full ... 4 = new)
- `thunderDarken`: extra darkness during a night thunderstorm

## Build

```bash
./gradlew runClient   # dev client with the mod loaded
./gradlew build       # mod jar in build/libs/
```

The first build downloads Minecraft, mappings and Fabric from Mojang/FabricMC.

## How it works

See **[DESIGN.md](DESIGN.md)** for the 1.21.11 lighting pipeline this builds on.

## License

[MIT](LICENSE).
