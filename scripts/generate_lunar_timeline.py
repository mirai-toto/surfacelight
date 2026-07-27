#!/usr/bin/env python3
"""Generate data/surfacelight/timeline/lunar_light.json.

The timeline spans one lunar cycle (8 days = 192000 ticks) and multiplies
vanilla's gameplay/sky_light_level. Daytime multiplier stays at 1.0 so days
are untouched; the night multiplier depends on the moon phase.

Vanilla reference (1.21.11, data/minecraft/timeline/day.json):
  - sky_light_level ramps at ticks 133 -> 11867 (day) and 13670 -> 22330 (night)
  - night value is 0.26666668 (= surface light 4/15)
We reuse the same ramp ticks so our transitions line up with vanilla's.

Resulting night surface light with the defaults below:
  full 6, gibbous 5, quarter 4 (vanilla), crescent 3, new 2.

Run from the repo root:  python3 scripts/generate_lunar_timeline.py
"""
import json
from pathlib import Path

DAY_TICKS = 24000
# tick 0 of the moon timeline = full moon (see vanilla data/minecraft/timeline/moon.json)
PHASE_NIGHT_MULTIPLIER = [
    1.5,   # full moon        -> 0.2667 * 1.5  = 0.4  -> light 6
    1.25,  # waning gibbous   -> light 5
    1.0,   # third quarter    -> light 4 (vanilla)
    0.75,  # waning crescent  -> light 3
    0.5,   # new moon         -> 0.2667 * 0.5  = 0.133 -> light 2
    0.75,  # waxing crescent
    1.0,   # first quarter
    1.25,  # waxing gibbous
]
DAWN_END, DUSK_START, DUSK_END, DAWN_START = 133, 11867, 13670, 22330

keyframes = []
for day, mult in enumerate(PHASE_NIGHT_MULTIPLIER):
    base = day * DAY_TICKS
    keyframes += [
        {"ticks": base + DAWN_END, "value": 1.0},
        {"ticks": base + DUSK_START, "value": 1.0},
        {"ticks": base + DUSK_END, "value": mult},
        {"ticks": base + DAWN_START, "value": mult},
    ]

timeline = {
    "period_ticks": DAY_TICKS * len(PHASE_NIGHT_MULTIPLIER),
    "tracks": {
        "minecraft:gameplay/sky_light_level": {
            "keyframes": keyframes,
            "modifier": "multiply",
        }
    },
}

out = Path(__file__).resolve().parent.parent / "src/main/resources/data/surfacelight/timeline/lunar_light.json"
out.write_text(json.dumps(timeline, indent="\t") + "\n")
print(f"wrote {out} ({len(keyframes)} keyframes)")
