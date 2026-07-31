package io.github.miraitoto.surfacelight;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;

/**
 * The runtime light rules, applied on top of vanilla's sky darkness.
 *
 * <p>How surface light works in 1.21.11: effective light at a position is
 * {@code raw sky light - skyDarken} ({@code skyDarken} is 0 at noon, ~11 at
 * clear-night midnight), and that effective value is what mob spawning, crops and
 * daylight sensors read. Vanilla's night sky light is a flat ~4 regardless of the
 * moon, so we adjust {@code skyDarken} here to bend it per moon phase (and per
 * weather), which is what makes the effect live-configurable from the GUI.
 */
public final class LightRules {
	/** Below this (in vanilla skyDarken terms) it's day/dusk and we leave vanilla alone. */
	private static final int NIGHT_THRESHOLD = 8;

	/** Vanilla clear-night sky light. A phase set to this value matches vanilla exactly. */
	private static final int VANILLA_NIGHT_LIGHT = 4;

	private LightRules() {
	}

	public static int modifySkyDarken(Level level, int original) {
		// The client renders from the server's synced config (if remote); the server and
		// singleplayer both read the authoritative local config.
		SurfaceLightConfig config = level.isClientSide()
				? SurfaceLightConfig.getForRender()
				: SurfaceLightConfig.get();
		if (!config.enabled || original < NIGHT_THRESHOLD) {
			return original;
		}

		// Moon phase: shift the night light toward the configured level for this phase,
		// relative to vanilla's ~4, so dusk/dawn transitions keep their shape.
		int target = config.activeMoonPhaseLight()[moonPhaseIndex(level)];
		int result = original - (target - VANILLA_NIGHT_LIGHT);

		// Weather effects (each is its own configurable amount).
		if (level.isThundering()) {
			result += config.thunderDarken;
		}

		return Mth.clamp(result, 0, 15);
	}

	/** Current overworld moon phase, 0 (full) .. 7, derived from the world day time. */
	public static int moonPhaseIndex(Level level) {
		return (int) (level.getDayTime() / MoonPhase.PHASE_LENGTH % MoonPhase.COUNT);
	}
}
