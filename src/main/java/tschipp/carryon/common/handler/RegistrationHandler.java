package tschipp.carryon.common.handler;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.event.RenderEntityEvents;
import tschipp.carryon.client.event.RenderEvents;
import tschipp.carryon.common.event.ItemEntityEvents;
import tschipp.carryon.common.event.ItemEvents;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;

public class RegistrationHandler
{
	public static Item itemTile;
	public static Item itemEntity;

	public static void regItems()
	{
		itemTile = new ItemTile();
		itemEntity = new ItemEntity();
	}
	
	public static void regItemRenders()
	{
		ModelLoader.setCustomModelResourceLocation(itemTile, 0, new ModelResourceLocation(CarryOn.MODID + ":" + "tile", "inventory"));
		ModelLoader.setCustomModelResourceLocation(itemEntity, 0, new ModelResourceLocation(CarryOn.MODID + ":" + "tile", "inventory"));
	}
	
	public static void regCommonEvents()
	{
		MinecraftForge.EVENT_BUS.register(new ItemEvents());
		MinecraftForge.EVENT_BUS.register(new ItemEntityEvents());
	}
	
	public static void regClientEvents()
	{
		MinecraftForge.EVENT_BUS.register(new RenderEvents());
		MinecraftForge.EVENT_BUS.register(new RenderEntityEvents());
	}
	
	public static void regOverrideList()
	{
		ModelOverridesHandler.initOverrides();
		CustomPickupOverrideHandler.initPickupOverrides();
		ForbiddenTileHandler.initForbiddenTiles();
	}
	
	

}
