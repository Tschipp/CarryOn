package tschipp.carryon.common.handler;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.registries.ForgeRegistries;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.config.Configs.Blacklist;
import tschipp.carryon.common.config.Configs.WhiteList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListHandler
{
	public static List<String> FORBIDDEN_TILES = new ArrayList<>();
	public static List<String> FORBIDDEN_ENTITIES = new ArrayList<>();
	public static List<String> ALLOWED_ENTITIES = new ArrayList<>();
	public static List<String> ALLOWED_TILES = new ArrayList<>();
	public static List<String> FORBIDDEN_STACKING = new ArrayList<>();
	public static List<String> ALLOWED_STACKING = new ArrayList<>();

	public static List<TagKey<Block>> FORBIDDEN_TILES_TAGS = new ArrayList<>();
	public static List<TagKey<EntityType<?>>> FORBIDDEN_ENTITIES_TAGS = new ArrayList<>();
	public static List<TagKey<EntityType<?>>> ALLOWED_ENTITIES_TAGS = new ArrayList<>();
	public static List<TagKey<Block>> ALLOWED_TILES_TAGS = new ArrayList<>();
	public static List<TagKey<EntityType<?>>> FORBIDDEN_STACKING_TAGS = new ArrayList<>();
	public static List<TagKey<EntityType<?>>> ALLOWED_STACKING_TAGS = new ArrayList<>();
	public static List<Runnable> IMCMessages = new ArrayList<>();

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

			for (TagKey<Block> tag : FORBIDDEN_TILES_TAGS)
			{
				if (block.defaultBlockState().is(tag))
					return true;
			}

			return contains;
		}
	}

	public static boolean isForbidden(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		boolean contains = FORBIDDEN_ENTITIES.contains(name);

		for (TagKey<EntityType<?>> tag : FORBIDDEN_ENTITIES_TAGS)
		{
			if (entity.getType().is(tag))
				return true;
		}

		return contains;
	}

	public static boolean isAllowed(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		boolean contains = ALLOWED_ENTITIES.contains(name);

		for (TagKey<EntityType<?>> tag : ALLOWED_ENTITIES_TAGS)
		{
			if (entity.getType().is(tag))
				return true;
		}

		return contains;
	}

	public static boolean isStackingForbidden(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		boolean contains = FORBIDDEN_STACKING.contains(name);

		for (TagKey<EntityType<?>> tag : FORBIDDEN_STACKING_TAGS)
		{
			if (entity.getType().is(tag))
				return true;
		}

		return contains;
	}

	public static boolean isStackingAllowed(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		boolean contains = ALLOWED_STACKING.contains(name);

		for (TagKey<EntityType<?>> tag : ALLOWED_STACKING_TAGS)
		{
			if (entity.getType().is(tag))
				return true;
		}

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

			for (TagKey<Block> tag : ALLOWED_TILES_TAGS)
			{
				if (block.defaultBlockState().is(tag))
					return true;
			}

			return contains;	
		}

	}

	private static void processIMC() {
		for(Runnable r : IMCMessages)
			r.run();
	}

	@SuppressWarnings("deprecation")
	public static void initConfigLists()
	{
		FORBIDDEN_ENTITIES.clear();
		FORBIDDEN_ENTITIES_TAGS.clear();
		FORBIDDEN_STACKING.clear();
		FORBIDDEN_STACKING_TAGS.clear();
		FORBIDDEN_TILES.clear();
		FORBIDDEN_TILES_TAGS.clear();
		ALLOWED_ENTITIES.clear();
		ALLOWED_ENTITIES_TAGS.clear();
		ALLOWED_STACKING.clear();
		ALLOWED_STACKING_TAGS.clear();
		ALLOWED_TILES.clear();
		ALLOWED_TILES_TAGS.clear();

		processIMC();

		List<String> forbidden = new ArrayList<>(Blacklist.forbiddenTiles.get());
		forbidden.add("#carryon:block_blacklist");

		for (int i = 0; i < forbidden.size(); i++)
		{
			if (!forbidden.get(i).startsWith("#"))
				FORBIDDEN_TILES.add(forbidden.get(i));
		}

		List<String> forbiddenEntity = new ArrayList<>(Blacklist.forbiddenEntities.get());
		forbiddenEntity.add("#carryon:entity_blacklist");

		for (int i = 0; i < forbiddenEntity.size(); i++)
		{
			if (!forbiddenEntity.get(i).startsWith("#"))
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
		}

		List<String> allowedEntities = new ArrayList<>(WhiteList.allowedEntities.get());
		allowedEntities.add("#carryon:entity_whitelist");
		for (int i = 0; i < allowedEntities.size(); i++)
		{
			if (!allowedEntities.get(i).startsWith("#"))
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
		}

		List<String> allowedBlocks = new ArrayList<>(WhiteList.allowedBlocks.get());
		allowedBlocks.add("#carryon:block_whitelist");
		for (int i = 0; i < allowedBlocks.size(); i++)
		{
			if (!allowedBlocks.get(i).startsWith("#"))
				ALLOWED_TILES.add(allowedBlocks.get(i));
		}

		List<String> forbiddenStacking = new ArrayList<>(Blacklist.forbiddenStacking.get());
		forbiddenStacking.add("#carryon:stacking_blacklist");

		for (int i = 0; i < forbiddenStacking.size(); i++)
		{
			if (!forbiddenStacking.get(i).startsWith("#"))
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
		}

		List<String> allowedStacking = new ArrayList<>(WhiteList.allowedStacking.get());
		allowedStacking.add("#carryon:stacking_whitelist");
		for (int i = 0; i < allowedStacking.size(); i++)
		{
			if (!allowedStacking.get(i).startsWith("#"))
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

		Map<ResourceLocation, TagKey<Block>> blocktags = Registry.BLOCK.getTagNames().collect(Collectors.toMap(t -> t.location(), t -> t));
		Map<ResourceLocation, TagKey<EntityType<?>>> entitytags = Registry.ENTITY_TYPE.getTagNames().collect(Collectors.toMap(t -> t.location(), t -> t));

		
		
//		System.out.println(blocktags.getAvailableTags());

		for (String s : forbidden)
		{
			if (s.startsWith("#"))
			{
				String sub = s.substring(1);
				TagKey<Block> tag = blocktags.get(new ResourceLocation(sub));
				if (tag != null)
					FORBIDDEN_TILES_TAGS.add(tag);
			}
		}

		for (String s : allowedBlocks)
		{
			if (s.startsWith("#"))
			{
				TagKey<Block> tag = blocktags.get(new ResourceLocation(s.substring(1)));
				if (tag != null)
					ALLOWED_TILES_TAGS.add(tag);
			}
		}

		for (String s : forbiddenEntity)
		{
			if (s.startsWith("#"))
			{
				TagKey<EntityType<?>> tag = entitytags.get(new ResourceLocation(s.substring(1)));
				if (tag != null)
					FORBIDDEN_ENTITIES_TAGS.add(tag);
			}
		}

		for (String s : allowedEntities)
		{
			if (s.startsWith("#"))
			{
				TagKey<EntityType<?>> tag = entitytags.get(new ResourceLocation(s.substring(1)));
				if (tag != null)
					ALLOWED_ENTITIES_TAGS.add(tag);
			}
		}

		for (String s : forbiddenStacking)
		{
			if (s.startsWith("#"))
			{
				TagKey<EntityType<?>> tag = entitytags.get(new ResourceLocation(s.substring(1)));
				if (tag != null)
					FORBIDDEN_STACKING_TAGS.add(tag);
			}
		}

		for (String s : allowedStacking)
		{
			if (s.startsWith("#"))
			{
				TagKey<EntityType<?>> tag = entitytags.get(new ResourceLocation(s.substring(1)));
				if (tag != null)
					ALLOWED_STACKING_TAGS.add(tag);
			}
		}
	}

	public static boolean containsAll(String str, String... strings)
	{
		boolean containsAll = true;

		for (String s : strings)
		{
			if (!str.contains(s))
				containsAll = false;
		}

		return containsAll;
	}

}
