package tektonikal.aimutils;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Random;

public class Config {
	public static final ConfigClassHandler<Config> CONFIG = ConfigClassHandler.createBuilder(Config.class)
			.serializer(configConfigClassHandler -> GsonConfigSerializerBuilder.create(configConfigClassHandler)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("aimutils.json"))
					.build()).build();
	@SerialEntry
	public double minRandomSens;
	@SerialEntry
	public double maxRandomSens;

	public static Random rand = new Random();

	public static double SafeRandom(double min, double max) {
		if (min == max) {
			return min;
		} else {
			double midpoint = Math.max(min, max) / 2 + Math.min(min, max) / 2;
			double half_range = Math.max(min, max) / 2 - Math.min(min, max) / 2;
			int plus_minus = rand.nextBoolean() ? 1 : -1;
			return midpoint + plus_minus * rand.nextFloat() * half_range;
		}
	}

	public static Screen getConfigScreen(Screen parent) {
		return YetAnotherConfigLib.create(CONFIG, (defaults, config, builder) -> builder
				.title(Text.of("Aim Utils"))
				.category(ConfigCategory.createBuilder()
						.name(Text.of("Aim Utils"))
						.group(OptionGroup.createBuilder()
								.name(Text.of("Precise Sensitivity"))
								.option(Option.<Double>createBuilder()
										.name(Text.of("Precise Sensitivity Input"))
										.description(OptionDescription.of(Text.of("Range [0, 1] corresponds to in-game [0%, 200%]. Do not worry about the value being rounded to the nearest 0.005, it actually gets applied!")))
										.controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(0d).max(1d).formatValue(value -> Text.of(String.format("%.1f", value * 200d) + "%")))
										.binding(Binding.minecraft(MinecraftClient.getInstance().options.getMouseSensitivity()))
										.build())
								.build())
						.group(OptionGroup.createBuilder()
								.name(Text.of("Sensitivity Randomization"))
								.option(Option.<Double>createBuilder()
										.name(Text.of("Min Random Sensitivity"))
										.description(OptionDescription.of(Text.of("Range [0, 1] corresponds to in-game [0%, 200%]. Gets rounded to the nearest 0.00001.")))
										.controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(0d).max(1d).formatValue(value -> Text.of(String.format("%.1f", value * 200d) + "%")))
										.stateManager(StateManager.createInstant(0.25d, () -> CONFIG.instance().minRandomSens, newVal -> CONFIG.instance().minRandomSens = newVal))
										.build())
								.option(Option.<Double>createBuilder()
										.name(Text.of("Max Random Sensitivity"))
										.description(OptionDescription.of(Text.of("Range [0, 1] corresponds to in-game [0%, 200%]. Gets rounded to the nearest 0.005.")))
										.controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).formatValue(value -> Text.of(String.format("%.1f", value * 200d) + "%")))
										.stateManager(StateManager.createInstant(0.75d, () -> CONFIG.instance().maxRandomSens, newVal -> CONFIG.instance().maxRandomSens = newVal))
										.build())
								.option(ButtonOption.createBuilder()
										.name(Text.of("Randomize Sensitivity"))
										.text(Text.of("Randomize"))
										.description(OptionDescription.of(Text.of("Set the mouse sensitivity to a random value between min and max")))
										.action((yaclScreen, buttonOption) -> MinecraftClient.getInstance().options.getMouseSensitivity().setValue(SafeRandom(CONFIG.instance().minRandomSens, CONFIG.instance().maxRandomSens)))
										.build())
								.build())
						.build())).generateScreen(parent);
	}
}
