package tschipp.carryon.common.scripting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
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
		Material material = state.getMaterial();
		float hardness = state.getDestroySpeed(level, pos);
		float resistance = block.getExplosionResistance();

		for (CarryOnScript script : SCRIPTS)
		{
			if (script.isBlock() && matchesAll(script, block, material, hardness, resistance, tag))
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

	private static boolean matchesAll(CarryOnScript script, Block block, Material material, float hardness, float resistance, CompoundTag nbt)
	{
		ScriptObjectBlock scBlock = script.scriptObject().block();

		boolean matchblock = true;
		if(scBlock.typeNameBlock().isPresent())
			matchblock = block == BuiltInRegistries.BLOCK.get(scBlock.typeNameBlock().get());
		boolean matchnbt = scBlock.typeBlockTag().matches(nbt);
		boolean matchmaterial = scBlock.typeMaterial().matches(material);
		boolean matchhardness = scBlock.typeHardness().matches(hardness);
		boolean matchresistance = scBlock.typeResistance().matches(resistance);

		return matchnbt && matchblock && matchmaterial && matchhardness && matchresistance;
	}
}
