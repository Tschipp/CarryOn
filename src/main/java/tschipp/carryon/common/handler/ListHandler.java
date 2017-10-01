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
		String name = block.getRegistryName().toString();
		if (FORBIDDEN_TILES.contains(name))
			return true;
		else
		{
			boolean contains = false;
			for (String s : FORBIDDEN_TILES)
			{
				if (s.contains("*"))
				{
					if(name.contains(s.replace("*", "")))
						contains = true;
				}
			}
			
			return contains;
		}
	}

	public static boolean isForbidden(Entity entity)
	{
		if (EntityList.getEntityString(entity) != null)
		{
			String name = EntityList.getEntityString(entity);
			boolean contains = FORBIDDEN_ENTITIES.contains(name);
			return contains;
		}
		return true;
	}

	public static boolean isAllowed(Entity entity)
	{
		if (EntityList.getEntityString(entity) != null)
		{
			String name = EntityList.getEntityString(entity).toString();
			boolean contains = ALLOWED_ENTITIES.contains(name);
			return contains;
		}
		return true;
	}

	public static boolean isAllowed(Block block)
	{
		String name = block.getRegistryName().toString();
		if (ALLOWED_TILES.contains(name))
			return true;
		else
		{
			boolean contains = false;
			for (String s : ALLOWED_TILES)
			{
				if (s.contains("*"))
				{
					if(name.contains(s.replace("*", "")))
						contains = true;
				}
			}
			return contains;
		}

	}

	public static void initForbiddenTiles()
	{
		String[] forbidden = CarryOnConfig.blacklist.forbiddenTiles;
		FORBIDDEN_TILES = new ArrayList<String>();

		for (int i = 0; i < forbidden.length; i++)
		{
			FORBIDDEN_TILES.add(forbidden[i]);
		}

		String[] forbiddenEntity = CarryOnConfig.blacklist.forbiddenEntities;
		FORBIDDEN_ENTITIES = new ArrayList<String>();

		for (int i = 0; i < forbiddenEntity.length; i++)
		{
			if (forbiddenEntity[i].contains("*"))
			{
				String modid = forbiddenEntity[i].replace("*", "");
				for (int k = 0; k < EntityList.getEntityNameList().size(); k++)
				{
					if (EntityList.getEntityNameList().get(k).contains(modid))
					{
						FORBIDDEN_ENTITIES.add(EntityList.getEntityNameList().get(k));
					}
				}
			}
			FORBIDDEN_ENTITIES.add(forbiddenEntity[i]);
		}

		String[] allowedEntities = CarryOnConfig.whitelist.allowedEntities;
		ALLOWED_ENTITIES = new ArrayList<String>();
		for (int i = 0; i < allowedEntities.length; i++)
		{
			if (allowedEntities[i].contains("*"))
			{
				String modid=allowedEntities[i].replace("*", "");
				for (int k = 0; k < EntityList.getEntityNameList().size(); k++)
				{
					if (EntityList.getEntityNameList().get(k).contains(modid))
						ALLOWED_ENTITIES.add(EntityList.getEntityNameList().get(k));
					
				
				}
			}
			ALLOWED_ENTITIES.add(allowedEntities[i]);
		}

		String[] allowedBlocks = CarryOnConfig.whitelist.allowedBlocks;
		ALLOWED_TILES = new ArrayList<String>();
		for (int i = 0; i < allowedBlocks.length; i++)
		{
			ALLOWED_TILES.add(allowedBlocks[i]);
		}
	}

}
