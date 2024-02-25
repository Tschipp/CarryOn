package tschipp.carryon.events;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import tschipp.carryon.Constants;
import tschipp.carryon.client.modeloverride.ModelOverrideHandler;
import tschipp.carryon.common.config.ListHandler;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public class ModBusEvents {

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void serverLoad(InterModProcessEvent event)
	{
		Stream<InterModComms.IMCMessage> messages = InterModComms.getMessages(Constants.MOD_ID);

		messages.forEach(msg -> {

			String method = msg.method();
			Object obj = msg.messageSupplier().get();

			if (!(obj instanceof String str))
				return;


			switch (method) {
				case "blacklistBlock":
					ListHandler.addForbiddenTiles(str);
					break;
				case "blacklistEntity":
					ListHandler.addForbiddenEntities(str);
					break;
				case "whitelistBlock":
					ListHandler.addAllowedTiles(str);
					break;
				case "whitelistEntity":
					ListHandler.addAllowedEntities(str);
					break;
				case "blacklistStacking":
					ListHandler.addForbiddenStacking(str);
					break;
				case "whitelistStacking":
					ListHandler.addAllowedStacking(str);
					break;
				case "addModelOverride":
					ModelOverrideHandler.addFromString(str);
					break;
			}

		});

	}
}
