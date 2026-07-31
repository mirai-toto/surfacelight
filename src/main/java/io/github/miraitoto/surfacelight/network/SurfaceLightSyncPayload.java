package io.github.miraitoto.surfacelight.network;

import io.github.miraitoto.surfacelight.SurfaceLight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client packet carrying the authoritative {@link io.github.miraitoto.surfacelight.SurfaceLightConfig}.
 *
 * <p>The config is sent as its on-disk JSON string rather than a per-field codec, so new
 * config fields sync automatically without touching this class.
 */
public record SurfaceLightSyncPayload(String configJson) implements CustomPacketPayload {
	public static final Type<SurfaceLightSyncPayload> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(SurfaceLight.MOD_ID, "config_sync"));

	public static final StreamCodec<FriendlyByteBuf, SurfaceLightSyncPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, SurfaceLightSyncPayload::configJson,
					SurfaceLightSyncPayload::new);

	@Override
	public Type<SurfaceLightSyncPayload> type() {
		return TYPE;
	}
}
