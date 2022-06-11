package tschipp.carryon.common.handler;

import java.util.HashMap;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import tschipp.carryon.common.config.Configs.CustomPickupConditions;
import tschipp.carryon.common.helper.InvalidConfigException;
import tschipp.carryon.common.helper.StringParser;

public class CustomPickupOverrideHandler
{

	public static HashMap<String, String> PICKUP_CONDITIONS = new HashMap<>();
	public static HashMap<String, String> PICKUP_CONDITIONS_ENTITIES = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static void initPickupOverrides()
	{
		if (ModList.get().isLoaded("gamestages"))
		{

			List<String> conditions = (List<String>) CustomPickupConditions.customPickupConditionsBlocks.get();

			for (int i = 0; i < conditions.size(); i++)
			{
				String line = conditions.get(i);

				if (!line.contains("(") || !line.contains(")"))
					new InvalidConfigException("Invalid Condition at line " + i + ": " + line).printException();

				String condition = line.substring(line.indexOf("("));
				String blockname = line.replace(condition, "");
				condition = condition.replace("(", "");
				condition = condition.replace(")", "");

				if (blockname.contains("*"))
				{
					String modid = blockname.replace("*", "");
					for (int k = 0; k < ForgeRegistries.BLOCKS.getKeys().size(); k++)
					{
						if (ForgeRegistries.BLOCKS.getKeys().toArray()[k].toString().contains(modid))
						{
							PICKUP_CONDITIONS.put(ForgeRegistries.BLOCKS.getKeys().toArray()[k].toString(), condition);
						}
					}
				}
				else
				{

					PICKUP_CONDITIONS.put(blockname, condition);
				}
			}

			List<String> entityConditions = (List<String>) CustomPickupConditions.customPickupConditionsEntities.get();

			for (int i = 0; i < entityConditions.size(); i++)
			{
				String line = entityConditions.get(i);

				if (!line.contains("(") || !line.contains(")"))
					new InvalidConfigException("Invalid Condition at line " + i + ": " + line).printException();

				String condition = line.substring(line.indexOf("("));
				String entityname = line.replace(condition, "");
				condition = condition.replace("(", "");
				condition = condition.replace(")", "");

				PICKUP_CONDITIONS_ENTITIES.put(entityname, condition);

			}
		}
	}

	public static boolean hasSpecialPickupConditions(BlockState state)
	{
		if (!ModList.get().isLoaded("gamestages"))
			return false;

		for (String cond : PICKUP_CONDITIONS.keySet())
		{
			if(state == StringParser.getBlockState(cond));
				return true;
		}

		return false;
	}

	public static String getPickupCondition(BlockState state)
	{
		for (String cond : PICKUP_CONDITIONS.keySet())
		{
			if(state == StringParser.getBlockState(cond));
				return PICKUP_CONDITIONS.get(cond);
		}
		return null;
	}

	public static boolean hasSpecialPickupConditions(Entity entity)
	{
		if (!ModList.get().isLoaded("gamestages"))
			return false;

		String name = ForgeRegistries.ENTITIES.getKey(entity.getType()).toString();
		return PICKUP_CONDITIONS_ENTITIES.containsKey(name);
	}

	public static String getPickupCondition(Entity entity)
	{
		String name = ForgeRegistries.ENTITIES.getKey(entity.getType()).toString();
		return PICKUP_CONDITIONS_ENTITIES.get(name);
	}

}
