package tschipp.carryon.common.item;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.GameRegistry;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.CustomPickupOverrideHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.network.client.CarrySlotPacket;

public class ItemTile extends Item
{

	public static final String TILE_DATA_KEY = "tileData";

	public ItemTile()
	{
		this.setUnlocalizedName("tile_item");
		this.setRegistryName(CarryOn.MODID, "tile_item");
		ForgeRegistries.ITEMS.register(this);
		this.setMaxStackSize(1);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		if (hasTileData(stack))
		{
			IBlockState state = getBlockState(stack);
			NBTTagCompound nbt = getTileData(stack);

			if (ModelOverridesHandler.hasCustomOverrideModel(state, nbt))
			{
				Object override = ModelOverridesHandler.getOverrideObject(state, nbt);
				if (override instanceof ItemStack)
					return ((ItemStack) override).getDisplayName();
				else
				{
					IBlockState ostate = (IBlockState) override;
					ItemStack itemstack = new ItemStack(ostate.getBlock().getItemDropped(ostate, this.itemRand, 0), 1, state.getBlock().damageDropped(ostate));
					return itemstack.getDisplayName();
				}
			}

			return getItemStack(stack).getDisplayName();
		}

		return "";
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(Loader.isModLoaded("betterplacement"))
		{
			if(CarryOnKeybinds.isKeyPressed(player))
				return EnumActionResult.FAIL;
		}
		
		
		Block block = world.getBlockState(pos).getBlock();
		ItemStack stack = player.getHeldItem(hand);
		if (hasTileData(stack))
		{
			try
			{
				Vec3d vec = player.getLookVec();
				EnumFacing facing2 = EnumFacing.getFacingFromVector((float) vec.x, 0f, (float) vec.z);
				BlockPos pos2 = pos;
				Block containedblock = getBlock(stack);
				int meta = getMeta(stack);
				IBlockState containedstate = getBlockState(stack);
				if (!world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
				{
					pos2 = pos.offset(facing);
				}

				if (world.getBlockState(pos2).getBlock().isReplaceable(world, pos2) && containedblock != null)
				{
					boolean canPlace = containedblock.canPlaceBlockAt(world, pos2);

					if (canPlace)
					{
						if (player.canPlayerEdit(pos, facing, stack) && world.mayPlace(containedblock, pos2, false, facing, (Entity) null))
						{
							boolean set = false;

							Iterator<IProperty<?>> iterator = containedblock.getDefaultState().getPropertyKeys().iterator();
							while (iterator.hasNext())
							{
								IProperty prop = iterator.next();
								Object[] allowedValues = prop.getAllowedValues().toArray();

								if (prop instanceof PropertyDirection && this.equal(allowedValues, EnumFacing.HORIZONTALS))
								{
									world.setBlockState(pos2, containedstate.withProperty(prop, containedblock instanceof BlockStairs ? facing2 : facing2.getOpposite()));
									set = true;
								}
								else if (prop instanceof PropertyDirection && this.equal(allowedValues, EnumFacing.VALUES))
								{
									facing2 = EnumFacing.getFacingFromVector((float) vec.x, (float) vec.y, (float) vec.z);
									world.setBlockState(pos2, containedstate.withProperty(prop, facing2.getOpposite()));
									set = true;
								}

							}

							if (!set)
								world.setBlockState(pos2, containedstate);

							TileEntity tile = world.getTileEntity(pos2);
							if (tile != null)
							{
								tile.readFromNBT(getTileData(stack));
								tile.setPos(pos2);
							}
							clearTileData(stack);
							player.playSound(containedblock.getSoundType().getPlaceSound(), 1.0f, 0.5f);
							player.setHeldItem(hand, ItemStack.EMPTY);
							player.getEntityData().removeTag("overrideKey");
							CarryOn.network.sendToAllAround(new CarrySlotPacket(9, player.getEntityId()), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 256));
							return EnumActionResult.SUCCESS;
						}

					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();

				if (world.isRemote)
				{
					CarryOn.LOGGER.info("Block: " + ItemTile.getBlock(stack));
					CarryOn.LOGGER.info("BlockState: " + ItemTile.getBlockState(stack));
					CarryOn.LOGGER.info("Meta: " + ItemTile.getMeta(stack));
					CarryOn.LOGGER.info("ItemStack: " + ItemTile.getItemStack(stack));

					if (ModelOverridesHandler.hasCustomOverrideModel(ItemTile.getBlockState(stack), ItemTile.getTileData(stack)))
						CarryOn.LOGGER.info("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemTile.getBlockState(stack), ItemTile.getTileData(stack)));

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemTile.getBlockState(stack)))
						CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemTile.getBlockState(stack)));

					player.sendMessage(new TextComponentString(TextFormatting.RED + "Error detected. Cannot place block. Execute \"/carryon clear\" to remove the item"));
					TextComponentString s = new TextComponentString(TextFormatting.GOLD + "here");
					s.getStyle().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
					player.sendMessage(new TextComponentString(TextFormatting.RED + "Please report this error ").appendSibling(s));

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
				if (entity instanceof EntityPlayer && CarryOnConfig.settings.slownessInCreative ? false : ((EntityPlayer) entity).isCreative())
					return;

				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 1, potionLevel(stack), false, false));
			}
		}
		else
		{
			stack = ItemStack.EMPTY;
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
		if (stack.isEmpty())
			return false;

		NBTTagCompound chest = new NBTTagCompound();
		if (tile != null)
			chest = tile.writeToNBT(chest);

		NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		if (tag.hasKey(TILE_DATA_KEY))
			return false;

		tag.setTag(TILE_DATA_KEY, chest);

		ItemStack drop = new ItemStack(state.getBlock().getItemDropped(state, itemRand, 0), 1, state.getBlock().damageDropped(state));

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

	public static Block getBlock(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			int id = tag.getInteger("stateid");
			return Block.getStateById(id).getBlock();
		}
		return Blocks.AIR;
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
		return new ItemStack(getBlock(stack), 1, getMeta(stack));
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

		return (int) (i * CarryOnConfig.settings.blockSlownessMultiplier);
	}
}
