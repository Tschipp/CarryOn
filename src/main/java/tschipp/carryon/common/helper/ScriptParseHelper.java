	package tschipp.carryon.common.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;

public class ScriptParseHelper
{

	public static boolean matches(double number, String cond)
	{
		if (cond == null || cond.isEmpty())
			return true;

		try
		{
			if (cond.contains("<="))
			{
				return number <= Double.parseDouble(cond.replace("<=", ""));
			}
			if (cond.contains(">="))
			{
				return number >= Double.parseDouble(cond.replace(">=", ""));
			}
			if (cond.contains("<"))
			{
				return number < Double.parseDouble(cond.replace("<", ""));
			}
			if (cond.contains(">"))
			{
				return number > Double.parseDouble(cond.replace(">", ""));
			}
			if (cond.contains("="))
			{
				return number == Double.parseDouble(cond.replace("=", ""));
			}
			else
				return number == Double.parseDouble(cond);

		}
		catch (Exception e)
		{
			new InvalidConfigException(e.getMessage()).printException();
		}

		return false;
	}

	public static boolean matches(Block block, String cond)
	{
		if (cond == null || cond.isEmpty())
			return true;

		Block toCheck = StringParser.getBlock(cond);
		if (toCheck != null)
			return block == toCheck;

		return false;
	}

	public static boolean matches(CompoundNBT toCheck, CompoundNBT toMatch)
	{
		if (toCheck == null || toMatch == null || toMatch.isEmpty())
			return true;

		boolean matching = true;
		for (String key : toMatch.keySet())
		{
			INBT tag = toMatch.get(key);
			key = key.replace("\"", "");
			INBT tagToCheck = toCheck.get(key);
			if (!tag.equals(tagToCheck))
				matching = false;
		}

		return matching;
	}
	
	public static float[] getXYZArray(String s)
	{
		float[] d = new float[3];
		d[0] = getValueFromString(s, "x");
		d[1] = getValueFromString(s, "y");
		d[2] = getValueFromString(s, "z");

		return d;
	}
	

	public static float[] getScaled(String s)
	{
		float[] d = new float[3];
		d[0] = getScaledValueFromString(s, "x");
		d[1] = getScaledValueFromString(s, "y");
		d[2] = getScaledValueFromString(s, "z");

		return d;
	}

	public static float getScaledValueFromString(String toGetFrom, String key)
	{
		if(toGetFrom == null || toGetFrom.isEmpty())
			return 1;
		
		String[] s = toGetFrom.split(",");
		for (String string : s)
		{
			if (string.contains(key) && string.contains("="))
			{
				float numb = 1;
				string = string.replace(key + "=", "");

				try
				{
					numb = Float.parseFloat(string);
				}
				catch (Exception e)
				{
				}

				return numb;
			}
		}

		return 1;
	}

	public static boolean matchesScore(PlayerEntity player, String cond)
	{
		if (cond == null || cond.isEmpty())
			return true;

		Scoreboard score = player.getWorldScoreboard();
		String numb;
		String scorename;
		int iE = cond.indexOf("=");
		int iG = cond.indexOf(">");
		int iL = cond.indexOf("<");

		if (iG == -1 ? true : iE < iG && iL == -1 ? true :  iE < iL && iE != -1)
			numb = cond.substring(iE);
		else if (iE == -1 ? true : iG < iE && iL == -1 ? true :  iG < iL && iG != -1)
			numb = cond.substring(iG);
		else
			numb = cond.substring(iL);

		scorename = cond.replace(numb, "");
		Map<ScoreObjective, Score> o = score.getObjectivesForEntity(player.getGameProfile().getName());
		if (o != null)
		{
			Score sc = o.get(score.getObjective(scorename));
			if (sc != null)
			{
				int points = sc.getScorePoints();

				return matches(points, numb);
			}
		}

		return false;
	}

