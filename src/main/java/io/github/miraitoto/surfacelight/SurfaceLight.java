package io.github.miraitoto.surfacelight;

import io.github.miraitoto.surfacelight.network.SurfaceLightServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurfaceLight implements ModInitializer {
	public static final String MOD_ID = "surfacelight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SurfaceLightConfig.load();
		SurfaceLightServerNetworking.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				SurfaceLightCommand.register(dispatcher));
		LOGGER.info("Surface Light loaded (enabled: {})", SurfaceLightConfig.get().enabled ? "yes" : "no");
	}
}
