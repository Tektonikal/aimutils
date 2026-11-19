//? if <26.3 {
/*package tektonikal.aimutils;

import me.decce.ixeris.api.IxerisApi;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.BooleanSupplier;

public final class IxerisCompat {
	private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("ixeris");

	public static void warp(BooleanSupplier warp, Runnable afterWarp) {
		if (LOADED) Api.warp(warp, afterWarp);
		else if (warp.getAsBoolean()) afterWarp.run();
	}

	private static final class Api {
		static void warp(BooleanSupplier warp, Runnable afterWarp) {
			IxerisApi api = IxerisApi.getInstance();
			if (api.isOnMainThread()) {
				if (warp.getAsBoolean()) afterWarp.run();
				return;
			}
			api.runLaterOnMainThread(() -> {
				if (warp.getAsBoolean()) api.runLaterOnRenderThread(afterWarp);
			});
		}
	}
}
*///?}
