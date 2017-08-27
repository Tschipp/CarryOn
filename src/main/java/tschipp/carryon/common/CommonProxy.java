package tschipp.carryon.common;

import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.network.server.SyncKeybindPacket;
import tschipp.carryon.network.server.SyncKeybindPacketHandler;

public class CommonProxy
{

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		CarryOn.network = NetworkRegistry.INSTANCE.newSimpleChannel("CarryOn");
		
		CarryOn.network.registerMessage(SyncKeybindPacketHandler.class, SyncKeybindPacket.class, 0, Side.SERVER);
		
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
