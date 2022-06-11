package tschipp.carryon.common.helper;

import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.arguments.item.ItemParser.ItemResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StringParser
{

	@Nullable
	public static Block getBlock(String string)
	{
		BlockState state = getBlockState(string);
		if (state != null)
			return state.getBlock();

		return null;
	}

	@Nullable
	public static BlockState getBlockState(String string)
	{
		if (string == null)
			return null;


		try
		{
			BlockResult result = BlockStateParser.parseForBlock(HolderLookup.forRegistry(Registry.BLOCK), new StringReader(string), false);
			return result.blockState();
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
		if (string == null)
			return null;


		try
		{
			ItemResult res = ItemParser.parseForItem(HolderLookup.forRegistry(Registry.ITEM), new StringReader(string));
			return res.item().get();
		}
		catch (Exception e)
		{
			new InvalidConfigException("Item parsing Exception at: " + string + " : " + e.getMessage()).printException();
			return null;
		}
	}

	public static ItemStack getItemStack(String string)
	{
		if (string == null)
			return null;


		try
		{
			ItemResult res = ItemParser.parseForItem(HolderLookup.forRegistry(Registry.ITEM), new StringReader(string));

			Item item = res.item().get();
			CompoundTag nbt = res.nbt();

			ItemStack stack = new ItemStack(item, 1);

			if (nbt != null)
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
	public static CompoundTag getTagCompound(String string)
	{
		CompoundTag tag = null;
		if (string == null)
			return null;

		if (string.contains("{"))
		{
			if (!string.contains("}"))
				new InvalidConfigException("Missing } at  : " + string).printException();

			String nbt = string.substring(string.indexOf("{"));
			string = string.replace(nbt, "");
			try
			{
				tag = TagParser.parseTag(nbt);
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
