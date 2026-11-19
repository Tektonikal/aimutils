package tektonikal.aimutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
//? if >=26.1 {
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
//?} else {
/*import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
*///?}

import static tektonikal.aimutils.Config.CONFIG;

import net.minecraft.client.Minecraft;


public class Client implements ClientModInitializer {
	private boolean openConfig;

	@Override
	public void onInitializeClient() {
		CONFIG.load();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("sens").executes(context -> {
				openConfig = true;
				return 1;
			}));
			dispatcher.register(literal("randomizesens").executes(context -> {
				Minecraft.getInstance().options.sensitivity().set(Cm360.randomSens(CONFIG.instance().minRandomSens, CONFIG.instance().maxRandomSens));
				Minecraft.getInstance().options.save();
				return 1;
			}));
		});
		//? if >=26.3
		ClientLifecycleEvents.CLIENT_STARTED.register(mc -> SdlMenuCursor.install());
		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> Config.saveAll());
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (openConfig) {
				ConfigScreen.open();
				openConfig = false;
			}
			Config.saveOnScreenChange();
		});
	}
}
