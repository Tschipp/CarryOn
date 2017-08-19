package tschipp.carryon.common.config;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Optional;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.helper.ReflectionUtil;

@Config(modid = CarryOn.MODID)
public class CarryOnConfig {

	@Config.LangKey(CarryOn.MODID)
	@Config.Comment("General Mod Settings")
	public static Configs.Settings settings = new Configs.Settings();
	
	@Config.LangKey(CarryOn.MODID)
	@Config.Comment("Blacklist for Blocks and Entities")
	public static Configs.Blacklist blacklist = new Configs.Blacklist();
	
	@Config.LangKey(CarryOn.MODID)
	@Config.Comment("Model Overrides based on NBT or on Meta. Advanced Users Only!")
	public static Configs.ModelOverrides modelOverrides = new Configs.ModelOverrides();
	
	@Config.LangKey(CarryOn.MODID)
	@Config.Comment("Custom Pickup Conditions for certain blocks. ONLY WORKS WHEN GAMESTAGES IS INSTALLED! Advanced Users Only!")
	public static Configs.CustomPickupConditions customPickupConditions = new Configs.CustomPickupConditions();

	@Mod.EventBusSubscriber
	public static class EventHandler {

		/**
		 * The {@link ConfigManager#CONFIGS} getter.
		 */
		private static final MethodHandle CONFIGS_GETTER = ReflectionUtil.findFieldGetter(ConfigManager.class, "CONFIGS");
		

		/**
		 * The {@link Configuration} instance.
		 */
		private static Configuration configuration;

		/**
		 * Get the {@link Configuration} instance from {@link ConfigManager}.
		 * <p>
		 * TODO: Use a less hackish method of getting the
		 * {@link Configuration}/{@link IConfigElement}s when possible.
		 *
		 * @return The Configuration instance
		 */
		public static Configuration getConfiguration() {
			if (EventHandler.configuration == null)
				try {
					final String fileName = CarryOn.MODID + ".cfg";

					@SuppressWarnings("unchecked")
					final Map<String, Configuration> configsMap = (Map<String, Configuration>) EventHandler.CONFIGS_GETTER
							.invokeExact();

					final Optional<Map.Entry<String, Configuration>> entryOptional = configsMap.entrySet().stream()
							.filter(entry -> fileName.equals(new File(entry.getKey()).getName())).findFirst();

					entryOptional
							.ifPresent(stringConfigurationEntry -> EventHandler.configuration = stringConfigurationEntry
									.getValue());
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}

			return EventHandler.configuration;
		}

		/**
		 * Inject the new values and save to the config file when the config has
		 * been changed from the GUI.
		 *
		 * @param event
		 *            The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(CarryOn.MODID))
				ConfigManager.load(CarryOn.MODID, Config.Type.INSTANCE);

		}

	}

}
