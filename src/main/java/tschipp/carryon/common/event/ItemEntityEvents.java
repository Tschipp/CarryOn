package tschipp.carryon.common.event;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.PickupHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;
import tschipp.carryon.network.client.CarrySlotPacket;

public class ItemEntityEvents
{


	private final List<Entity> riddenByEntities = Lists.<Entity>newArrayList();

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
		{
			player.getEntityData().removeTag("carrySlot");
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

			if (main.isEmpty() && off.isEmpty() && CarryOnKeybinds.isKeyPressed(player))
			{
				ItemStack stack = new ItemStack(RegistrationHandler.itemEntity);

				if (entity.hurtResistantTime == 0)
				{
					if(entity instanceof EntityAnimal)
						((EntityAnimal) entity).clearLeashed(true, true);

					if (PickupHandler.canPlayerPickUpEntity(player, entity))
					{
						if (ItemEntity.storeEntityData(entity, world, stack))
						{
							if (entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
							{
								IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
								for (int i = 0; i < handler.getSlots(); i++)
								{
									handler.extractItem(i, 64, false);
								}
							}

							CarryOnOverride override = ScriptChecker.inspectEntity(entity);
							int overrideHash = 0;
							if (override != null)
								overrideHash = override.hashCode();

							CarryOn.network.sendToAllAround(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), overrideHash), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 256));
							entity.setDead();
							player.setHeldItem(EnumHand.MAIN_HAND, stack);
							event.setCanceled(true);
						}
					}
				}

			} else if (!main.isEmpty() && main.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(main) && CarryOnKeybinds.isKeyPressed(player) && CarryOnConfig.settings.stackableEntities) {


				Entity entityHeld = ItemEntity.getEntity(main, world);

				if (entity.hurtResistantTime == 0 && entityHeld instanceof EntityLivingBase) {

					if (!world.isRemote && entityHeld.getUniqueID() != entity.getUniqueID() && !entityHeld.isDead && !entity.isDead) {

						double sizeEntity = entity.height * entity.width;
						double sizeHeldEntity = entityHeld.height * entityHeld.width;
						//if no riders
						if (!entity.isBeingRidden()) {
							//if entity size matters in stacking
							if ((CarryOnConfig.settings.entitySizeMattersStacking && sizeHeldEntity <= sizeEntity) || !CarryOnConfig.settings.entitySizeMattersStacking) {
								//Tame Horse so it doens't buck rider
								if (entity instanceof EntityHorse) {
									EntityHorse horse = (EntityHorse) entity;
									horse.setHorseTamed(true);
								}
								double distance = pos.distanceSqToCenter(player.posX, player.posY + 0.5, player.posZ);

								//if too close, change position (and back) to bypass protected canBeRidden
								if (distance < 6) {
									double tempX = entity.posX;
									double tempY = entity.posY;
									double tempZ = entity.posZ;
									entity.setPosition(tempX, tempY + 2.6, tempZ);
									world.spawnEntity(entityHeld);
									entityHeld.startRiding(entity, false);
									entityHeld.setPositionAndUpdate(tempX, tempY, tempZ);
								} else {
									world.spawnEntity(entityHeld);
									entityHeld.startRiding(entity, false);
								}
							} else {
								world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_BASS, SoundCategory.PLAYERS, 0.5F, 1.5F);
								world.spawnEntity(entityHeld);
							}

							//if multiple riders, loop through and add next to top (elevator style)
						} else {
							Entity entityTry = entity.getPassengers().get(0);
							int tempLimit = CarryOnConfig.settings.maxEntityStackLimit;

							//force limit to prevent crash
							if (tempLimit < 0) {
								tempLimit = 1;
							}
							for (int i = 0; i <= tempLimit; i++) { 

								if (entityTry.isBeingRidden()) {
									entityTry = entityTry.getPassengers().get(0);
								} else {
									break;
								}
							}

							double distance = pos.distanceSqToCenter(player.posX, player.posY + 0.5, player.posZ);
							if (distance < 6) {
								double tempX = entity.posX;
								double tempY = entity.posY;
								double tempZ = entity.posZ;
								entity.setPosition(tempX, tempY + 2.6, tempZ);
								world.spawnEntity(entityHeld);
								if (entityTry != null) {
									//if entity size matters in stacking
									if ((CarryOnConfig.settings.entitySizeMattersStacking && sizeHeldEntity <= sizeEntity) || !CarryOnConfig.settings.entitySizeMattersStacking) {
										entityHeld.startRiding(entityTry, false);
										entityHeld.setPositionAndUpdate(tempX, tempY, tempZ);
									} else { 
										world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_BASS, SoundCategory.PLAYERS, 0.5F, 1.5F);
									}
								}
							} else {
								world.spawnEntity(entityHeld);
								if ((CarryOnConfig.settings.entitySizeMattersStacking && sizeHeldEntity <= sizeEntity) || !CarryOnConfig.settings.entitySizeMattersStacking) {
									entityHeld.startRiding(entityTry, false);
								} else {
									world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_BASS, SoundCategory.PLAYERS, 0.5F, 1.5F);
								}
							}
						}

						if ((CarryOnConfig.settings.entitySizeMattersStacking && sizeHeldEntity <= sizeEntity) || !CarryOnConfig.settings.entitySizeMattersStacking) {
							world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_HORSE_SADDLE, SoundCategory.PLAYERS, 0.5F, 1.5F);
						}

						ItemEntity.clearEntityData(main);
						player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
						CarryOn.network.sendToAllAround(new CarrySlotPacket(9, player.getEntityId()), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 256));
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
		World world = entity.world;
		ItemStack main = entity.getHeldItemMainhand();
		if (!main.isEmpty() && main.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(main))
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
