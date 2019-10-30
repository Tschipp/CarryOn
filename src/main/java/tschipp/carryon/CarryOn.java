package tschipp.carryon;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import tschipp.carryon.common.CommonProxy;
import tschipp.carryon.common.command.CommandCarryOn;
import tschipp.carryon.common.command.CommandCarryOnReload;
import tschipp.carryon.common.handler.ListHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;

@EventBusSubscriber
@Mod(modid = CarryOn.MODID, name = CarryOn.NAME, version = CarryOn.VERSION, guiFactory = "tschipp.carryon.client.gui.GuiFactoryCarryOn", dependencies = CarryOn.DEPENDENCIES, updateJSON = CarryOn.UPDATE_JSON, acceptedMinecraftVersions = CarryOn.ACCEPTED_VERSIONS, certificateFingerprint = CarryOn.CERTIFICATE_FINGERPRINT)
public class CarryOn {

	@SidedProxy(clientSide = "tschipp.carryon.client.ClientProxy", serverSide = "tschipp.carryon.common.CommonProxy")
	public static CommonProxy proxy;

	// Instance
	@Instance(CarryOn.MODID)
	public static CarryOn instance;

	public static final String MODID = "carryon";
	public static final String VERSION = "1.12.3";
	public static final String NAME = "Carry On";
	public static final String ACCEPTED_VERSIONS = "[1.12.2,1.13)";
	public static final String UPDATE_JSON = "https://gist.githubusercontent.com/Tschipp/dccadee7c90d7a34e6e76a35d9d6fa2e/raw/";
	public static final Logger LOGGER = LogManager.getFormatterLogger("CarryOn");
	public static final String DEPENDENCIES = "required-after:forge@[13.20.1.2386,);after:gamestages;";
	public static final String CERTIFICATE_FINGERPRINT = "55e88f24d04398481ae6f1ce76f65fd776f14227";
	public static File CONFIGURATION_FILE;
 
	public static boolean FINGERPRINT_VIOLATED = false;
	
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
	
	@EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        
		LOGGER.error("WARNING! Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with! If you didn't download the file from https://minecraft.curseforge.com/projects/carry-on or through any kind of mod launcher, immediately delete the file and re-download it from https://minecraft.curseforge.com/projects/carry-on");
		FINGERPRINT_VIOLATED = true;
	}
	
	@EventHandler
	public void imcEvent(IMCEvent event)
	{		
		ImmutableList<IMCMessage> messages = event.getMessages();
		
		messages.forEach((msg) -> {

			String method = msg.key;
			String str = msg.getStringValue();
			
			switch (method)
			{
			case "blacklistBlock":
				ListHandler.FORBIDDEN_TILES.add(str);
				break;
			case "blacklistEntity":
				ListHandler.FORBIDDEN_ENTITIES.add(str);
				break;
			case "whitelistBlock":
				ListHandler.ALLOWED_TILES.add(str);
				break;
			case "whitelistEntity":
				ListHandler.ALLOWED_ENTITIES.add(str);
				break;
			case "blacklistStacking":
				ListHandler.FORBIDDEN_STACKING.add(str);
				break;
			case "whitelistStacking":
				ListHandler.ALLOWED_STACKING.add(str);
				break;
			case "addModelOverride":
				ModelOverridesHandler.parseOverride(str, 0);
				break;
			}

		});

	}

}