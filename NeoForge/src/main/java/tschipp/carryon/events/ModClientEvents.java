package tschipp.carryon.events;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import tschipp.carryon.Constants;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents
{
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerKeybinds(RegisterKeyMappingsEvent event)
	{
		CarryOnKeybinds.registerKeybinds(event::register);
	}
}
