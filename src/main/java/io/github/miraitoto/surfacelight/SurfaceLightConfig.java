package io.github.miraitoto.surfacelight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain JSON config in {@code config/surfacelight.json}, edited in-game via Cloth
 * Config + ModMenu ({@link io.github.miraitoto.surfacelight.client.SurfaceLightConfigScreen}).
 *
 * <p>Moon lighting is code-driven ({@link LightRules}): a {@link MoonPreset} chooses the
 * per-phase night light levels, and {@link MoonPreset#CUSTOM} falls back to the eight
 * {@link #moonPhaseLight} sliders.
 */
public final class SurfaceLightConfig {
	/** Master switch. When off, sky light is left completely vanilla. */
	public boolean enabled = true;

	/** Which named set of per-phase light levels to use. CUSTOM uses {@link #moonPhaseLight}. */
	public MoonPreset preset = MoonPreset.CUSTOM;

	/**
	 * Night sky light level per moon phase, indexed 0 (full) .. 7, used when {@link #preset}
	 * is {@link MoonPreset#CUSTOM}. Defaults are symmetric around the cycle: full 6,
	 * gibbous 5, quarter 4 (= vanilla), crescent 3, new 2. 0 = darkest, 15 = full daylight.
	 */
	public int[] moonPhaseLight = defaultMoonPhaseLight();

	/** Extra sky darkness during a thunderstorm at night, on top of vanilla weather dimming. */
	public int thunderDarken = 1;

	/** The per-phase light levels currently in effect: the preset's, or the sliders for CUSTOM. */
	public int[] activeMoonPhaseLight() {
		return preset == MoonPreset.CUSTOM || preset == null ? moonPhaseLight : preset.levels();
	}

	private static int[] defaultMoonPhaseLight() {
		return new int[] {6, 5, 4, 3, 2, 3, 4, 5};
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static SurfaceLightConfig instance;

	public static SurfaceLightConfig get() {
		if (instance == null) {
			load();
		}
		return instance;
	}

	public static void load() {
		Path path = path();
		if (Files.exists(path)) {
			try {
				instance = GSON.fromJson(Files.readString(path), SurfaceLightConfig.class);
			} catch (IOException | JsonParseException e) {
				SurfaceLight.LOGGER.error("Could not read {}, falling back to defaults", path, e);
			}
		}
		if (instance == null) {
			instance = new SurfaceLightConfig();
			save();
		}
		if (instance.moonPhaseLight == null || instance.moonPhaseLight.length != 8) {
			instance.moonPhaseLight = defaultMoonPhaseLight();
		}
		if (instance.preset == null) {
			instance.preset = MoonPreset.CUSTOM;
		}
	}

	public static void save() {
		try {
			Files.writeString(path(), GSON.toJson(get()));
		} catch (IOException e) {
			SurfaceLight.LOGGER.error("Could not write {}", path(), e);
		}
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(SurfaceLight.MOD_ID + ".json");
	}
}
