package tschipp.carryon.common.item;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.CharMatcher;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.event.ItemEvents;
import tschipp.carryon.common.handler.CustomPickupOverrideHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;

public class ItemCarryonBlock extends Item
{

	public static final String TILE_DATA_KEY = "tileData";
	public static final String[] FACING_KEYS = new String[] { "rotation", "rot", "facing", "face", "direction", "dir", "front", "forward" };

	public ItemCarryonBlock()
	{
		super(new Item.Properties().stacksTo(1));
		this.setRegistryName(CarryOn.MODID, "tile_item");
	}

	@Override
	public ITextComponent getName(ItemStack stack)
	{
		if (hasTileData(stack))
		{
			BlockState state = getBlockState(stack);
			CompoundNBT nbt = getTileData(stack);

			if (ModelOverridesHandler.hasCustomOverrideModel(state, nbt))
			{
				Object override = ModelOverridesHandler.getOverrideObject(state, nbt);
				if (override instanceof ItemStack)
					return ((ItemStack) override).getHoverName();
				else
				{
					BlockState ostate = (BlockState) override;
					return ostate.getBlock().getName();
				}
			}

			return getItemStack(stack).getHoverName();
		}

		return new StringTextComponent("");
	}

	@Override
	public ActionResultType useOn(ItemUseContext context)
	{
		Direction facing = context.getClickedFace();
		PlayerEntity player = context.getPlayer();
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		ItemStack stack = context.getItemInHand();

		if (ModList.get().isLoaded("betterplacement"))
		{
			if (CarryOnKeybinds.isKeyPressed(player))
				return ActionResultType.FAIL;
		}

		if (hasTileData(stack))
		{
			try
			{
				Vector3d vec = player.getLookAngle();
				Direction facing2 = Direction.getNearest((float) vec.x, 0f, (float) vec.z);
				BlockPos pos2 = pos;
				Block containedblock = getBlock(stack);
				BlockState containedstate = getBlockState(stack);
				if (!world.getBlockState(pos2).canBeReplaced(new BlockItemUseContext(context)))
				{
					pos2 = pos.relative(facing);
				}

				if (world.getBlockState(pos2).canBeReplaced(new BlockItemUseContext(context)) && containedblock != null)
				{
					boolean canPlace = containedstate.canSurvive(world, pos2);

					if (canPlace)
					{
						if (player.mayUseItemAt(pos, facing, stack) && world.mayInteract(player, pos2))
						{

							BlockState placementState = containedblock.getStateForPlacement(new BlockItemUseContext(context));
							
							BlockState actualState = placementState == null ? containedstate : placementState;

							//Attempted fix for #287
//							for (IProperty<?> prop : placementState.getValues().keySet())
//							{
//								if (prop instanceof DirectionProperty)
//									actualState = actualState.with((DirectionProperty) prop, placementState.get((DirectionProperty) prop));
//								else if (prop == BlockStateProperties.WATERLOGGED)
//									actualState = actualState.with((BooleanProperty) prop, placementState.get((BooleanProperty) prop));
//								else if(prop instanceof EnumProperty<?>)
//								{
//									Object value = placementState.get(prop);
//									if(value instanceof Direction.Axis)
//									{
//										actualState = actualState.with((EnumProperty)prop, (Direction.Axis)value);
//									}
//								}
//							}

							BlockSnapshot snapshot = BlockSnapshot.create(world.dimension(), world, pos2);
							EntityPlaceEvent event = new EntityPlaceEvent(snapshot, world.getBlockState(pos), player);
							MinecraftForge.EVENT_BUS.post(event);

							if (!event.isCanceled())
							{
								world.setBlockAndUpdate(pos2, actualState);

								// If the blockstate doesn't handle rotation,
								// try to
								// change rotation via NBT
								if (!getTileData(stack).isEmpty())
								{
									CompoundNBT tag = getTileData(stack);
									Set<String> keys = tag.getAllKeys();
									keytester: for (String key : keys)
									{
										for (String facingKey : FACING_KEYS)
										{
											if (key.toLowerCase().equals(facingKey))
											{
												byte type = tag.getTagType(key);
												switch (type)
												{
												case 8:
													tag.putString(key, CharMatcher.javaUpperCase().matchesAllOf(tag.getString(key)) ? facing2.getOpposite().getName().toUpperCase() : facing2.getOpposite().getName());
													break;
												case 3:
													tag.putInt(key, facing2.getOpposite().get3DDataValue());
													break;
												case 1:
													tag.putByte(key, (byte) facing2.getOpposite().get3DDataValue());
													break;
												default:
													break;
												}

												break keytester;
											}
										}
									}
								}

								TileEntity tile = world.getBlockEntity(pos2);
								if (tile != null)
								{
									CompoundNBT data = getTileData(stack);
									updateTileLocation(data, pos2);
									tile.load(actualState, data);
								}
								clearTileData(stack);
								player.playSound(actualState.getSoundType(world, pos2, player).getPlaceSound(), 1.0f, 0.5f);
								player.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
								player.getPersistentData().remove("overrideKey");
								ItemEvents.sendPacket(player, 9, 0);
								return ActionResultType.SUCCESS;

							}
						}

					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();

				if (world != null && world.isClientSide)
				{
					CarryOn.LOGGER.info("Block: " + ItemCarryonBlock.getBlock(stack));
					CarryOn.LOGGER.info("BlockState: " + ItemCarryonBlock.getBlockState(stack));
					// CarryOn.LOGGER.info("Meta: " + ItemTile.getMeta(stack));
					CarryOn.LOGGER.info("ItemStack: " + ItemCarryonBlock.getItemStack(stack));

					if (ModelOverridesHandler.hasCustomOverrideModel(ItemCarryonBlock.getBlockState(stack), ItemCarryonBlock.getTileData(stack)))
						CarryOn.LOGGER.info("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemCarryonBlock.getBlockState(stack), ItemCarryonBlock.getTileData(stack)));

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonBlock.getBlockState(stack)))
						CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonBlock.getBlockState(stack)));

					player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Error detected. Cannot place block. Execute \"/carryon clear\" to remove the item"), false);
					StringTextComponent s = new StringTextComponent(TextFormatting.GOLD + "here");
					s.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
					player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Please report this error ").append(s), false);

				}
			}

		}

		return ActionResultType.FAIL;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
	{
		if (hasTileData(stack))
		{
			if (entity instanceof LivingEntity)
			{
				if (entity instanceof PlayerEntity && Settings.slownessInCreative.get() ? false : ((PlayerEntity) entity).isCreative())
					return;

				((LivingEntity) entity).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 1, potionLevel(stack), false, false));
			}
		}
		else
		{
			stack = ItemStack.EMPTY;
		}
	}

	public static boolean hasTileData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundNBT tag = stack.getTag();
			return tag.contains(TILE_DATA_KEY) && tag.contains("block") && tag.contains("stateid");
		}
		return false;
	}

	public static boolean storeTileData(@Nullable TileEntity tile, World world, BlockPos pos, BlockState state, ItemStack stack)
	{
		if (stack.isEmpty())
			return false;

		CompoundNBT tileTag = new CompoundNBT();
		if (tile != null)
			tileTag = tile.save(tileTag);

		CompoundNBT tag = stack.hasTag() ? stack.getTag() : new CompoundNBT();
		if (tag.contains(TILE_DATA_KEY))
			return false;

		tag.put(TILE_DATA_KEY, tileTag);

		// ItemStack drop = new ItemStack(state.getBlock().getItemDropped(state,
		// itemRand, 0), 1, state.getBlock().damageDropped(state));

		tag.putString("block", state.getBlock().getRegistryName().toString());
		// Item item = Item.getItemFromBlock(state.getBlock());
		// tag.setInt("meta", drop.getItemDamage());
		tag.putInt("stateid", Block.getId(state));
		stack.setTag(tag);
		return true;
	}
	
	public static void updateTileLocation(CompoundNBT tag, BlockPos pos)
	{
		tag.putInt("x", pos.getX());
		tag.putInt("y", pos.getY());
		tag.putInt("z", pos.getZ());
	}

	public static void clearTileData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundNBT tag = stack.getTag();
			tag.remove(TILE_DATA_KEY);
			tag.remove("block");
			tag.remove("stateid");
		}
	}

	public static CompoundNBT getTileData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundNBT tag = stack.getTag();
			return tag.getCompound(TILE_DATA_KEY);
		}
		return null;
	}

	public static Block getBlock(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundNBT tag = stack.getTag();
			int id = tag.getInt("stateid");
			return Block.stateById(id).getBlock();
		}
		return Blocks.AIR;
	}

	// public static int getMeta(ItemStack stack)
	// {
	// if (stack.hasTag())
	// {
	// CompoundNBT tag = stack.getTag();
	// int meta = tag.getInt("meta");
	// return meta;
	// }
	// return 0;
	// }

	public static ItemStack getItemStack(ItemStack stack)
	{
		return new ItemStack(getBlock(stack), 1);
	}

	public static BlockState getBlockState(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundNBT tag = stack.getTag();
			int id = tag.getInt("stateid");
			return Block.stateById(id);
		}
		return Blocks.AIR.defaultBlockState();
	}

	public static boolean isLocked(BlockPos pos, World world)
	{
		TileEntity te = world.getBlockEntity(pos);
		if (te != null)
		{
			CompoundNBT tag = new CompoundNBT();
			te.save(tag);
			return tag.contains("Lock") ? !tag.getString("Lock").equals("") : false;
		}

		return false;
	}

	// private boolean equal(Object[] a, Object[] b)
	// {
	// if (a.length != b.length)
	// return false;
	//
	// List lA = Arrays.asList(a);
	// List lB = Arrays.asList(b);
	//
	// return lA.containsAll(lB);
	// }

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
