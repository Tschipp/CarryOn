package tschipp.carryon.items;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ItemTile extends Item {

	public static final String TILE_DATA_KEY = "tileData";
	public static final String[] Direction_KEYS = new String[] { "rotation", "rot", "Direction", "face", "direction", "dir",
			"front" };

	public ItemTile() {
		super(new Item.Settings().stackSize(1));
	}

	@Override
	public TextComponent getTranslatedNameTrimmed(ItemStack stack) {
		if (hasTileData(stack)) {
			// BlockState state = getBlockState(stack);
			// CompoundTag nbt = getTileData(stack);

			// if (ModelOverridesHandler.hasCustomOverrideModel(state, nbt))
			// {
			// 	Object override = ModelOverridesHandler.getOverrideObject(state, nbt);
			// 	if (override instanceof ItemStack)
			// 		return ((ItemStack) override).getDisplayName();
			// 	else
			// 	{
			// 		BlockState ostate = (BlockState) override;
			// 		List<ItemStack> drops = ostate.getDroppedStacks(new LootContext.Builder());
			// 		ItemStack itemstack = new ItemStack(ostate.getBlock().getItemDropped(ostate, this.random, 0), 1, state.getBlock().damageDropped(ostate));
			// 		return itemstack.getDisplayName();
			// 	}
			// }

			return getItemStack(stack).getItem().getTranslatedNameTrimmed(getItemStack(stack));
		}

		return new StringTextComponent("");
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) 
	{
		// if (Loader.isModLoaded("betterplacement"))
		// {
		// 	if (CarryOnKeybinds.isKeyPressed(player))
		// 		return EnumActionResult.FAIL;
		// }

		Direction direction = context.getFacing();
		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		ItemStack stack = context.getItemStack();


		// Block block = world.getBlockState(pos).getBlock();
		if (hasTileData(stack))
		{
			try
			{
				Vec3d vec = player.getRotationVecClient();
				Direction direction2 = Direction.getFacing((float) vec.x, 0f, (float) vec.z);
				BlockPos pos2 = pos;
				Block containedblock = getBlock(stack);
				BlockState containedstate = getBlockState(stack);
				if (!world.getBlockState(pos).method_11587(new ItemPlacementContext(context)))
				{
					pos2 = pos.offset(direction);
				}

				if (world.getBlockState(pos2).getBlock().method_9579(world.getBlockState(pos2), world, pos2) && containedblock != null)
				{
					boolean canPlace = containedblock.canPlaceAt(world.getBlockState(pos2), world, pos2);

					if (canPlace)
					{
						if (player.canPlaceBlock(pos2, direction, stack) && world.canPlayerModifyAt(player, pos2))
						{
							// Handles Blockstate rotation
							// Iterator<Property<?>> iterator = containedblock.getDefaultState().getProperties().iterator();
							// while (iterator.hasNext())
							// {
							// 	Property prop = iterator.next();
							// 	Object[] allowedValues = prop.getValues().toArray();

							// 	if (prop instanceof DirectionProperty && this.equal(allowedValues, horizontals))
							// 	{
							// 		world.setBlockState(pos2, containedstate.with(prop, containedblock instanceof StairsBlock ? Direction2 : Direction2.getOpposite()));
							// 		set = true;
							// 	}
							// 	else if (prop instanceof DirectionProperty && this.equal(allowedValues, Direction.values()))
							// 	{
							// 		Direction2 = Direction.getDirection((float) vec.x, (float) vec.y, (float) vec.z);
							// 		world.setBlockState(pos2, containedstate.with(prop, Direction2.getOpposite()));
							// 		set = true;
							// 	}
							// }

							BlockState actualState = containedblock.getPlacementState(new ItemPlacementContext(context));
							world.setBlockState(pos2, actualState);

							// world.updateNeighborsAlways(pos2, containedblock);
							// If the blockstate doesn't handle rotation, try to
							// change rotation via NBT
							if (!getTileData(stack).isEmpty())
							{
								CompoundTag tag = getTileData(stack);
								Set<String> keys = tag.getKeys();
								keytester:
								for (String key : keys)
								{
									for (String DirectionKey : Direction_KEYS)
									{
										if (key.toLowerCase().equals(DirectionKey))
										{
											byte type = tag.getType(key);
											switch (type)
											{
											case 8:
												tag.putString(key, direction2.getOpposite().getName());
												break;
											case 3:
												tag.putInt(key, direction2.getOpposite().getId());
												break;
											case 1:
												tag.putByte(key, (byte) direction2.getOpposite().getId());
												break;
											default:
												break;
											}
											
											break keytester;
										}
									}
								}
							}

							BlockEntity tile = world.getBlockEntity(pos2);
							if (tile != null)
							{
								tile.fromTag(getTileData(stack));
								tile.setPos(pos2);
							}
							clearTileData(stack);
							player.playSoundAtEntity(containedstate.getSoundGroup().getPlaceSound(), 1.0f, 0.5f);
							player.setStackInHand(Hand.MAIN, ItemStack.EMPTY);
							// player.nbt.remove("overrideKey");
							// ItemEvents.sendPacket(player, 9, 0);
							return ActionResult.SUCCESS;
						}

					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();

				// if (world != null && world.isRemote)
				// {
				// 	CarryOn.LOGGER.info("Block: " + ItemTile.getBlock(stack));
				// 	CarryOn.LOGGER.info("BlockState: " + ItemTile.getBlockState(stack));
				// 	CarryOn.LOGGER.info("Meta: " + ItemTile.getMeta(stack));
				// 	CarryOn.LOGGER.info("ItemStack: " + ItemTile.getItemStack(stack));

				// 	if (ModelOverridesHandler.hasCustomOverrideModel(ItemTile.getBlockState(stack), ItemTile.getTileData(stack)))
				// 		CarryOn.LOGGER.info("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemTile.getBlockState(stack), ItemTile.getTileData(stack)));

				// 	if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemTile.getBlockState(stack)))
				// 		CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemTile.getBlockState(stack)));

				// 	player.sendMessage(new TextComponentString(TextFormatting.RED + "Error detected. Cannot place block. Execute \"/carryon clear\" to remove the item"));
				// 	TextComponentString s = new TextComponentString(TextFormatting.GOLD + "here");
				// 	s.getStyle().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
				// 	player.sendMessage(new TextComponentString(TextFormatting.RED + "Please report this error ").appendSibling(s));

				// }
			}

		}

		return ActionResult.FAILURE;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
	{
		if (hasTileData(stack))
		{
			if (entity instanceof LivingEntity)
			{
				if (entity instanceof PlayerEntity && /*CarryOnConfig.settings.slownessInCreative ? false :  */ ((PlayerEntity) entity).isCreative())
					return;

				((LivingEntity) entity).addPotionEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1, potionLevel(stack), false, false));
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
			CompoundTag tag = stack.getTag();
			return tag.containsKey(TILE_DATA_KEY) && tag.containsKey("block") && tag.containsKey("stateid");
		}
		return false;
	}

	public static boolean storeTileData(BlockEntity tile, World world, BlockPos pos, BlockState state, ItemStack stack)
	{
		if (stack.isEmpty())
			return false;

		CompoundTag chest = new CompoundTag();
		if (tile != null)
			chest = tile.toTag(chest);

		CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
		if (tag.containsKey(TILE_DATA_KEY))
			return false;

		tag.put(TILE_DATA_KEY, chest);

		// ItemStack drop = new ItemStack(state.getBlock().getItemDropped(state, ItemTile.random, 0), 1, state.getBlock().damageDropped(state));

		tag.putString("block", Registry.BLOCK.getId(getBlock(stack)).toString());
		// Item item = Item.getItemFromBlock(state.getBlock());
		tag.putInt("stateid", Block.getRawIdFromState(state));
		stack.setTag(tag);
		return true;
	}

	public static void clearTileData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			tag.remove(TILE_DATA_KEY);
			tag.remove("block");
			tag.remove("stateid");
		}
	}

	public static CompoundTag getTileData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			return (CompoundTag)tag.getTag(TILE_DATA_KEY);
		}
		return null;
	}

	public static Block getBlock(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			int id = tag.getInt("stateid");
			return Block.getStateFromRawId(id).getBlock();
		}
		return Blocks.AIR;
	}

	public static ItemStack getItemStack(ItemStack stack)
	{
		return new ItemStack(getBlock(stack), 1);
	}

	public static BlockState getBlockState(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			int id = tag.getInt("stateid");
			return Block.getStateFromRawId(id);
		}
		return Blocks.AIR.getDefaultState();
	}

	public static boolean isLocked(BlockPos pos, World world)
	{
		BlockEntity te = world.getBlockEntity(pos);
		if (te != null)
		{
			CompoundTag tag = new CompoundTag();
			te.toTag(tag);
			return tag.containsKey("Lock") ? !tag.getString("Lock").equals("") : false;
		}

		return false;
	}

	// private boolean equal(Object[] a, Object[] b)
	// {
	// 	if (a.length != b.length)
	// 		return false;

	// 	List lA = Arrays.asList(a);
	// 	List lB = Arrays.asList(b);

	// 	return lA.containsAll(lB);
	// }

	private int potionLevel(ItemStack stack)
	{
		String nbt = getTileData(stack).toString();
		int i = nbt.length() / 500;

		if (i > 4)
			i = 4;

		// if (!CarryOnConfig.settings.heavyTiles)
		// 	i = 1;

		// return (int) (i * CarryOnConfig.settings.blockSlownessMultiplier);

		return i * 1;
		// return 0;
	}
}
