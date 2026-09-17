package tektonikal.aimutils;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.impl.controller.IntegerFieldControllerBuilderImpl;
import dev.isxander.yacl3.impl.controller.TickBoxControllerBuilderImpl;
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
    public double minRandomSens = 0.25d;
    @SerialEntry
    public double maxRandomSens = 0.75d;
    @SerialEntry
    public boolean linearSens = false;
    @SerialEntry
    public int mouseDPI = 800;
    @SerialEntry
    public double targetCM360 = 40;

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
        Option<Double> o_sens = Option.<Double>createBuilder()
                .name(Text.of("Precise Sensitivity Value"))
                .description(OptionDescription.of(Text.of("Range [0, 1] corresponds to in-game [0%, 200%]. Do not worry about the value being rounded to the nearest 0.005, it actually gets applied!")))
                .controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(0d).max(1d).formatValue(value -> Text.of(String.format("%.1f", value * 200d) + "%")))
                .binding(Binding.minecraft(MinecraftClient.getInstance().options.getMouseSensitivity()))
                .build();
        return YetAnotherConfigLib.create(CONFIG, (defaults, config, builder) -> builder
                .title(Text.of("Aim Utils"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.of("Aim Utils"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Precise Sensitivity"))
                                .option(o_sens)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Sensitivity Randomization"))
                                .option(Option.<Double>createBuilder()
                                        .name(Text.of("Min Sensitivity"))
                                        .description(OptionDescription.of(Text.of("Range [0, 1] corresponds to in-game [0%, 200%]. Gets rounded to the nearest 0.00001.")))
                                        .controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(0d).max(1d).formatValue(value -> Text.of(String.format("%.1f", value * 200d) + "%")))
                                        .stateManager(StateManager.createInstant(0.25d, () -> CONFIG.instance().minRandomSens, newVal -> CONFIG.instance().minRandomSens = newVal))
                                        .build())
                                .option(Option.<Double>createBuilder()
                                        .name(Text.of("Max Sensitivity"))
                                        .description(OptionDescription.of(Text.of("Range [0, 1] corresponds to in-game [0%, 200%]. Gets rounded to the nearest 0.005.")))
                                        .controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).formatValue(value -> Text.of(String.format("%.1f", value * 200d) + "%")))
                                        .stateManager(StateManager.createInstant(0.75d, () -> CONFIG.instance().maxRandomSens, newVal -> CONFIG.instance().maxRandomSens = newVal))
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Text.of("Randomize Sensitivity"))
                                        .text(Text.of("Randomize"))
                                        .description(OptionDescription.of(Text.of("Set the mouse sensitivity to a random value between min and max")))
                                        .action((yaclScreen, buttonOption) -> {
                                            o_sens.stateManager().set(SafeRandom(CONFIG.instance().minRandomSens, CONFIG.instance().maxRandomSens));
                                            o_sens.stateManager().apply();
                                        })
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.of("Miscellaneous"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of("Enable Linear Sensitivity"))
                                        .stateManager(StateManager.createInstant(false, () -> CONFIG.instance().linearSens, newVal -> CONFIG.instance().linearSens = newVal))
                                        .controller(TickBoxControllerBuilderImpl::new)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.of("Mouse DPI"))
                                        .controller(doubleOption -> IntegerFieldControllerBuilder.create(doubleOption).min(200).max(20000))
                                        .stateManager(StateManager.createInstant(800, () -> CONFIG.instance().mouseDPI, newVal -> CONFIG.instance().mouseDPI = newVal))
                                        .build())
                                .option(Option.<Double>createBuilder()
                                        .name(Text.of("Target cm/360"))
                                        .controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(0d).max(100d).formatValue(value -> Text.of(String.format("%.2f", value))))
                                        .stateManager(StateManager.createInstant(40d, () -> CONFIG.instance().targetCM360, newVal -> CONFIG.instance().targetCM360 = newVal))
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Text.of("Apply cm/360"))
                                        .text(Text.of("Apply"))
                                        .action((yaclScreen, buttonOption) -> {
                                            o_sens.stateManager().set(getCm360());
                                            o_sens.stateManager().apply();
                                        })
                                        .build())
                                .build())
                        .build())).generateScreen(parent);
    }

    public static Double getCm360() {
        if (CONFIG.instance().linearSens) {
            return ((360.0 / ((CONFIG.instance().targetCM360 / 2.54) * CONFIG.instance().mouseDPI)) / 0.15);
        }
        return (Math.cbrt((360.0 / ((CONFIG.instance().targetCM360 / 2.54) * CONFIG.instance().mouseDPI)) / 1.2) - 0.2) / 0.6;
    }
}
