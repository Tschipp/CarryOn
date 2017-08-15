package tschipp.carryon.common.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.ForbiddenTileHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;

public class ItemEntityEvents
{

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
		{
			event.setUseBlock(Result.DENY);
		}

	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemDropped(EntityJoinWorldEvent event)
	{
		Entity e = event.getEntity();
		World world = event.getWorld();
		if (e instanceof EntityItem)
		{
			EntityItem eitem = (EntityItem) e;
			ItemStack stack = eitem.getEntityItem();
			Item item = stack.getItem();
			if (item == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
			{
				BlockPos pos = eitem.getPosition();
				Entity entity = ItemEntity.getEntity(stack, world);
				entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				world.spawnEntity(entity);

				ItemEntity.clearEntityData(stack);
				eitem.setEntityItemStack(ItemStack.EMPTY);
			}
		}
	}

	@SubscribeEvent
	public void onEntityRightClick(PlayerInteractEvent.EntityInteract event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();
		World world = event.getWorld();
		Entity entity = event.getTarget();
		BlockPos pos = entity.getPosition();

		if (main.isEmpty() && off.isEmpty() && player.isSneaking())
		{
			ItemStack stack = new ItemStack(RegistrationHandler.itemEntity);

			if (!(entity instanceof EntityPlayer) && !ForbiddenTileHandler.isForbidden(entity) && (CarryOnConfig.settings.pickupHostileMobs ? true : !entity.isCreatureType(EnumCreatureType.MONSTER, false) || player.isCreative()) && (entity.height <= CarryOnConfig.settings.maxEntityHeight && entity.width <= CarryOnConfig.settings.maxEntityWidth || player.isCreative()))
			{
				double distance = pos.distanceSqToCenter(player.posX, player.posY + 0.5, player.posZ);

				if (distance < Math.pow(CarryOnConfig.settings.maxDistance, 2))
				{
					if (entity instanceof EntityTameable)
					{
						EntityTameable tame = (EntityTameable) entity;
						if (tame.getOwnerId() != null && tame.getOwnerId() != player.getUUID(player.getGameProfile()))
							return;
					}
					if (ItemEntity.storeEntityData(entity, world, stack))
					{
						entity.setDead();
						player.setHeldItem(EnumHand.MAIN_HAND, stack);
					}
				}

			}

		}
	}

}
