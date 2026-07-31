package io.github.miraitoto.surfacelight.network;

import io.github.miraitoto.surfacelight.SurfaceLightConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server side of config sync: registers the payload and pushes the authoritative config to
 * clients on join, and on demand after a reload or GUI edit (see {@link #broadcast}).
 */
public final class SurfaceLightServerNetworking {
	private SurfaceLightServerNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(SurfaceLightSyncPayload.TYPE, SurfaceLightSyncPayload.CODEC);
		// Every joining client gets the current config, so its rendering matches server rules.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendTo(handler.player));
	}

	/** Send the current server config to one player, if their client can receive it. */
	public static void sendTo(ServerPlayer player) {
		if (ServerPlayNetworking.canSend(player, SurfaceLightSyncPayload.TYPE)) {
			ServerPlayNetworking.send(player, new SurfaceLightSyncPayload(SurfaceLightConfig.toJson()));
		}
	}

	/** Re-send the current config to every connected player, after a reload or change. */
	public static void broadcast(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			sendTo(player);
		}
	}
}
