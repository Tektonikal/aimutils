package tektonikal.aimutils;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static tektonikal.aimutils.Config.CONFIG;

public final class ConfigScreen {
	private static final String MENU_DESCRIPTION = "aimutils.menu.description";

	private static boolean syncing;

	public static Screen create(Screen parent) {
		Config defaults = CONFIG.defaults();
		Option<Double> o_sens = Option.<Double>createBuilder()
				.name(Component.translatable("aimutils.sensitivity"))
				.description(OptionDescription.of(Component.translatable("aimutils.sensitivity.description")))
				.controller(ConfigScreen::sensField)
				.stateManager(instant(0.5d, () -> Minecraft.getInstance().options.sensitivity().get(), newVal -> Minecraft.getInstance().options.sensitivity().set(newVal)))
				.build();
		Option<Boolean> o_linear = Option.<Boolean>createBuilder()
				.name(Component.translatable("aimutils.linear"))
				.stateManager(instant(defaults.linearSens, () -> CONFIG.instance().linearSens, newVal -> CONFIG.instance().linearSens = newVal))
				.controller(TickBoxControllerBuilder::create)
				.build();
		Option<Integer> o_dpi = Option.<Integer>createBuilder()
				.name(Component.translatable("aimutils.dpi"))
				.controller(intOption -> IntegerFieldControllerBuilder.create(intOption).min(1).max(100000))
				.stateManager(instant(defaults.mouseDPI, () -> CONFIG.instance().mouseDPI, newVal -> CONFIG.instance().mouseDPI = newVal))
				.build();
		double[] savedCm360 = {Cm360.fromSens(o_sens.pendingValue(), o_dpi.pendingValue(), o_linear.pendingValue())};
		Option<Double> o_cm360 = Option.<Double>createBuilder()
				.name(Component.translatable("aimutils.cm360"))
				.controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(Cm360.MIN).max(Cm360.MAX).formatValue(value -> Component.literal(String.format("%.2f", value))))
				.stateManager(StateManager.createInstant(Cm360.fromSens(0.5d, o_dpi.pendingValue(), o_linear.pendingValue()), () -> savedCm360[0], newVal -> savedCm360[0] = newVal))
				.build();
		Runnable cm360Changed = () -> {
			if (syncing) return;
			syncing = true;
			double sens = Cm360.reachableSens(o_cm360.pendingValue(), o_dpi.pendingValue(), o_linear.pendingValue());
			if (Math.abs(sens - o_sens.pendingValue()) > 1e-9) o_sens.requestSet(sens);
			if (sens == 0 || sens == 1) o_cm360.requestSet(Cm360.fromSens(sens, o_dpi.pendingValue(), o_linear.pendingValue()));
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
		Boolean menuSupported = Config.menuCursorSupported;
		OptionDescription menuDescription = OptionDescription.of(menuSupported == Boolean.FALSE
				? new Component[]{Component.translatable("aimutils.menu.description"), Component.translatable("aimutils.menu.unsupported")}
				: new Component[]{Component.translatable(MENU_DESCRIPTION)});
		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("aimutils.title"))
				.save(Config::saveAll)
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("aimutils.title"))
						.group(OptionGroup.createBuilder()
								.name(Component.translatable("aimutils.group.precise"))
								.option(o_sens)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.translatable("aimutils.group.cm360"))
								.option(o_dpi)
								.option(o_cm360)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.translatable("aimutils.group.randomizer"))
								.option(Option.<Boolean>createBuilder()
										.name(Component.translatable("aimutils.randomizer.enabled"))
										.controller(TickBoxControllerBuilder::create)
										.stateManager(instant(defaults.randomizerEnabled, () -> CONFIG.instance().randomizerEnabled, newVal -> CONFIG.instance().randomizerEnabled = newVal))
										.build())
								.option(randomizerMultOption("aimutils.randomizer.min", defaults.randomizerMin, () -> CONFIG.instance().randomizerMin, newVal -> CONFIG.instance().randomizerMin = newVal))
								.option(randomizerMultOption("aimutils.randomizer.max", defaults.randomizerMax, () -> CONFIG.instance().randomizerMax, newVal -> CONFIG.instance().randomizerMax = newVal))
								.option(Option.<Double>createBuilder()
										.name(Component.translatable("aimutils.randomizer.frequency"))
										.controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(SensRandomizer.MIN_FREQUENCY).max(SensRandomizer.MAX_FREQUENCY).formatValue(value -> Component.literal(String.format("%.2f Hz", value))))
										.stateManager(instant(defaults.randomizerFrequency, () -> CONFIG.instance().randomizerFrequency, newVal -> CONFIG.instance().randomizerFrequency = newVal))
										.build())
								.option(Option.<SensRandomizer.Interpolation>createBuilder()
										.name(Component.translatable("aimutils.randomizer.interpolation"))
										.controller(enumOption -> EnumDropdownControllerBuilder.create(enumOption).formatValue(value -> Component.translatable("aimutils.randomizer.interpolation." + value.name().toLowerCase(Locale.ROOT))))
										.stateManager(instant(defaults.randomizerInterpolation, () -> CONFIG.instance().randomizerInterpolation, newVal -> CONFIG.instance().randomizerInterpolation = newVal))
										.build())
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.translatable("aimutils.group.multipliers"))
								.option(multiplierOption("aimutils.right", 0d, defaults.rightMult, () -> CONFIG.instance().rightMult, newVal -> CONFIG.instance().rightMult = newVal))
								.option(multiplierOption("aimutils.left", 0d, defaults.leftMult, () -> CONFIG.instance().leftMult, newVal -> CONFIG.instance().leftMult = newVal))
								.option(multiplierOption("aimutils.up", 0d, defaults.upMult, () -> CONFIG.instance().upMult, newVal -> CONFIG.instance().upMult = newVal))
								.option(multiplierOption("aimutils.down", 0d, defaults.downMult, () -> CONFIG.instance().downMult, newVal -> CONFIG.instance().downMult = newVal))
								.option(multiplierOption("aimutils.menu", Cm360.MENU_MIN, defaults.menuMult, () -> CONFIG.instance().menuMult, newVal -> CONFIG.instance().menuMult = newVal, menuDescription, menuSupported != Boolean.FALSE))
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.translatable("aimutils.group.misc"))
								.option(o_linear)
								.build())
						.build())
				.build()
				.generateScreen(parent);
	}

	public static void open() {
		Minecraft client = Minecraft.getInstance();
		//? if >=26.2 {
		client.gui.setScreen(create(Config.currentScreen()));
		//?} else {
		/*client.setScreen(create(Config.currentScreen()));
		*///?}
	}

	private static Option<Double> randomizerMultOption(String key, double def, Supplier<Double> getter, Consumer<Double> setter) {
		return Option.<Double>createBuilder()
				.name(Component.translatable(key))
				.controller(doubleOption -> DoubleFieldControllerBuilder.create(doubleOption).min(SensRandomizer.MIN_MULT).max(SensRandomizer.MAX_MULT).formatValue(value -> Component.literal(String.format("%.2fx", value))))
				.stateManager(instant(def, getter, setter))
				.build();
	}

	private static DoubleFieldControllerBuilder sensField(Option<Double> option) {
		return DoubleFieldControllerBuilder.create(option).min(0d).max(1d).formatValue(value -> Component.literal(CONFIG.instance().linearSens
				? String.format("%.3fx", value)
				: String.format("%.1f%%", value * 200d)));
	}

	private static Option<Double> multiplierOption(String key, double min, double def, Supplier<Double> getter, Consumer<Double> setter) {
		return multiplierOption(key, min, def, getter, setter, OptionDescription.EMPTY, true);
	}

	private static Option<Double> multiplierOption(String key, double min, double def, Supplier<Double> getter, Consumer<Double> setter, OptionDescription description, boolean available) {
		return Option.<Double>createBuilder()
				.name(Component.literal(" - ").append(Component.translatable(key)))
				.description(description)
				.available(available)
				.controller(doubleOption -> DoubleSliderControllerBuilder.create(doubleOption).range(min, 2d).step(0.01).formatValue(value -> Component.literal(String.format("%.2f", value) + "x")))
				.stateManager(instant(def, getter, setter))
				.build();
	}

	private static <T> StateManager<T> instant(T def, Supplier<T> getter, Consumer<T> setter) {
		return StateManager.createInstant(def, getter, value -> {
			setter.accept(value);
			Config.dirty = true;
		});
	}

	private static <T> void onChange(Option<T> option, Runnable listener) {
		option.addEventListener((opt, event) -> {
			if (event == OptionEventListener.Event.STATE_CHANGE) listener.run();
		});
	}
}
