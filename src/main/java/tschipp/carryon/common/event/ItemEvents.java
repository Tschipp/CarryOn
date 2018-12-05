package tschipp.carryon.common.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
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
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.ListHandler;
import tschipp.carryon.common.handler.PickupHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;
import tschipp.carryon.network.client.CarrySlotPacket;

public class ItemEvents
{

	public static Map<BlockPos, Integer> positions = new HashMap<BlockPos, Integer>();

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (event.isCanceled())
			return;

		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
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
						player.getServer().getCommandManager().executeCommand(player.getServer(), "/execute " + player.getGameProfile().getName() + " ~ ~ ~ " + command);
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
			if (item == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
			{
				BlockPos pos = eitem.getPosition();
				BlockPos finalPos = pos;
				Block block = ItemTile.getBlock(stack);
				if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos) || !block.canPlaceBlockAt(world, pos))
				{
					for (EnumFacing facing : EnumFacing.VALUES)
					{
						BlockPos offsetPos = pos.offset(facing);
						if (world.getBlockState(offsetPos).getBlock().isReplaceable(world, offsetPos) && block.canPlaceBlockAt(world, offsetPos))
						{
							finalPos = offsetPos;
							break;
						}
					}
				}
				world.setBlockState(finalPos, ItemTile.getBlockState(stack));
				TileEntity tile = world.getTileEntity(finalPos);
				if (tile != null)
				{
					tile.readFromNBT(ItemTile.getTileData(stack));
					tile.setPos(finalPos);
				}
				ItemTile.clearTileData(stack);
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
		if (event.player instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.player;
			World world = player.getEntityWorld();

			ItemStack carried = player.getHeldItemMainhand();
			if (!carried.isEmpty() && carried.getItem() == RegistrationHandler.itemTile || carried.getItem() == RegistrationHandler.itemEntity)
			{
				if (carried.getItem() == RegistrationHandler.itemTile)
				{
					CarryOnOverride override = ScriptChecker.inspectBlock(((ItemTile) carried.getItem()).getBlockState(carried), world, player.getPosition(), ((ItemTile) carried.getItem()).getTileData(carried));
					if (override != null)
						CarryOn.network.sendTo(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), override.hashCode()), (EntityPlayerMP) player);
					else
						CarryOn.network.sendTo(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId()), (EntityPlayerMP) player);
				}
				else
				{
					CarryOnOverride override = ScriptChecker.inspectEntity(((ItemEntity) carried.getItem()).getEntity(carried, world));
					if (override != null)
						CarryOn.network.sendTo(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), override.hashCode()), (EntityPlayerMP) player);
					else
						CarryOn.network.sendTo(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId()), (EntityPlayerMP) player);
				}
			}

		}
	}

	@SubscribeEvent
	public void onEntityStartTracking(StartTracking event)
	{
		Entity e = event.getTarget();
		EntityPlayer tracker = event.getEntityPlayer();

		if (e instanceof EntityPlayer && tracker instanceof EntityPlayerMP)
		{
			EntityPlayer player = (EntityPlayer) e;
			World world = player.getEntityWorld();

			ItemStack carried = player.getHeldItemMainhand();
			if (!carried.isEmpty() && carried.getItem() == RegistrationHandler.itemTile || carried.getItem() == RegistrationHandler.itemEntity)
			{
				if (carried.getItem() == RegistrationHandler.itemTile)
				{
					CarryOnOverride override = ScriptChecker.inspectBlock(((ItemTile) carried.getItem()).getBlockState(carried), world, player.getPosition(), ((ItemTile) carried.getItem()).getTileData(carried));
					if (override != null)
						CarryOn.network.sendTo(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), override.hashCode()), (EntityPlayerMP) tracker);
					else
						CarryOn.network.sendTo(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId()), (EntityPlayerMP) tracker);
				}
				else
				{
					CarryOnOverride override = ScriptChecker.inspectEntity(((ItemEntity) carried.getItem()).getEntity(carried, world));
					if (override != null)
						CarryOn.network.sendTo(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), override.hashCode()), (EntityPlayerMP) tracker);
					else
						CarryOn.network.sendTo(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId()), (EntityPlayerMP) tracker);
				}
			}

		}
	}

	@SubscribeEvent
	public void harvestSpeed(BreakSpeed event)
	{
		EntityPlayer player = event.getEntityPlayer();
		if (player != null && !CarryOnConfig.settings.hitWhileCarrying)
		{
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
				event.setNewSpeed(0);
		}
	}

	@SubscribeEvent
	public void attackEntity(AttackEntityEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && !CarryOnConfig.settings.hitWhileCarrying && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void harvestSpeed(BreakEvent event)
	{
		EntityPlayer player = event.getPlayer();
		if (player != null && !CarryOnConfig.settings.hitWhileCarrying)
		{
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void playerAttack(LivingAttackEvent event)
	{
		EntityLivingBase eliving = event.getEntityLiving();
		if (eliving instanceof EntityPlayer && CarryOnConfig.settings.dropCarriedWhenHit)
		{
			EntityPlayer player = (EntityPlayer) eliving;
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
			{
				if (!player.world.isRemote)
				{
					player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
					EntityItem item = new EntityItem(player.world, player.posX, player.posY, player.posZ, stack);
					sendPacket(player, 9, 0);
					player.world.spawnEntity(item);
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
		EntityPlayer player = event.getEntityPlayer();

		if (player instanceof EntityPlayerMP)
		{

			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
			World world = event.getWorld();
			BlockPos pos = event.getPos();
			Block block = world.getBlockState(pos).getBlock();
			IBlockState state = world.getBlockState(pos);

			if (main.isEmpty() && off.isEmpty() && CarryOnKeybinds.isKeyPressed(player))
			{

				ItemStack stack = new ItemStack(RegistrationHandler.itemTile);

				TileEntity te = world.getTileEntity(pos);
				if (PickupHandler.canPlayerPickUpBlock(player, te, world, pos))
				{
					player.closeScreen();

					if (ItemTile.storeTileData(te, world, pos, state.getActualState(world, pos), stack))
					{

						IBlockState statee = world.getBlockState(pos);
						NBTTagCompound tag = new NBTTagCompound();
						tag = world.getTileEntity(pos) != null ? world.getTileEntity(pos).writeToNBT(tag) : new NBTTagCompound();
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
							world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
							player.setHeldItem(EnumHand.MAIN_HAND, stack);
							event.setUseBlock(Result.DENY);
							event.setUseItem(Result.DENY);
							event.setCanceled(true);
							success = true;
						}
						catch (Exception e)
						{
							try
							{
								sendPacket(player, player.inventory.currentItem, overrideHash);
								emptyTileEntity(te);
								world.setBlockToAir(pos);
								player.setHeldItem(EnumHand.MAIN_HAND, stack);
								event.setUseBlock(Result.DENY);
								event.setUseItem(Result.DENY);
								event.setCanceled(true);
								success = true;
							}
							catch (Exception ex)
							{
								sendPacket(player, 9, 0);
								world.setBlockState(pos, statee);
								if (!tag.hasNoTags())
									TileEntity.create(world, tag);

								player.sendMessage(new TextComponentString(TextFormatting.RED + "Error detected. Cannot pick up block."));
								TextComponentString s = new TextComponentString(TextFormatting.GOLD + "here");
								s.getStyle().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
								player.sendMessage(new TextComponentString(TextFormatting.RED + "Please report this error ").appendSibling(s));
							}

						}

						if (success && override != null)
						{
							String command = override.getCommandInit();
							if (command != null)
								player.getServer().getCommandManager().executeCommand(player.getServer(), "/execute " + player.getGameProfile().getName() + " ~ ~ ~ " + command);
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
			for (EnumFacing facing : EnumFacing.VALUES)
			{
				if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing))
				{
					IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
					for (int i = 0; i < itemHandler.getSlots(); i++)
					{
						itemHandler.extractItem(i, 64, false);
					}
				}
			}

			if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			{
				IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				for (int i = 0; i < itemHandler.getSlots(); i++)
				{
					itemHandler.extractItem(i, 64, false);
				}
			}

			if (te instanceof IInventory)
			{
				IInventory inv = (IInventory) te;
				inv.clear();
			}

			if (te instanceof IItemHandler)
			{
				IItemHandler itemHandler = (IItemHandler) te;
				for (int i = 0; i < itemHandler.getSlots(); i++)
				{
					itemHandler.extractItem(i, 64, false);
				}
			}

			te.markDirty();
		}
	}

	@SubscribeEvent
	public void onRespawn(PlayerEvent.Clone event)
	{
		EntityPlayer original = event.getOriginal();
		EntityPlayer player = event.getEntityPlayer();
		boolean wasDead = event.isWasDeath();
		GameRules rules = player.world.getGameRules();
		boolean keepInv = rules.getBoolean("keepInventory");
		boolean wasCarrying = player.inventory.hasItemStack(new ItemStack(RegistrationHandler.itemTile)) || player.inventory.hasItemStack(new ItemStack(RegistrationHandler.itemEntity));

		if ((wasDead ? keepInv : true) && wasCarrying)
		{
			int carrySlot = original.inventory.currentItem;

			ItemStack stack = player.inventory.removeStackFromSlot(carrySlot);
			World world = player.world;

			EntityItem item = new EntityItem(world);
			item.setItem(stack);
			BlockPos pos = original.getBedLocation();
			if (pos == null)
				pos = player.getPosition();
			item.setPosition(pos.getX(), pos.getY(), pos.getZ());
			world.spawnEntity(item);
		}

	}

	@SubscribeEvent
	public void dropNonHotbarItems(LivingUpdateEvent event)
	{
		EntityLivingBase entity = event.getEntityLiving();
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entity;

			if (!entity.world.isRemote)
			{
				boolean hasCarried = player.inventory.hasItemStack(new ItemStack(RegistrationHandler.itemTile)) || player.inventory.hasItemStack(new ItemStack(RegistrationHandler.itemEntity));
				ItemStack inHand = player.getHeldItemMainhand();

				if (hasCarried)
				{
					if (inHand.getItem() != RegistrationHandler.itemTile && inHand.getItem() != RegistrationHandler.itemEntity)
					{
						int slotBlock = getSlot(player, RegistrationHandler.itemTile);
						int slotEntity = getSlot(player, RegistrationHandler.itemEntity);

						EntityItem item = null;
						if (slotBlock != -1)
						{
							ItemStack dropped = player.inventory.removeStackFromSlot(slotBlock);
							item = new EntityItem(player.world, player.posX, player.posY, player.posZ, dropped);
						}
						if (slotEntity != -1)
						{
							ItemStack dropped = player.inventory.removeStackFromSlot(slotEntity);
							item = new EntityItem(player.world, player.posX, player.posY, player.posZ, dropped);
						}
						if (item != null)
						{
							player.world.spawnEntity(item);
							sendPacket(player, 9, 0);
						}
					}
				}

				CarryOnOverride override = ScriptChecker.getOverride(player);

				if (override != null)
				{
					String command = override.getCommandLoop();
					if (command != null)
						player.getServer().getCommandManager().executeCommand(player.getServer(), "/execute " + player.getGameProfile().getName() + " ~ ~ ~ " + command);
				}

			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equals(CarryOn.MODID))
		{
			ListHandler.initLists();
			ConfigManager.load(CarryOn.MODID, Config.Type.INSTANCE);
		}
	}

	public int getSlot(EntityPlayer player, Item item)
	{
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack.getItem() == item)
				return i;
		}
		return -1;
	}

	public static void sendPacket(EntityPlayer player, int currentItem, int hash)
	{
		if (player instanceof EntityPlayerMP)
		{
			CarryOn.network.sendToAllAround(new CarrySlotPacket(currentItem, player.getEntityId(), hash), new TargetPoint(player.world.provider.getDimension(), player.posX, player.posY, player.posZ, 256));
			CarryOn.network.sendTo(new CarrySlotPacket(currentItem, player.getEntityId(), hash), (EntityPlayerMP) player);

			if (currentItem >= 9)
			{
				player.getEntityData().removeTag("carrySlot");
				player.getEntityData().removeTag("overrideKey");
			}
			else
			{

				player.getEntityData().setInteger("carrySlot", currentItem);
				if (hash != 0)
					ScriptChecker.setCarryOnOverride(player, hash);
			}
		}
	}

}
