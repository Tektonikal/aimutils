package tektonikal.aimutils;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
	public double rightMult = 1;
	@SerialEntry
	public double leftMult = 1;
	@SerialEntry
	public double upMult = 1;
	@SerialEntry
	public double downMult = 1;
	@SerialEntry
	public boolean fixCrosshair = true;

    private static final Random rand = new Random();
    private static boolean syncing;

    public static double SafeRandom(double min, double max) {
        min = Math.clamp(min, 0d, 1d);
        max = Math.clamp(max, 0d, 1d);
        return min + rand.nextDouble() * (max - min);
    }

    public static Screen getConfigScreen(Screen parent) {
        Config defaults = CONFIG.defaults();
        Option<Double> o_sens = Option.<Double>createBuilder()
                .name(Component.literal("Precise Sensitivity Value"))
                .description(OptionDescription.of(Component.literal("Range [0, 1] corresponds to in-game [0%, 200%]. Do not worry about the value being rounded to the nearest 0.005, it actually gets applied!")))
                .controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(0d).max(1d).formatValue(value -> Component.literal(String.format("%.1f", value * 200d) + "%")))
                .binding(Binding.minecraft(Minecraft.getInstance().options.sensitivity()))
                .build();
        Option<Boolean> o_linear = Option.<Boolean>createBuilder()
                .name(Component.literal("Enable Linear Sensitivity"))
                .stateManager(StateManager.createSimple(defaults.linearSens, () -> CONFIG.instance().linearSens, newVal -> CONFIG.instance().linearSens = newVal))
                .controller(TickBoxControllerBuilder::create)
                .build();
        Option<Integer> o_dpi = Option.<Integer>createBuilder()
                .name(Component.literal("Mouse DPI"))
                .controller(doubleOption -> IntegerFieldControllerBuilder.create(doubleOption).min(1).max(100000))
                .stateManager(StateManager.createSimple(defaults.mouseDPI, () -> CONFIG.instance().mouseDPI, newVal -> CONFIG.instance().mouseDPI = newVal))
                .build();
        double[] savedCm360 = {Cm360.fromSens(o_sens.pendingValue(), o_dpi.pendingValue(), o_linear.pendingValue())};
        Option<Double> o_cm360 = Option.<Double>createBuilder()
                .name(Component.literal("Cm/360"))
                .controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(Cm360.MIN).max(Cm360.MAX).formatValue(value -> Component.literal(String.format("%.2f", value))))
                .stateManager(StateManager.createSimple(Cm360.fromSens(0.5d, defaults.mouseDPI, defaults.linearSens), () -> savedCm360[0], newVal -> savedCm360[0] = newVal))
                .build();
        Option<Double> o_minRandom = randomLimitOption("Min Sensitivity", defaults.minRandomSens, () -> CONFIG.instance().minRandomSens, newVal -> CONFIG.instance().minRandomSens = newVal);
        Option<Double> o_maxRandom = randomLimitOption("Max Sensitivity", defaults.maxRandomSens, () -> CONFIG.instance().maxRandomSens, newVal -> CONFIG.instance().maxRandomSens = newVal);
        Runnable cm360Changed = () -> {
            if (syncing) return;
            syncing = true;
            double exact = Cm360.toSens(o_cm360.pendingValue(), o_dpi.pendingValue(), o_linear.pendingValue());
            double sens = Math.clamp(exact, 0d, 1d);
            if (Math.abs(sens - o_sens.pendingValue()) > 1e-9) o_sens.requestSet(sens);
            if (sens != exact) o_cm360.requestSet(Cm360.fromSens(sens, o_dpi.pendingValue(), o_linear.pendingValue()));
            syncing = false;
        };
        onChange(o_cm360, cm360Changed);
        onChange(o_dpi, cm360Changed);
        onChange(o_linear, cm360Changed);
        onChange(o_sens, () -> {
            if (syncing) return;
            syncing = true;
            o_cm360.requestSet(Cm360.fromSens(o_sens.pendingValue(), o_dpi.pendingValue(), o_linear.pendingValue()));
            syncing = false;
        });
        return YetAnotherConfigLib.create(CONFIG, (unused, config, builder) -> builder
                .title(Component.literal("Aim Utils"))
                .save(() -> {
                    CONFIG.save();
                    Minecraft.getInstance().options.save();
                })
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Aim Utils"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Precise Sensitivity"))
                                .option(o_sens)
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Sensitivity Randomization"))
                                .option(o_minRandom)
                                .option(o_maxRandom)
                                .option(ButtonOption.createBuilder()
                                        .name(Component.literal("Randomize Sensitivity"))
                                        .text(Component.literal("Randomize"))
                                        .description(OptionDescription.of(Component.literal("Set the mouse sensitivity to a random value between min and max")))
                                        .action((yaclScreen, buttonOption) -> {
                                            o_sens.stateManager().set(SafeRandom(o_minRandom.pendingValue(), o_maxRandom.pendingValue()));
                                        })
                                        .build())
                                .build())
		                .group(OptionGroup.createBuilder()
				                .name(Component.literal("Sensitivity Multipliers"))
				                .option(multiplierOption(" - Right", defaults.rightMult, () -> CONFIG.instance().rightMult, newVal -> CONFIG.instance().rightMult = newVal))
				                .option(multiplierOption(" - Left", defaults.leftMult, () -> CONFIG.instance().leftMult, newVal -> CONFIG.instance().leftMult = newVal))
				                .option(multiplierOption(" - Up", defaults.upMult, () -> CONFIG.instance().upMult, newVal -> CONFIG.instance().upMult = newVal))
				                .option(multiplierOption(" - Down", defaults.downMult, () -> CONFIG.instance().downMult, newVal -> CONFIG.instance().downMult = newVal))
				                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Miscellaneous"))
                                .option(o_linear)
		                        .option(Option.<Boolean>createBuilder()
				                        .name(Component.literal("Crosshair Alignment Fix"))
				                        .stateManager(StateManager.createSimple(defaults.fixCrosshair, () -> CONFIG.instance().fixCrosshair, newVal -> CONFIG.instance().fixCrosshair = newVal))
				                        .controller(TickBoxControllerBuilder::create)
				                        .build())
                                .option(o_dpi)
                                .option(o_cm360)
                                .build())
                        .build())).generateScreen(parent);
    }

    private static Option<Double> randomLimitOption(String name, double def, Supplier<Double> getter, Consumer<Double> setter) {
        return Option.<Double>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.of(Component.literal("Range [0, 1] corresponds to in-game [0%, 200%].")))
                .controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(0d).max(1d).formatValue(value -> Component.literal(String.format("%.1f", value * 200d) + "%")))
                .stateManager(StateManager.createSimple(def, getter, setter))
                .build();
    }

    private static Option<Double> multiplierOption(String name, double def, Supplier<Double> getter, Consumer<Double> setter) {
        return Option.<Double>createBuilder()
                .name(Component.literal(name))
                .controller(doubleOption -> DoubleSliderControllerBuilder.create(doubleOption).range(0d, 2d).step(0.01).formatValue(value -> Component.literal(String.format("%.2f", value) + "x")))
                .stateManager(StateManager.createSimple(def, getter, setter))
                .build();
    }

    private static <T> void onChange(Option<T> option, Runnable listener) {
        option.addEventListener((opt, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) listener.run();
        });
    }
}
