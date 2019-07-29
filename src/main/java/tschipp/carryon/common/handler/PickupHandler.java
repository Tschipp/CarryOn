package tschipp.carryon.common.handler;

import java.lang.reflect.Method;
import java.util.UUID;

import javax.annotation.Nullable;

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
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.item.ItemCarryonBlock;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;

public class PickupHandler
{

	public static boolean canPlayerPickUpBlock(PlayerEntity player, @Nullable TileEntity tile, World world, BlockPos pos)
	{		
		
		BlockState state = world.getBlockState(pos);
		CompoundNBT tag = new CompoundNBT();
		if (tile != null)
			tile.write(tag);
		
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

			if ((state.getBlockHardness(world, pos) != -1 || player.isCreative()))
			{
				double distance = pos.distanceSq(player.getPosition());

				if (distance < Math.pow(Settings.maxDistance.get(), 2))
				{

					if (!ItemCarryonBlock.isLocked(pos, world))
					{

						if (CustomPickupOverrideHandler.hasSpecialPickupConditions(state))
						{
							try
							{
								Class<?> gameStageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
								Class<?> iStageData = Class.forName("net.darkhax.gamestages.data.IStageData");

								Method getPlayerData = ObfuscationReflectionHelper.findMethod(gameStageHelper, "getPlayerData", PlayerEntity.class);
								Method hasStage = ObfuscationReflectionHelper.findMethod(iStageData, "hasStage", String.class);

								Object stageData = getPlayerData.invoke(null, player);
								String condition = CustomPickupOverrideHandler.getPickupCondition(state);
								boolean has = (boolean) hasStage.invoke(stageData, condition);

								if (has)
									return handleProtections((ServerPlayerEntity) player, world, pos, state);
							}
							catch (Exception e)
							{
								try
								{
									Class<?> playerDataHandler = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler");
									Class<?> iStageData = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler$IStageData");

									Method getStageData = ObfuscationReflectionHelper.findMethod(playerDataHandler, "getStageData", PlayerEntity.class);
									Method hasUnlockedStage = ObfuscationReflectionHelper.findMethod(iStageData, "hasUnlockedStage", String.class);

									Object stageData = getStageData.invoke(null, player);
									String condition = CustomPickupOverrideHandler.getPickupCondition(state);
									boolean has = (boolean) hasUnlockedStage.invoke(stageData, condition);

									if (has)
										return handleProtections((ServerPlayerEntity) player, world, pos, state);
								}
								catch (Exception ex)
								{
									return handleProtections((ServerPlayerEntity) player, world, pos, state);
								}
							}

						}
						else if (Settings.pickupAllBlocks.get() ? true : tile != null)
						{
							return handleProtections((ServerPlayerEntity) player, world, pos, state);
						}

					}
				}
			}
		}

