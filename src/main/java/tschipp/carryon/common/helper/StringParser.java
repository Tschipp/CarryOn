package tschipp.carryon.common.helper;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class StringParser
{

	@Nullable
	public static Block getBlock(String string)
	{
		NBTTagCompound tag = getTagCompound(string);
		if(tag != null)
			string = string.replace(tag.toString(), "");
		
		if (string.contains(";"))
			string = string.replace(string.substring(string.indexOf(";")), "");

		Block block = Block.getBlockFromName(string);
		if(block == null)
			throw new InvalidConfigException("Block Parsing Error. Invalid Name: " + string);

		return block;
	}

	public static int getMeta(String string)
	{
		NBTTagCompound tag = getTagCompound(string);
		if(tag != null)
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
				throw new InvalidConfigException("Meta Parsing Error at: " + string + " : " + e.getMessage());
			}

			return meta;
		}
		return 0;
	}

	public static IBlockState getBlockState(String string)
	{
		NBTTagCompound tag = getTagCompound(string);
		if(tag != null)
			string = string.replace(tag.toString(), "");
		
		int meta = getMeta(string);
		if(meta == 0)
			return getBlock(string).getDefaultState();
		try
		{
			return getBlock(string).getStateFromMeta(meta);
		}
		catch (Exception e)
		{
			throw new InvalidConfigException("Blockstate parsing Exception at: " + string + " : " + e.getMessage());
		}
	}

	public static Item getItem(String string)
	{
		NBTTagCompound tag = getTagCompound(string);
		if(tag != null)
			string = string.replace(tag.toString(), "");
		
		if(string.contains(";"))
			string = string.replace(string.substring(string.indexOf(";")), "");
		
		return Item.getByNameOrId(string);
	}

	public static ItemStack getItemStack(String string)
	{
		ItemStack stack = new ItemStack(getItem(string), 1, getMeta(string));
		NBTTagCompound tag = getTagCompound(string);
		if(tag != null)
			stack.setTagCompound(tag);
		
		return stack;
	}
	
	@Nullable
	public static NBTTagCompound getTagCompound(String string)
	{
		NBTTagCompound tag = null;
		if (string.contains("{"))
		{
			if (!string.contains("}"))
				throw new InvalidConfigException("Missing } at  : " + string);

			String nbt = string.substring(string.indexOf("{"));
			string = string.replace(nbt, "");
			try
			{
				tag = JsonToNBT.getTagFromJson(nbt);
			}
			catch (NBTException e)
			{
				throw new InvalidConfigException("Error while parsing NBT: " + e.getMessage());
			}

		}
		else if (string.contains("}"))
			throw new InvalidConfigException("Missing { at  : " + string);
		
		
		return tag;
	}
	
	
}
