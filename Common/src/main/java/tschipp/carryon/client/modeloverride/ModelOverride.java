package tschipp.carryon.client.modeloverride;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.arguments.item.ItemParser.ItemResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import tschipp.carryon.common.scripting.Matchables.NBTCondition;

import javax.annotation.Nullable;
import java.util.Map;

public class ModelOverride
{
	public static Codec<ModelOverride> CODEC = Codec.STRING.comapFlatMap(ModelOverride::of, override -> override.raw);

	private String raw;
	private BlockResult parsedBlock;
	private Type type;
	private Either<ItemResult, BlockResult> parsedRHS;
	private Either<ItemStack, BlockState> renderObject;

	private ModelOverride(String raw, BlockResult parsedBlock, Type type, Either<ItemResult, BlockResult> parsedRHS)
	{
		this.raw = raw;
		this.parsedBlock = parsedBlock;
		this.type = type;
		this.parsedRHS = parsedRHS;

		parsedRHS.ifLeft(res -> {
			ItemStack stack = new ItemStack(res.item());
			if(res.nbt() != null)
				stack.setTag(res.nbt());
			this.renderObject = Either.left(stack);
		});

		parsedRHS.ifRight(res -> {
			BlockState state = res.blockState();
			this.renderObject = Either.right(state);
		});
	}

	public static DataResult<ModelOverride> of(String str)
	{
		if(!str.contains("->"))
			return DataResult.error(str + " must contain -> Arrow!");
		String[] split = str.split("->");
		String from = split[0];
		String to = split[1];

		BlockResult res;

		try {
			res = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), from, true);
		} catch (Exception e) {
			return DataResult.error("Error while parsing " + from + ":" + e.getMessage());
		}

		Type type = Type.ITEM;

		if(to.contains("(") && to.contains(")"))
		{
			String t = to.substring(to.indexOf("(") + 1, to.indexOf(")"));
			if(t.equals("block"))
				type = Type.BLOCK;
			to = to.substring(to.indexOf(")") + 1);
		}

		Either<ItemResult, BlockResult> either;
		try {
			if(type == Type.ITEM)
				either = Either.left(ItemParser.parseForItem(BuiltInRegistries.ITEM.asLookup(), new StringReader(to)));
			else
				either = Either.right(BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), to, true));
		}catch (CommandSyntaxException e) {
			return DataResult.error("Error while parsing " + to + ":" + e.getMessage());
		}

		return DataResult.success(new ModelOverride(str, res, type, either));
	}

	public boolean matches(BlockState state, @Nullable CompoundTag tag)
	{
		if(state.getBlock() == parsedBlock.blockState().getBlock() && matchesProperties(state, parsedBlock.properties()))
		{
			if(tag == null || parsedBlock.nbt() == null)
				return true;
			NBTCondition nbt = new NBTCondition(parsedBlock.nbt());
			return nbt.matches(tag);
		}
		return false;
	}

	public Either<ItemStack, BlockState> getRenderObject()
	{
		return this.renderObject;
	}

	private boolean matchesProperties(BlockState state, Map<Property<?>, Comparable<?>> props)
	{
		for(var entry : props.entrySet())
		{
			var val = state.getValue(entry.getKey());
			if(val != entry.getValue())
				return false;
		}
		return true;
	}

	public enum Type {
		ITEM,
		BLOCK
	}

}
