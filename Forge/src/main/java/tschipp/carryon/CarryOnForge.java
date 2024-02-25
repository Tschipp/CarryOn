package tschipp.carryon;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import tschipp.carryon.config.forge.ConfigLoaderImpl;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CarryOnForge {

    public static SimpleChannel network;

    public CarryOnForge() {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
        // Use Forge to bootstrap the Common mod.
        CarryOnCommon.registerConfig();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        ConfigLoaderImpl.initialize();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        network = ChannelBuilder.named(new ResourceLocation(Constants.MOD_ID, "carryonpackets")).simpleChannel();

        CarryOnCommon.registerServerPackets();
        CarryOnCommon.registerClientPackets();
    }

}