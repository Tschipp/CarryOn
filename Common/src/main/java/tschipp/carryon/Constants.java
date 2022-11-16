package tschipp.carryon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tschipp.carryon.common.config.CarryConfig;
import tschipp.carryon.config.ConfigLoader;

public class Constants {

	public static final String MOD_ID = "carryon";
	public static final String MOD_NAME = "Carry On";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final CarryConfig.Common COMMON_CONFIG = new CarryConfig.Common();
	public static final CarryConfig.Client CLIENT_CONFIG = new CarryConfig.Client();

	static {
		ConfigLoader.registerConfig(COMMON_CONFIG);
		ConfigLoader.registerConfig(CLIENT_CONFIG);
	}
}