package tschipp.carryon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import tschipp.carryon.keybinds.CarryOnKeybinds;


public class CarryOn implements ModInitializer {

	public static String MODID = "carryon";
	public static final Logger LOGGER = LogManager.getFormatterLogger("CarryOn");


	@Override
	public void onInitialize() {
		RegistryHandler.regItems();
		CarryOnKeybinds.init();
	}


	
}
