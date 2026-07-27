package io.github.miraitoto.surfacelight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain JSON config in {@code config/surfacelight.json}.
 *
 * <p>The static moon cycle lives in the bundled datapack
 * ({@code data/surfacelight/timeline/lunar_light.json}); this config only drives the
 * dynamic layer, i.e. rules that data can't express (weather extras, advancement
 * state, per-server tuning). An in-game screen can come later via Cloth Config +
 * ModMenu, see DESIGN.md.
 */
public final class SurfaceLightConfig {
	/** Master switch for the code-driven (mixin) layer. The datapack layer is unaffected. */
	public boolean dynamicLayer = true;

	/** Extra sky darkness during a thunderstorm at night, on top of vanilla weather dimming. */
	public int thunderExtraDarken = 1;

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
