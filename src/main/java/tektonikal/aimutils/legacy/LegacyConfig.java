package tektonikal.aimutils.legacy;

import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.Property;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.ValueFormatter;
import org.polyfrost.oneconfig.api.config.v1.annotations.Button;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.ui.v1.OptionWidgets;
import tektonikal.aimutils.Cm360;

public class LegacyConfig extends Config {
	public static LegacyConfig INSTANCE;

	@Number(title = "aimutils.sensitivity", description = "aimutils.sensitivity.description", subcategory = "aimutils.group.precise", min = 0, max = 1, formatter = PlainFloat.class)
	public float sensitivity = 0.5f;

	@Number(title = "aimutils.dpi", subcategory = "aimutils.group.cm360", min = 1, max = 100000)
	public int mouseDPI = 800;
	@Number(title = "aimutils.cm360", subcategory = "aimutils.group.cm360", min = (float) Cm360.MIN, max = (float) Cm360.MAX)
	public float cm360 = (float) Cm360.fromSens(0.5, 800, false);

	@Number(title = "aimutils.minRandom", description = "aimutils.range.description", subcategory = "aimutils.group.random", min = 0, max = 1, formatter = PlainFloat.class)
	public float minRandomSens = 0.25f;
	@Number(title = "aimutils.maxRandom", description = "aimutils.range.description", subcategory = "aimutils.group.random", min = 0, max = 1, formatter = PlainFloat.class)
	public float maxRandomSens = 0.75f;

	@Slider(title = "aimutils.right", subcategory = "aimutils.group.multipliers", min = 0, max = 2, step = 0.01f)
	public float rightMult = 1;
	@Slider(title = "aimutils.left", subcategory = "aimutils.group.multipliers", min = 0, max = 2, step = 0.01f)
	public float leftMult = 1;
	@Slider(title = "aimutils.up", subcategory = "aimutils.group.multipliers", min = 0, max = 2, step = 0.01f)
	public float upMult = 1;
	@Slider(title = "aimutils.down", subcategory = "aimutils.group.multipliers", min = 0, max = 2, step = 0.01f)
	public float downMult = 1;
	@Slider(title = "aimutils.menu", description = "aimutils.menu.description", subcategory = "aimutils.group.multipliers", min = (float) Cm360.MENU_MIN, max = (float) Cm360.MENU_MAX, step = 0.01f)
	public float menuMult = 1;

	@Switch(title = "aimutils.linear", subcategory = "aimutils.group.misc")
	public boolean linearSens = false;

	private LegacyConfig() {
		super("aimutils.json", "assets/aimutils/icon.png", "Aim Utils", Category.COMBAT);
		this.<Float>addCallback("sensitivity", v -> {
			applySens(v);
			setSilently("cm360", (float) Cm360.fromSens(v, mouseDPI, linearSens));
			return false;
		});
		this.<Float>addCallback("cm360", v -> {
			sensFromCm360(v, mouseDPI, linearSens);
			return false;
		});
		this.<Integer>addCallback("mouseDPI", v -> {
			sensFromCm360(cm360, v, linearSens);
			return false;
		});
		this.<Boolean>addCallback("linearSens", v -> {
			sensFromCm360(cm360, mouseDPI, v);
			return false;
		});
	}

	@Override
	protected void onInitialized(Tree tree) {
		for (String option : new String[]{"sensitivity", "minRandomSens", "maxRandomSens"}) {
			getProperty(option).addMetadata("step", 1e-6f);
		}
	}

	public static void init() {
		if (INSTANCE == null) INSTANCE = new LegacyConfig();
	}

	@Button(title = "aimutils.randomize", text = "aimutils.randomize.button", description = "aimutils.randomize.description", subcategory = "aimutils.group.random")
	private void randomize() {
		Property<?> prop = getProperty("sensitivity");
		prop.setAs(randomSens());
		OptionWidgets.refresh(prop);
	}

	public static float randomSens() {
		return (float) Cm360.randomSens(INSTANCE.minRandomSens, INSTANCE.maxRandomSens);
	}

	public void syncFromGame() {
		float sens = Minecraft.getInstance().options.mouseSensitivity;
		if (tree == null || sens == sensitivity) return;
		setSilently("sensitivity", sens);
		setSilently("cm360", (float) Cm360.fromSens(sens, mouseDPI, linearSens));
	}

	private void sensFromCm360(float cm, int dpi, boolean linear) {
		float sens = (float) Cm360.reachableSens(cm, dpi, linear);
		setSilently("sensitivity", sens);
		applySens(sens);
	}

	private void setSilently(String option, float value) {
		Property<?> prop = getProperty(option);
		prop.setAsSilently(value);
		OptionWidgets.refresh(prop);
	}

	private static boolean dirty;
	private static Object lastScreen;

	private static void applySens(float sens) {
		Minecraft.getInstance().options.mouseSensitivity = sens;
		dirty = true;
	}

	public static void saveOnScreenChange() {
		Minecraft mc = Minecraft.getInstance();
		if (dirty && mc.screen != lastScreen) {
			dirty = false;
			mc.options.save();
		}
		lastScreen = mc.screen;
	}

	public static class PlainFloat implements ValueFormatter {
		@Override
		public String format(java.lang.Number value) {
			return String.valueOf(value.floatValue());
		}
	}
}
