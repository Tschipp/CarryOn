package tschipp.carryon.common.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.command.CommandCarryOn;
import tschipp.carryon.common.config.Configs;
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

public class ItemEvents
{

	public static Map<BlockPos, Integer> positions = new HashMap<BlockPos, Integer>();

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (event.isCanceled())
			return;

		PlayerEntity player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack))
		{
			player.getEntityData().remove("carrySlot");
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
		if (e instanceof net.minecraft.entity.item.ItemEntity)
		{
			net.minecraft.entity.item.ItemEntity eitem = (net.minecraft.entity.item.ItemEntity) e;
			ItemStack stack = eitem.getItem();
			Item item = stack.getItem();
			if (item == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack))
			{
				BlockPos pos = eitem.getPosition();
				BlockPos finalPos = pos;
				BlockItemUseContext context = new DirectionalPlaceContext(world, pos, Direction.DOWN, stack, Direction.UP);

				if (!world.getBlockState(pos).isReplaceable(context) || !context.canPlace())
				{
					for (Direction facing : Direction.values())
					{
						BlockPos offsetPos = pos.offset(facing);
						BlockItemUseContext newContext = new DirectionalPlaceContext(world, offsetPos, Direction.DOWN, stack, Direction.UP);
						if (world.getBlockState(offsetPos).isReplaceable(newContext) && newContext.canPlace())
						{
							finalPos = offsetPos;
							break;
						}
					}
				}
				world.setBlockState(finalPos, ItemCarryonBlock.getBlockState(stack));
				TileEntity tile = world.getTileEntity(finalPos);
				if (tile != null)
				{
					tile.deserializeNBT(ItemCarryonBlock.getTileData(stack));
					tile.setPos(finalPos);
				}
				ItemCarryonBlock.clearTileData(stack);
				eitem.setItem(ItemStack.EMPTY);
			}

			BlockPos pos = new BlockPos(Math.floor(eitem.posX), Math.floor(eitem.posY), Math.floor(eitem.posZ));
			if (positions.containsKey(pos))
			{
				event.setCanceled(true);
			}
		}

	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event)
	{
		if (event.getPlayer() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) event.getPlayer();
			World world = player.getEntityWorld();

			ItemStack carried = player.getHeldItemMainhand();
			if (!carried.isEmpty() && carried.getItem() == RegistrationHandler.itemTile || carried.getItem() == RegistrationHandler.itemEntity)
			{
				if (carried.getItem() == RegistrationHandler.itemTile)
				{
					CarryOnOverride override = ScriptChecker.inspectBlock(ItemCarryonBlock.getBlockState(carried), world, player.getPosition(), ItemCarryonBlock.getTileData(carried));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(player.inventory.currentItem, player.getEntityId()));
				} else
				{
					CarryOnOverride override = ScriptChecker.inspectEntity(ItemCarryonEntity.getEntity(carried, world));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(player.inventory.currentItem, player.getEntityId()));

				}
			}

		}
	}

	@SubscribeEvent
	public void serverLoad(FMLServerStartingEvent event)
	{
		CommandCarryOn.register(event.getCommandDispatcher());
		ScriptReader.reloadScripts();
		CustomPickupOverrideHandler.initPickupOverrides();
		ListHandler.initLists();

	}

	@SubscribeEvent
	public void onEntityStartTracking(StartTracking event)
	{
		Entity e = event.getTarget();
		PlayerEntity tracker = event.getEntityPlayer();

		if (e instanceof PlayerEntity && tracker instanceof ServerPlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) e;
			World world = player.getEntityWorld();

			ItemStack carried = player.getHeldItemMainhand();
			if (!carried.isEmpty() && carried.getItem() == RegistrationHandler.itemTile || carried.getItem() == RegistrationHandler.itemEntity)
			{
				if (carried.getItem() == RegistrationHandler.itemTile)
				{
					CarryOnOverride override = ScriptChecker.inspectBlock(ItemCarryonBlock.getBlockState(carried), world, player.getPosition(), ItemCarryonBlock.getTileData(carried));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) tracker), new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) tracker), new CarrySlotPacket(player.inventory.currentItem, player.getEntityId()));
				} else
				{
					CarryOnOverride override = ScriptChecker.inspectEntity(ItemCarryonEntity.getEntity(carried, world));
					if (override != null)
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) tracker), new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), override.hashCode()));
					else
						CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) tracker), new CarrySlotPacket(player.inventory.currentItem, player.getEntityId()));
				}
			}

		}
	}

	@SubscribeEvent
	public void harvestSpeed(BreakSpeed event)
	{
		PlayerEntity player = event.getEntityPlayer();
		if (player != null && !Settings.hitWhileCarrying.get())
		{
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
				event.setNewSpeed(0);
		}
	}

	@SubscribeEvent
	public void attackEntity(AttackEntityEvent event)
	{
		PlayerEntity player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
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
			ItemStack stack = player.getHeldItemMainhand();
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
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
			{
				if (!player.world.isRemote)
				{
					player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
					ItemEntity item = new ItemEntity(player.world, player.posX, player.posY, player.posZ, stack);
					sendPacket(player, 9, 0);
					player.world.addEntity(item);
				}
			}

		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event)
	{
		for (Entry<BlockPos, Integer> entry : positions.entrySet())
		{
			entry.setValue(entry.getValue() + 1);

			if (entry.getValue() > 3)
				positions.remove(entry.getKey());
		}
	}

	@SubscribeEvent
	public void onDrop(HarvestDropsEvent event)
	{
		if (positions.containsKey(event.getPos()))
		{
			event.getDrops().clear();
		}
	}

	@SubscribeEvent
	public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		PlayerEntity player = event.getEntityPlayer();

		if (player instanceof ServerPlayerEntity)
		{

			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
			World world = event.getWorld();
			BlockPos pos = event.getPos();
			BlockState state = world.getBlockState(pos);

			if (main.isEmpty() && off.isEmpty() && CarryOnKeybinds.isKeyPressed(player))
			{

				ItemStack stack = new ItemStack(RegistrationHandler.itemTile);

				TileEntity te = world.getTileEntity(pos);
				if (PickupHandler.canPlayerPickUpBlock(player, te, world, pos))
				{
					player.closeScreen();

					if (ItemCarryonBlock.storeTileData(te, world, pos, state, stack))
					{

						BlockState statee = world.getBlockState(pos);
						CompoundNBT tag = new CompoundNBT();
						tag = world.getTileEntity(pos) != null ? world.getTileEntity(pos).write(tag) : new CompoundNBT();
						CarryOnOverride override = ScriptChecker.inspectBlock(state, world, pos, tag);
						int overrideHash = 0;
						if (override != null)
							overrideHash = override.hashCode();

						positions.put(pos, 0);

						boolean success = false;

						try
						{
							sendPacket(player, player.inventory.currentItem, overrideHash);

							world.removeTileEntity(pos);
							world.removeBlock(pos, false);
							player.setHeldItem(Hand.MAIN_HAND, stack);
							event.setUseBlock(Result.DENY);
							event.setUseItem(Result.DENY);
							event.setCanceled(true);
							success = true;
						} catch (Exception e)
						{
							try
							{
								sendPacket(player, player.inventory.currentItem, overrideHash);
								emptyTileEntity(te);
								world.removeBlock(pos,false);
								player.setHeldItem(Hand.MAIN_HAND, stack);
								event.setUseBlock(Result.DENY);
								event.setUseItem(Result.DENY);
								event.setCanceled(true);
								success = true;
							} catch (Exception ex)
							{
								sendPacket(player, 9, 0);
								world.setBlockState(pos, statee);
								if (!tag.isEmpty())
									TileEntity.create(tag);

								player.sendMessage(new StringTextComponent(TextFormatting.RED + "Error detected. Cannot pick up block."));
								StringTextComponent s = new StringTextComponent(TextFormatting.GOLD + "here");
								s.getStyle().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
								player.sendMessage(new StringTextComponent(TextFormatting.RED + "Please report this error ").appendSibling(s));
							}

						}

						if (success && override != null)
						{
							String command = override.getCommandInit();

							if (command != null)
								player.getServer().getCommandManager().handleCommand(player.getServer().getCommandSource(), "/execute as " + player.getGameProfile().getName() + " run " + command);
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
				inv.clear();
			}

			if (te instanceof IItemHandler)
			{
				IItemHandler itemHandler1 = (IItemHandler) te;
				for (int i = 0; i < itemHandler1.getSlots(); i++)
				{
					itemHandler1.extractItem(i, 64, false);
				}
			}

			te.markDirty();
		}
	}

	@SubscribeEvent
	public void onRespawn(PlayerEvent.Clone event)
	{
		PlayerEntity original = event.getOriginal();
		PlayerEntity player = event.getEntityPlayer();
		boolean wasDead = event.isWasDeath();
		GameRules rules = player.world.getGameRules();
		boolean keepInv = rules.getBoolean(new RuleKey<BooleanValue>("keepInventory"));
		boolean wasCarrying = player.inventory.hasItemStack(new ItemStack(RegistrationHandler.itemTile)) || player.inventory.hasItemStack(new ItemStack(RegistrationHandler.itemEntity));

		if ((wasDead ? keepInv : true) && wasCarrying)
		{
			int carrySlot = original.inventory.currentItem;

			ItemStack stack = player.inventory.removeStackFromSlot(carrySlot);
			World world = player.world;

			ItemEntity item = new ItemEntity(world, 0, 0, 0);
			item.setItem(stack);
			BlockPos pos = original.getBedLocation(original.dimension);
			if (pos == null)
				pos = player.getPosition();
			item.setPosition(pos.getX(), pos.getY(), pos.getZ());
			world.addEntity(item);
		}

	}

	@SubscribeEvent
	public void dropNonHotbarItems(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if (entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) entity;

			if (!entity.world.isRemote)
			{
				boolean hasCarried = player.inventory.hasItemStack(new ItemStack(RegistrationHandler.itemTile)) || player.inventory.hasItemStack(new ItemStack(RegistrationHandler.itemEntity));
				ItemStack inHand = player.getHeldItemMainhand();

				if (hasCarried)
				{
					if ((inHand.getItem() != RegistrationHandler.itemTile && inHand.getItem() != RegistrationHandler.itemEntity) && player.getPortalCooldown() == 0)
					{
						int slotBlock = getSlot(player, RegistrationHandler.itemTile);
						int slotEntity = getSlot(player, RegistrationHandler.itemEntity);

						
						
						ItemEntity item = null;
						if (slotBlock != -1)
						{
							ItemStack dropped = player.inventory.removeStackFromSlot(slotBlock);
							item = new ItemEntity(player.world, player.posX, player.posY, player.posZ, dropped);
						}
						if (slotEntity != -1)
						{
							ItemStack dropped = player.inventory.removeStackFromSlot(slotEntity);
							item = new ItemEntity(player.world, player.posX, player.posY, player.posZ, dropped);
						}
						if (item != null)
						{
							player.world.addEntity(item);
							sendPacket(player, 9, 0);
						}
					}
				}

				CarryOnOverride override = ScriptChecker.getOverride(player);

				if (override != null)
				{
					String command = override.getCommandLoop();

					if (command != null)
						player.getServer().getCommandManager().handleCommand(player.getServer().getCommandSource(), "/execute as " + player.getGameProfile().getName() + " run " + command);
				}

			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ModConfig.ConfigReloading event)
	{
		if (event.getConfig().getModId().equals(CarryOn.MODID))
		{
			ListHandler.initLists();

			Configs.loadConfig(Configs.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("carryon-client.toml"));
			Configs.loadConfig(Configs.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("carryon-server.toml"));
		}
	}

	public int getSlot(PlayerEntity player, Item item)
	{
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack.getItem() == item)
				return i;
		}
		return -1;
	}

	public static void sendPacket(PlayerEntity player, int currentItem, int hash)
	{
		if (player instanceof ServerPlayerEntity)
		{
			CarryOn.network.send(PacketDistributor.NEAR.with(() -> new TargetPoint(player.posX, player.posY, player.posZ, 128, player.world.getDimension().getType())), new CarrySlotPacket(currentItem, player.getEntityId(), hash));
			CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(currentItem, player.getEntityId(), hash));

			if (currentItem >= 9)
			{
				player.getEntityData().remove("carrySlot");
				player.getEntityData().remove("overrideKey");
			} else
			{
				player.getEntityData().putInt("carrySlot", currentItem);
				if (hash != 0)
					ScriptChecker.setCarryOnOverride(player, hash);
			}
		}
	}

}
