package tektonikal.aimutils.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import tektonikal.aimutils.Cm360;
import tektonikal.aimutils.Config;
import tektonikal.aimutils.SensRandomizer;
//? if <26.3 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import tektonikal.aimutils.IxerisCompat;
*///?}

@Mixin(MouseHandler.class)
public class MouseMixin {
	@Shadow private double accumulatedDX;

	@Shadow private double accumulatedDY;

	@Unique private final SensRandomizer randomizer = new SensRandomizer();

	@ModifyExpressionValue(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
	private Object sensitivity(Object sens) {
		Config config = Config.CONFIG.instance();
		double random = randomizer.sample(System.nanoTime(), config.randomizerEnabled, config.randomizerMin, config.randomizerMax, config.randomizerFrequency, config.randomizerInterpolation);
		accumulatedDX *= random * (accumulatedDX > 0 ? config.rightMult : config.leftMult);
		accumulatedDY *= random * (accumulatedDY > 0 ? config.downMult : config.upMult);
		return config.linearSens ? Cm360.sensFor((Double) sens) : sens;
	}

	//? if <26.3 {
	/*@Shadow private double xpos;

	@Shadow private double ypos;

	@Shadow private boolean ignoreFirstMove;

	@Unique private double rawX = Double.NaN, rawY = Double.NaN;
	@Unique private static boolean probeQueued;

	@WrapMethod(method = "onMove")
	private void menuMult(long handle, double x, double y, Operation<Void> original) {
		Window window = Minecraft.getInstance().getWindow();
		boolean ours = handle == window.handle();
		if (ours && !probeQueued) {
			probeQueued = true;
			IxerisCompat.warp(() -> probe(handle), () -> {});
		}
		double mult = !ours || ignoreFirstMove || Config.menuCursorSupported != Boolean.TRUE ? 1 : Config.menuMultiplier(x, y);
		if (mult == 1) {
			rawX = Double.NaN;
			original.call(handle, x, y);
			return;
		}
		if (Double.isNaN(rawX)) {
			rawX = xpos;
			rawY = ypos;
		}
		double dx = x - rawX, dy = y - rawY;
		rawX = x;
		rawY = y;
		if (Cm360.isWarpNoise(dx, dy)) return;
		double sx = Cm360.scaleMove(xpos, dx, mult, window.getScreenWidth());
		double sy = Cm360.scaleMove(ypos, dy, mult, window.getScreenHeight());
		double wx = Math.round(sx), wy = Math.round(sy);
		if (wx == x && wy == y) {
			original.call(handle, sx, sy);
			return;
		}
		IxerisCompat.warp(() -> {
			GLFW.glfwSetCursorPos(handle, wx, wy);
			return true;
		}, () -> {
			rawX = wx;
			rawY = wy;
		});
		original.call(handle, sx, sy);
	}

	@Unique
	private static boolean probe(long handle) {
		double[] x = new double[1], y = new double[1];
		GLFW.glfwGetCursorPos(handle, x, y);
		boolean[] failed = {false};
		GLFWErrorCallback previous = GLFW.glfwSetErrorCallback((error, description) -> failed[0] = true);
		GLFW.glfwSetCursorPos(handle, x[0], y[0]);
		GLFWErrorCallback probe = GLFW.glfwSetErrorCallback(previous);
		if (probe != null) probe.free();
		Config.menuCursorSupported = !failed[0];
		return false;
	}
	*///?}
}
