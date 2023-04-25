package tschipp.carryon.common.item;

import com.google.common.base.CharMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.event.ItemEvents;
import tschipp.carryon.common.handler.CustomPickupOverrideHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;

import javax.annotation.Nullable;
import java.util.Set;

public class ItemCarryonBlock extends Item
{

	public static final String TILE_DATA_KEY = "tileData";
	public static final String[] FACING_KEYS = {"rotation", "rot", "facing", "face", "direction", "dir", "front", "forward"};

	public ItemCarryonBlock()
	{
		super(new Item.Properties().stacksTo(1));
		this.setRegistryName(CarryOn.MODID, "tile_item");
	}

	@Override
	public Component getName(ItemStack stack)
	{
		if (hasTileData(stack)) {
			BlockState state = getBlockState(stack);
			CompoundTag nbt = getTileData(stack);

			if (ModelOverridesHandler.hasCustomOverrideModel(state, nbt)) {
				Object override = ModelOverridesHandler.getOverrideObject(state, nbt);
				if (override instanceof ItemStack)
					return ((ItemStack) override).getHoverName();
				else {
					BlockState ostate = (BlockState) override;
					return ostate.getBlock().getName();
				}
			}

			return getItemStack(stack).getHoverName();
		}

		return new TextComponent("");
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Direction facing = context.getClickedFace();
		Player player = context.getPlayer();
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		ItemStack stack = context.getItemInHand();

		if (ModList.get().isLoaded("betterplacement") && CarryOnKeybinds.isKeyPressed(player))
			return InteractionResult.FAIL;

		if (hasTileData(stack)) {
			try {
				Vec3 vec = player.getLookAngle();
				Direction facing2 = Direction.getNearest((float) vec.x, 0f, (float) vec.z);
				BlockPos pos2 = pos;
				Block containedblock = getBlock(stack);
				BlockState containedstate = getBlockState(stack);
				if (!level.getBlockState(pos2).canBeReplaced(new BlockPlaceContext(context))) {
					pos2 = pos.relative(facing);
				}

				if (level.getBlockState(pos2).canBeReplaced(new BlockPlaceContext(context)) && containedblock != null) {
					boolean canPlace = containedstate.canSurvive(level, pos2);

					if (canPlace && player.mayUseItemAt(pos, facing, stack) && level.mayInteract(player, pos2)) {

						BlockState placementState = containedblock.getStateForPlacement(new BlockPlaceContext(context));

						BlockState actualState = placementState == null ? containedstate : placementState;

						// Attempted fix for #287
						// for (IProperty<?> prop :
						// placementState.getValues().keySet())
						// {
						// if (prop instanceof DirectionProperty)
						// actualState = actualState.with((DirectionProperty)
						// prop, placementState.get((DirectionProperty) prop));
						// else if (prop == BlockStateProperties.WATERLOGGED)
						// actualState = actualState.with((BooleanProperty)
						// prop, placementState.get((BooleanProperty) prop));
						// else if(prop instanceof EnumProperty<?>)
						// {
						// Object value = placementState.get(prop);
						// if(value instanceof Direction.Axis)
						// {
						// actualState = actualState.with((EnumProperty)prop,
						// (Direction.Axis)value);
						// }
						// }
						// }

						BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos2);
						EntityPlaceEvent event = new EntityPlaceEvent(snapshot, level.getBlockState(pos), player);
						MinecraftForge.EVENT_BUS.post(event);

						if (!event.isCanceled()) {
							level.setBlockAndUpdate(pos2, actualState);

							// If the blockstate doesn't handle rotation,
							// try to
							// change rotation via NBT
							if (!getTileData(stack).isEmpty()) {
								CompoundTag tag = getTileData(stack);
								Set<String> keys = tag.getAllKeys();
								keytester:
								for (String key : keys) {
									for (String facingKey : FACING_KEYS) {
										if (key.toLowerCase().equals(facingKey)) {
											byte type = tag.getTagType(key);
											switch (type) {
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

							BlockEntity tile = level.getBlockEntity(pos2);
							if (tile != null) {
								CompoundTag data = getTileData(stack);
								updateTileLocation(data, pos2);
								try {
									tile.load(data);
								} catch (Exception e) {}
							}
							clearTileData(stack);
							player.playSound(actualState.getSoundType(level, pos2, player).getPlaceSound(), 1.0f, 0.5f);
							player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
							player.getPersistentData().remove("overrideKey");
							ItemEvents.sendPacket(player, 9, 0);
							return InteractionResult.SUCCESS;

						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();

				if (level != null && level.isClientSide) {
					CarryOn.LOGGER.info("Block: " + ItemCarryonBlock.getBlock(stack));
					CarryOn.LOGGER.info("BlockState: " + ItemCarryonBlock.getBlockState(stack));
					// CarryOn.LOGGER.info("Meta: " + ItemTile.getMeta(stack));
					CarryOn.LOGGER.info("ItemStack: " + ItemCarryonBlock.getItemStack(stack));

					if (ModelOverridesHandler.hasCustomOverrideModel(ItemCarryonBlock.getBlockState(stack), ItemCarryonBlock.getTileData(stack)))
						CarryOn.LOGGER.info("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemCarryonBlock.getBlockState(stack), ItemCarryonBlock.getTileData(stack)));

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonBlock.getBlockState(stack)))
						CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonBlock.getBlockState(stack)));

					player.displayClientMessage(new TextComponent(ChatFormatting.RED + "Error detected. Cannot place block. Execute \"/carryon clear\" to remove the item"), false);
					TextComponent s = new TextComponent(ChatFormatting.GOLD + "here");
					s.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
					player.displayClientMessage(new TextComponent(ChatFormatting.RED + "Please report this error ").append(s), false);

				}
			}

		}

		return InteractionResult.FAIL;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected)
	{
		if (hasTileData(stack)) {
			if (entity instanceof LivingEntity) {
				if (entity instanceof Player && Settings.slownessInCreative.get() ? false : ((Player) entity).isCreative())
					return;

				((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1, potionLevel(stack), false, false));
			}
		} else {
			stack = ItemStack.EMPTY;
		}
	}

	public static boolean hasTileData(ItemStack stack)
	{
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			return tag.contains(TILE_DATA_KEY) && tag.contains("block") && tag.contains("stateid");
		}
		return false;
	}

	public static boolean storeTileData(@Nullable BlockEntity tile, Level level, BlockPos pos, BlockState state, ItemStack stack)
	{
		if (stack.isEmpty())
			return false;

		CompoundTag tileTag = new CompoundTag();
		if (tile != null)
			tileTag = tile.saveWithId();

		CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
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

	public static void updateTileLocation(CompoundTag tag, BlockPos pos)
	{
		tag.putInt("x", pos.getX());
		tag.putInt("y", pos.getY());
		tag.putInt("z", pos.getZ());
	}

	public static void clearTileData(ItemStack stack)
	{
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			tag.remove(TILE_DATA_KEY);
			tag.remove("block");
			tag.remove("stateid");
		}
	}

	public static CompoundTag getTileData(ItemStack stack)
	{
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			return tag.getCompound(TILE_DATA_KEY);
		}
		return null;
	}

	public static Block getBlock(ItemStack stack)
	{
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			int id = tag.getInt("stateid");
			return Block.stateById(id).getBlock();
		}
		return Blocks.AIR;
	}

	// public static int getMeta(ItemStack stack)
	// {
	// if (stack.hasTag())
	// {
	// CompoundTag tag = stack.getTag();
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
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			int id = tag.getInt("stateid");
			return Block.stateById(id);
		}
		return Blocks.AIR.defaultBlockState();
	}

	public static boolean isLocked(BlockPos pos, Level level)
	{
		BlockEntity te = level.getBlockEntity(pos);
		if (te != null) {
			CompoundTag tag = new CompoundTag();
			te.saveWithId();
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
