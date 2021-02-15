package tschipp.carryon.common.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.Entity;
import net.minecraft.state.Property;
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

	private static final Function<Entry<Property<?>, Comparable<?>>, String> func = new Function<Entry<Property<?>, Comparable<?>>, String>() {
		public String apply(@Nullable Entry<Property<?>, Comparable<?>> p_apply_1_)
		{
			if (p_apply_1_ == null)
			{
				return "<NULL>";
			}
			else
			{
				Property<?> property = p_apply_1_.getKey();
				return property.getName() + "=" + this.func_235905_a_(property, p_apply_1_.getValue());
			}
		}

		@SuppressWarnings("unchecked")
		private <T extends Comparable<T>> String func_235905_a_(Property<T> p_235905_1_, Comparable<?> comp)
		{
			return p_235905_1_.getName((T) comp);
		}
	};

	public static boolean hasSpecialPickupConditions(BlockState state)
	{
		if (!ModList.get().isLoaded("gamestages"))
			return false;

		for(String cond : PICKUP_CONDITIONS.keySet())
		{
			BlockStateParser parser = new BlockStateParser(new StringReader(cond), false);
			try
			{
				parser.parse(false);
			}
			catch (CommandSyntaxException e)
			{
			}
			if(parser.getState() == state)
				return true;
		}
		
		return false;
	}

	public static String getPickupCondition(BlockState state)
	{
		for(String cond : PICKUP_CONDITIONS.keySet())
		{
			BlockStateParser parser = new BlockStateParser(new StringReader(cond), false);
			try
			{
				parser.parse(false);
			}
			catch (CommandSyntaxException e)
			{
			}
			if(parser.getState() == state)
				return PICKUP_CONDITIONS.get(cond);
		}
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
