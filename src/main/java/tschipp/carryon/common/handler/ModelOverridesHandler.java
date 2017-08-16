package tschipp.carryon.common.handler;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.helper.InvalidConfigException;
import tschipp.carryon.common.helper.StringParser;

public class ModelOverridesHandler
{
	public static HashMap<NBTTagCompound, Object> OVERRIDE_OBJECTS = new HashMap<NBTTagCompound, Object>();

	/*
	 * This class is really ugly, will probably be replaced by something else -
	 * Tschipp
	 */
	public static void initOverrides()
	{
		String[] overrides = CarryOnConfig.modelOverrides.modelOverrides;

		for (int i = 0; i < overrides.length; i++)
		{
			boolean errored = false;

			Object toOverrideObject;
			Object overrideObject;
			NBTTagCompound tag = new NBTTagCompound();

			String currentline = overrides[i];
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
				catch (NBTException e)
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

			if (Loader.isModLoaded(modidOverride) && Loader.isModLoaded(modidToOverride) && !errored)
			{

				int meta = StringParser.getMeta(toOverride);
				if (meta == 0)
					toOverrideObject = StringParser.getBlock(toOverride);
				else
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
							keyComp.setInteger("stateid", Block.getStateId((IBlockState) toOverrideObject));
							keyComp.setString("block", ((IBlockState) toOverrideObject).getBlock().getRegistryName().toString());
						}
						OVERRIDE_OBJECTS.put(keyComp, overrideObject);
					}
				}
			}
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
			int id = key.getInteger("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				NBTTagCompound toCheckForCompound = key.getCompoundTag("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.getKeySet();
				Set<String> kSetTile = tag.getKeySet();

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

	@SideOnly(Side.CLIENT)
	public static IBakedModel getCustomOverrideModel(IBlockState state, NBTTagCompound tag, World world, EntityPlayer player)
	{
		int stateid = Block.getStateId(state);
		NBTTagCompound[] keys = new NBTTagCompound[OVERRIDE_OBJECTS.size()];
		OVERRIDE_OBJECTS.keySet().toArray(keys);
		for (NBTTagCompound key : keys)
		{
			int id = key.getInteger("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				NBTTagCompound toCheckForCompound = key.getCompoundTag("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.getKeySet();
				Set<String> kSetTile = tag.getKeySet();

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
							return Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState((IBlockState) override);
						else
							return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides((ItemStack) override, world, player);
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
			int id = key.getInteger("stateid");
			Block block = StringParser.getBlock(key.getString("block"));
			if (id == 0 ? block == state.getBlock() : id == stateid)
			{
				NBTTagCompound toCheckForCompound = key.getCompoundTag("nbttag");
				Set<String> kSetToCheck = toCheckForCompound.getKeySet();
				Set<String> kSetTile = tag.getKeySet();

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
