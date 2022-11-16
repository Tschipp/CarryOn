package tschipp.carryon.common.carry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import tschipp.carryon.common.carry.CarryOnData.CarryType;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class PlacementHandler
{

	public static boolean tryPlaceBlock(ServerPlayer player, BlockPos pos, Direction facing, @Nullable BiFunction<BlockPos, BlockState, Boolean> placementCallback)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if (!carry.isCarrying(CarryOnData.CarryType.BLOCK))
			return false;

		if (player.tickCount == carry.getTick())
			return false;

		Level level = player.getLevel();
		BlockState state = carry.getBlock();

		BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(player.position(), facing, pos));

		if (!level.getBlockState(pos).canBeReplaced(context))
			pos = pos.relative(facing);

		context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(player.position(), facing, pos));

		BlockEntity blockEntity = carry.getBlockEntity(pos);

		boolean canPlace = state.canSurvive(level, pos) && level.mayInteract(player, pos) && level.getBlockState(pos).canBeReplaced(context) && level.isUnobstructed(state, pos, CollisionContext.of(player));
		if (!canPlace)
			return false;

		state = getPlacementState(state, player, context, pos);
		boolean doPlace = placementCallback == null || placementCallback.apply(pos, state);

		if (!doPlace)
			return false;

		level.setBlock(pos, state, 3);
		if (blockEntity != null)
			level.setBlockEntity(blockEntity);
		carry.clear();
		CarryOnDataManager.setCarryData(player, carry);
		player.playSound(state.getSoundType().getPlaceSound(), 1.0f, 0.5f);
		level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0f, 0.5f);
		player.swing(InteractionHand.MAIN_HAND, true);
		return true;
	}

	private static BlockState getPlacementState(BlockState state, ServerPlayer player, BlockPlaceContext context, BlockPos pos)
	{
		BlockState placementState = state.getBlock().getStateForPlacement(context);
		System.out.println(placementState);

		for (var prop : placementState.getProperties()) {
			if (prop instanceof DirectionProperty) {
				state = updateProperty(state, placementState, prop);
			}
			if (prop.getValueClass() == Direction.Axis.class) {
				state = updateProperty(state, placementState, prop);
			}

			//This is needed for certain blocks, otherwise we get problems like chests not connecting
			if (ListHandler.isPropertyException(prop)) {
				state = updateProperty(state, placementState, prop);
			}
		}

		System.out.println(state);

		state = Block.updateFromNeighbourShapes(state, player.level, pos);

		if (placementState.hasProperty(BlockStateProperties.WATERLOGGED))
			state = state.setValue(BlockStateProperties.WATERLOGGED, placementState.getValue(BlockStateProperties.WATERLOGGED));

		return state;
	}

	private static <T extends Comparable<T>> BlockState updateProperty(BlockState state, BlockState otherState, Property<T> prop)
	{
		var val = otherState.getValue(prop);
		return state.setValue(prop, val);
	}

	public static boolean tryPlaceEntity(ServerPlayer player, BlockPos pos, Direction facing, @Nullable BiFunction<Vec3, Entity, Boolean> placementCallback)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if (!carry.isCarrying(CarryType.ENTITY))
			return false;

		if (player.tickCount == carry.getTick())
			return false;

		Level level = player.getLevel();

		BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(player.position(), facing, pos));
		if (!level.getBlockState(pos).canBeReplaced(context))
			pos = pos.relative(facing);

		Vec3 placementPos = Vec3.atBottomCenterOf(pos);


		Entity entity = carry.getEntity(level);
		entity.setPos(placementPos);

		boolean doPlace = placementCallback == null || placementCallback.apply(placementPos, entity);
		if (!doPlace)
			return false;

        level.addFreshEntity(entity);
        if(entity instanceof Mob mob)
	        mob.playAmbientSound();

        player.swing(InteractionHand.MAIN_HAND, true);
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        return true;
	}

}
