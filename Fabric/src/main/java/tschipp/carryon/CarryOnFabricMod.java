package tschipp.carryon;

import net.fabricmc.api.ModInitializer;
import tschipp.carryon.config.fabric.ConfigLoaderImpl;
import tschipp.carryon.events.CommonEvents;

import java.io.IOException;

public class CarryOnFabricMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.
        CarryOnCommon.registerConfig();


        try {
            ConfigLoaderImpl.initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CommonEvents.registerEvents();
        CarryOnCommon.registerServerPackets();

    }
}
