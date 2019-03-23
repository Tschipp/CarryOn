package tschipp.carryon.common.helper;

import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;

public class StringParser
{

	@Nullable
	public static Block getBlock(String string)
	{
		IBlockState state = getBlockState(string);
		if(state != null)
			return state.getBlock();
				
		return null;
	}


	@Nullable
	public static IBlockState getBlockState(String string)
	{
		if(string == null)
			return null;
				
		BlockStateParser parser = new BlockStateParser(new StringReader(string), false);

		try
		{
			parser.parse(false);
			return parser.getState();
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
	
		ItemParser parser = new ItemParser(new StringReader(string), false);
	
		try
		{
			parser.parse();
			return parser.getItem();
		}
		catch (Exception e)
		{
			new InvalidConfigException("Item parsing Exception at: " + string + " : " + e.getMessage()).printException();
			return null;
		}
	}

	public static ItemStack getItemStack(String string)
	{
		if(string == null)
			return null;
		
		ItemParser parser = new ItemParser(new StringReader(string), false);
		
		try
		{
			parser.parse();
			Item item =  parser.getItem();
			NBTTagCompound nbt = parser.getNbt();
			
			ItemStack stack = new ItemStack(item, 1);
			
			if(nbt != null)
			{
				stack.setTag(nbt);
			}
			
			return stack;
		}
		catch (Exception e)
		{
			new InvalidConfigException("Item parsing Exception at: " + string + " : " + e.getMessage()).printException();
			return ItemStack.EMPTY;
			
		}		

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
			catch (Exception e)
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
