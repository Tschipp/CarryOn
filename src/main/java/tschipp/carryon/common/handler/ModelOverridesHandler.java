package tschipp.carryon.common.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.common.config.Configs.ModelOverrides;
import tschipp.carryon.common.helper.InvalidConfigException;
import tschipp.carryon.common.helper.StringParser;

public class ModelOverridesHandler
{
	public static HashMap<CompoundTag, Object> OVERRIDE_OBJECTS = new HashMap<CompoundTag, Object>();

	/*
	 * This class is really ugly, will probably be replaced by something else -
	 * Tschipp
	 */	
	public static void parseOverride(String overrideString, int i)
	{
		boolean errored = false;

		Object toOverrideObject;
		Object overrideObject;
		CompoundTag tag = new CompoundTag();

		String currentline = overrideString;
		if (StringUtils.isEmpty(currentline) || !StringUtils.contains(currentline, "->"))
			new InvalidConfigException("Missing Override Model at line " + i + " : " + currentline).printException();

		String[] sa = currentline.split("->");
		String toOverride = "";
		String override = "";
		try
		{
			toOverride = sa[0];
			override = sa[1];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			errored = true;
			new InvalidConfigException("Missing Override Model at line " + i + " : " + currentline).printException();
		}

		if (toOverride.contains("{"))
		{
			if (!toOverride.contains("}"))
			{
				errored = true;
				new InvalidConfigException("Missing } at line " + i + " : " + currentline).printException();
			}

			String nbt = toOverride.substring(toOverride.indexOf("{"));
			toOverride = toOverride.replace(nbt, "");
			try
			{
				tag = TagParser.parseTag(nbt);
			}
			catch (Exception e)
			{
				errored = true;
				new InvalidConfigException("Error while parsing NBT at line " + i + " : " + e.getMessage()).printException();
			}

		}
		else if (toOverride.contains("}"))
		{
			errored = true;
			new InvalidConfigException("Missing { at line " + i + " : " + currentline).printException();
		}

		String overridetype = "item";
		if (override.contains("("))
		{
			if (!override.contains(")"))
			{
				errored = true;
				new InvalidConfigException("Missing ) at line " + i + " : " + currentline).printException();
			}

			overridetype = override.substring(0, override.indexOf(")") + 1);
			override =override.replace(overridetype, "");
			overridetype = overridetype.replace("(", "");
			overridetype = overridetype.replace(")", "");

		}
		else if (override.contains(")"))
		{
			errored = true;
			new InvalidConfigException("Missing ( at line " + i + " : " + currentline).printException();
		}

		String modidToOverride = "minecraft";
		String modidOverride = "minecraft";

		if (toOverride.contains(":"))
			modidToOverride = toOverride.replace(toOverride.substring(toOverride.indexOf(":")), "");

		if (override.contains(":"))
			modidOverride = override.replace(override.substring(override.indexOf(":")), "");

		if ((ModList.get().isLoaded(modidOverride) || modidOverride.equals("minecraft")) && (ModList.get().isLoaded(modidToOverride) || modidToOverride.equals("minecraft")) && !errored)
		{
				toOverrideObject = StringParser.getBlockState(toOverride);

			if (toOverrideObject != null)
			{
				if (overridetype.equals("block"))
					overrideObject = StringParser.getBlockState(override);
				else
					overrideObject = StringParser.getItemStack(override);

				if (overrideObject != null)
				{
					CompoundTag keyComp = new CompoundTag();
					keyComp.put("nbttag", tag);
					if (toOverrideObject instanceof Block)
					{
						keyComp.putString("block", ((Block) toOverrideObject).getRegistryName().toString());
					}
					else
					{
						keyComp.putInt("stateid", Block.getId((BlockState) toOverrideObject));
						keyComp.putString("block", ((BlockState) toOverrideObject).getBlock().getRegistryName().toString());
					}
					OVERRIDE_OBJECTS.put(keyComp, overrideObject);
				}
			}
		}
	}
	
	
	public static void initOverrides()
	{
		@SuppressWarnings("unchecked")
		List<String> overrides = (List<String>) ModelOverrides.modelOverrides.get();

		for (int i = 0; i < overrides.size(); i++)
		{
			parseOverride(overrides.get(i), i);
		}
	}

	public static boolean hasCustomOverrideModel(BlockState state, CompoundTag tag)
	{
		if (OVERRIDE_OBJECTS.isEmpty())
			return false;

		int stateid = Block.getId(state);
		CompoundTag[] keys = new CompoundTag[OVERRIDE_OBJECTS.size()];
		OVERRIDE_OBJECTS.keySet().toArray(keys);
		for (CompoundTag key : keys)
		{
			int id = key.getInt("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				CompoundTag toCheckForCompound = key.getCompound("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.getAllKeys();
				Set<String> kSetTile = tag.getAllKeys();

				boolean flag = true;
				if (kSetTile.containsAll(kSetToCheck))
				{
					for (String skey : kSetToCheck)
					{
						if (!NbtUtils.compareNbt(tag.get(skey), toCheckForCompound.get(skey), true))
							flag = false;
					}
					if (flag)
						return true;
				}
			}
		}

		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public static BakedModel getCustomOverrideModel(BlockState state, CompoundTag tag, Level world, Player player)
	{
		int stateid = Block.getId(state);
		CompoundTag[] keys = new CompoundTag[OVERRIDE_OBJECTS.size()];
		OVERRIDE_OBJECTS.keySet().toArray(keys);
		for (CompoundTag key : keys)
		{
			int id = key.getInt("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				CompoundTag toCheckForCompound = key.getCompound("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.getAllKeys();
				Set<String> kSetTile = tag.getAllKeys();

				boolean flag = true;
				if (kSetTile.containsAll(kSetToCheck))
				{
					for (String skey : kSetToCheck)
					{
						if (!NbtUtils.compareNbt(tag.get(skey), toCheckForCompound.get(skey), true))
							flag = false;
					}
					if (flag)
					{
						Object override = OVERRIDE_OBJECTS.get(key);

						if (override == null)
							return null;

						if (override instanceof BlockState)
							return Minecraft.getInstance().getBlockRenderer().getBlockModel((BlockState) override);
						else
							return Minecraft.getInstance().getItemRenderer().getModel((ItemStack) override, world, player, 0);
					}
				}
			}
		}
		return null;

	}
	
	public static Object getOverrideObject(BlockState state, CompoundTag tag)
	{
		int stateid = Block.getId(state);
		CompoundTag[] keys = new CompoundTag[OVERRIDE_OBJECTS.size()];
		OVERRIDE_OBJECTS.keySet().toArray(keys);
		for (CompoundTag key : keys)
		{
			int id = key.getInt("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				CompoundTag toCheckForCompound = key.getCompound("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.getAllKeys();
				Set<String> kSetTile = tag.getAllKeys();

				boolean flag = true;
				if (kSetTile.containsAll(kSetToCheck))
				{
					for (String skey : kSetToCheck)
					{
						if (!NbtUtils.compareNbt(tag.get(skey), toCheckForCompound.get(skey), true))
							flag = false;
					}
					if (flag)
					{
						Object override = OVERRIDE_OBJECTS.get(key);
						return override;
					}
				}
			}
		}
		return null;
	}

}
