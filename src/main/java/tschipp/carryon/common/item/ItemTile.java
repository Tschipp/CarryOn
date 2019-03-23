package tschipp.carryon.common.item;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.CharMatcher;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.event.ItemEvents;
import tschipp.carryon.common.handler.CustomPickupOverrideHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;

public class ItemTile extends Item
{

	public static final String TILE_DATA_KEY = "tileData";
	public static final String[] FACING_KEYS = new String[] { "rotation", "rot", "facing", "face", "direction", "dir", "front", "forward" };

	public ItemTile()
	{
		super(new Item.Properties().maxStackSize(1));
		this.setRegistryName(CarryOn.MODID, "tile_item");
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack)
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
					return ostate.getBlock().getNameTextComponent();
				}
			}

			return getItemStack(stack).getDisplayName();
		}

		return new TextComponentString("");
	}

	@Override
	public EnumActionResult onItemUse(ItemUseContext context)
	{
		EnumFacing facing = context.getFace();
		EntityPlayer player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		ItemStack stack = context.getItem();

		if (ModList.get().isLoaded("betterplacement"))
		{
			if (CarryOnKeybinds.isKeyPressed(player))
				return EnumActionResult.FAIL;
		}

		if (hasTileData(stack))
		{
			try
			{
				Vec3d vec = player.getLookVec();
				EnumFacing facing2 = EnumFacing.getFacingFromVector((float) vec.x, 0f, (float) vec.z);
				BlockPos pos2 = pos;
				Block containedblock = getBlock(stack);
				IBlockState containedstate = getBlockState(stack);
				if (!world.getBlockState(pos2).isReplaceable(new BlockItemUseContext(context)))
				{
					pos2 = pos.offset(facing);
				}

				if (world.getBlockState(pos2).isReplaceable(new BlockItemUseContext(context)) && containedblock != null)
				{
					boolean canPlace = containedstate.isValidPosition(world, pos2);

					if (canPlace)
					{
						if (player.canPlayerEdit(pos, facing, stack) && world.isBlockModifiable(player, pos2))
						{
							
							IBlockState actualState = containedblock.getStateForPlacement(new BlockItemUseContext(context));
							BlockSnapshot snapshot = new BlockSnapshot(world, pos2, containedstate);
							PlaceEvent event = new PlaceEvent(snapshot, world.getBlockState(pos), player, EnumHand.MAIN_HAND);
							MinecraftForge.EVENT_BUS.post(event);

							if (!event.isCanceled())
							{
								world.setBlockState(pos2, actualState);

								// If the blockstate doesn't handle rotation,
								// try to
								// change rotation via NBT
								if (!getTileData(stack).isEmpty())
								{
									NBTTagCompound tag = getTileData(stack);
									Set<String> keys = tag.keySet();
									keytester: for (String key : keys)
									{
										for (String facingKey : FACING_KEYS)
										{
											if (key.toLowerCase().equals(facingKey))
											{
												byte type = tag.getTagId(key);
												switch (type)
												{
												case 8:
													tag.setString(key, CharMatcher.javaUpperCase().matchesAllOf(tag.getString(key)) ? facing2.getOpposite().getName().toUpperCase() : facing2.getOpposite().getName());
													break;
												case 3:
													tag.setInt(key, facing2.getOpposite().getIndex());
													break;
												case 1:
													tag.setByte(key, (byte) facing2.getOpposite().getIndex());
													break;
												default:
													break;
												}

												break keytester;
											}
										}
									}
								}

								TileEntity tile = world.getTileEntity(pos2);
								if (tile != null)
								{
									tile.deserializeNBT(getTileData(stack));
									tile.setPos(pos2);
								}
								clearTileData(stack);
								player.playSound(actualState.getSoundType(world, pos2, player).getPlaceSound(), 1.0f, 0.5f);
								player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
								player.getEntityData().removeTag("overrideKey");
								ItemEvents.sendPacket(player, 9, 0);
								return EnumActionResult.SUCCESS;

							}
						}

					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();

				if (world != null && world.isRemote)
				{
					CarryOn.LOGGER.info("Block: " + ItemTile.getBlock(stack));
					CarryOn.LOGGER.info("BlockState: " + ItemTile.getBlockState(stack));
//					CarryOn.LOGGER.info("Meta: " + ItemTile.getMeta(stack));
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
	public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
	{
		if (hasTileData(stack))
		{
			if (entity instanceof EntityLivingBase)
			{
				if (entity instanceof EntityPlayer && Settings.slownessInCreative.get() ? false : ((EntityPlayer) entity).isCreative())
					return;

				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 1, potionLevel(stack), false, false));
			}
		} else
		{
			stack = ItemStack.EMPTY;
		}
	}

	public static boolean hasTileData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			return tag.hasKey(TILE_DATA_KEY) && tag.hasKey("block") && tag.hasKey("stateid");
		}
		return false;
	}

	public static boolean storeTileData(@Nullable TileEntity tile, World world, BlockPos pos, IBlockState state, ItemStack stack)
	{
		if (stack.isEmpty())
			return false;

		NBTTagCompound chest = new NBTTagCompound();
		if (tile != null)
			chest = tile.write(chest);

		NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();
		if (tag.hasKey(TILE_DATA_KEY))
			return false;

		tag.setTag(TILE_DATA_KEY, chest);

//		ItemStack drop = new ItemStack(state.getBlock().getItemDropped(state, itemRand, 0), 1, state.getBlock().damageDropped(state));

		tag.setString("block", state.getBlock().getRegistryName().toString());
//		Item item = Item.getItemFromBlock(state.getBlock());
//		tag.setInt("meta", drop.getItemDamage());
		tag.setInt("stateid", Block.getStateId(state));
		stack.setTag(tag);
		return true;
	}

	public static void clearTileData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			tag.removeTag(TILE_DATA_KEY);
			tag.removeTag("block");
			tag.removeTag("stateid");
		}
	}

	public static NBTTagCompound getTileData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			return tag.getCompound(TILE_DATA_KEY);
		}
		return null;
	}

	public static Block getBlock(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			int id = tag.getInt("stateid");
			return Block.getStateById(id).getBlock();
		}
		return Blocks.AIR;
	}

//	public static int getMeta(ItemStack stack)
//	{
//		if (stack.hasTag())
//		{
//			NBTTagCompound tag = stack.getTag();
//			int meta = tag.getInt("meta");
//			return meta;
//		}
//		return 0;
//	}

	public static ItemStack getItemStack(ItemStack stack)
	{
		return new ItemStack(getBlock(stack), 1);
	}

	public static IBlockState getBlockState(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			int id = tag.getInt("stateid");
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
			te.write(tag);
			return tag.hasKey("Lock") ? !tag.getString("Lock").equals("") : false;
		}

		return false;
	}

//	private boolean equal(Object[] a, Object[] b)
//	{
//		if (a.length != b.length)
//			return false;
//
//		List lA = Arrays.asList(a);
//		List lB = Arrays.asList(b);
//
//		return lA.containsAll(lB);
//	}

	private int potionLevel(ItemStack stack)
	{
		String nbt = getTileData(stack).toString();
		int i = nbt.length() / 500;

		if (i > 4)
			i = 4;

		if (!Settings.heavyTiles.get())
			i = 1;

		return (int) (i * Settings.blockSlownessMultiplier.get());
	}
}
