package tschipp.carryon;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import tschipp.carryon.common.CommonProxy;

@EventBusSubscriber
@Mod(modid = CarryOn.MODID, name = CarryOn.NAME, version = CarryOn.VERSION, guiFactory = "tschipp.carryon.client.gui.GuiFactoryCarryOn", dependencies = "required-after:forge@[13.20.1.2386,)", updateJSON = CarryOn.UPDATE_JSON)
public class CarryOn {

	@SidedProxy(clientSide = "tschipp.carryon.client.ClientProxy", serverSide = "tschipp.carryon.common.CommonProxy")
	public static CommonProxy proxy;

	// Instance
	@Instance(CarryOn.MODID)
	public static CarryOn instance;

	public static final String MODID = "carryon";
	public static final String VERSION = "1.2";
	public static final String NAME = "Carry On";
	public static final String UPDATE_JSON = "https://gist.githubusercontent.com/Tschipp/dccadee7c90d7a34e6e76a35d9d6fa2e/raw/bf7fb60d5e59f73eee65b271d5c01585e26a0352/update.json";
	public static final Logger LOGGER = LogManager.getFormatterLogger("CarryOn");
	
	//public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
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

}