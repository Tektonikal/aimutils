package tektonikal.aimutils;

import com.mojang.blaze3d.platform.Window;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class Config {
	public static final ConfigClassHandler<Config> CONFIG = ConfigClassHandler.createBuilder(Config.class)
			.serializer(configConfigClassHandler -> GsonConfigSerializerBuilder.create(configConfigClassHandler)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("aimutils.json"))
					.build()).build();

	public static volatile Boolean menuCursorSupported;
	static boolean dirty;
	private static Screen lastScreen;

	@SerialEntry
	public boolean randomizerEnabled = false;
	@SerialEntry
	public double randomizerMin = 0.5;
	@SerialEntry
	public double randomizerMax = 2;
	@SerialEntry
	public double randomizerFrequency = 0.5;
	@SerialEntry
	public SensRandomizer.Interpolation randomizerInterpolation = SensRandomizer.Interpolation.HERMITE;
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
	public double menuMult = 1;

	public static double menuMultiplier(double x, double y) {
		Minecraft mc = Minecraft.getInstance();
		Window window = mc.getWindow();
		if (mc.mouseHandler.isMouseGrabbed() || !mc.isWindowActive()) return 1;
		if (x < 0 || y < 0 || x >= window.getScreenWidth() || y >= window.getScreenHeight()) return 1;
		return Math.clamp(CONFIG.instance().menuMult, Cm360.MENU_MIN, Cm360.MENU_MAX);
	}

	static Screen currentScreen() {
		//? if >=26.2 {
		return Minecraft.getInstance().gui.screen();
		//?} else {
		/*return Minecraft.getInstance().screen;
		*///?}
	}

	public static void saveOnScreenChange() {
		Screen screen = currentScreen();
		if (dirty && screen != lastScreen) {
			dirty = false;
			saveAll();
		}
		lastScreen = screen;
	}

	public static void saveAll() {
		Minecraft mc = Minecraft.getInstance();
		//stop log spam
		if (!mc.isSameThread()) {
			mc.execute(Config::saveAll);
			return;
		}
		CONFIG.save();
		mc.options.save();
	}
}
