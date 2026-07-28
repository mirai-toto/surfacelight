package io.github.miraitoto.surfacelight.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Registers the "Config" button shown next to Surface Light in the Mod Menu list.
 * The actual screen is built by {@link SurfaceLightConfigScreen}.
 */
public final class SurfaceLightModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return SurfaceLightConfigScreen::create;
	}
}
