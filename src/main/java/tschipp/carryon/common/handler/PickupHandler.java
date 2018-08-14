package tschipp.carryon.common.handler;

import java.lang.reflect.Method;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.item.ItemTile;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;

public class PickupHandler
{

	public static boolean canPlayerPickUpBlock(EntityPlayer player, @Nullable TileEntity tile, World world, BlockPos pos)
	{		
		
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		NBTTagCompound tag = new NBTTagCompound();
		if (tile != null)
			tile.writeToNBT(tag);

		CarryOnOverride override = ScriptChecker.inspectBlock(world.getBlockState(pos), world, pos, tag);
		if (override != null)
		{
			return (ScriptChecker.fulfillsConditions(override, player)) && handleProtections((EntityPlayerMP) player, world, pos, state);
		}
		else
		{
			if (CarryOnConfig.settings.useWhitelistBlocks)
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

			if ((block.getBlockHardness(state, world, pos) != -1 || player.isCreative()))
			{
				double distance = pos.distanceSqToCenter(player.posX, player.posY + 0.5, player.posZ);

				if (distance < Math.pow(CarryOnConfig.settings.maxDistance, 2))
				{

					if (!ItemTile.isLocked(pos, world))
					{

						if (CustomPickupOverrideHandler.hasSpecialPickupConditions(state))
						{
							try
							{
								Class<?> gameStageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
								Class<?> iStageData = Class.forName("net.darkhax.gamestages.data.IStageData");

								Method getPlayerData = ReflectionHelper.findMethod(gameStageHelper, "getPlayerData", null, EntityPlayer.class);
								Method hasStage = ReflectionHelper.findMethod(iStageData, "hasStage", null, String.class);

								Object stageData = getPlayerData.invoke(null, player);
								String condition = CustomPickupOverrideHandler.getPickupCondition(state);
								boolean has = (boolean) hasStage.invoke(stageData, condition);

								if (has)
									return handleProtections((EntityPlayerMP) player, world, pos, state);
							}
							catch (Exception e)
							{
								try
								{
									Class<?> playerDataHandler = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler");
									Class<?> iStageData = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler$IStageData");

									Method getStageData = ReflectionHelper.findMethod(playerDataHandler, "getStageData", null, EntityPlayer.class);
									Method hasUnlockedStage = ReflectionHelper.findMethod(iStageData, "hasUnlockedStage", null, String.class);

									Object stageData = getStageData.invoke(null, player);
									String condition = CustomPickupOverrideHandler.getPickupCondition(state);
									boolean has = (boolean) hasUnlockedStage.invoke(stageData, condition);

									if (has)
										return handleProtections((EntityPlayerMP) player, world, pos, state);
								}
								catch (Exception ex)
								{
									return handleProtections((EntityPlayerMP) player, world, pos, state);
								}
							}

						}
						else if (CarryOnConfig.settings.pickupAllBlocks ? true : tile != null)
						{
							return handleProtections((EntityPlayerMP) player, world, pos, state);
						}

					}
				}
			}
		}

		return false;
	}

