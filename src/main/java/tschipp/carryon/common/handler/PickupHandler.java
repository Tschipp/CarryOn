package tschipp.carryon.common.handler;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.helper.CarryonGamestageHelper;
import tschipp.carryon.common.item.ItemCarryonBlock;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;

import javax.annotation.Nullable;
import java.util.UUID;

public class PickupHandler
{

	public static boolean canPlayerPickUpBlock(ServerPlayerEntity player, @Nullable TileEntity tile, World world, BlockPos pos)
	{		
		if(player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
			return false;
		
		
		BlockState state = world.getBlockState(pos);
		CompoundNBT tag = new CompoundNBT();
		if (tile != null)
			tile.save(tag);
		
		CarryOnOverride override = ScriptChecker.inspectBlock(world.getBlockState(pos), world, pos, tag);
		if (override != null)
		{
			return (ScriptChecker.fulfillsConditions(override, player)) && handleProtections((ServerPlayerEntity) player, world, pos, state);
		}
		else
		{
			if (Settings.useWhitelistBlocks.get())
			{
				if (!ListHandler.isAllowed(world.getBlockState(pos).getBlock()))
				{
					return false;
				}
			}
			else
			{
				if (ListHandler.isForbidden(world.getBlockState(pos).getBlock()))
				{
					return false;
				}
			}

			if ((state.getDestroySpeed(world, pos) != -1 || player.isCreative()))
			{
				double distance = Vector3d.atLowerCornerOf(pos).distanceTo(player.position());
				double maxDist = Settings.maxDistance.get();
				
				if (distance < maxDist)
				{

					if (!ItemCarryonBlock.isLocked(pos, world))
					{

						if (CustomPickupOverrideHandler.hasSpecialPickupConditions(state))
						{
							return CarryonGamestageHelper.hasGamestage(CustomPickupOverrideHandler.getPickupCondition(state), player) && handleProtections((ServerPlayerEntity) player, world, pos, state);
						}
						else if (Settings.pickupAllBlocks.get() ? true : tile != null)
						{
							return handleProtections(player, world, pos, state);
						}

					}
				}
			}
		}

		return false;
	}

	public static boolean canPlayerPickUpEntity(ServerPlayerEntity player, Entity toPickUp)
	{
		if(player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
			return false;
		
		BlockPos pos = toPickUp.blockPosition();

		if (toPickUp instanceof PlayerEntity)
			return false;

		CarryOnOverride override = ScriptChecker.inspectEntity(toPickUp);
		if (override != null)
		{
			return (ScriptChecker.fulfillsConditions(override, player)) && handleProtections(player, toPickUp);
		}
		else
		{
			if (toPickUp instanceof AgeableEntity && Settings.allowBabies.get())
			{
				AgeableEntity living = (AgeableEntity) toPickUp;
				if (living.getAge() < 0 || living.isBaby())
				{

					double distance = pos.distSqr(player.blockPosition());
					if (distance < Math.pow(Settings.maxDistance.get(), 2))
					{
						if (toPickUp instanceof TameableEntity)
						{
							TameableEntity tame = (TameableEntity) toPickUp;
							if (tame.getOwnerUUID() != null && tame.getOwnerUUID() != PlayerEntity.createPlayerUUID(player.getGameProfile()))
								return false;
						}
					}

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
					{
						return CarryonGamestageHelper.hasGamestage(CustomPickupOverrideHandler.getPickupCondition(toPickUp), player) && handleProtections((ServerPlayerEntity) player, toPickUp);
					}
					else
						return handleProtections((ServerPlayerEntity) player, toPickUp);
				}
			}

			if (Settings.useWhitelistEntities.get())
			{
				if (!ListHandler.isAllowed(toPickUp))
				{
					return false;
				}
			}
			else
			{
				if (ListHandler.isForbidden(toPickUp))
				{
					return false;
				}
			}

			if ((Settings.pickupHostileMobs.get() ? true : toPickUp.getType().getCategory() != EntityClassification.MONSTER || player.isCreative()))
			{
				if ((Settings.pickupHostileMobs.get() ? true : toPickUp.getType().getCategory() != EntityClassification.MONSTER  || player.isCreative()))
				{
					if ((toPickUp.getBbHeight() <= Settings.maxEntityHeight.get() && toPickUp.getBbWidth() <= Settings.maxEntityWidth.get() || player.isCreative()))
					{
						double distance = pos.distSqr(player.blockPosition());
						if (distance < Math.pow(Settings.maxDistance.get(), 2))
						{
							if (toPickUp instanceof TameableEntity)
							{
								TameableEntity tame = (TameableEntity) toPickUp;
								UUID owner = tame.getOwnerUUID();
								UUID playerID = PlayerEntity.createPlayerUUID(player.getGameProfile());
								if (owner != null && !owner.equals(playerID))
									return false;
							}

							if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
							{
								return CarryonGamestageHelper.hasGamestage(CustomPickupOverrideHandler.getPickupCondition(toPickUp), player) && handleProtections((ServerPlayerEntity) player, toPickUp);
							}
							else
								return handleProtections((ServerPlayerEntity) player, toPickUp);
						}
						

					}
				}

			}
		}

		return false;
	}

	public static class PickUpBlockEvent extends BlockEvent.BreakEvent
	{
		public PickUpBlockEvent(World world, BlockPos pos, BlockState state, PlayerEntity player)
		{
			super(world, pos, state, player);
		}		
	}
	
	public static class PickUpEntityEvent extends Event
	{
		public final PlayerEntity player;
		public final Entity target;
		public PickUpEntityEvent(PlayerEntity player, Entity target)
		{
			this.player = player;
			this.target = target;
		}
	}
	
	private static boolean handleProtections(ServerPlayerEntity player, World world, BlockPos pos, BlockState state)
	{
		boolean breakable = true;

		PickUpBlockEvent event = new PickUpBlockEvent(world, pos, state, player);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			breakable = false;

		return breakable;
	}

	private static boolean handleProtections(ServerPlayerEntity player, Entity entity)
	{
		boolean canPickup = true;

		PickUpEntityEvent event = new PickUpEntityEvent(player, entity);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			canPickup = false;

		return canPickup;
	}

}
