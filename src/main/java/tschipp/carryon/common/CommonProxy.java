package tschipp.carryon.common;

import java.io.FileNotFoundException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.nbt.NBTException;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.scripting.ScriptReader;
import tschipp.carryon.network.client.CarrySlotPacket;
import tschipp.carryon.network.client.CarrySlotPacketHandler;
import tschipp.carryon.network.server.SyncKeybindPacket;
import tschipp.carryon.network.server.SyncKeybindPacketHandler;

public class CommonProxy
{

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ScriptReader.preInit(event);
		
		CarryOn.network = NetworkRegistry.INSTANCE.newSimpleChannel("CarryOn");
		
		CarryOn.network.registerMessage(SyncKeybindPacketHandler.class, SyncKeybindPacket.class, 0, Side.SERVER);
		CarryOn.network.registerMessage(CarrySlotPacketHandler.class, CarrySlotPacket.class, 1, Side.CLIENT);

		RegistrationHandler.regItems();
		RegistrationHandler.regCommonEvents();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		try
		{
			ScriptReader.parseScripts();
		}
		catch (JsonIOException | JsonSyntaxException | FileNotFoundException | NBTException e)
		{
			e.printStackTrace();
		}
		RegistrationHandler.regOverrideList();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
	}

}
