//? if >=26.3 {
package tektonikal.aimutils;

import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_EventFilter;
import org.lwjgl.sdl.SDL_MouseMotionEvent;
import org.slf4j.Logger;

public final class SdlMenuCursor {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static SDL_EventFilter watch;
	private static boolean warping, failed;
	private static int windowId;
	private static double rawX = Double.NaN, rawY, cursorX, cursorY;

	public static void install() {
		if (watch != null) return;
		windowId = SDLVideo.SDL_GetWindowID(Minecraft.getInstance().getWindow().handle());
		watch = SDL_EventFilter.create((userdata, event) -> {
			if (!failed) {
				try {
					onEvent(SDL_Event.create(event));
				} catch (Throwable t) {
					failed = true;
					LOGGER.error("Menu cursor scaling failed, turning it off", t);
				}
			}
			return true;
		});
		SDLEvents.SDL_AddEventWatch(watch, 0L);
	}

	private static void onEvent(SDL_Event event) {
		if (event.type() != SDLEvents.SDL_EVENT_MOUSE_MOTION) return;
		SDL_MouseMotionEvent motion = event.motion();
		if (warping) {
			motion.x((float) cursorX).y((float) cursorY).xrel(0).yrel(0);
			return;
		}
		//glup.. prevent deadlock
		Minecraft mc = Minecraft.getInstance();
		if (mc.mouseHandler.isMouseGrabbed()) {
			rawX = Double.NaN;
			return;
		}
		if (motion.windowID() != windowId) return;
		Window window = mc.getWindow();
		long handle = window.handle();
		double x = motion.x(), y = motion.y();
		if (Config.menuCursorSupported == null) testAvailability(handle, x, y);
		double mult = Config.menuCursorSupported == Boolean.TRUE ? Config.menuMultiplier(x, y) : 1;
		if (mult == 1) {
			rawX = Double.NaN;
			return;
		}
		if (Double.isNaN(rawX)) {
			cursorX = rawX = x - motion.xrel();
			cursorY = rawY = y - motion.yrel();
		}
		double dx = x - rawX, dy = y - rawY;
		if (Cm360.isWarpNoise(dx, dy)) {
			dx = dy = 0;
			rawX = x;
			rawY = y;
		} else {
			cursorX = Cm360.scaleMove(cursorX, dx, mult, window.getScreenWidth());
			cursorY = Cm360.scaleMove(cursorY, dy, mult, window.getScreenHeight());
			rawX = Math.round(cursorX);
			rawY = Math.round(cursorY);
			if (rawX != x || rawY != y) warp(handle, rawX, rawY);
		}
		motion.x((float) cursorX).y((float) cursorY).xrel((float) (dx * mult)).yrel((float) (dy * mult));
	}

	private static void testAvailability(long handle, double x, double y) {
		cursorX = x;
		cursorY = y;
		SDLError.SDL_ClearError();
		warp(handle, x, y);
		String error = SDLError.SDL_GetError();
		Config.menuCursorSupported = error == null || error.isEmpty();
		if (!Config.menuCursorSupported) LOGGER.warn("The cursor can't be moved here, so the menu cursor speed is off: {}", error);
	}

	private static void warp(long handle, double x, double y) {
		warping = true;
		try {
			SDLMouse.SDL_WarpMouseInWindow(handle, (float) x, (float) y);
		} finally {
			warping = false;
		}
	}
}
//?}
