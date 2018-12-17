package tschipp.carryon.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ItemEntity extends Item {

	public static final String ENTITY_DATA_KEY = "entityData";

	public ItemEntity() {
		super(new Item.Settings().stackSize(1));
	}

	@Override
	public TextComponent getTranslatedNameTrimmed(ItemStack stack) {
		if (hasEntityData(stack)) {
			return new TranslatableTextComponent(getEntityType(stack).getTranslationKey());
		}

		return new StringTextComponent("");
	}

	public static boolean hasEntityData(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			return tag.containsKey(ENTITY_DATA_KEY) && tag.containsKey("entity");
		}
		return false;
	}

	public static boolean storeEntityData(Entity entity, World world, ItemStack stack) {
		if (entity == null)
			return false;

		if (stack.isEmpty())
			return false;

		CompoundTag entityData = new CompoundTag();
		entityData = entity.toTag(entityData);
		EntityType<?> type = entity.getType();

		String name = EntityType.getId(type).toString();

		CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
		if (tag.containsKey(ENTITY_DATA_KEY))
			return false;

		tag.put(ENTITY_DATA_KEY, entityData);
		tag.putString("entity", name);
		stack.setTag(tag);
		return true;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {

		Direction Direction = context.getFacing();
		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		ItemStack stack = context.getItemStack();

		// if(Loader.isModLoaded("betterplacement"))
		// {
		// if(CarryOnKeybinds.isKeyPressed(player))
		// return ActionResult.FAIL;
		// }

		if (hasEntityData(stack)) {
			BlockPos finalPos = pos;

			if (!world.getBlockState(finalPos).method_11587(new ItemPlacementContext(context)))
			{
				finalPos = pos.offset(Direction);
			}

			Entity entity = getEntity(stack, world);
			if (entity != null) {
				if (!world.isClient) {
					entity.setPositionAndAngles(finalPos.getX() + 0.5, finalPos.getY(), finalPos.getZ() + 0.5,
							180 + player.yaw, 0.0f);
					world.spawnEntity(entity);
					if (entity instanceof LivingEntity) {
						// ((LivingEntity) entity).sound.playLivingSound();
					}
					clearEntityData(stack);
					player.setStackInHand(Hand.MAIN, ItemStack.EMPTY);
					// ItemEvents.sendPacket(player, 9, 0);

				}
				// player.getEntityData().remove("overrideKey");
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.FAILURE;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (hasEntityData(stack)) {
			if (getEntity(stack, world) == null)
				stack = ItemStack.EMPTY;

			if (entity instanceof LivingEntity) {
				if (entity instanceof PlayerEntity
						&& /* CarryOnConfig.settings.slownessInCreative ? false : */ ((PlayerEntity) entity)
								.isCreative())
					return;

				((LivingEntity) entity).addPotionEffect(
						new StatusEffectInstance(StatusEffects.SLOWNESS, 1, potionLevel(stack, world), false, false));
			}

		} else {
			stack = ItemStack.EMPTY;
		}
	}

	public static void clearEntityData(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			tag.remove(ENTITY_DATA_KEY);
			tag.remove("entity");
		}
	}

	public static CompoundTag getEntityData(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			return tag.getCompound(ENTITY_DATA_KEY);
		}
		return null;
	}

	public static Entity getEntity(ItemStack stack, World world) {
		if (world == null)
			return null;

		String name = getEntityName(stack);

		CompoundTag e = getEntityData(stack);
		Entity entity = EntityType.createInstance(world, new Identifier(name));
		if (entity != null)
			entity.fromTag(e);

		return entity;
	}

	public static String getEntityName(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			return tag.getString("entity");
		}
		return null;
	}

	public static EntityType<?> getEntityType(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			String name = tag.getString("entity");
			EntityType<?> type = EntityType.get(name);
			return type;
		}
		return null;
	}

	public static String getCustomName(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if (tag.containsKey("CustomName") && !tag.getString("CustomName").isEmpty()) {
				return tag.toString();
			} else {
				return tag.toString();
			}
		}
		return null;
	}

	private int potionLevel(ItemStack stack, World world) {
		Entity e = getEntity(stack, world);
		if (e == null)
			return 1;

		int i = (int) (e.height * e.width);
		if (i > 4)
			i = 4;

		// if (!CarryOnConfig.settings.heavyEntities)
		// i = 1;

		// return (int) (i * CarryOnConfig.settings.entitySlownessMultiplier);
		return i * 1;
	}
}
