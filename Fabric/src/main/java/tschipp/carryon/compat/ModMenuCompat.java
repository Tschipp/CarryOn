package tschipp.carryon.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.fabric.ConfigLoaderImpl;
import tschipp.carryon.platform.Services;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {

        if (!Services.PLATFORM.isModLoaded("cloth-config"))
            return (parent) -> null;

        BuiltConfig[] configs = ConfigLoaderImpl.CONFIGS.values().toArray(new BuiltConfig[0]);

        return (parent) -> ClothConfigCompatFabric.createScreen(configs[1], configs[0], parent);
    }
}
