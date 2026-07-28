package io.github.miraitoto.surfacelight;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;

/**
 * {@code /surfacelight light} — prints the light values at the caller's position.
 *
 * <p>F3 only shows the raw stored sky/block light (15 in open air, day or night).
 * The value Surface Light actually moves is the <em>effective</em> brightness, i.e.
 * {@code raw sky light - skyDarken}, which is what mob spawning reads. This command
 * surfaces that number so the moon-phase effect is observable.
 */
public final class SurfaceLightCommand {
	private SurfaceLightCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("surfacelight")
				.then(Commands.literal("light")
						.executes(ctx -> reportLight(ctx.getSource()))));
	}

	private static int reportLight(CommandSourceStack source) {
		ServerLevel level = source.getLevel();
		BlockPos pos = BlockPos.containing(source.getPosition());

		int rawSky = level.getBrightness(LightLayer.SKY, pos);
		int rawBlock = level.getBrightness(LightLayer.BLOCK, pos);
		int skyDarken = level.getSkyDarken();
		int effectiveSky = Math.max(0, rawSky - skyDarken);
		int effective = level.getMaxLocalRawBrightness(pos);
		int moonPhase = LightRules.moonPhaseIndex(level);
		String preset = SurfaceLightConfig.get().preset.name();

		source.sendSuccess(() -> Component.literal(String.format(
				"Surface Light @ %d %d %d%n"
						+ "  raw sky %d, raw block %d%n"
						+ "  skyDarken %d -> effective sky %d%n"
						+ "  effective light %d (mob-spawn value)%n"
						+ "  moon phase %d, preset %s",
				pos.getX(), pos.getY(), pos.getZ(),
				rawSky, rawBlock,
				skyDarken, effectiveSky,
				effective,
				moonPhase, preset)), false);

		return effective;
	}
}
