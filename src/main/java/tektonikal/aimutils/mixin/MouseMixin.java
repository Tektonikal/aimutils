package tektonikal.aimutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tektonikal.aimutils.Config;

@Mixin(Mouse.class)
public class MouseMixin {
    @ModifyVariable(method = "updateMouse", at = @At("STORE"), ordinal = 3)
    double yeah(double f){
        return Config.CONFIG.instance().linearSens ? MinecraftClient.getInstance().options.getMouseSensitivity().getValue() : f;
    }
}
