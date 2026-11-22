package tektonikal.aimutils.mixin.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tektonikal.aimutils.legacy.LegacyClient;
import tektonikal.aimutils.SensRandomizer;
import tektonikal.aimutils.legacy.LegacyConfig;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void frame(float tickDelta, long startTime, CallbackInfo ci) {
		LegacyClient.onFrame();
	}

	@Unique private final SensRandomizer randomizer = new SensRandomizer();

	@ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), ordinal = 2)
	private float sensitivity(float g) {
		LegacyConfig config = LegacyConfig.INSTANCE;
		float mult = config.linearSens ? Minecraft.getInstance().options.mouseSensitivity : g;
		return mult * (float) randomizer.sample(System.nanoTime(), config.randomizerEnabled, config.randomizerMin, config.randomizerMax, config.randomizerFrequency, config.randomizerInterpolation);
	}

	@ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
	private float horizontal(float h) {
		return h * (h > 0 ? LegacyConfig.INSTANCE.rightMult : LegacyConfig.INSTANCE.leftMult);
	}

	@ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), ordinal = 4)
	private float vertical(float i) {
		return i * (i > 0 ? LegacyConfig.INSTANCE.upMult : LegacyConfig.INSTANCE.downMult);
	}
}
