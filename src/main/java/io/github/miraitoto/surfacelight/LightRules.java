package io.github.miraitoto.surfacelight;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * The dynamic layer: runtime rules applied on top of vanilla's sky darkness.
 *
 * <p>How surface light works in 1.21.11: the data-driven day timeline animates the
 * {@code minecraft:gameplay/sky_light_level} environment attribute (1.0 by day,
 * 0.2667 = light 4/15 at night). That feeds {@code Level#skyDarken} (0 = noon,
 * 11 = clear-night midnight), and effective light at a position is
 * {@code raw sky light - skyDarken}, which is what mob spawning, crops and
 * daylight sensors read. We adjust the final value here, downstream of both the
 * timeline and the weather, so this works no matter what produced the base value.
 */
public final class LightRules {
	/** Below this, it's day (or dusk barely started) and we leave vanilla alone. */
	private static final int NIGHT_THRESHOLD = 8;

	private LightRules() {
	}

	public static int modifySkyDarken(Level level, int original) {
		SurfaceLightConfig config = SurfaceLightConfig.get();
		if (!config.dynamicLayer || original < NIGHT_THRESHOLD) {
			return original;
		}

		int result = original;

		if (level.isThundering()) {
			result += config.thunderExtraDarken;
		}

		// Next dynamic rules go here, e.g. an advancement-driven bonus
		// (see DESIGN.md: PlayerAdvancements#award hook + payload sync).

		return Mth.clamp(result, 0, 15);
	}
}
