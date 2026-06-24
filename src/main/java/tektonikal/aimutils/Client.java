package tektonikal.aimutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static tektonikal.aimutils.Config.CONFIG;
import static tektonikal.aimutils.Config.SafeRandom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;


public class Client implements ClientModInitializer {
	boolean yeah;

	@Override
	public void onInitializeClient() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("sens").executes(context -> {
				yeah = true;
				return 1;
			}));
			dispatcher.register(CommandManager.literal("randomizesens").executes(context -> {
				MinecraftClient.getInstance().options.getMouseSensitivity().setValue(SafeRandom(CONFIG.instance().minRandomSens, CONFIG.instance().maxRandomSens));
				return 1;
			}));
		});
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (yeah) {
				MinecraftClient client = MinecraftClient.getInstance();
				client.setScreen(Config.getConfigScreen(client.currentScreen));
				yeah = false;
			}
		});
	}
}
