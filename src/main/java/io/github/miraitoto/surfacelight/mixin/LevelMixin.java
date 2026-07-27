package io.github.miraitoto.surfacelight.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.miraitoto.surfacelight.LightRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public abstract class LevelMixin {
	/**
	 * {@code skyDarken} is recomputed every tick by {@code Level#updateSkyBrightness()}
	 * and read through this getter by all effective-light checks. The explicit
	 * {@code ()I} descriptor matters: a float overload for rendering may exist.
	 *
	 * <p>If Mixin ever reports this target as missing after a Minecraft update, run
	 * {@code ./gradlew genSources} and check the current name on
	 * {@code net.minecraft.world.level.Level}.
	 */
	@ModifyReturnValue(method = "getSkyDarken()I", at = @At("RETURN"))
	private int surfacelight$modifySkyDarken(int original) {
		return LightRules.modifySkyDarken((Level) (Object) this, original);
	}
}