		return false;
	}

	public static boolean canPlayerPickUpEntity(PlayerEntity player, Entity toPickUp)
	{
		BlockPos pos = toPickUp.getPosition();

		if (toPickUp instanceof PlayerEntity)
			return false;

		CarryOnOverride override = ScriptChecker.inspectEntity(toPickUp);
		if (override != null)
		{
			return (ScriptChecker.fulfillsConditions(override, player)) && handleProtections((ServerPlayerEntity) player, toPickUp);
		}
		else
		{
			if (toPickUp instanceof AgeableEntity && Settings.allowBabies.get())
			{
				AgeableEntity living = (AgeableEntity) toPickUp;
				if (living.getGrowingAge() < 0 || living.isChild())
				{

					double distance = pos.distanceSq(player.getPosition());
					if (distance < Math.pow(Settings.maxDistance.get(), 2))
					{
						if (toPickUp instanceof TameableEntity)
						{
							TameableEntity tame = (TameableEntity) toPickUp;
							if (tame.getOwnerId() != null && tame.getOwnerId() != PlayerEntity.getUUID(player.getGameProfile()))
								return false;
						}
					}

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
					{
						try
						{
							Class<?> gameStageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
							Class<?> iStageData = Class.forName("net.darkhax.gamestages.data.IStageData");

							Method getPlayerData = ObfuscationReflectionHelper.findMethod(gameStageHelper, "getPlayerData", PlayerEntity.class);
							Method hasStage = ObfuscationReflectionHelper.findMethod(iStageData, "hasStage", String.class);

							Object stageData = getPlayerData.invoke(null, player);
							String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
							boolean has = (boolean) hasStage.invoke(stageData, condition);

							if (has)
								return handleProtections((ServerPlayerEntity) player, toPickUp);
						}
						catch (Exception e)
						{
							try
							{
								Class<?> playerDataHandler = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler");
								Class<?> iStageData = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler$IStageData");

								Method getStageData = ObfuscationReflectionHelper.findMethod(playerDataHandler, "getStageData", PlayerEntity.class);
								Method hasUnlockedStage = ObfuscationReflectionHelper.findMethod(iStageData, "hasUnlockedStage", String.class);

								Object stageData = getStageData.invoke(null, player);
								String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
								boolean has = (boolean) hasUnlockedStage.invoke(stageData, condition);

								if (has)
									return handleProtections((ServerPlayerEntity) player, toPickUp);
							}
							catch (Exception ex)
							{
								return handleProtections((ServerPlayerEntity) player, toPickUp);
							}
						}
					}
					else
						return true && handleProtections((ServerPlayerEntity) player, toPickUp);
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

			if ((Settings.pickupHostileMobs.get() ? true : toPickUp.getType().getClassification() != EntityClassification.MONSTER || player.isCreative()))
			{
				if ((Settings.pickupHostileMobs.get() ? true : toPickUp.getType().getClassification() != EntityClassification.MONSTER  || player.isCreative()))
				{
					if ((toPickUp.getHeight() <= Settings.maxEntityHeight.get() && toPickUp.getWidth() <= Settings.maxEntityWidth.get() || player.isCreative()))
					{
						double distance = pos.distanceSq(player.getPosition());
						if (distance < Math.pow(Settings.maxDistance.get(), 2))
						{
							if (toPickUp instanceof TameableEntity)
							{
								TameableEntity tame = (TameableEntity) toPickUp;
								UUID owner = tame.getOwnerId();
								UUID playerID = PlayerEntity.getUUID(player.getGameProfile());
								if (owner != null && !owner.equals(playerID))
									return false;
							}

							if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
							{
								try
								{
									Class<?> gameStageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
									Class<?> iStageData = Class.forName("net.darkhax.gamestages.data.IStageData");

									Method getPlayerData = ObfuscationReflectionHelper.findMethod(gameStageHelper, "getPlayerData", PlayerEntity.class);
									Method hasStage = ObfuscationReflectionHelper.findMethod(iStageData, "hasStage", String.class);

									Object stageData = getPlayerData.invoke(null, player);
									String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
									boolean has = (boolean) hasStage.invoke(stageData, condition);

									if (has)
										return handleProtections((ServerPlayerEntity) player, toPickUp);
								}
								catch (Exception e)
								{
									try
									{
										Class<?> playerDataHandler = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler");
										Class<?> iStageData = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler$IStageData");

										Method getStageData = ObfuscationReflectionHelper.findMethod(playerDataHandler, "getStageData", PlayerEntity.class);
										Method hasUnlockedStage = ObfuscationReflectionHelper.findMethod(iStageData, "hasUnlockedStage", String.class);

										Object stageData = getStageData.invoke(null, player);
										String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
										boolean has = (boolean) hasUnlockedStage.invoke(stageData, condition);

										if (has)
											return handleProtections((ServerPlayerEntity) player, toPickUp);
									}
									catch (Exception ex)
									{
										return handleProtections((ServerPlayerEntity) player, toPickUp);
									}
								}
							}
							else
								return true && handleProtections((ServerPlayerEntity) player, toPickUp);
						}
						

					}
				}

			}
		}

		return false;
	}

	private static boolean handleProtections(ServerPlayerEntity player, World world, BlockPos pos, BlockState state)
	{
		boolean breakable = true;

		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			breakable = false;

		return breakable;
	}

	private static boolean handleProtections(ServerPlayerEntity player, Entity entity)
	{
		boolean canPickup = true;

		AttackEntityEvent event = new AttackEntityEvent(player, entity);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			canPickup = false;

		return canPickup;
	}

}
