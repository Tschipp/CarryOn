package tschipp.carryon.common.handler;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
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
	@ObjectHolder("carryon:tile_item")
	public static Item itemTile;

	@ObjectHolder("carryon:entity_item")
	public static Item itemEntity;

	public static void regItems()
	{
		itemTile = new ItemCarryonBlock();
		itemEntity = new ItemCarryonEntity();
	}

	public static void regCommonEvents()
	{
		MinecraftForge.EVENT_BUS.register(new ItemEvents());
		MinecraftForge.EVENT_BUS.register(new ItemEntityEvents());
		MinecraftForge.EVENT_BUS.register(new PositionCommonEvents());
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
