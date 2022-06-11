package tschipp.carryon.common.handler;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.event.RenderEntityEvents;
import tschipp.carryon.client.event.RenderEvents;
import tschipp.carryon.common.capabilities.IPosition;
import tschipp.carryon.common.capabilities.event.PositionClientEvents;
import tschipp.carryon.common.capabilities.event.PositionCommonEvents;
import tschipp.carryon.common.event.IMCEvents;
import tschipp.carryon.common.event.ItemEntityEvents;
import tschipp.carryon.common.event.ItemEvents;
import tschipp.carryon.common.item.ItemCarryonBlock;
import tschipp.carryon.common.item.ItemCarryonEntity;

@EventBusSubscriber(modid = CarryOn.MODID, bus = Bus.MOD)
public class RegistrationHandler
{
	
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CarryOn.MODID);

	public static final RegistryObject<Item> itemTile = ITEMS.register("tile_item", () -> new ItemCarryonBlock());
	public static final RegistryObject<Item> itemEntity = ITEMS.register("entity_item", () -> new ItemCarryonEntity());


	public static void init() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	public static void regCommonEvents()
	{
		MinecraftForge.EVENT_BUS.register(new ItemEvents());
		MinecraftForge.EVENT_BUS.register(new ItemEntityEvents());
		MinecraftForge.EVENT_BUS.register(new PositionCommonEvents());
		MinecraftForge.EVENT_BUS.register(new IMCEvents());
	}

	public static void regClientEvents()
	{
		MinecraftForge.EVENT_BUS.register(new RenderEvents());
		MinecraftForge.EVENT_BUS.register(new RenderEntityEvents());
		MinecraftForge.EVENT_BUS.register(new PositionClientEvents());

		// if(ModList.get().isLoaded("obfuscate"))
		// MinecraftForge.EVENT_BUS.register(new ObfuscateEvents());

	}

	public static void regOverrideList()
	{
		ModelOverridesHandler.initOverrides();
		CustomPickupOverrideHandler.initPickupOverrides();
		// ListHandler.initConfigLists();
	}

	@SubscribeEvent
	public static void regCaps(RegisterCapabilitiesEvent event)
	{
		event.register(IPosition.class);
	}

}
