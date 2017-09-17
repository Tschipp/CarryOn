package tschipp.carryon.common.handler;

import javax.annotation.Nullable;

import com.feed_the_beast.ftbl.lib.math.BlockPosContainer;
import com.feed_the_beast.ftbu.FTBUPermissions;
import com.feed_the_beast.ftbu.api.chunks.BlockInteractionType;
import com.feed_the_beast.ftbu.api_impl.ClaimedChunkStorage;

import net.darkhax.gamestages.capabilities.PlayerDataHandler;
import net.darkhax.gamestages.capabilities.PlayerDataHandler.IStageData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.item.ItemTile;

public class PickupHandler
{

	public static boolean canPlayerPickUpBlock(EntityPlayer player, @Nullable TileEntity tile, World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		player.closeScreen();

		if (CarryOnConfig.settings.useWhiteistBlocks)
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
						IStageData stageData = PlayerDataHandler.getStageData(player);
						String condition = CustomPickupOverrideHandler.getPickupCondition(state);
						if (stageData.hasUnlockedStage(condition))
							return true && handleFTBUtils((EntityPlayerMP) player, world, pos, state);

					}
					else if (CarryOnConfig.settings.pickupAllBlocks ? true : tile != null)
					{
						return true && handleFTBUtils((EntityPlayerMP) player, world, pos, state);
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
		
		// check for allow babies to be picked up
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

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
					{
						IStageData stageData = PlayerDataHandler.getStageData(player);
						String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
						if (stageData.hasUnlockedStage(condition))
							return true;
					}
					else
						return true;
				}
			}
		}

		if (CarryOnConfig.settings.useWhiteistEntities)
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
			if ((toPickUp.height <= CarryOnConfig.settings.maxEntityHeight && toPickUp.width <= CarryOnConfig.settings.maxEntityWidth || player.isCreative()))
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

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(toPickUp))
					{
						IStageData stageData = PlayerDataHandler.getStageData(player);
						String condition = CustomPickupOverrideHandler.getPickupCondition(toPickUp);
						if (stageData.hasUnlockedStage(condition))
							return true;
					}
					else
						return true;
				}
			}
		}

		return false;
	}

	private static boolean handleFTBUtils(EntityPlayerMP player, World world, BlockPos pos, IBlockState state)
	{
		if (Loader.isModLoaded("ftbu"))
		{
			BlockPosContainer container = new BlockPosContainer(world, pos, state);
			return ClaimedChunkStorage.INSTANCE.canPlayerInteract((EntityPlayerMP) player, EnumHand.MAIN_HAND, container, BlockInteractionType.CNB_BREAK);
		}
		return true;
	}

}
