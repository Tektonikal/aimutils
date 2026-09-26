package tektonikal.aimutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tektonikal.aimutils.Config;

@Mixin(Mouse.class)
public class MouseMixin {
	@Shadow private double cursorDeltaX;

	@Shadow private double cursorDeltaY;

	@ModifyVariable(method = "updateMouse", at = @At("STORE"), ordinal = 3)
    double yeah(double f){
		cursorDeltaX = cursorDeltaX > 0 ? cursorDeltaX * Config.CONFIG.instance().rightMult : cursorDeltaX * Config.CONFIG.instance().leftMult;
		cursorDeltaY = cursorDeltaY > 0 ? cursorDeltaY * Config.CONFIG.instance().downMult : cursorDeltaY * Config.CONFIG.instance().upMult;
        return Config.CONFIG.instance().linearSens ? MinecraftClient.getInstance().options.getMouseSensitivity().getValue() : f;
    }
}
