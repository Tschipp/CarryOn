package tschipp.carryon;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import tschipp.carryon.config.neoforge.ConfigLoaderImpl;

@Mod(Constants.MOD_ID)
public class CarryOnNeoForge {

    public CarryOnNeoForge(IEventBus bus) {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
        // Use Forge to bootstrap the Common mod.
        CarryOnCommon.registerConfig();
        bus.addListener(this::setup);
        bus.addListener(this::registerPackets);

        ConfigLoaderImpl.initialize();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    public void registerPackets(final RegisterPayloadHandlerEvent event) {

        final IPayloadRegistrar registrar = event.registrar(Constants.MOD_ID)
                .versioned("1.0.0");

        CarryOnCommon.registerServerPackets(registrar);
        CarryOnCommon.registerClientPackets(registrar);
    }

}