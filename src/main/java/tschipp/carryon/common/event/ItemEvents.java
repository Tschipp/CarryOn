package tschipp.carryon.common.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.PickupHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;
import tschipp.carryon.network.client.CarrySlotPacket;

public class ItemEvents
{

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		if(event.isCanceled())
			return;
		
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
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
					CarryOn.network.sendToAllAround(new CarrySlotPacket(9, player.getEntityId()), new TargetPoint(player.world.provider.getDimension(), player.posX, player.posY, player.posZ, 256));
					player.world.spawnEntity(item);
				}
			}

		}
	}

	@SubscribeEvent
	public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) throws InstantiationException, IllegalAccessException
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

					if (ItemTile.storeTileData(te, world, pos, state.getActualState(world, pos), stack))
					{

						IBlockState statee = world.getBlockState(pos);
						NBTTagCompound tag = new NBTTagCompound();
						tag = world.getTileEntity(pos) != null ? world.getTileEntity(pos).writeToNBT(tag) : new NBTTagCompound();
						CarryOnOverride override = ScriptChecker.inspectBlock(state, world, pos, tag);
						int overrideHash = 0;
						if (override != null)
							overrideHash = override.hashCode();

						try
						{
							CarryOn.network.sendToAllAround(new CarrySlotPacket(player.inventory.currentItem, player.getEntityId(), overrideHash), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 256));
							emptyTileEntity(te);
							world.setBlockToAir(pos);
							player.setHeldItem(EnumHand.MAIN_HAND, stack);
							event.setUseBlock(Result.DENY);
							event.setCanceled(true);
						}
						catch (Exception e)
						{
							e.printStackTrace();
							CarryOn.network.sendToAllAround(new CarrySlotPacket(9, player.getEntityId()), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 256));
							world.setBlockState(pos, statee);
							if (!tag.hasNoTags())
								TileEntity.create(world, tag);

							player.sendMessage(new TextComponentString(TextFormatting.RED + "Error detected. Cannot pick up block."));
							TextComponentString s = new TextComponentString(TextFormatting.GOLD + "here");
							s.getStyle().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
							player.sendMessage(new TextComponentString(TextFormatting.RED + "Please report this error ").appendSibling(s));
						}

					}

				}

			}
		}
	}
	
	public static void emptyTileEntity(TileEntity te)
	{
		if (te != null && !te.isInvalid())
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

}
