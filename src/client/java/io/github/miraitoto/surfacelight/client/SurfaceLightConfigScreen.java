package io.github.miraitoto.surfacelight.client;

import io.github.miraitoto.surfacelight.LightRules;
import io.github.miraitoto.surfacelight.MoonPreset;
import io.github.miraitoto.surfacelight.SurfaceLightConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Builds the Cloth Config screen for Surface Light. Entries write straight to the
 * live {@link SurfaceLightConfig} singleton on save, then {@link SurfaceLightConfig#save()}
 * persists it to {@code config/surfacelight.json}.
 */
public final class SurfaceLightConfigScreen {
	/** Lang key suffixes for the eight moon phases, in index order (0 = full moon). */
	private static final String[] PHASE_KEYS = {
			"full", "waning_gibbous", "third_quarter", "waning_crescent",
			"new", "waxing_crescent", "first_quarter", "waxing_gibbous"
	};

	/** Design defaults per phase, used by each slider's reset button. */
	private static final int[] PHASE_DEFAULTS = {6, 5, 4, 3, 2, 3, 4, 5};

	/** At or above this sky-light level the 0-7 spawn roll can never succeed: no surface spawns. */
	private static final int SPAWN_SAFE_LIGHT = 8;

	private SurfaceLightConfigScreen() {
	}

	/** Slider value label: the light level plus a live green/red spawn verdict. */
	private static Component phaseSliderLabel(int light) {
		boolean safe = light >= SPAWN_SAFE_LIGHT;
		return Component.translatable(
						safe ? "surfacelight.config.moon.value.safe" : "surfacelight.config.moon.value.spawn", light)
				.withStyle(safe ? ChatFormatting.GREEN : ChatFormatting.RED);
	}

	/** A read-only line naming the moon phase in the loaded world, or a hint if none. */
	private static Component currentPhaseText() {
		Level level = Minecraft.getInstance().level;
		if (level == null) {
			return Component.translatable("surfacelight.config.currentPhase.unknown");
		}
		int phase = LightRules.moonPhaseIndex(level);
		return Component.translatable("surfacelight.config.currentPhase",
				Component.translatable("surfacelight.config.moon." + PHASE_KEYS[phase]), phase);
	}

	public static Screen create(Screen parent) {
		SurfaceLightConfig config = SurfaceLightConfig.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("surfacelight.config.title"))
				.setSavingRunnable(SurfaceLightConfig::save);

		ConfigEntryBuilder entry = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(
				Component.translatable("surfacelight.config.category.general"));
		general.addEntry(entry
				.startBooleanToggle(Component.translatable("surfacelight.config.enabled"), config.enabled)
				.setTooltip(Component.translatable("surfacelight.config.enabled.tooltip"))
				.setDefaultValue(true)
				.setSaveConsumer(value -> config.enabled = value)
				.build());

		ConfigCategory moon = builder.getOrCreateCategory(
				Component.translatable("surfacelight.config.category.moon"));

		moon.addEntry(entry.startTextDescription(currentPhaseText()).build());

		EnumListEntry<MoonPreset> presetEntry = entry
				.startEnumSelector(Component.translatable("surfacelight.config.preset"),
						MoonPreset.class, config.preset)
				.setEnumNameProvider(value -> Component.translatable(((MoonPreset) value).langKey()))
				.setTooltip(Component.translatable("surfacelight.config.preset.tooltip"))
				.setDefaultValue(MoonPreset.CUSTOM)
				.setSaveConsumer(value -> config.preset = value)
				.build();
		moon.addEntry(presetEntry);

		// The manual sliders only make sense for CUSTOM, so hide them otherwise.
		Requirement customSelected = Requirement.isValue(presetEntry, MoonPreset.CUSTOM);
		for (int i = 0; i < PHASE_KEYS.length; i++) {
			final int phase = i;
			moon.addEntry(entry
					.startIntSlider(Component.translatable("surfacelight.config.moon." + PHASE_KEYS[i]),
							config.moonPhaseLight[phase], 0, 15)
					.setTextGetter(SurfaceLightConfigScreen::phaseSliderLabel)
					.setTooltip(Component.translatable("surfacelight.config.moon.tooltip"))
					.setDefaultValue(PHASE_DEFAULTS[phase])
					.setSaveConsumer(value -> config.moonPhaseLight[phase] = value)
					.setDisplayRequirement(customSelected)
					.build());
		}

		ConfigCategory weather = builder.getOrCreateCategory(
				Component.translatable("surfacelight.config.category.weather"));
		weather.addEntry(entry
				.startIntSlider(Component.translatable("surfacelight.config.thunder"), config.thunderDarken, 0, 15)
				.setTooltip(Component.translatable("surfacelight.config.thunder.tooltip"))
				.setDefaultValue(1)
				.setSaveConsumer(value -> config.thunderDarken = value)
				.build());

		return builder.build();
	}
}
