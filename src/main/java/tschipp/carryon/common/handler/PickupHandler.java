package tschipp.carryon.common.handler;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.helper.CarryonGamestageHelper;
import tschipp.carryon.common.item.ItemCarryonBlock;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;

public class PickupHandler
{

	public static boolean canPlayerPickUpBlock(ServerPlayer player, @Nullable BlockEntity tile, Level world, BlockPos pos)
	{
		if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
			return false;

		BlockState state = world.getBlockState(pos);
		CompoundTag tag = new CompoundTag();
		if (tile != null)
			tile.save(tag);

		CarryOnOverride override = ScriptChecker.inspectBlock(world.getBlockState(pos), world, pos, tag);
		if (override != null)
		{
			return ScriptChecker.fulfillsConditions(override, player) && handleProtections(player, world, pos, state);
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
			else if (ListHandler.isForbidden(world.getBlockState(pos).getBlock()))
			{
				return false;
			}

			if (state.getDestroySpeed(world, pos) != -1 || player.isCreative())
			{
				double distance = Vec3.atLowerCornerOf(pos).distanceTo(player.position());
				double maxDist = Settings.maxDistance.get();

				if (distance < maxDist && !ItemCarryonBlock.isLocked(pos, world))
				{

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(state))
					{
						return CarryonGamestageHelper.hasGamestage(CustomPickupOverrideHandler.getPickupCondition(state), player) && handleProtections(player, world, pos, state);
					}
					else if (Settings.pickupAllBlocks.get() ? true : tile != null)
					{
						return handleProtections(player, world, pos, state);
					}

				}
			}
		}

		return false;
	}

	public static boolean canPlayerPickUpEntity(ServerPlayer player, Entity toPickUp)
	{
		if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
			return false;

		Vec3 pos = toPickUp.position();

		if (toPickUp instanceof Player)
			return false;

		CarryOnOverride override = ScriptChecker.inspectEntity(toPickUp);
		if (override != null)
		{
			return ScriptChecker.fulfillsConditions(override, player) && handleProtections(player, toPickUp);
		}
		else
		{
			if (toPickUp instanceof AgeableMob living && Settings.allowBabies.get() && (living.getAge() < 0 || living.isBaby()))
			{

				double distance = pos.distanceToSqr(player.position());
				if (distance <= Math.pow(Settings.maxDistance.get(), 2) && toPickUp instanceof TamableAnimal tame && tame.getOwnerUUID() != null && tame.getOwnerUUID() != Player.createPlayerUUID(player.getGameProfile()))
					return false;

				if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
				{
					return CarryonGamestageHelper.hasGamestage(CustomPickupOverrideHandler.getPickupCondition(toPickUp), player) && handleProtections(player, toPickUp);
				}
				else
					return handleProtections(player, toPickUp);
			}

			if (Settings.useWhitelistEntities.get())
			{
				if (!ListHandler.isAllowed(toPickUp))
				{
					return false;
				}
			}
			else if (ListHandler.isForbidden(toPickUp))
			{
				return false;
			}

			if ((Settings.pickupHostileMobs.get() ? true : toPickUp.getType().getCategory() != MobCategory.MONSTER || player.isCreative()) && (Settings.pickupHostileMobs.get() ? true : toPickUp.getType().getCategory() != MobCategory.MONSTER || player.isCreative()))
			{
				if (toPickUp.getBbHeight() <= Settings.maxEntityHeight.get() && toPickUp.getBbWidth() <= Settings.maxEntityWidth.get() || player.isCreative())
				{
					double distance = pos.distanceToSqr(player.position());
					if (distance < Math.pow(Settings.maxDistance.get(), 2))
					{
						if (toPickUp instanceof TamableAnimal tame)
						{
							UUID owner = tame.getOwnerUUID();
							UUID playerID = Player.createPlayerUUID(player.getGameProfile());
							if (owner != null && !owner.equals(playerID))
								return false;
						}

						if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
						{
							return CarryonGamestageHelper.hasGamestage(CustomPickupOverrideHandler.getPickupCondition(toPickUp), player) && handleProtections(player, toPickUp);
						}
						else
							return handleProtections(player, toPickUp);
					}

				}
			}
		}

		return false;
	}

	public static class PickUpBlockEvent extends BlockEvent.BreakEvent
	{
		public PickUpBlockEvent(Level world, BlockPos pos, BlockState state, Player player)
		{
			super(world, pos, state, player);
		}
	}

	public static class PickUpEntityEvent extends AttackEntityEvent
	{
		public PickUpEntityEvent(Player player, Entity target)
		{
			super(player, target);
		}
	}

	private static boolean handleProtections(ServerPlayer player, Level world, BlockPos pos, BlockState state)
	{
		boolean breakable = true;

		PickUpBlockEvent event = new PickUpBlockEvent(world, pos, state, player);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			breakable = false;

		return breakable;
	}

	private static boolean handleProtections(ServerPlayer player, Entity entity)
	{
		boolean canPickup = true;

		PickUpEntityEvent event = new PickUpEntityEvent(player, entity);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			canPickup = false;

		return canPickup;
	}

}
