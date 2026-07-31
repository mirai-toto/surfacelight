package io.github.miraitoto.surfacelight.client;

import io.github.miraitoto.surfacelight.SurfaceLightConfig;
import io.github.miraitoto.surfacelight.network.SurfaceLightSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client side of config sync: applies a remote server's config as the render override, and
 * drops it on disconnect so local config (singleplayer, or the next server) takes over.
 */
public final class SurfaceLightClientNetworking {
	private SurfaceLightClientNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(SurfaceLightSyncPayload.TYPE, (payload, context) -> {
			String json = payload.configJson();
			context.client().execute(() -> {
				// Ignore our own integrated server: there the local config is already
				// authoritative and the GUI live-edits it. Only a remote server overrides.
				if (context.client().hasSingleplayerServer()) {
					return;
				}
				SurfaceLightConfig.setClientOverride(SurfaceLightConfig.fromJson(json));
			});
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
				SurfaceLightConfig.clearClientOverride());
	}
}
