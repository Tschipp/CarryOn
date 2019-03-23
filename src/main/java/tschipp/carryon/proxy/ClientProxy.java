package tschipp.carryon.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.helper.KeyboardCallbackWrapper;
import tschipp.carryon.common.helper.ScrollCallbackWrapper;

public class ClientProxy implements IProxy {


	@Override
	public void setup(FMLCommonSetupEvent event)
	{
		RegistrationHandler.regClientEvents();
		
		CarryOnKeybinds.init();
		
		new ScrollCallbackWrapper().setup(Minecraft.getInstance());;
		new KeyboardCallbackWrapper().setup(Minecraft.getInstance());
	}

	@Override
	public EntityPlayer getPlayer()
	{
		return Minecraft.getInstance().player;
	}

	@Override
	public World getWorld()
	{
		return Minecraft.getInstance().world;
	}
}
