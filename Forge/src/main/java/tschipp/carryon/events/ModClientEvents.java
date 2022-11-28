package tschipp.carryon.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tschipp.carryon.Constants;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;

@Mod.EventBusSubscriber(bus = Bus.MOD, modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents
{
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerKeybinds(RegisterKeyMappingsEvent event)
	{
		CarryOnKeybinds.registerKeybinds(event::register);
	}
}
