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

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tschipp.carryon.Constants;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptObject.ScriptObjectBlock;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptObject.ScriptObjectEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScriptManager
{
	public static final List<CarryOnScript> SCRIPTS = new ArrayList<>();

	public static Optional<CarryOnScript> inspectBlock(BlockState state, Level level, BlockPos pos, @Nullable CompoundTag tag)
	{
		if (!Constants.COMMON_CONFIG.settings.useScripts)
			return Optional.empty();

		Block block = state.getBlock();
		float hardness = state.getDestroySpeed(level, pos);
		float resistance = block.getExplosionResistance();

		for (CarryOnScript script : SCRIPTS)
		{
			if (script.isBlock() && matchesAll(script, block, hardness, resistance, tag))
				return Optional.of(script);
		}

		return Optional.empty();
	}

	public static Optional<CarryOnScript> inspectEntity(Entity entity)
	{
		if (!Constants.COMMON_CONFIG.settings.useScripts)
			return Optional.empty();

		float height = entity.getBbHeight();
		float width = entity.getBbWidth();
		float health = entity instanceof LivingEntity ? ((LivingEntity) entity).getHealth() : 0.0f;
		CompoundTag tag = new CompoundTag();
		entity.save(tag);

		for (CarryOnScript script : SCRIPTS)
		{
			if (script.isEntity() && matchesAll(script, entity, height, width, health, tag))
				return Optional.of(script);
		}

		return Optional.empty();
	}

	private static boolean matchesAll(CarryOnScript script, Entity entity, float height, float width, float health, CompoundTag tag)
	{
		ScriptObjectEntity scEntity = script.scriptObject().entity();

		boolean matchname = true;
		if(scEntity.typeNameEntity().isPresent())
			matchname = entity.getType().equals(BuiltInRegistries.ENTITY_TYPE.get(scEntity.typeNameEntity().get()));
		boolean matchheight = scEntity.typeHeight().matches(height);
		boolean matchwidth = scEntity.typeWidth().matches(width);
		boolean matchhealth = scEntity.typeHealth().matches(health);
		boolean matchnbt = scEntity.typeEntityTag().matches(tag);

		return matchname && matchheight && matchwidth && matchhealth && matchnbt;
	}

	private static boolean matchesAll(CarryOnScript script, Block block, float hardness, float resistance, CompoundTag nbt)
	{
		ScriptObjectBlock scBlock = script.scriptObject().block();

		boolean matchblock = true;
		if(scBlock.typeNameBlock().isPresent())
			matchblock = block == BuiltInRegistries.BLOCK.get(scBlock.typeNameBlock().get());
		boolean matchnbt = scBlock.typeBlockTag().matches(nbt);
		boolean matchhardness = scBlock.typeHardness().matches(hardness);
		boolean matchresistance = scBlock.typeResistance().matches(resistance);

		return matchnbt && matchblock && matchhardness && matchresistance;
	}
}
