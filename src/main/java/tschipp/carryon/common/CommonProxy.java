package tschipp.carryon.common;

import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.RegistrationHandler;

public class CommonProxy
{

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		RegistrationHandler.regItems();
		RegistrationHandler.regCommonEvents();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		RegistrationHandler.regOverrideList();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
	}

}
