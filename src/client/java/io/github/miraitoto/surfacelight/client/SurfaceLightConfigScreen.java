package io.github.miraitoto.surfacelight.client;

import io.github.miraitoto.surfacelight.LightRules;
import io.github.miraitoto.surfacelight.MoonPreset;
import io.github.miraitoto.surfacelight.SurfaceLightConfig;
import io.github.miraitoto.surfacelight.network.SurfaceLightServerNetworking;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.impl.builders.FieldBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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

	/** A requirement that is never met, used to lock entries when the server controls the config. */
	private static final Requirement NEVER = () -> false;

	private SurfaceLightConfigScreen() {
	}

	/** Persist the edited config, then re-sync it to any LAN guests if this world is hosted. */
	private static void save() {
		SurfaceLightConfig.save();
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server != null) {
			server.execute(() -> SurfaceLightServerNetworking.broadcast(server));
		}
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
		// On a remote server the config is server-controlled: show its synced values, locked.
		boolean readOnly = SurfaceLightConfig.isServerControlled();
		SurfaceLightConfig config = readOnly ? SurfaceLightConfig.getForRender() : SurfaceLightConfig.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("surfacelight.config.title"))
				// Read-only mode persists nothing: the values shown are the server's, not local config.
				.setSavingRunnable(readOnly ? () -> {} : SurfaceLightConfigScreen::save);

		ConfigEntryBuilder entry = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(
				Component.translatable("surfacelight.config.category.general"));
		if (readOnly) {
			general.addEntry(entry.startTextDescription(
					Component.translatable("surfacelight.config.serverControlled")
							.withStyle(ChatFormatting.YELLOW)).build());
		}
		var enabledEntry = entry
				.startBooleanToggle(Component.translatable("surfacelight.config.enabled"), config.enabled)
				.setTooltip(Component.translatable("surfacelight.config.enabled.tooltip"))
				.setDefaultValue(true)
				.setSaveConsumer(value -> config.enabled = value);
		general.addEntry(lockIf(readOnly, enabledEntry).build());

		ConfigCategory moon = builder.getOrCreateCategory(
				Component.translatable("surfacelight.config.category.moon"));

		moon.addEntry(entry.startTextDescription(currentPhaseText()).build());

		var presetBuilder = entry
				.startEnumSelector(Component.translatable("surfacelight.config.preset"),
						MoonPreset.class, config.preset)
				.setEnumNameProvider(value -> Component.translatable(((MoonPreset) value).langKey()))
				.setTooltip(Component.translatable("surfacelight.config.preset.tooltip"))
				.setDefaultValue(MoonPreset.CUSTOM)
				.setSaveConsumer(value -> config.preset = value);
		EnumListEntry<MoonPreset> presetEntry = lockIf(readOnly, presetBuilder).build();
		moon.addEntry(presetEntry);

		// Editable sliders: the user's own per-phase levels, shown only for CUSTOM.
		Requirement customSelected = Requirement.isValue(presetEntry, MoonPreset.CUSTOM);
		for (int i = 0; i < PHASE_KEYS.length; i++) {
			final int phase = i;
			var sliderEntry = entry
					.startIntSlider(Component.translatable("surfacelight.config.moon." + PHASE_KEYS[i]),
							config.moonPhaseLight[phase], 0, 15)
					.setTextGetter(SurfaceLightConfigScreen::phaseSliderLabel)
					.setTooltip(Component.translatable("surfacelight.config.moon.tooltip"))
					.setDefaultValue(PHASE_DEFAULTS[phase])
					.setSaveConsumer(value -> config.moonPhaseLight[phase] = value)
					.setDisplayRequirement(customSelected);
			moon.addEntry(lockIf(readOnly, sliderEntry).build());
		}

		// For each named preset, a matching row of read-only sliders that reveal its fixed
		// levels. Only the selected preset's row shows, and it updates live as the dropdown
		// changes (display requirements are polled), so you can preview any preset's phases.
		for (MoonPreset preset : MoonPreset.values()) {
			if (preset == MoonPreset.CUSTOM) {
				continue;
			}
			Requirement presetSelected = Requirement.isValue(presetEntry, preset);
			int[] levels = preset.levels();
			for (int i = 0; i < PHASE_KEYS.length; i++) {
				moon.addEntry(entry
						.startIntSlider(Component.translatable("surfacelight.config.moon." + PHASE_KEYS[i]),
								levels[i], 0, 15)
						.setTextGetter(SurfaceLightConfigScreen::phaseSliderLabel)
						.setTooltip(Component.translatable("surfacelight.config.moon.preset.tooltip"))
						.setDefaultValue(levels[i])
						.setRequirement(NEVER)
						.setDisplayRequirement(presetSelected)
						.build());
			}
		}

		ConfigCategory weather = builder.getOrCreateCategory(
				Component.translatable("surfacelight.config.category.weather"));
		var thunderEntry = entry
				.startIntSlider(Component.translatable("surfacelight.config.thunder"), config.thunderDarken, 0, 15)
				.setTooltip(Component.translatable("surfacelight.config.thunder.tooltip"))
				.setDefaultValue(1)
				.setSaveConsumer(value -> config.thunderDarken = value);
		weather.addEntry(lockIf(readOnly, thunderEntry).build());

		return builder.build();
	}

	/** Disable an entry (greyed, non-editable) when the server controls the config. */
	private static <B extends FieldBuilder<?, ?, B>> B lockIf(boolean readOnly, B builder) {
		if (readOnly) {
			builder.setRequirement(NEVER);
		}
		return builder;
	}
}
