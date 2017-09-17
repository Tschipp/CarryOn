package tschipp.carryon.common.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import tschipp.carryon.common.config.CarryOnConfig;

public class ListHandler
{
	public static List<String> FORBIDDEN_TILES;
	public static List<String> FORBIDDEN_ENTITIES;
	public static List<String> ALLOWED_ENTITIES;
	public static List<String> ALLOWED_TILES;

	public static boolean isForbidden(Block block)
	{
		return FORBIDDEN_TILES.contains(block.getRegistryName().toString());
	}

	public static boolean isForbidden(Entity entity)
	{
		if (EntityList.getKey(entity) != null)
		{
			String name = EntityList.getKey(entity).toString();
			boolean contains = FORBIDDEN_ENTITIES.contains(name);
			return contains;
		}
		return true;
	}

	public static boolean isAllowed(Entity entity){
		if (EntityList.getKey(entity) != null)
		{
			String name = EntityList.getKey(entity).toString();
			boolean contains = ALLOWED_ENTITIES.contains(name);
			return contains;
		}
		return true;
	}
	
	public static boolean isAllowed(Block block)
	{
		return ALLOWED_TILES.contains(block.getRegistryName().toString());
	}
	
	public static void initForbiddenTiles()
	{
		String[] forbidden = CarryOnConfig.blacklist.forbiddenTiles;
		FORBIDDEN_TILES = new ArrayList<String>();

		for (int i = 0; i < forbidden.length; i++)
		{
			if (forbidden[i].contains("*"))
			{
				String modid = forbidden[i].replace("*", "");
				for (int k = 0; k < Block.REGISTRY.getKeys().size(); k++)
				{
					if (Block.REGISTRY.getKeys().toArray()[k].toString().contains(modid))
					{
						FORBIDDEN_TILES.add(Block.REGISTRY.getKeys().toArray()[k].toString());
					}
				}
			}
			FORBIDDEN_TILES.add(forbidden[i]);
		}

		String[] forbiddenEntity = CarryOnConfig.blacklist.forbiddenEntities;
		FORBIDDEN_ENTITIES = new ArrayList<String>();

		for (int i = 0; i < forbiddenEntity.length; i++)
		{
			if (forbiddenEntity[i].contains("*"))
			{
				String modid = forbiddenEntity[i].replace("*", "");
				for (int k = 0; k < ForgeRegistries.ENTITIES.getKeys().size(); k++)
				{
					if (ForgeRegistries.ENTITIES.getKeys().toArray()[k].toString().contains(modid))
					{
						FORBIDDEN_ENTITIES.add(ForgeRegistries.ENTITIES.getKeys().toArray()[k].toString());
					}
				}
			}
			FORBIDDEN_ENTITIES.add(forbiddenEntity[i]);
		}
		
		String [] allowedEntities=CarryOnConfig.whitelist.allowedEntities;
		ALLOWED_ENTITIES=new ArrayList<String>();
		for(int i=0;i<allowedEntities.length;i++){
			if(allowedEntities[i].contains("*"))
			{
				String modid=allowedEntities[i].replace("*", "");
				for(int k=0;k<ForgeRegistries.ENTITIES.getKeys().size();k++)
				{
					if(ForgeRegistries.ENTITIES.getKeys().toArray()[k].toString().contains(modid)){
						ALLOWED_ENTITIES.add(ForgeRegistries.ENTITIES.getKeys().toArray()[k].toString());
					}
				}
			}
			ALLOWED_ENTITIES.add(allowedEntities[i]);
		}
		
		String[] allowedBlocks = CarryOnConfig.whitelist.allowedBlocks;
		ALLOWED_TILES = new ArrayList<String>();

		for (int i = 0; i < allowedBlocks.length; i++)
		{
			if (allowedBlocks[i].contains("*"))
			{
				String modid = allowedBlocks[i].replace("*", "");
				for (int k = 0; k < Block.REGISTRY.getKeys().size(); k++)
				{
					if (Block.REGISTRY.getKeys().toArray()[k].toString().contains(modid))
					{
						ALLOWED_TILES.add(Block.REGISTRY.getKeys().toArray()[k].toString());
					}
				}
			}
			ALLOWED_TILES.add(allowedBlocks[i]);
		}
	}

}
