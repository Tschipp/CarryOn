package tschipp.carryon;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tschipp.carryon.common.config.CarryConfig;

public class Constants {

	public static final String MOD_ID = "carryon";
	public static final String MOD_NAME = "Carry On";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final CarryConfig.Common COMMON_CONFIG = new CarryConfig.Common();
	public static final CarryConfig.Client CLIENT_CONFIG = new CarryConfig.Client();

	public static final ResourceLocation PACKET_ID_KEY_PRESSED =  new ResourceLocation(Constants.MOD_ID, "key_pressed");
	public static final ResourceLocation PACKET_ID_START_RIDING =  new ResourceLocation(Constants.MOD_ID, "start_riding");
	public static final ResourceLocation PACKET_ID_SYNC_SCRIPTS =  new ResourceLocation(Constants.MOD_ID, "sync_scripts");

}