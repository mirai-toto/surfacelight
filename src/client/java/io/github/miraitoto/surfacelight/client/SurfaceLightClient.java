package io.github.miraitoto.surfacelight.client;

import net.fabricmc.api.ClientModInitializer;

public class SurfaceLightClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Apply the server's synced config on remote connections so night brightness on
		// screen matches the rules the server enforces for mob spawning.
		SurfaceLightClientNetworking.register();
	}
}
