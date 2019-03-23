package tschipp.carryon.common.event;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.handler.ListHandler;
import tschipp.carryon.common.handler.PickupHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;

public class ItemEntityEvents
{

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
		{
			player.getEntityData().removeTag("carrySlot");
			event.setUseBlock(Result.DENY);

			if (!player.world.isRemote)
			{
				CarryOnOverride override = ScriptChecker.getOverride(player);
				if (override != null)
				{
					String command = override.getCommandPlace();
					
					if (command != null)
						player.getServer().getCommandManager().handleCommand(player.getServer().getCommandSource(), "/execute as " + player.getGameProfile().getName() + " run " + command);
				}
			}
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
			ItemStack stack = eitem.getItem();
			Item item = stack.getItem();
			if (item == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
			{
				BlockPos pos = eitem.getPosition();
				Entity entity = ItemEntity.getEntity(stack, world);
				entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				world.spawnEntity(entity);

				ItemEntity.clearEntityData(stack);
				eitem.setItem(ItemStack.EMPTY);
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
					if (entity instanceof EntityAnimal)
						((EntityAnimal) entity).clearLeashed(true, true);

					if (PickupHandler.canPlayerPickUpEntity(player, entity))
					{
						if (ItemEntity.storeEntityData(entity, world, stack))
						{
							LazyOptional<IItemHandler> handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

							handler.ifPresent((hand) -> {
								for (int i = 0; i < hand.getSlots(); i++)
								{
									hand.extractItem(i, 64, false);
								}
							});

							CarryOnOverride override = ScriptChecker.inspectEntity(entity);
							int overrideHash = 0;
							if (override != null)
								overrideHash = override.hashCode();

							ItemEvents.sendPacket(player, player.inventory.currentItem, overrideHash);

							if (entity instanceof EntityLiving)
								((EntityLiving) entity).setHealth(0);

							entity.remove();
							player.setHeldItem(EnumHand.MAIN_HAND, stack);
							event.setCanceled(true);
							event.setCancellationResult(EnumActionResult.FAIL);
						}
					}
				}

			} else if (!main.isEmpty() && main.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(main) && !CarryOnKeybinds.isKeyPressed(player) && Settings.stackableEntities.get())
			{
				Entity entityHeld = ItemEntity.getEntity(main, world);

				if (entity.hurtResistantTime == 0 && entityHeld instanceof EntityLivingBase)
				{

					if (!world.isRemote && entityHeld.getUniqueID() != entity.getUniqueID() && !entityHeld.removed && !entity.removed)
					{

						double sizeHeldEntity = entityHeld.height * entityHeld.width;
						double distance = pos.distanceSqToCenter(player.posX, player.posY + 0.5, player.posZ);
						Entity lowestEntity = entity.getLowestRidingEntity();
						int numPassengers = getAllPassengers(lowestEntity);
						if (numPassengers < Settings.maxEntityStackLimit.get() - 1)
						{
							Entity topEntity = getTopPassenger(lowestEntity);

							if (Settings.useWhitelistStacking.get() ? ListHandler.isStackingAllowed(topEntity) : !ListHandler.isStackingForbidden(topEntity))
							{
								double sizeEntity = topEntity.height * topEntity.width;
								if ((Settings.entitySizeMattersStacking.get() && sizeHeldEntity <= sizeEntity) || !Settings.entitySizeMattersStacking.get())
								{
									if (topEntity instanceof EntityHorse)
									{
										EntityHorse horse = (EntityHorse) topEntity;
										horse.setHorseTamed(true);
									}

									if (distance < 6)
									{
										double tempX = entity.posX;
										double tempY = entity.posY;
										double tempZ = entity.posZ;
										entityHeld.setPosition(tempX, tempY + 2.6, tempZ);
										world.spawnEntity(entityHeld);
										entityHeld.startRiding(topEntity, false);
										entityHeld.setPositionAndUpdate(tempX, tempY, tempZ);
									} else
									{
										entityHeld.setPosition(entity.posX, entity.posY, entity.posZ);
										world.spawnEntity(entityHeld);
										entityHeld.startRiding(topEntity, false);
									}

									ItemEntity.clearEntityData(main);
									player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
									ItemEvents.sendPacket(player, 9, 0);
									event.setCanceled(true);
									event.setCancellationResult(EnumActionResult.FAIL);
									world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_HORSE_SADDLE, SoundCategory.PLAYERS, 0.5F, 1.5F);
								} else
								{
									world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.PLAYERS, 0.5F, 1.5F);
									return;
								}
							}
						} else
						{
							world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.PLAYERS, 0.5F, 1.5F);
							return;
						}
					}

				}

			}
		}

	}

	public static int getAllPassengers(Entity entity)
	{
		int passengers = 0;
		while (entity.isBeingRidden())
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

	public static Entity getTopPassenger(Entity entity)
	{
		Entity top = entity;
		while (entity.isBeingRidden())
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