	public static boolean matches(BlockPos pos, String cond)
	{
		if (cond == null || cond.isEmpty())
			return true;

		BlockPos blockpos = new BlockPos(getValueFromString(cond, "x"), getValueFromString(cond, "y"), getValueFromString(cond, "z"));
		BlockPos expand = new BlockPos(getValueFromString(cond, "dx"), getValueFromString(cond, "dy"), getValueFromString(cond, "dz"));
		BlockPos expanded = blockpos.add(expand);

		boolean x = (pos.getX() >= blockpos.getX() && pos.getX() <= expanded.getX()) || blockpos.getX() == 0;
		boolean y = (pos.getY() >= blockpos.getY() && pos.getY() <= expanded.getY()) || blockpos.getY() == 0;
		boolean z = (pos.getZ() >= blockpos.getZ() && pos.getZ() <= expanded.getZ()) || blockpos.getZ() == 0;

		return x && y && z;
	}

	public static float getValueFromString(String toGetFrom, String key)
	{
		if(toGetFrom == null || toGetFrom.isEmpty())
			return 0;
		
		String[] s = toGetFrom.split(",");
		for (String string : s)
		{
			if (string.contains(key) && string.contains("="))
			{
				float numb = 0;
				string = string.replace(key + "=", "");

				try
				{
					numb = Float.parseFloat(string);
				}
				catch (Exception e)
				{
				}

				return numb;
			}
		}

		return 0;
	}
	
	public static boolean hasEffects(PlayerEntity player, String cond)
	{
		if(cond == null || cond.isEmpty())
			return true;
		
		Collection<EffectInstance> effects = player.getActivePotionEffects();
		String[] potions = cond.split(",");
		
		List<String> names = new ArrayList<String>();
		List<Integer> levels = new ArrayList<Integer>();
		
		for(int i = 0; i < potions.length; i++)
		{
			String pot = potions[i];
			if(pot.contains("#"))
			{
				String level = pot.substring(pot.indexOf("#"));
				String name = pot.substring(0, pot.indexOf("#"));
				level = level.replace("#", "");
				int lev = 0;
				try
				{
					lev = Integer.parseInt(level);
				}
				catch(Exception e)
				{}
				
				levels.add(lev);
				names.add(name);
			}
			else
			{
				levels.add(0);
				names.add(pot);
			}
		}
		
		int matches = 0;
		for(EffectInstance effect : effects)
		{
			int amp = effect.getAmplifier();
			String name = effect.getPotion().getRegistryName().toString();
			
			if(names.contains(name))
			{
				int idx = names.indexOf(name);
				int lev = levels.get(idx);
				
				if(lev == amp)
					matches++;
			}
		}
		
		return matches == potions.length;
	}

	public static boolean matches(Material material, String cond)
	{
		if (cond == null || cond.isEmpty())
			return true;

		switch (cond)
		{
		case "air":
			return material == Material.AIR;
		case "anvil":
			return material == Material.ANVIL;
		case "barrier":
			return material == Material.BARRIER;
		case "cactus":
			return material == Material.CACTUS;
		case "cake":
			return material == Material.CAKE;
		case "carpet":
			return material == Material.CARPET;
		case "clay":
			return material == Material.CLAY;
		case "cloth":
			return material == Material.WOOL;
		case "coral":
			return material == Material.CORAL;
		case "dragon_egg":
			return material == Material.DRAGON_EGG;
		case "fire":
			return material == Material.FIRE;
		case "glass":
			return material == Material.GLASS;
		case "gourd":
			return material == Material.GOURD;
		case "grass":
			return material == Material.ORGANIC;
		case "ground":
			return material == Material.ORGANIC;
		case "ice":
			return material == Material.ICE;
		case "iron":
			return material == Material.IRON;
		case "lava":
			return material == Material.LAVA;
		case "leaves":
			return material == Material.LEAVES;
		case "packed_ice":
			return material == Material.PACKED_ICE;
		case "piston":
			return material == Material.PISTON;
		case "plants":
			return material == Material.PLANTS;
		case "portal":
			return material == Material.PORTAL;
		case "redstone_light":
			return material == Material.REDSTONE_LIGHT;
		case "rock":
			return material == Material.ROCK;
		case "sand":
			return material == Material.SAND;
		case "snow":
			return material == Material.SNOW;
		case "sponge":
			return material == Material.SPONGE;
		case "structure_void":
			return material == Material.STRUCTURE_VOID;
		case "tnt":
			return material == Material.TNT;
		case "vine":
			return material == Material.PLANTS;
		case "water":
			return material == Material.WATER;
		case "web":
			return material == Material.WEB;
		case "wood":
			return material == Material.WOOD;
		}

		return false;
	}
}
