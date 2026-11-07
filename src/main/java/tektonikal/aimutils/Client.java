package tektonikal.aimutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
//? if >=26.1 {
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
//?} else {
/*import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
*///?}

import static tektonikal.aimutils.Config.CONFIG;
import static tektonikal.aimutils.Config.SafeRandom;

import net.minecraft.client.Minecraft;


public class Client implements ClientModInitializer {
	boolean yeah;

	@Override
	public void onInitializeClient() {
		CONFIG.load();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("sens").executes(context -> {
				yeah = true;
				return 1;
			}));
			dispatcher.register(literal("randomizesens").executes(context -> {
				Minecraft.getInstance().options.sensitivity().set(SafeRandom(CONFIG.instance().minRandomSens, CONFIG.instance().maxRandomSens));
				Minecraft.getInstance().options.save();
				return 1;
			}));
		});
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (yeah) {
				Minecraft client = Minecraft.getInstance();
				//? if >=26.2 {
				client.gui.setScreen(Config.getConfigScreen(client.gui.screen()));
				//?} else {
				/*client.setScreen(Config.getConfigScreen(client.screen));
				*///?}
				yeah = false;
			}
		});
	}
}
