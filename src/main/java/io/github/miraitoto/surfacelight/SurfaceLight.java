package io.github.miraitoto.surfacelight;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurfaceLight implements ModInitializer {
	public static final String MOD_ID = "surfacelight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SurfaceLightConfig.load();
		LOGGER.info("Surface Light loaded (dynamic layer: {})", SurfaceLightConfig.get().dynamicLayer ? "on" : "off");
	}
}
