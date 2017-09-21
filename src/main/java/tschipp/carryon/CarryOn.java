package tschipp.carryon;


import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import tschipp.carryon.common.CommonProxy;
import tschipp.carryon.common.command.CommandCarryOn;
import tschipp.carryon.common.command.CommandCarryOnReload;

@EventBusSubscriber
@Mod(modid = CarryOn.MODID, name = CarryOn.NAME, version = CarryOn.VERSION, guiFactory = "tschipp.carryon.client.gui.GuiFactoryCarryOn", dependencies = "required-after:forge@[13.20.1.2386,)", updateJSON = CarryOn.UPDATE_JSON)
public class CarryOn {

	@SidedProxy(clientSide = "tschipp.carryon.client.ClientProxy", serverSide = "tschipp.carryon.common.CommonProxy")
	public static CommonProxy proxy;

	// Instance
	@Instance(CarryOn.MODID)
	public static CarryOn instance;

	public static final String MODID = "carryon";
	public static final String VERSION = "1.5.1";
	public static final String NAME = "Carry On";
	public static final String UPDATE_JSON = "https://gist.githubusercontent.com/Tschipp/dccadee7c90d7a34e6e76a35d9d6fa2e/raw/";
	public static final Logger LOGGER = LogManager.getFormatterLogger("CarryOn");
	public static File CONFIGURATION_FILE;
 
	public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		CarryOn.proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		CarryOn.proxy.init(event);		
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		CarryOn.proxy.postInit(event);
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandCarryOn());
		event.registerServerCommand(new CommandCarryOnReload());

	}
	
	public static File getMcDir()
	{
		if (FMLCommonHandler.instance().getMinecraftServerInstance() != null && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
		{
			return new File(".");
		}
		return Minecraft.getMinecraft().mcDataDir;
	}

}