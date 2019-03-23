package tschipp.carryon.common.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.common.config.Configs.ModelOverrides;
import tschipp.carryon.common.helper.InvalidConfigException;
import tschipp.carryon.common.helper.StringParser;

public class ModelOverridesHandler
{
	public static HashMap<NBTTagCompound, Object> OVERRIDE_OBJECTS = new HashMap<NBTTagCompound, Object>();

	/*
	 * This class is really ugly, will probably be replaced by something else -
	 * Tschipp
	 */	
	public static void parseOverride(String overrideString, int i)
	{
		boolean errored = false;

		Object toOverrideObject;
		Object overrideObject;
		NBTTagCompound tag = new NBTTagCompound();

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
				tag = JsonToNBT.getTagFromJson(nbt);
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
					NBTTagCompound keyComp = new NBTTagCompound();
					keyComp.setTag("nbttag", tag);
					if (toOverrideObject instanceof Block)
					{
						keyComp.setString("block", ((Block) toOverrideObject).getRegistryName().toString());
					}
					else
					{
						keyComp.setInt("stateid", Block.getStateId((IBlockState) toOverrideObject));
						keyComp.setString("block", ((IBlockState) toOverrideObject).getBlock().getRegistryName().toString());
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

	public static boolean hasCustomOverrideModel(IBlockState state, NBTTagCompound tag)
	{
		if (OVERRIDE_OBJECTS.isEmpty())
			return false;

		int stateid = Block.getStateId(state);
		NBTTagCompound[] keys = new NBTTagCompound[OVERRIDE_OBJECTS.size()];
		OVERRIDE_OBJECTS.keySet().toArray(keys);
		for (NBTTagCompound key : keys)
		{
			int id = key.getInt("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				NBTTagCompound toCheckForCompound = key.getCompound("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.keySet();
				Set<String> kSetTile = tag.keySet();

				boolean flag = true;
				if (kSetTile.containsAll(kSetToCheck))
				{
					for (String skey : kSetToCheck)
					{
						if (!NBTUtil.areNBTEquals(tag.getTag(skey), toCheckForCompound.getTag(skey), true))
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
	public static IBakedModel getCustomOverrideModel(IBlockState state, NBTTagCompound tag, World world, EntityPlayer player)
	{
		int stateid = Block.getStateId(state);
		NBTTagCompound[] keys = new NBTTagCompound[OVERRIDE_OBJECTS.size()];
		OVERRIDE_OBJECTS.keySet().toArray(keys);
		for (NBTTagCompound key : keys)
		{
			int id = key.getInt("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				NBTTagCompound toCheckForCompound = key.getCompound("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.keySet();
				Set<String> kSetTile = tag.keySet();

				boolean flag = true;
				if (kSetTile.containsAll(kSetToCheck))
				{
					for (String skey : kSetToCheck)
					{
						if (!NBTUtil.areNBTEquals(tag.getTag(skey), toCheckForCompound.getTag(skey), true))
							flag = false;
					}
					if (flag)
					{
						Object override = OVERRIDE_OBJECTS.get(key);

						if (override == null)
							return null;

						if (override instanceof IBlockState)
							return Minecraft.getInstance().getBlockRendererDispatcher().getModelForState((IBlockState) override);
						else
							return Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides((ItemStack) override, world, player);
					}
				}
			}
		}
		return null;

	}
	
	public static Object getOverrideObject(IBlockState state, NBTTagCompound tag)
	{
		int stateid = Block.getStateId(state);
		NBTTagCompound[] keys = new NBTTagCompound[OVERRIDE_OBJECTS.size()];
		OVERRIDE_OBJECTS.keySet().toArray(keys);
		for (NBTTagCompound key : keys)
		{
			int id = key.getInt("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				NBTTagCompound toCheckForCompound = key.getCompound("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.keySet();
				Set<String> kSetTile = tag.keySet();

				boolean flag = true;
				if (kSetTile.containsAll(kSetToCheck))
				{
					for (String skey : kSetToCheck)
					{
						if (!NBTUtil.areNBTEquals(tag.getTag(skey), toCheckForCompound.getTag(skey), true))
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
