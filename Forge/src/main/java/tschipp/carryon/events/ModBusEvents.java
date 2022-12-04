package tschipp.carryon.events;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.InterModComms.IMCMessage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import tschipp.carryon.Constants;
import tschipp.carryon.client.modeloverride.ModelOverrideHandler;
import tschipp.carryon.common.config.ListHandler;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public class ModBusEvents {

	@SubscribeEvent(priority = EventPriority.LOW)
	public void serverLoad(FMLDedicatedServerSetupEvent event)
	{
		Stream<IMCMessage> messages = InterModComms.getMessages(Constants.MOD_ID);

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
