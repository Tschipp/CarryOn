package tschipp.carryon;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tschipp.carryon.items.ItemEntity;
import tschipp.carryon.items.ItemTile;

public class RegistryHandler {

    public static Item TILE_ITEM;
    public static Item ENTITY_ITEM;

    public static void regItems()
    {
        TILE_ITEM = register(new ItemTile(), "tile_item");
        ENTITY_ITEM = register(new ItemEntity(), "entity_item");
    }

    private static Item register(Item item, String name)
    {
       return Registry.register(Registry.ITEM, new Identifier(CarryOn.MODID, name), item);
    }

}