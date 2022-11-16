package tschipp.carryon.platform;

import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.fabric.ConfigLoaderImpl;
import tschipp.carryon.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void registerConfig(BuiltConfig cfg) {
        ConfigLoaderImpl.registerConfig(cfg);
    }
}
