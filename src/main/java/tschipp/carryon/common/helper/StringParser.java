package tschipp.carryon.common.helper;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import tschipp.carryon.CarryOn;

public class StringParser
{

	@Nullable
	public static Block getBlock(String string)
	{
		if(string == null)
			return null;
		
		NBTTagCompound tag = getTagCompound(string);
		if (tag != null)
			string = string.replace(tag.toString(), "");

		if (string.contains(";"))
			string = string.replace(string.substring(string.indexOf(";")), "");

		Block block = Block.getBlockFromName(string);
		if (block == null)
			new InvalidConfigException("Block Parsing Error. Invalid Name: " + string).printException();

		return block;
	}

	public static int getMeta(String string)
	{
		if(string == null)
			return 0;
		
		NBTTagCompound tag = getTagCompound(string);
		if (tag != null)
			string = string.replace(tag.toString(), "");

		if (string.contains(";"))
		{
			int meta = 0;
			try
			{
				meta = Integer.parseInt(string.substring(string.indexOf(";")).replace(";", ""));
			}
			catch (Exception e)
			{
				new InvalidConfigException("Meta Parsing Error at: " + string + " : " + e.getMessage()).printException();
			}

			return meta;
		}
		return 0;
	}

	@Nullable
	public static IBlockState getBlockState(String string)
	{
		if(string == null)
			return null;
		
		NBTTagCompound tag = getTagCompound(string);
		if (tag != null)
			string = string.replace(tag.toString(), "");

		int meta = getMeta(string);
		if (meta == 0)
		{
			Block block = getBlock(string);
			if(block != null)
				return block.getDefaultState();
		}
		try
		{
			return getBlock(string).getStateFromMeta(meta);
		}
		catch (Exception e)
		{
			new InvalidConfigException("Blockstate parsing Exception at: " + string + " : " + e.getMessage()).printException();
			return null;
		}
	}

	@Nullable
	public static Item getItem(String string)
	{
		if(string == null)
			return null;
		
		NBTTagCompound tag = getTagCompound(string);
		if (tag != null)
			string = string.replace(tag.toString(), "");

		if (string.contains(";"))
			string = string.replace(string.substring(string.indexOf(";")), "");

		return Item.getByNameOrId(string);
	}

	public static ItemStack getItemStack(String string)
	{
		if(string == null)
			return null;
		
		Item item = getItem(string);
		
		if(item == null)
			return null;
		
		ItemStack stack = new ItemStack(item, 1, getMeta(string));
		NBTTagCompound tag = getTagCompound(string);
		if (tag != null)
			stack.setTagCompound(tag);

		return stack;
	}

	@Nullable
	public static NBTTagCompound getTagCompound(String string)
	{
		NBTTagCompound tag = null;
		if(string == null)
			return null;
		
		if (string.contains("{"))
		{
			if (!string.contains("}"))
				new InvalidConfigException("Missing } at  : " + string).printException();

			String nbt = string.substring(string.indexOf("{"));
			string = string.replace(nbt, "");
			try
			{
				tag = JsonToNBT.getTagFromJson(nbt);
			}
			catch (NBTException e)
			{
				new InvalidConfigException("Error while parsing NBT: " + e.getMessage()).printException();
				return null;
			}

		}
		else if (string.contains("}"))
			new InvalidConfigException("Missing { at  : " + string).printException();

		return tag;
	}

}
