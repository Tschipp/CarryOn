package tschipp.carryon.common.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import tschipp.carryon.common.config.Configs.Blacklist;
import tschipp.carryon.common.config.Configs.WhiteList;

public class ListHandler
{
	public static List<String> FORBIDDEN_TILES;
	public static List<String> FORBIDDEN_ENTITIES;
	public static List<String> ALLOWED_ENTITIES;
	public static List<String> ALLOWED_TILES;
	public static List<String> FORBIDDEN_STACKING;
	public static List<String> ALLOWED_STACKING;

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
					String[] filter = s.replace("*", ",").split(",");
					if (containsAll(name, filter))
						contains = true;
				}
			}

			return contains;
		}
	}

	public static boolean isForbidden(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		boolean contains = FORBIDDEN_ENTITIES.contains(name);
		return contains;
	}

	public static boolean isAllowed(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		boolean contains = ALLOWED_ENTITIES.contains(name);
		return contains;
	}

	public static boolean isStackingForbidden(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		boolean contains = FORBIDDEN_STACKING.contains(name);
		return contains;
	}

	public static boolean isStackingAllowed(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		boolean contains = ALLOWED_STACKING.contains(name);
		return contains;
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
					String[] filter = s.replace("*", ",").split(",");
					if (containsAll(name, filter))
						contains = true;
				}
			}

			return contains;
		}

	}

	@SuppressWarnings("unchecked")
	public static void initLists()
	{
		List<String> forbidden = (List<String>) Blacklist.forbiddenTiles.get();
		FORBIDDEN_TILES = new ArrayList<String>();

		for (int i = 0; i < forbidden.size(); i++)
		{
			FORBIDDEN_TILES.add(forbidden.get(i));
		}

		List<String> forbiddenEntity = (List<String>) Blacklist.forbiddenEntities.get();
		FORBIDDEN_ENTITIES = new ArrayList<String>();

		for (int i = 0; i < forbiddenEntity.size(); i++)
		{
			if (forbiddenEntity.get(i).contains("*"))
			{
				String[] filter = forbiddenEntity.get(i).replace("*", ",").split(",");

				ResourceLocation[] keys = ForgeRegistries.ENTITIES.getKeys().toArray(new ResourceLocation[0]);
				for (ResourceLocation key : keys)
				{
					if (containsAll(key.toString(), filter))
					{
						FORBIDDEN_ENTITIES.add(key.toString());
					}
				}
			}
			FORBIDDEN_ENTITIES.add(forbiddenEntity.get(i));
		}

		List<String> allowedEntities = (List<String>) WhiteList.allowedEntities.get();
		ALLOWED_ENTITIES = new ArrayList<String>();
		for (int i = 0; i < allowedEntities.size(); i++)
		{
			if (allowedEntities.get(i).contains("*"))
			{
				String[] filter = allowedEntities.get(i).replace("*", ",").split(",");

				ResourceLocation[] keys = ForgeRegistries.ENTITIES.getKeys().toArray(new ResourceLocation[0]);
				for (ResourceLocation key : keys)
				{
					if (containsAll(key.toString(), filter))
					{
						ALLOWED_ENTITIES.add(key.toString());
					}
				}
			}
			ALLOWED_ENTITIES.add(allowedEntities.get(i));
		}

		List<String> allowedBlocks = (List<String>) WhiteList.allowedBlocks.get();
		ALLOWED_TILES = new ArrayList<String>();
		for (int i = 0; i < allowedBlocks.size(); i++)
		{
			ALLOWED_TILES.add(allowedBlocks.get(i));
		}

		List<String> forbiddenStacking = (List<String>) Blacklist.forbiddenStacking.get();
		FORBIDDEN_STACKING = new ArrayList<String>();

		for (int i = 0; i < forbiddenStacking.size(); i++)
		{
			if (forbiddenStacking.get(i).contains("*"))
			{
				String[] filter = forbiddenStacking.get(i).replace("*", ",").split(",");

				ResourceLocation[] keys = ForgeRegistries.ENTITIES.getKeys().toArray(new ResourceLocation[0]);
				for (ResourceLocation key : keys)
				{
					if (containsAll(key.toString(), filter))
					{
						FORBIDDEN_STACKING.add(key.toString());
					}
				}
			}
			FORBIDDEN_STACKING.add(forbiddenStacking.get(i));
		}

		List<String> allowedStacking = (List<String>) WhiteList.allowedStacking.get();
		ALLOWED_STACKING = new ArrayList<String>();
		for (int i = 0; i < allowedStacking.size(); i++)
		{
			if (allowedStacking.get(i).contains("*"))
			{
				String[] filter = allowedStacking.get(i).replace("*", ",").split(",");

				ResourceLocation[] keys = ForgeRegistries.ENTITIES.getKeys().toArray(new ResourceLocation[0]);
				for (ResourceLocation key : keys)
				{
					if (containsAll(key.toString(), filter))
					{
						ALLOWED_STACKING.add(key.toString());
					}
				}
			}
			ALLOWED_STACKING.add(allowedStacking.get(i));
		}
	}
	
	public static boolean containsAll(String str, String... strings)
	{
		boolean containsAll = true;
		
		for(String s : strings)
		{
			if(!str.contains(s))
				containsAll = false;
		}
		
		return containsAll;
	}

}
