package tschipp.carryon.common.handler;

import java.util.HashMap;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import tschipp.carryon.common.config.Configs.CustomPickupConditions;
import tschipp.carryon.common.helper.InvalidConfigException;

public class CustomPickupOverrideHandler
{

	public static HashMap<String, String> PICKUP_CONDITIONS = new HashMap<String, String>();
	public static HashMap<String, String> PICKUP_CONDITIONS_ENTITIES = new HashMap<String, String>();

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

	public static boolean hasSpecialPickupConditions(IBlockState state)
	{
		if (!ModList.get().isLoaded("gamestages"))
			return false;

		String block = state.getBlock().getRegistryName().toString();
		
		boolean absolute = PICKUP_CONDITIONS.containsKey(block);

		return absolute;
	}

	public static String getPickupCondition(IBlockState state)
	{
		String block = state.getBlock().getRegistryName().toString();

		String absolute = PICKUP_CONDITIONS.get(block);

		if (absolute != null)
			return absolute;
		else
			return null;
	}

	public static boolean hasSpecialPickupConditions(Entity entity)
	{
		if (!ModList.get().isLoaded("gamestages"))
			return false;

		String name = entity.getType().getRegistryName().toString();
		boolean condition = PICKUP_CONDITIONS_ENTITIES.containsKey(name);

		return condition;
	}

	public static String getPickupCondition(Entity entity)
	{
		String name = entity.getType().getRegistryName().toString();
		String condition = PICKUP_CONDITIONS_ENTITIES.get(name);

		return condition;
	}

}
