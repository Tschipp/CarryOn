/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.carryon.common.scripting;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import tschipp.carryon.platform.Services;

import java.util.*;

public final class Matchables
{
	public interface Matchable<T>
	{
		boolean matches(T elem);
	}

	private static float getValueFromStringOrDefault(String toGetFrom, String key, float defaultVal)
	{
		Optional<Float> val = getValueFromString(toGetFrom, key);
		return val.orElse(defaultVal);
	}

	private static Optional<Float> getValueFromString(String toGetFrom, String key)
	{
		if (toGetFrom == null || toGetFrom.isEmpty())
			return Optional.empty();

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

				return Optional.of(numb);
			}
		}

		return Optional.empty();
	}

	public record NumberBoundCondition(String bounds) implements Matchable<Number>
	{
		public static final Codec<NumberBoundCondition> CODEC = Codec.STRING.xmap(NumberBoundCondition::new, NumberBoundCondition::bounds);

		public static final NumberBoundCondition NONE = new NumberBoundCondition("");

		@Override
		public boolean matches(Number num)
		{
			double number = num.doubleValue();

			if (bounds == null || bounds.isEmpty())
				return true;

			try
			{
				if (bounds.contains("<="))
					return number <= Double.parseDouble(bounds.replace("<=", ""));
				if (bounds.contains(">="))
					return number >= Double.parseDouble(bounds.replace(">=", ""));
				if (bounds.contains("<"))
					return number < Double.parseDouble(bounds.replace("<", ""));
				if (bounds.contains(">"))
					return number > Double.parseDouble(bounds.replace(">", ""));
				if (bounds.contains("="))
					return number == Double.parseDouble(bounds.replace("=", ""));
				else
					return number == Double.parseDouble(bounds);

			}
			catch (Exception e)
			{
				throw new RuntimeException("Error while parsing Number bound for string: "+ bounds + ". Error: " + e.getMessage());
			}
		}
	}

	public record AdvancementCondition(String advancement) implements Matchable<ServerPlayer>
	{
		public static final Codec<AdvancementCondition> CODEC = Codec.STRING.xmap(AdvancementCondition::new, AdvancementCondition::advancement);

		public static final AdvancementCondition NONE = new AdvancementCondition("");

		@Override
		public boolean matches(ServerPlayer player)
		{
			ServerAdvancementManager manager = player.server.getAdvancements();
			AdvancementHolder adv = manager.get(new ResourceLocation(advancement.isEmpty() ? "" : advancement));

			boolean achievement = adv == null ? true : player.getAdvancements().getOrStartProgress(adv).isDone();
			return achievement;
		}
	}

	public record GamestageCondition(String gamestage) implements Matchable<ServerPlayer>
	{
		public static final Codec<GamestageCondition> CODEC = Codec.STRING.xmap(GamestageCondition::new, GamestageCondition::gamestage);

		public static final GamestageCondition NONE = new GamestageCondition("");

		@Override
		public boolean matches(ServerPlayer player)
		{
			if(!Services.PLATFORM.isModLoaded("gamestages"))
				return true;

			if(gamestage == null || gamestage.isEmpty())
				return true;

			return Services.GAMESTAGES.hasStage(player, gamestage);
		}
	}

	public record ScoreboardCondition(String cond) implements Matchable<ServerPlayer>
	{
		public static final Codec<ScoreboardCondition> CODEC = Codec.STRING.xmap(ScoreboardCondition::new, ScoreboardCondition::cond);

		public static final ScoreboardCondition NONE = new ScoreboardCondition("");


		@Override
		public boolean matches(ServerPlayer player)
		{
			if (cond == null || cond.isEmpty())
				return true;

			Scoreboard score = player.getScoreboard();
			String numb;
			String scorename;
			int iE = cond.indexOf("=");
			int iG = cond.indexOf(">");
			int iL = cond.indexOf("<");

			if (iG == -1 || (iE < iG && iL == -1 || iE < iL && iE != -1))
				numb = cond.substring(iE);
			else if (iE == -1 || (iG < iE && iL == -1 || iG < iL && iG != -1))
				numb = cond.substring(iG);
			else
				numb = cond.substring(iL);

			scorename = cond.replace(numb, "");
			Object2IntMap<Objective> scores = score.listPlayerScores(player);
			int scoreVal = scores.getInt(score.getObjective(scorename));

			return new NumberBoundCondition(numb).matches(scoreVal);
		}
	}

	public record PositionCondition(String cond) implements Matchable<ServerPlayer>
	{
		public static final Codec<PositionCondition> CODEC = Codec.STRING.xmap(PositionCondition::new, PositionCondition::cond);

		public static final PositionCondition NONE = new PositionCondition("");


		@Override
		public boolean matches(ServerPlayer elem)
		{
			if (cond == null || cond.isEmpty())
				return true;

			BlockPos blockpos = new BlockPos((int) getValueFromStringOrDefault(cond, "x", 0), (int) getValueFromStringOrDefault(cond, "y", 0), (int) getValueFromStringOrDefault(cond, "z", 0));
			BlockPos expand = new BlockPos((int) getValueFromStringOrDefault(cond, "dx", 0), (int) getValueFromStringOrDefault(cond, "dy", 0), (int) getValueFromStringOrDefault(cond, "dz", 0));
			BlockPos expanded = blockpos.offset(expand);
			BlockPos pos = elem.blockPosition();

			boolean x = pos.getX() >= blockpos.getX() && pos.getX() <= expanded.getX() || blockpos.getX() == 0;
			boolean y = pos.getY() >= blockpos.getY() && pos.getY() <= expanded.getY() || blockpos.getY() == 0;
			boolean z = pos.getZ() >= blockpos.getZ() && pos.getZ() <= expanded.getZ() || blockpos.getZ() == 0;

			return x && y && z;
		}
	}

	public record EffectsCondition(String effects) implements Matchable<ServerPlayer>
	{
		public static final Codec<EffectsCondition> CODEC = Codec.STRING.xmap(EffectsCondition::new, EffectsCondition::effects);

		public static final EffectsCondition NONE = new EffectsCondition("");

		@Override
		public boolean matches(ServerPlayer player)
		{
			if (effects == null || effects.isEmpty())
				return true;

			Collection<MobEffectInstance> fx = player.getActiveEffects();
			String[] potions = effects.split(",");

			List<String> names = new ArrayList<>();
			List<Integer> levels = new ArrayList<>();

			for (String pot : potions)
			{
				if (pot.contains("#"))
				{
					String level = pot.substring(pot.indexOf("#"));
					String name = pot.substring(0, pot.indexOf("#"));
					level = level.replace("#", "");
					int lev = 0;
					try
					{
						lev = Integer.parseInt(level);
					}
					catch (Exception e)
					{
					}

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
			for (MobEffectInstance effect : fx)
			{
				int amp = effect.getAmplifier();
				String name = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect()).toString();

				if (names.contains(name))
				{
					int idx = names.indexOf(name);
					int lev = levels.get(idx);

					if (lev <= amp)
						matches++;
				}
			}

			return matches == potions.length;
		}
	}

	public record NBTCondition(CompoundTag tag) implements Matchable<CompoundTag>
	{
		public static final Codec<NBTCondition> CODEC = CompoundTag.CODEC.xmap(NBTCondition::new, NBTCondition::tag);

		public static final NBTCondition NONE = new NBTCondition(new CompoundTag());

		@Override
		public boolean matches(CompoundTag other)
		{
			if(other == null)
				return true;
			return NbtUtils.compareNbt(tag, other, true);
		}
	}

	public static class OptionalVec3 {

		public static final Codec<OptionalVec3> CODEC = Codec.STRING.xmap(OptionalVec3::new, OptionalVec3::source);

		public static final OptionalVec3 NONE = new OptionalVec3("");

		String source;
		Vec3 vec;

		boolean x, y, z;

		public OptionalVec3(String source)
		{
			this.source = source;
			Optional<Float> xOpt = getValueFromString(source, "x");
			Optional<Float> yOpt = getValueFromString(source, "y");
			Optional<Float> zOpt = getValueFromString(source, "z");

			float x = 0, y = 0, z = 0;

			if(xOpt.isPresent()) {
				x = xOpt.get();
				this.x = true;
			}
			if(yOpt.isPresent()) {
				y = yOpt.get();
				this.y = true;
			}
			if(zOpt.isPresent()) {
				z = zOpt.get();
				this.z = true;
			}

			vec = new Vec3(x, y, z);
		}

		private String source()
		{
			return source;
		}

		/**
		 * Gets the contained optional vector. Nonexisting numbers are set to 0.
		 */
		public Vec3 getVec()
		{
			return vec;
		}

		public Vec3 getVec(double dX, double dY, double dZ)
		{
			double x = !this.x ? dX : vec.x;
			double y = !this.y ? dY : vec.y;
			double z = !this.z ? dZ : vec.z;
			return new Vec3(x, y, z);
		}
	}
}
