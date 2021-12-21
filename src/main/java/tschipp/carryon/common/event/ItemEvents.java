package tschipp.carryon.common.event;

import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
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

		Player player = event.getPlayer();
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
		Level world = event.getWorld();
		if (e instanceof net.minecraft.world.entity.item.ItemEntity)
		{
			net.minecraft.world.entity.item.ItemEntity eitem = (net.minecraft.world.entity.item.ItemEntity) e;
			ItemStack stack = eitem.getItem();
			Item item = stack.getItem();
			if (item == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack))
			{
				BlockPos pos = eitem.blockPosition();
				BlockPos finalPos = pos;
				BlockPlaceContext context = new DirectionalPlaceContext(world, pos, Direction.DOWN, stack, Direction.UP);

				if (!world.getBlockState(pos).canBeReplaced(context) || !context.canPlace())
				{
					for (Direction facing : Direction.values())
					{
						BlockPos offsetPos = pos.relative(facing);
						BlockPlaceContext newContext = new DirectionalPlaceContext(world, offsetPos, Direction.DOWN, stack, Direction.UP);
						if (world.getBlockState(offsetPos).canBeReplaced(newContext) && newContext.canPlace())
						{
							finalPos = offsetPos;
							break;
						}
					}
				}
				world.setBlockAndUpdate(finalPos, ItemCarryonBlock.getBlockState(stack));
				BlockEntity tile = world.getBlockEntity(finalPos);
				if (tile != null)
				{
					var nbt = ItemCarryonBlock.getTileData(stack);
					ItemCarryonBlock.updateTileLocation(nbt, finalPos);
					tile.load(nbt);
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
		if (event.getPlayer() instanceof Player)
		{
			Player player = (Player) event.getPlayer();
			Level world = player.getCommandSenderWorld();

			ItemStack carried = player.getMainHandItem();
			if (!carried.isEmpty() && carried.getItem() == RegistrationHandler.itemTile || carried.getItem() == RegistrationHandler.itemEntity)
			{
				if (carried.getItem() == RegistrationHandler.itemTile)
				{
					CarryOnOverride override = ScriptChecker.inspectBlock(ItemCarryonBlock.getBlockState(carried), world, player.blockPosition(), ItemCarryonBlock.getTileData(carried));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CarrySlotPacket(player.getInventory().selected, player.getId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CarrySlotPacket(player.getInventory().selected, player.getId()));
				} else
				{
					CarryOnOverride override = ScriptChecker.inspectEntity(ItemCarryonEntity.getEntity(carried, world));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CarrySlotPacket(player.getInventory().selected, player.getId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CarrySlotPacket(player.getInventory().selected, player.getId()));

				}
			}

		}
		if(event.getPlayer() instanceof ServerPlayer)
		{
			CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getPlayer()), new ScriptReloadPacket(ScriptReader.OVERRIDES.values()));
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
	}

	@SubscribeEvent
	public void onEntityStartTracking(StartTracking event)
	{
		Entity e = event.getTarget();
		Player tracker = event.getPlayer();

		if (e instanceof Player && tracker instanceof ServerPlayer)
		{
			Player player = (Player) e;
			Level world = player.getCommandSenderWorld();

			ItemStack carried = player.getMainHandItem();
			if (!carried.isEmpty() && carried.getItem() == RegistrationHandler.itemTile || carried.getItem() == RegistrationHandler.itemEntity)
			{
				if (carried.getItem() == RegistrationHandler.itemTile)
				{
					CarryOnOverride override = ScriptChecker.inspectBlock(ItemCarryonBlock.getBlockState(carried), world, player.blockPosition(), ItemCarryonBlock.getTileData(carried));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) tracker), new CarrySlotPacket(player.getInventory().selected, player.getId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) tracker), new CarrySlotPacket(player.getInventory().selected, player.getId()));
				} else
				{
					CarryOnOverride override = ScriptChecker.inspectEntity(ItemCarryonEntity.getEntity(carried, world));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) tracker), new CarrySlotPacket(player.getInventory().selected, player.getId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) tracker), new CarrySlotPacket(player.getInventory().selected, player.getId()));
				}
			}

		}
	}

	@SubscribeEvent
	public void harvestSpeed(BreakSpeed event)
	{
		Player player = event.getPlayer();
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
		Player player = event.getPlayer();
		ItemStack stack = player.getMainHandItem();
		if (!stack.isEmpty() && !Settings.hitWhileCarrying.get() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void harvestSpeed(BreakEvent event)
	{
		Player player = event.getPlayer();
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
		if (eliving instanceof Player && Settings.dropCarriedWhenHit.get())
		{
			Player player = (Player) eliving;
			ItemStack stack = player.getMainHandItem();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
			{
				if (!player.level.isClientSide)
				{
					player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
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
		Player player = event.getPlayer();
		
		if(event.isCanceled())
			return;

		if (!player.level.isClientSide)
		{

			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			Level world = event.getWorld();
			BlockPos pos = event.getPos();
			BlockState state = world.getBlockState(pos);

			if (main.isEmpty() && off.isEmpty() && CarryOnKeybinds.isKeyPressed(player))
			{

				ItemStack stack = new ItemStack(RegistrationHandler.itemTile);

				BlockEntity te = world.getBlockEntity(pos);
				if (PickupHandler.canPlayerPickUpBlock((ServerPlayer) player, te, world, pos))
				{
					player.closeContainer();
		            world.levelEvent(1010, pos, 0);


					if (ItemCarryonBlock.storeTileData(te, world, pos, state, stack))
					{

						BlockState statee = world.getBlockState(pos);
						CompoundTag tag = new CompoundTag();
						tag = world.getBlockEntity(pos) != null ? world.getBlockEntity(pos).save(tag) : new CompoundTag();
						CarryOnOverride override = ScriptChecker.inspectBlock(state, world, pos, tag);
						int overrideHash = 0;
						if (override != null)
							overrideHash = override.hashCode();

						boolean success = false;

						try
						{
							sendPacket(player, player.getInventory().selected, overrideHash);

							world.removeBlockEntity(pos);
							world.removeBlock(pos, false);
							player.setItemInHand(InteractionHand.MAIN_HAND, stack);
							event.setUseBlock(Result.DENY);
							event.setUseItem(Result.DENY);
							event.setCanceled(true);
							success = true;
						} catch (Exception e)
						{
							try
							{
								sendPacket(player, player.getInventory().selected, overrideHash);
								emptyTileEntity(te);
								world.removeBlock(pos,false);
								player.setItemInHand(InteractionHand.MAIN_HAND, stack);
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
									BlockEntity.loadStatic(pos, statee, tag);
								}

								player.displayClientMessage(new TextComponent(ChatFormatting.RED + "Error detected. Cannot pick up block."), false);
								TextComponent s = new TextComponent(ChatFormatting.GOLD + "here");
								s.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
								player.displayClientMessage(new TextComponent(ChatFormatting.RED + "Please report this error ").append(s), false);
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

	public static void emptyTileEntity(BlockEntity te)
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

			if (te instanceof Container)
			{
				Container inv = (Container) te;
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
		Player original = event.getOriginal();
		Player player = event.getPlayer();
		boolean wasDead = event.isWasDeath();
		GameRules rules = player.level.getGameRules();
		boolean keepInv = rules.getBoolean(GameRules.RULE_KEEPINVENTORY);
		boolean wasCarrying = player.getInventory().contains(new ItemStack(RegistrationHandler.itemTile)) || player.getInventory().contains(new ItemStack(RegistrationHandler.itemEntity));

		if ((wasDead ? keepInv : true) && wasCarrying)
		{
			int carrySlot = original.getInventory().selected;

			ItemStack stack = player.getInventory().removeItemNoUpdate(carrySlot);
			Level world = player.level;

			ItemEntity item = new ItemEntity(world, 0, 0, 0, stack);
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
		if (entity instanceof Player)
		{
			Player player = (Player) entity;

			if (!entity.level.isClientSide)
			{
				boolean hasCarried = player.getInventory().contains(new ItemStack(RegistrationHandler.itemTile)) || player.getInventory().contains(new ItemStack(RegistrationHandler.itemEntity));
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
							ItemStack dropped = player.getInventory().removeItemNoUpdate(slotBlock);
							item = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), dropped);
						}
						if (slotEntity != -1)
						{
							ItemStack dropped = player.getInventory().removeItemNoUpdate(slotEntity);
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

	public int getSlot(Player player, Item item)
	{
		for (int i = 0; i < player.getInventory().getContainerSize(); i++)
		{
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.getItem() == item)
				return i;
		}
		return -1;
	}

	public static void sendPacket(Player player, int currentItem, int hash)
	{
		if (player instanceof ServerPlayer)
		{
			CarryOn.network.send(PacketDistributor.NEAR.with(() -> new TargetPoint(player.getX(), player.getY(), player.getZ(), 128, player.level.dimension())), new CarrySlotPacket(currentItem, player.getId(), hash));
			CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CarrySlotPacket(currentItem, player.getId(), hash));

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