	public static boolean canPlayerPickUpEntity(EntityPlayer player, Entity toPickUp)
	{
		BlockPos pos = toPickUp.getPosition();

		if (toPickUp instanceof EntityPlayer)
			return false;

		CarryOnOverride override = ScriptChecker.inspectEntity(toPickUp);
		if (override != null)
		{
			return (ScriptChecker.fulfillsConditions(override, player)) && handleProtections((EntityPlayerMP) player, toPickUp);
		}
		else
		{
			if (toPickUp instanceof EntityAgeable && CarryOnConfig.settings.allowBabies)
			{
				EntityAgeable living = (EntityAgeable) toPickUp;
				if (living.getGrowingAge() < 0 || living.isChild())
				{

					double distance = pos.distanceSqToCenter(player.posX, player.posY + 0.5, player.posZ);
					if (distance < Math.pow(CarryOnConfig.settings.maxDistance, 2))
					{
						if (toPickUp instanceof EntityTameable)
						{
							EntityTameable tame = (EntityTameable) toPickUp;
							if (tame.getOwnerId() != null && tame.getOwnerId() != player.getUUID(player.getGameProfile()))
								return false;
						}
					}

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
					{
						try
						{
							Class<?> gameStageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
							Class<?> iStageData = Class.forName("net.darkhax.gamestages.data.IStageData");

							Method getPlayerData = ReflectionHelper.findMethod(gameStageHelper, "getPlayerData", null, EntityPlayer.class);
							Method hasStage = ReflectionHelper.findMethod(iStageData, "hasStage", null, String.class);

							Object stageData = getPlayerData.invoke(null, player);
							String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
							boolean has = (boolean) hasStage.invoke(stageData, condition);

							if (has)
								return handleProtections((EntityPlayerMP) player, toPickUp);
						}
						catch (Exception e)
						{
							try
							{
								Class<?> playerDataHandler = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler");
								Class<?> iStageData = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler$IStageData");

								Method getStageData = ReflectionHelper.findMethod(playerDataHandler, "getStageData", null, EntityPlayer.class);
								Method hasUnlockedStage = ReflectionHelper.findMethod(iStageData, "hasUnlockedStage", null, String.class);

								Object stageData = getStageData.invoke(null, player);
								String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
								boolean has = (boolean) hasUnlockedStage.invoke(stageData, condition);

								if (has)
									return handleProtections((EntityPlayerMP) player, toPickUp);
							}
							catch (Exception ex)
							{
								return handleProtections((EntityPlayerMP) player, toPickUp);
							}
						}
					}
					else
						return true && handleProtections((EntityPlayerMP) player, toPickUp);
				}
			}

			if (CarryOnConfig.settings.useWhitelistEntities)
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

			if ((CarryOnConfig.settings.pickupHostileMobs ? true : !toPickUp.isCreatureType(EnumCreatureType.MONSTER, false) || player.isCreative()))
			{
				if ((CarryOnConfig.settings.pickupHostileMobs ? true : !toPickUp.isCreatureType(EnumCreatureType.MONSTER, false) || player.isCreative()))
				{
					if ((toPickUp.height <= CarryOnConfig.settings.maxEntityHeight && toPickUp.width <= CarryOnConfig.settings.maxEntityWidth || player.isCreative()))
					{
						double distance = pos.distanceSqToCenter(player.posX, player.posY + 0.5, player.posZ);
						if (distance < Math.pow(CarryOnConfig.settings.maxDistance, 2))
						{
							if (toPickUp instanceof EntityTameable)
							{
								EntityTameable tame = (EntityTameable) toPickUp;
								UUID owner = tame.getOwnerId();
								UUID playerID = player.getUUID(player.getGameProfile());
								if (owner != null && !owner.equals(playerID))
									return false;
							}

							if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
							{
								try
								{
									Class<?> gameStageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
									Class<?> iStageData = Class.forName("net.darkhax.gamestages.data.IStageData");

									Method getPlayerData = ReflectionHelper.findMethod(gameStageHelper, "getPlayerData", null, EntityPlayer.class);
									Method hasStage = ReflectionHelper.findMethod(iStageData, "hasStage", null, String.class);

									Object stageData = getPlayerData.invoke(null, player);
									String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
									boolean has = (boolean) hasStage.invoke(stageData, condition);

									if (has)
										return handleProtections((EntityPlayerMP) player, toPickUp);
								}
								catch (Exception e)
								{
									try
									{
										Class<?> playerDataHandler = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler");
										Class<?> iStageData = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler$IStageData");

										Method getStageData = ReflectionHelper.findMethod(playerDataHandler, "getStageData", null, EntityPlayer.class);
										Method hasUnlockedStage = ReflectionHelper.findMethod(iStageData, "hasUnlockedStage", null, String.class);

										Object stageData = getStageData.invoke(null, player);
										String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
										boolean has = (boolean) hasUnlockedStage.invoke(stageData, condition);

										if (has)
											return handleProtections((EntityPlayerMP) player, toPickUp);
									}
									catch (Exception ex)
									{
										return handleProtections((EntityPlayerMP) player, toPickUp);
									}
								}
							}
							else
								return true && handleProtections((EntityPlayerMP) player, toPickUp);
						}
						

					}
				}

			}
		}

		return false;
	}

	private static boolean handleProtections(EntityPlayerMP player, World world, BlockPos pos, IBlockState state)
	{
		boolean breakable = true;

		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			breakable = false;

		return breakable;
	}

	private static boolean handleProtections(EntityPlayerMP player, Entity entity)
	{
		boolean canPickup = true;

		AttackEntityEvent event = new AttackEntityEvent(player, entity);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			canPickup = false;

		return canPickup;
	}

}
