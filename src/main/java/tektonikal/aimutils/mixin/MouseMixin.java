package tektonikal.aimutils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tektonikal.aimutils.Config;

@Mixin(MouseHandler.class)
public class MouseMixin {
	@Shadow private double accumulatedDX;

	@Shadow private double accumulatedDY;

	@ModifyVariable(method = "turnPlayer", at = @At(value = "STORE", ordinal = 0), ordinal = 2)
    double yeah(double f){
		accumulatedDX = accumulatedDX > 0 ? accumulatedDX * Config.CONFIG.instance().rightMult : accumulatedDX * Config.CONFIG.instance().leftMult;
		accumulatedDY = accumulatedDY > 0 ? accumulatedDY * Config.CONFIG.instance().downMult : accumulatedDY * Config.CONFIG.instance().upMult;
        //account for spyglass
        return Config.CONFIG.instance().linearSens ? Minecraft.getInstance().options.sensitivity().get() / 8 : f;
    }
}
