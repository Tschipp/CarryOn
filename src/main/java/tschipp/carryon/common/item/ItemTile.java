package tschipp.carryon.common.item;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.config.CarryOnConfig;

public class ItemTile extends Item
{

	public static final String TILE_DATA_KEY = "tileData";

	public ItemTile()
	{
		this.setUnlocalizedName("tile_item");
		this.setRegistryName(CarryOn.MODID, "tile_item");
		GameRegistry.register(this);
		this.setMaxStackSize(1);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		if (stack != null)
		{
			if (hasTileData(stack))
			{
				ItemStack contained = getItemStack(stack);
				if (contained != null)
					return contained.getDisplayName();
			}
		}

		return "";
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		Block block = world.getBlockState(pos).getBlock();
		if (hasTileData(stack))
		{
			Vec3d vec = player.getLookVec();
			EnumFacing facing2 = EnumFacing.getFacingFromVector((float) vec.xCoord, 0f, (float) vec.zCoord);
			BlockPos pos2 = pos;
			Block containedblock = getBlock(stack);
			int meta = getMeta(stack);
			IBlockState containedstate = getBlockState(stack);
			if (!world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
			{
				pos2 = pos.offset(facing);
			}

			if (world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
			{
				boolean canPlace = containedblock.canPlaceBlockAt(world, pos2);

				if (canPlace)
				{
					boolean canEdit = player.canPlayerEdit(pos2, facing, stack);
					boolean canBePlaced = world.canBlockBePlaced(containedblock, pos2, false, facing, (Entity) null, null);
					if (canEdit && canBePlaced)
					{
						boolean hasDirection = false;
						boolean hasAllDirection = false;

						Iterator<IProperty<?>> iterator = containedblock.getDefaultState().getPropertyNames().iterator();
						while (iterator.hasNext())
						{
							IProperty<?> prop = iterator.next();
							Object[] allowedValues = prop.getAllowedValues().toArray();

							if (prop instanceof PropertyDirection && this.equal(allowedValues, EnumFacing.HORIZONTALS))
								hasDirection = true;

							if (prop instanceof PropertyDirection && this.equal(allowedValues, EnumFacing.VALUES))
							{
								hasAllDirection = true;
								facing2 = EnumFacing.getFacingFromVector((float) vec.xCoord, (float) vec.yCoord, (float) vec.zCoord);
							}

						}

						if (hasAllDirection)
							world.setBlockState(pos2, containedstate.withProperty(BlockDirectional.FACING, facing2.getOpposite()));
						else if (hasDirection)
							world.setBlockState(pos2, containedstate.withProperty(BlockHorizontal.FACING, facing2.getOpposite()));
						else
							world.setBlockState(pos2, containedstate);

						TileEntity tile = world.getTileEntity(pos2);
						if (tile != null)
						{
							tile.readFromNBT(getTileData(stack));
							tile.setPos(pos2);
						}
						clearTileData(stack);
						player.playSound(containedblock.getSoundType().getPlaceSound(), 1.0f, 0.5f);
						player.setHeldItem(hand, null);
						return EnumActionResult.SUCCESS;
					}

				}
			}

		}

		return EnumActionResult.FAIL;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
	{
		if (hasTileData(stack))
		{
			if (entity instanceof EntityLivingBase)
			{
				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 1, potionLevel(stack), false, false));
			}
		}
		else
		{
			stack = null;
		}
	}

	public static boolean hasTileData(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			return tag.hasKey(TILE_DATA_KEY) && tag.hasKey("block") && tag.hasKey("meta") && tag.hasKey("stateid");
		}
		return false;
	}

	public static boolean storeTileData(@Nullable TileEntity tile, World world, BlockPos pos, IBlockState state, ItemStack stack)
	{
		if (CarryOnConfig.settings.pickupAllBlocks ? false : tile == null)
			return false;

		if (stack == null)
			return false;

		NBTTagCompound chest = new NBTTagCompound();
		if (tile != null)
			chest = tile.writeToNBT(chest);

		NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		if (tag.hasKey(TILE_DATA_KEY))
			return false;

		tag.setTag(TILE_DATA_KEY, chest);

		ItemStack drop = state.getBlock().getItem(world, pos, state);

		tag.setString("block", state.getBlock().getRegistryName().toString());
		Item item = Item.getItemFromBlock(state.getBlock());
		tag.setInteger("meta", drop.getItemDamage());
		tag.setInteger("stateid", Block.getStateId(state));
		stack.setTagCompound(tag);
		return true;
	}

	public static void clearTileData(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			tag.removeTag(TILE_DATA_KEY);
			tag.removeTag("block");
			tag.removeTag("meta");
			tag.removeTag("stateid");
		}
	}

	public static NBTTagCompound getTileData(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			return tag.getCompoundTag(TILE_DATA_KEY);
		}
		return null;
	}

	@Nullable
	public static Block getBlock(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			String name = tag.getString("block");
			return Block.getBlockFromName(name);
		}
		return null;
	}

	public static int getMeta(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			int meta = tag.getInteger("meta");
			return meta;
		}
		return 0;
	}

	public static ItemStack getItemStack(ItemStack stack)
	{
		Block block = getBlock(stack);
		if (block != null)
		{
			Item item = Item.getItemFromBlock(block);
			if (item != null)
			{
				ItemStack ret = new ItemStack(item, 1, getMeta(stack));
				return ret;
			}

			return null;
		}
		else
			return null;

	}

	public static IBlockState getBlockState(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			int id = tag.getInteger("stateid");
			return Block.getStateById(id);
		}
		return Blocks.AIR.getDefaultState();
	}

	public static boolean isLocked(BlockPos pos, World world)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te != null)
		{
			NBTTagCompound tag = new NBTTagCompound();
			te.writeToNBT(tag);
			return tag.hasKey("Lock") ? !tag.getString("Lock").equals("") : false;
		}

		return false;
	}

	private boolean equal(Object[] a, Object[] b)
	{
		if (a.length != b.length)
			return false;

		List lA = Arrays.asList(a);
		List lB = Arrays.asList(b);

		return lA.containsAll(lB);
	}

	private int potionLevel(ItemStack stack)
	{
		String nbt = getTileData(stack).toString();
		int i = nbt.length() / 500;

		if (i > 4)
			i = 4;

		if (!CarryOnConfig.settings.heavyTiles)
			i = 1;

		return i;
	}
}
