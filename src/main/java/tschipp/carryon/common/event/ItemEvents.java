package tschipp.carryon.common.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.carryon.common.handler.ForbiddenTileHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemTile;

public class ItemEvents
{

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
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
				tile.readFromNBT(ItemTile.getTileData(stack));
				tile.setPos(finalPos);
				ItemTile.clearTileData(stack);
				eitem.setEntityItemStack(ItemStack.EMPTY);
			}
		}
	}


	@SubscribeEvent
	public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		Block block = world.getBlockState(pos).getBlock();
		IBlockState state = world.getBlockState(pos);

		if (main.isEmpty() && off.isEmpty() && player.isSneaking() && !ForbiddenTileHandler.isForbidden(block))
		{
			ItemStack stack = new ItemStack(RegistrationHandler.itemTile);

			TileEntity te = world.getTileEntity(pos);
			if (te != null && (block.getBlockHardness(state, world, pos) != -1 || player.isCreative()))
			{

				if (!ItemTile.isLocked(pos, world))
				{
					if (ItemTile.storeTileData(te, state, stack))
					{
						world.removeTileEntity(pos);
						world.setBlockToAir(pos);
						player.setHeldItem(EnumHand.MAIN_HAND, stack);
						event.setUseBlock(Result.DENY);
					}
				}

			}

		}
	}

}
