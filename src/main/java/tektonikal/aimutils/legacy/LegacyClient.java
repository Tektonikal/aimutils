package tektonikal.aimutils.legacy;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.polyfrost.oneconfig.api.commands.v1.CommandManager;
import org.polyfrost.oneconfig.utils.v1.dsl.ScreensKt;
import tektonikal.aimutils.Cm360;

public class LegacyClient implements ClientModInitializer {
	private static boolean openConfig;
	private static int lastX = -1, lastY;
	private static double cursorX, cursorY;

	@Override
	public void onInitializeClient() {
		LegacyConfig.init();
		CommandManager.INSTANCE.register(CommandManager.literal("sens").executes(context -> {
			openConfig = true;
			return 1;
		}));
		CommandManager.INSTANCE.register(CommandManager.literal("randomizesens").executes(context -> {
			Minecraft mc = Minecraft.getInstance();
			mc.options.mouseSensitivity = LegacyConfig.randomSens();
			mc.options.save();
			return 1;
		}));
		MinecraftClientEvents.STOP.register(mc -> mc.options.save());
	}

	public static void onFrame() {
		LegacyConfig.INSTANCE.syncFromGame();
		Minecraft mc = Minecraft.getInstance();
		if (openConfig && mc.screen == null) {
			openConfig = false;
			ScreensKt.openUI(LegacyConfig.INSTANCE);
		}
		scaleMenuCursor(mc);
		LegacyConfig.saveOnScreenChange();
	}

	private static void scaleMenuCursor(Minecraft mc) {
		double mult = Math.clamp(LegacyConfig.INSTANCE.menuMult, Cm360.MENU_MIN, Cm360.MENU_MAX);
		if (mult == 1 || mc.screen == null || Mouse.isGrabbed() || !Display.isActive() || !Mouse.isInsideWindow()) {
			lastX = -1;
			return;
		}
		int x = Mouse.getX(), y = Mouse.getY();
		if (lastX == -1) {
			cursorX = lastX = x;
			cursorY = lastY = y;
			return;
		}
		if (x == lastX && y == lastY) return;
		cursorX = Cm360.scaleMove(cursorX, x - lastX, mult, Display.getWidth());
		cursorY = Cm360.scaleMove(cursorY, y - lastY, mult, Display.getHeight());
		lastX = (int) Math.round(cursorX);
		lastY = (int) Math.round(cursorY);
		Mouse.setCursorPosition(lastX, lastY);
	}
}
