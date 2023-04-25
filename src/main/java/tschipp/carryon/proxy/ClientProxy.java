package tschipp.carryon.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.helper.KeyboardCallbackWrapper;
import tschipp.carryon.common.helper.ScrollCallbackWrapper;


@EventBusSubscriber(modid = CarryOn.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientProxy
{
	@SubscribeEvent
	public static void setup(FMLClientSetupEvent event)
	{
		RegistrationHandler.regClientEvents();

		CarryOnKeybinds.init();

		new ScrollCallbackWrapper().setup(Minecraft.getInstance());
		new KeyboardCallbackWrapper().setup(Minecraft.getInstance());
	}

	public static PlayerEntity getPlayer()
	{
		return Minecraft.getInstance().player;
	}

	public static World getWorld()
	{
		return Minecraft.getInstance().level;
	}
}
