package tschipp.carryon.common.event;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.command.CommandCarryOn;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.handler.CustomPickupOverrideHandler;
import tschipp.carryon.common.handler.ListHandler;
import tschipp.carryon.common.handler.PickupHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemCarryonBlock;
import tschipp.carryon.common.item.ItemCarryonEntity;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;
import tschipp.carryon.common.scripting.ScriptReader;
import tschipp.carryon.network.client.CarrySlotPacket;
import tschipp.carryon.network.client.ScriptReloadPacket;

@EventBusSubscriber(modid = CarryOn.MODID)
public class ItemEvents
{
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (event.isCanceled())
			return;

		PlayerEntity player = event.getPlayer();
		ItemStack stack = player.getMainHandItem();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack))
		{
			player.getPersistentData().remove("carrySlot");
			event.setUseBlock(Result.DENY);

			if (!player.level.isClientSide)
			{
				CarryOnOverride override = ScriptChecker.getOverride(player);
				if (override != null)
				{
					String command = override.getCommandPlace();

					if (command != null && !command.isEmpty())
						player.getServer().getCommands().performCommand(player.getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().getName() + " run " + command);
				}
			}
		}

	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemDropped(EntityJoinWorldEvent event)
	{
		Entity e = event.getEntity();
		World world = event.getWorld();
		if (e instanceof net.minecraft.entity.item.ItemEntity)
		{
			net.minecraft.entity.item.ItemEntity eitem = (net.minecraft.entity.item.ItemEntity) e;
			ItemStack stack = eitem.getItem();
			Item item = stack.getItem();
			if (item == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack))
			{
				BlockPos pos = eitem.blockPosition();
				BlockPos finalPos = pos;
				BlockItemUseContext context = new DirectionalPlaceContext(world, pos, Direction.DOWN, stack, Direction.UP);

				if (!world.getBlockState(pos).canBeReplaced(context) || !context.canPlace())
				{
					for (Direction facing : Direction.values())
					{
						BlockPos offsetPos = pos.relative(facing);
						BlockItemUseContext newContext = new DirectionalPlaceContext(world, offsetPos, Direction.DOWN, stack, Direction.UP);
						if (world.getBlockState(offsetPos).canBeReplaced(newContext) && newContext.canPlace())
						{
							finalPos = offsetPos;
							break;
						}
					}
				}
				world.setBlockAndUpdate(finalPos, ItemCarryonBlock.getBlockState(stack));
				TileEntity tile = world.getBlockEntity(finalPos);
				if (tile != null)
				{
					tile.deserializeNBT(ItemCarryonBlock.getTileData(stack));
					tile.setPosition(finalPos);
				}
				ItemCarryonBlock.clearTileData(stack);
				eitem.setItem(ItemStack.EMPTY);
			}

//			BlockPos pos = new BlockPos(Math.floor(eitem.getPosX()), Math.floor(eitem.getPosY()), Math.floor(eitem.getPosZ()));
//			if (positions.containsKey(pos))
//			{
//				event.setCanceled(true);
//			}
		}

	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event)
	{
		if (event.getPlayer() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) event.getPlayer();
			World world = player.getCommandSenderWorld();

			ItemStack carried = player.getMainHandItem();
			if (!carried.isEmpty() && carried.getItem() == RegistrationHandler.itemTile || carried.getItem() == RegistrationHandler.itemEntity)
			{
				if (carried.getItem() == RegistrationHandler.itemTile)
				{
					CarryOnOverride override = ScriptChecker.inspectBlock(ItemCarryonBlock.getBlockState(carried), world, player.blockPosition(), ItemCarryonBlock.getTileData(carried));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(player.inventory.selected, player.getId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(player.inventory.selected, player.getId()));
				} else
				{
					CarryOnOverride override = ScriptChecker.inspectEntity(ItemCarryonEntity.getEntity(carried, world));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(player.inventory.selected, player.getId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(player.inventory.selected, player.getId()));

				}
			}

		}
		if(event.getPlayer() instanceof ServerPlayerEntity)
		{
			CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)event.getPlayer()), new ScriptReloadPacket(ScriptReader.OVERRIDES.values()));
		}
	}


	@SubscribeEvent
	public void serverLoad(RegisterCommandsEvent event)
	{
		CommandCarryOn.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void serverLoad(FMLServerStartingEvent event)
	{
		CustomPickupOverrideHandler.initPickupOverrides();
	}

	@SubscribeEvent
	public void reloadTags(TagsUpdatedEvent event)
	{
		ListHandler.initConfigLists();
		CustomPickupOverrideHandler.initPickupOverrides();
	}

	@SubscribeEvent
	public void onEntityStartTracking(StartTracking event)
	{
		Entity e = event.getTarget();
		PlayerEntity tracker = event.getPlayer();

		if (e instanceof PlayerEntity && tracker instanceof ServerPlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) e;
			World world = player.getCommandSenderWorld();

			ItemStack carried = player.getMainHandItem();
			if (!carried.isEmpty() && carried.getItem() == RegistrationHandler.itemTile || carried.getItem() == RegistrationHandler.itemEntity)
			{
				if (carried.getItem() == RegistrationHandler.itemTile)
				{
					CarryOnOverride override = ScriptChecker.inspectBlock(ItemCarryonBlock.getBlockState(carried), world, player.blockPosition(), ItemCarryonBlock.getTileData(carried));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) tracker), new CarrySlotPacket(player.inventory.selected, player.getId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) tracker), new CarrySlotPacket(player.inventory.selected, player.getId()));
				} else
				{
					CarryOnOverride override = ScriptChecker.inspectEntity(ItemCarryonEntity.getEntity(carried, world));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) tracker), new CarrySlotPacket(player.inventory.selected, player.getId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) tracker), new CarrySlotPacket(player.inventory.selected, player.getId()));
				}
			}

		}
	}

	@SubscribeEvent
	public void harvestSpeed(BreakSpeed event)
	{
		PlayerEntity player = event.getPlayer();
		if (player != null && !Settings.hitWhileCarrying.get())
		{
			ItemStack stack = player.getMainHandItem();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
				event.setNewSpeed(0);
		}
	}

	@SubscribeEvent
	public void attackEntity(AttackEntityEvent event)
	{
		PlayerEntity player = event.getPlayer();
		ItemStack stack = player.getMainHandItem();
		if (!stack.isEmpty() && !Settings.hitWhileCarrying.get() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void harvestSpeed(BreakEvent event)
	{
		PlayerEntity player = event.getPlayer();
		if (player != null && !Settings.hitWhileCarrying.get())
		{
			ItemStack stack = player.getMainHandItem();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void playerAttack(LivingAttackEvent event)
	{
		LivingEntity eliving = event.getEntityLiving();
		if (eliving instanceof PlayerEntity && Settings.dropCarriedWhenHit.get())
		{
			PlayerEntity player = (PlayerEntity) eliving;
			ItemStack stack = player.getMainHandItem();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
			{
				if (!player.level.isClientSide)
				{
					player.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
					ItemEntity item = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), stack);
					sendPacket(player, 9, 0);
					player.level.addFreshEntity(item);
				}
			}

		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		PlayerEntity player = event.getPlayer();

		if(event.isCanceled())
			return;

		if (!player.level.isClientSide)
		{

			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			World world = event.getWorld();
			BlockPos pos = event.getPos();
			BlockState state = world.getBlockState(pos);

			if (main.isEmpty() && off.isEmpty() && CarryOnKeybinds.isKeyPressed(player))
			{

				ItemStack stack = new ItemStack(RegistrationHandler.itemTile);

				TileEntity te = world.getBlockEntity(pos);
				if (PickupHandler.canPlayerPickUpBlock((ServerPlayerEntity) player, te, world, pos))
				{
					player.closeContainer();
		            world.levelEvent(1010, pos, 0);


					if (ItemCarryonBlock.storeTileData(te, world, pos, state, stack))
					{

						BlockState statee = world.getBlockState(pos);
						CompoundNBT tag = new CompoundNBT();
						tag = world.getBlockEntity(pos) != null ? world.getBlockEntity(pos).save(tag) : new CompoundNBT();
						CarryOnOverride override = ScriptChecker.inspectBlock(state, world, pos, tag);
						int overrideHash = 0;
						if (override != null)
							overrideHash = override.hashCode();

						boolean success = false;

						try
						{
							sendPacket(player, player.inventory.selected, overrideHash);

							world.removeBlockEntity(pos);
							world.removeBlock(pos, false);
							player.setItemInHand(Hand.MAIN_HAND, stack);
							event.setUseBlock(Result.DENY);
							event.setUseItem(Result.DENY);
							event.setCanceled(true);
							success = true;
						} catch (Exception e)
						{
							try
							{
								sendPacket(player, player.inventory.selected, overrideHash);
								emptyTileEntity(te);
								world.removeBlock(pos,false);
								player.setItemInHand(Hand.MAIN_HAND, stack);
								event.setUseBlock(Result.DENY);
								event.setUseItem(Result.DENY);
								event.setCanceled(true);
								success = true;
							} catch (Exception ex)
							{
								sendPacket(player, 9, 0);
								world.setBlockAndUpdate(pos, statee);
								if (!tag.isEmpty())
								{
									TileEntity.loadStatic(statee, tag);
								}

								player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Error detected. Cannot pick up block."), false);
								StringTextComponent s = new StringTextComponent(TextFormatting.GOLD + "here");
								s.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
								player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Please report this error ").append(s), false);
							}

						}

						if (success && override != null)
						{
							String command = override.getCommandInit();

							if (command != null)
								player.getServer().getCommands().performCommand(player.getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().getName() + " run " + command);
						}

					}
				}
			}
		}
	}

	public static void emptyTileEntity(TileEntity te)
	{
		if (te != null)
		{
			for (Direction facing : Direction.values())
			{
				LazyOptional<IItemHandler> itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);

				itemHandler.ifPresent((handler) -> {

					for (int i = 0; i < handler.getSlots(); i++)
					{
						handler.extractItem(i, 64, false);
					}

				});

			}

			LazyOptional<IItemHandler> itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

			itemHandler.ifPresent((handler) -> {

				for (int i = 0; i < handler.getSlots(); i++)
				{
					handler.extractItem(i, 64, false);
				}

			});

			if (te instanceof IInventory)
			{
				IInventory inv = (IInventory) te;
				inv.clearContent();
			}

			if (te instanceof IItemHandler)
			{
				IItemHandler itemHandler1 = (IItemHandler) te;
				for (int i = 0; i < itemHandler1.getSlots(); i++)
				{
					itemHandler1.extractItem(i, 64, false);
				}
			}

			te.setChanged();
		}
	}

	@SubscribeEvent
	public void onRespawn(PlayerEvent.Clone event)
	{
		PlayerEntity original = event.getOriginal();
		PlayerEntity player = event.getPlayer();
		boolean wasDead = event.isWasDeath();
		GameRules rules = player.level.getGameRules();
		boolean keepInv = rules.getBoolean(GameRules.RULE_KEEPINVENTORY);
		boolean wasCarrying = player.inventory.contains(new ItemStack(RegistrationHandler.itemTile)) || player.inventory.contains(new ItemStack(RegistrationHandler.itemEntity));

		if ((wasDead ? keepInv : true) && wasCarrying)
		{
			int carrySlot = original.inventory.selected;

			ItemStack stack = player.inventory.removeItemNoUpdate(carrySlot);
			World world = player.level;

			ItemEntity item = new ItemEntity(world, 0, 0, 0);
			item.setItem(stack);
			BlockPos pos = null;
			Optional<BlockPos> bedpos = original.getSleepingPos();
			if(bedpos.isPresent())
				pos = bedpos.get();
			if (pos == null)
				pos = player.blockPosition();
			item.setPos(pos.getX(), pos.getY(), pos.getZ());
			world.addFreshEntity(item);
		}

	}

	@SubscribeEvent
	public void dropNonHotbarItems(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if (entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) entity;

			if (!entity.level.isClientSide)
			{
				boolean hasCarried = player.inventory.contains(new ItemStack(RegistrationHandler.itemTile)) || player.inventory.contains(new ItemStack(RegistrationHandler.itemEntity));
				ItemStack inHand = player.getMainHandItem();

				if (hasCarried)
				{
					if ((inHand.getItem() != RegistrationHandler.itemTile && inHand.getItem() != RegistrationHandler.itemEntity) && player.getDimensionChangingDelay() == 0)
					{
						int slotBlock = getSlot(player, RegistrationHandler.itemTile);
						int slotEntity = getSlot(player, RegistrationHandler.itemEntity);



						ItemEntity item = null;
						if (slotBlock != -1)
						{
							ItemStack dropped = player.inventory.removeItemNoUpdate(slotBlock);
							item = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), dropped);
						}
						if (slotEntity != -1)
						{
							ItemStack dropped = player.inventory.removeItemNoUpdate(slotEntity);
							item = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), dropped);
						}
						if (item != null)
						{
							player.level.addFreshEntity(item);
							sendPacket(player, 9, 0);
						}
					}
				}

				CarryOnOverride override = ScriptChecker.getOverride(player);

				if (override != null)
				{
					String command = override.getCommandLoop();

					if (command != null)
						player.getServer().getCommands().performCommand(player.getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().getName() + " run " + command);
				}

			}
		}
	}

	public int getSlot(PlayerEntity player, Item item)
	{
		for (int i = 0; i < player.inventory.getContainerSize(); i++)
		{
			ItemStack stack = player.inventory.getItem(i);
			if (stack.getItem() == item)
				return i;
		}
		return -1;
	}

	public static void sendPacket(PlayerEntity player, int currentItem, int hash)
	{
		if (player instanceof ServerPlayerEntity)
		{
			CarryOn.network.send(PacketDistributor.NEAR.with(() -> new TargetPoint(player.getX(), player.getY(), player.getZ(), 128, player.level.dimension())), new CarrySlotPacket(currentItem, player.getId(), hash));
			CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(currentItem, player.getId(), hash));

			if (currentItem >= 9)
			{
				player.getPersistentData().remove("carrySlot");
				player.getPersistentData().remove("overrideKey");
			} else
			{
				player.getPersistentData().putInt("carrySlot", currentItem);
				if (hash != 0)
					ScriptChecker.setCarryOnOverride(player, hash);
			}
		}
	}

}
