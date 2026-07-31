package io.github.miraitoto.surfacelight;

/**
 * A named set of per-moon-phase night sky-light levels (index 0 = full moon .. 7).
 *
 * <p>Presets are just a source for the phase array. {@link #CUSTOM} carries no levels
 * of its own — it defers to the user's {@link SurfaceLightConfig#moonPhaseLight} sliders.
 *
 * <p>Spawn note: the overworld spawns hostiles when the effective light is {@code <=} a
 * random 0..7, so a level of 8+ blocks surface spawns entirely. {@link #SAFE_NIGHTS}
 * uses full daylight brightness on every phase for that reason.
 */
public enum MoonPreset {
	/** Full daylight brightness every phase: no hostile surface spawns, any night. */
	SAFE_NIGHTS(new int[] {15, 15, 15, 15, 15, 15, 15, 15}),
	/** Only the full moon is safe (>= 8); nights darken to a pitch-black new moon. */
	FULL_MOON_RESPITE(new int[] {8, 6, 4, 2, 0, 2, 4, 6}),
	/** Every phase safe (>= 8) except the new moon, which goes dark and spawns mobs. */
	NEW_MOON_PROWL(new int[] {12, 11, 10, 9, 0, 9, 10, 11}),
	/** Flat level 4 on every phase: identical to a vanilla night. */
	VANILLA(new int[] {4, 4, 4, 4, 4, 4, 4, 4}),
	/** Uses the eight per-phase sliders in {@link SurfaceLightConfig#moonPhaseLight}. */
	CUSTOM(null);

	private final int[] levels;

	MoonPreset(int[] levels) {
		this.levels = levels;
	}

	/** The per-phase levels for this preset, or {@code null} for {@link #CUSTOM}. */
	public int[] levels() {
		return levels;
	}

	/** Translation key for this preset's display name in the config screen. */
	public String langKey() {
		return "surfacelight.config.preset." + name().toLowerCase(java.util.Locale.ROOT);
	}
}
