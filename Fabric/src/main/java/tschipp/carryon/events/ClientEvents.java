package tschipp.carryon.events;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import tschipp.carryon.CarryOnCommonClient;

public class ClientEvents {

	public static void registerEvents()
	{
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			CarryOnCommonClient.checkForKeybinds();
			CarryOnCommonClient.onCarryClientTick();
		});
	}

}
