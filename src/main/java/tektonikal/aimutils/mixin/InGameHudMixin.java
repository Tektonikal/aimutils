package tektonikal.aimutils.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.aimutils.Config;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@WrapMethod(method = "renderCrosshair")
	void yeah(DrawContext context, RenderTickCounter tickCounter, Operation<Void> original){
		if(Config.CONFIG.instance().fixCrosshair){
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(0.5F, 0.5F);
			original.call(context, tickCounter);
			context.getMatrices().popMatrix();
		}else{
			original.call(context, tickCounter);
		}
	}
}
