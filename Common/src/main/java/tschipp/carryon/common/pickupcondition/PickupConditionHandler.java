package tschipp.carryon.common.pickupcondition;

import com.mojang.serialization.DataResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import tschipp.carryon.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PickupConditionHandler
{
	private static final List<PickupCondition> BLOCK_CONDITIONS = new ArrayList<>();
	private static final List<PickupCondition> ENTITY_CONDITIONS = new ArrayList<>();

	public static void initPickupConditions()
	{
		BLOCK_CONDITIONS.clear();
		ENTITY_CONDITIONS.clear();

		for(String cond : Constants.COMMON_CONFIG.customPickupConditions.customPickupConditionsBlocks)
		{
			DataResult<PickupCondition> res =  PickupCondition.of(cond);
			if(res.result().isPresent())
			{
				PickupCondition pickupCondition = res.result().get();
				BLOCK_CONDITIONS.add(pickupCondition);
			}
			else
			{
				Constants.LOG.debug("Error while parsing Pickup Conditions: " + res.error().get().message());
			}

		}

		for(String cond : Constants.COMMON_CONFIG.customPickupConditions.customPickupConditionsEntities)
		{
			DataResult<PickupCondition> res =  PickupCondition.of(cond);
			if(res.result().isPresent())
			{
				PickupCondition pickupCondition = res.result().get();
				ENTITY_CONDITIONS.add(pickupCondition);
			}
			else
			{
				Constants.LOG.debug("Error while parsing Pickup Conditions: " + res.error().get().message());
			}
		}
	}

	public static Optional<PickupCondition> getPickupCondition(BlockState state)
	{
		for(PickupCondition cond : BLOCK_CONDITIONS)
		{
			if(cond.matches(state))
				return Optional.of(cond);
		}
		return Optional.empty();
	}

	public static Optional<PickupCondition> getPickupCondition(Entity entity)
	{
		for(PickupCondition cond : ENTITY_CONDITIONS)
		{
			if(cond.matches(entity))
				return Optional.of(cond);
		}
		return Optional.empty();
	}
}
