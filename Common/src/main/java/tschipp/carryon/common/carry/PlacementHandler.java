package tschipp.carryon.common.carry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.Horse;
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
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;

import javax.annotation.Nullable;
import java.util.List;
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
		{
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 0.5F, 0.5F);
			return false;
		}

		state = getPlacementState(state, player, context, pos);
		boolean doPlace = placementCallback == null || placementCallback.apply(pos, state);

		if (!doPlace)
			return false;

		if(carry.getActiveScript().isPresent())
		{
			ScriptEffects effects = carry.getActiveScript().get().scriptEffects();
			String cmd = effects.commandPlace();
			if(!cmd.isEmpty())
				player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().getName() + " run " + cmd);
		}

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

		if (!carry.isCarrying(CarryType.ENTITY) && !carry.isCarrying(CarryType.PLAYER))
			return false;

		if (player.tickCount == carry.getTick())
			return false;

		Level level = player.getLevel();

		BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(player.position(), facing, pos));
		if (!level.getBlockState(pos).canBeReplaced(context))
			pos = pos.relative(facing);

		Vec3 placementPos = Vec3.atBottomCenterOf(pos);

		if(carry.isCarrying(CarryType.PLAYER))
		{
			Entity otherPlayer = player.getFirstPassenger();
			player.ejectPassengers();
			carry.clear();
			CarryOnDataManager.setCarryData(player, carry);
			if(otherPlayer == null)
				return true;
			otherPlayer.teleportTo(placementPos.x, placementPos.y, placementPos.z);
			player.swing(InteractionHand.MAIN_HAND, true);
			return true;
		}

		Entity entity = carry.getEntity(level);
		entity.setPos(placementPos);

		boolean doPlace = placementCallback == null || placementCallback.apply(placementPos, entity);
		if (!doPlace)
			return false;

		if(carry.getActiveScript().isPresent())
		{
			ScriptEffects effects = carry.getActiveScript().get().scriptEffects();
			String cmd = effects.commandPlace();
			if(!cmd.isEmpty())
				player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().getName() + " run " + cmd);
		}

        level.addFreshEntity(entity);
        if(entity instanceof Mob mob)
	        mob.playAmbientSound();

        player.swing(InteractionHand.MAIN_HAND, true);
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        return true;
	}

	public static void tryStackEntity(ServerPlayer player, Entity entityClicked)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if(!carry.isCarrying(CarryType.ENTITY) && !carry.isCarrying(CarryType.PLAYER))
			return;

		Level level = player.level;
		Entity entityHeld;
		if(carry.isCarrying(CarryType.ENTITY))
			entityHeld = carry.getEntity(level);
		else
			entityHeld = player.getFirstPassenger();


		double sizeHeldEntity = entityHeld.getBbHeight() * entityHeld.getBbWidth();
		double distance = entityClicked.blockPosition().distSqr(player.blockPosition());
		Entity lowestEntity = entityClicked.getRootVehicle();
		int numPassengers = getPassengerCount(lowestEntity);
		if (numPassengers < Constants.COMMON_CONFIG.settings.maxEntityStackLimit - 1)
		{
			Entity topEntity = getTopPassenger(lowestEntity);

			if(topEntity == entityHeld)
				return;

			if (ListHandler.isStackingPermitted(topEntity))
			{
				double sizeEntity = topEntity.getBbHeight() * topEntity.getBbWidth();
				if (Constants.COMMON_CONFIG.settings.entitySizeMattersStacking && sizeHeldEntity <= sizeEntity || !Constants.COMMON_CONFIG.settings.entitySizeMattersStacking)
				{
					if (topEntity instanceof Horse horse)
						horse.setTamed(true);

					if (distance < 6)
					{
						double tempX = entityClicked.getX();
						double tempY = entityClicked.getY();
						double tempZ = entityClicked.getZ();
						if(carry.isCarrying(CarryType.ENTITY)) {
							entityHeld.setPos(tempX, tempY + 2.6, tempZ);
							level.addFreshEntity(entityHeld);
							entityHeld.teleportTo(tempX, tempY, tempZ);
						}
						entityHeld.startRiding(topEntity, false);
					}
					else
					{
						if(carry.isCarrying(CarryType.ENTITY)) {
							entityHeld.setPos(entityClicked.getX(), entityClicked.getY(), entityClicked.getZ());
							level.addFreshEntity(entityHeld);
						}
						entityHeld.startRiding(topEntity, false);
					}

					if(carry.getActiveScript().isPresent())
					{
						ScriptEffects effects = carry.getActiveScript().get().scriptEffects();
						String cmd = effects.commandPlace();
						if(!cmd.isEmpty())
							player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().getName() + " run " + cmd);
					}

					player.swing(InteractionHand.MAIN_HAND, true);
					carry.clear();
					CarryOnDataManager.setCarryData(player, carry);
					level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.HORSE_SADDLE, SoundSource.PLAYERS, 0.5F, 1.5F);
				}
				else
				{
					level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 0.5F, 0.5F);
				}
			}
		}
		else
		{
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 0.5F, 0.5F);
		}
	}

	private static int getPassengerCount(Entity entity)
	{
		int passengers = 0;
		while (entity.isVehicle())
		{
			List<Entity> pass = entity.getPassengers();
			if (!pass.isEmpty())
			{
				entity = pass.get(0);
				passengers++;
			}
		}

		return passengers;
	}

	private static Entity getTopPassenger(Entity entity)
	{
		Entity top = entity;
		while (entity.isVehicle())
		{
			List<Entity> pass = entity.getPassengers();
			if (!pass.isEmpty())
			{
				entity = pass.get(0);
				top = entity;
			}
		}

		return top;
	}

}
