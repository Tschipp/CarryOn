package tschipp.carryon.common.event;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.handler.PickupHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;

public class ItemEntityEvents
{

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (stack != null && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
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
				world.spawnEntityInWorld(entity);

				ItemEntity.clearEntityData(stack);
				eitem.setEntityItemStack(null);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityRightClick(PlayerInteractEvent.EntityInteract event)
	{
		EntityPlayer player = event.getEntityPlayer();

		if (player instanceof EntityPlayerMP)
		{
			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
			World world = event.getWorld();
			Entity entity = event.getTarget();
			BlockPos pos = entity.getPosition();

			if (main == null && off == null && CarryOnKeybinds.isKeyPressed(player))
			{
				ItemStack stack = new ItemStack(RegistrationHandler.itemEntity);

				if (PickupHandler.canPlayerPickUpEntity(player, entity))
				{
					if (ItemEntity.storeEntityData(entity, world, stack))
					{
						entity.setDead();
						player.setHeldItem(EnumHand.MAIN_HAND, stack);
						event.setCanceled(true);
					}
				}

			}
		}

	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.worldObj;
		ItemStack main = entity.getHeldItemMainhand();
		if (main != null ? (main.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(main)) : false)
		{
			BlockPos pos = entity.getPosition();
			BlockPos below = pos.offset(EnumFacing.DOWN);

			if (world.getBlockState(pos).getMaterial() == Material.WATER || world.getBlockState(below).getMaterial() == Material.WATER)
			{
				Entity contained = ItemEntity.getEntity(main, world);
				if (contained != null)
				{
					float height = contained.height;
					float width = contained.width;
					
					entity.addVelocity(0, -0.01 * height * width, 0);
				}
			}
		}
	}

}
