package tschipp.carryon.common.handler;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.fml.common.Loader;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.helper.InvalidConfigException;

public class CustomPickupOverrideHandler
{

	public static HashMap<String, String> PICKUP_CONDITIONS = new HashMap<String, String>();
	public static HashMap<String, String> PICKUP_CONDITIONS_ENTITIES = new HashMap<String, String>();

	public static void initPickupOverrides()
	{
		if (Loader.isModLoaded("gamestages"))
		{

			String[] conditions = CarryOnConfig.customPickupConditions.customPickupConditionsBlocks;

			for (int i = 0; i < conditions.length; i++)
			{
				String line = conditions[i];

				if (!line.contains("(") || !line.contains(")"))
					new InvalidConfigException("Invalid Condition at line " + i + ": " + line).printException();

				String condition = line.substring(line.indexOf("("));
				String blockname = line.replace(condition, "");
				condition = condition.replace("(", "");
				condition = condition.replace(")", "");

				if (blockname.contains("*"))
				{
					String modid = blockname.replace("*", "");
					for (int k = 0; k < Block.REGISTRY.getKeys().size(); k++)
					{
						if (Block.REGISTRY.getKeys().toArray()[k].toString().contains(modid))
						{
							PICKUP_CONDITIONS.put(Block.REGISTRY.getKeys().toArray()[k].toString() + ";any", condition);
						}
					}
				}
				else
				{
					if (!blockname.contains(";"))
						blockname = blockname + ";any";

					PICKUP_CONDITIONS.put(blockname, condition);
				}
			}

			String[] entityConditions = CarryOnConfig.customPickupConditions.customPickupConditionsEntities;

			for (int i = 0; i < entityConditions.length; i++)
			{
				String line = entityConditions[i];

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

	public static boolean hasSpecialPickupConditions(IBlockState state)
	{
		if (!Loader.isModLoaded("gamestages"))
			return false;

		String block = state.getBlock().getRegistryName().toString();
		String meta = "" + state.getBlock().getMetaFromState(state);

		boolean absolute = PICKUP_CONDITIONS.containsKey(block + ";" + meta);
		boolean any = PICKUP_CONDITIONS.containsKey(block + ";any");

		return absolute || any;
	}

	public static String getPickupCondition(IBlockState state)
	{
		String block = state.getBlock().getRegistryName().toString();
		String meta = "" + state.getBlock().getMetaFromState(state);

		String absolute = PICKUP_CONDITIONS.get(block + ";" + meta);
		String any = PICKUP_CONDITIONS.get(block + ";any");

		if (absolute != null)
			return absolute;
		else
			return any;
	}

	public static boolean hasSpecialPickupConditions(Entity entity)
	{
		if (!Loader.isModLoaded("gamestages"))
			return false;

		String entityname = EntityList.getKey(entity).toString();
		boolean condition = PICKUP_CONDITIONS_ENTITIES.containsKey(entityname);

		return condition;
	}

	public static String getPickupCondition(Entity entity)
	{
		String entityname = EntityList.getKey(entity).toString();
		String condition = PICKUP_CONDITIONS_ENTITIES.get(entityname);

		return condition;
	}

}
