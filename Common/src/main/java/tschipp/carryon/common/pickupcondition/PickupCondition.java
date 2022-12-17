package tschipp.carryon.common.pickupcondition;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import tschipp.carryon.platform.Services;
import tschipp.carryon.utils.StringHelper;

import java.util.Map;

public class PickupCondition
{
	public static Codec<PickupCondition> CODEC = Codec.STRING.comapFlatMap(PickupCondition::of, pickupCondition -> pickupCondition.str);

	private String str, cond, match;
	private boolean wildcards;

	private PickupCondition(String str, String cond, String match)
	{
		this.str = str;
		this.cond = cond;
		this.match = match;
	}

	public static DataResult<PickupCondition> of(String str)
	{
		if(!(str.contains("(") && str.endsWith(")")))
			return DataResult.error("Error while parsing: "+ str +". Pickup Condition must contain proper brackets.");

		String cond = str.substring(str.indexOf("(") + 1, str.length()-1);

		String match = str.substring(0, str.indexOf("("));

		PickupCondition condition = new PickupCondition(str, cond, match);
		if(match.contains("*"))
			condition.wildcards = true;

		return DataResult.success(condition);
	}

	public boolean matches(BlockState state)
	{
		if(wildcards)
		{
			String name = match.contains("[") ? match.substring(0, match.indexOf("[")) : match;
			String[] split = name.replace("*", ",").split(",");
			String stateName = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

			if(StringHelper.matchesWildcards(stateName, split))
			{
				if(match.contains("["))
				{
					stateName = stateName + match.substring(match.indexOf("["));
					BlockResult result = parseState(stateName);
					return matchesProperties(state, result.properties());
				}
				else
					return true;
			}
			else
				return false;
		}
		else
		{
			BlockResult res = parseState(match);
			return res.blockState().getBlock() == state.getBlock() && matchesProperties(state, res.properties());
		}
	}

	public boolean matches(Entity entity)
	{
		String entityName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
		if(wildcards)
		{
			String[] split = match.replace("*", ",").split(",");
			return StringHelper.matchesWildcards(entityName, split);
		}
		else
			return entityName.equals(match);
	}

	public String getCondition()
	{
		return cond;
	}

	public boolean isFulfilled(ServerPlayer player)
	{
		return Services.GAMESTAGES.hasStage(player, cond);
	}

	private BlockResult parseState(String state)
	{
		try {
			BlockResult result = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), state, false);
			return result;
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		return null;
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


}

