package tektonikal.aimutils.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >=26.3 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?} else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
*///?}
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tektonikal.aimutils.Config;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public class InGameHudMixin {
	//? if >=26.1 {
	@WrapOperation(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite", ordinal = 0))
	void yeah(GuiGraphicsExtractor context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original){
	//?} else {
	/*@WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
	void yeah(GuiGraphics context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original){
	*///?}
		if(Config.CONFIG.instance().fixCrosshair){
			context.pose().pushMatrix();
			context.pose().translate(context.guiWidth() % 2 == 0 ? 0.5F : 0F, context.guiHeight() % 2 == 0 ? 0.5F : 0F);
			original.call(context, pipeline, sprite, x, y, width, height);
			context.pose().popMatrix();
		}else{
			original.call(context, pipeline, sprite, x, y, width, height);
		}
	}
}
