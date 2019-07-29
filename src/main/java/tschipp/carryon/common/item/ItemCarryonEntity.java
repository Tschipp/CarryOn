package tschipp.carryon.common.item;

import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.event.ItemEvents;

public class ItemCarryonEntity extends Item {

	public static final String ENTITY_DATA_KEY = "entityData";

	public ItemCarryonEntity() {
		super(new Item.Properties().maxStackSize(1));
		this.setRegistryName(CarryOn.MODID, "entity_item");
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack)
	{
		if (hasEntityData(stack)) {
			
			return new TranslationTextComponent(getEntityType(stack).getTranslationKey());
		}

		return new StringTextComponent("");
	}

	public static boolean hasEntityData(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return tag.contains(ENTITY_DATA_KEY) && tag.contains("entity");
		}
		return false;
	}

	public static boolean storeEntityData(@Nonnull Entity entity, World world, ItemStack stack) {
		if (entity == null)
			return false;

		if (stack.isEmpty())
			return false;

		CompoundNBT entityData = new CompoundNBT();
		entityData = entity.serializeNBT();

		String name = EntityType.getKey(entity.getType()).toString();

		CompoundNBT tag = stack.hasTag() ? stack.getTag() : new CompoundNBT();
		if (tag.contains(ENTITY_DATA_KEY))
			return false;

		tag.put(ENTITY_DATA_KEY, entityData);
		tag.putString("entity", name);
		stack.setTag(tag);
		return true;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		Direction facing = context.getFace();

		ItemStack stack = context.getItem();

		BlockState state = world.getBlockState(pos);

		if (ModList.get().isLoaded("betterplacement")) {
			if (CarryOnKeybinds.isKeyPressed(player))
				return ActionResultType.FAIL;
		}

		if (hasEntityData(stack)) {
			BlockPos finalPos = pos;

			if (!state.isReplaceable(new BlockItemUseContext(context))) {
				finalPos = pos.offset(facing);
			}

			Entity entity = getEntity(stack, world);
			if (entity != null) {
				if (!world.isRemote) {
					entity.setPositionAndRotation(finalPos.getX() + 0.5, finalPos.getY(), finalPos.getZ() + 0.5,
							180 + player.rotationYawHead, 0.0f);
					world.addEntity(entity);
					if (entity instanceof MobEntity) {
						((MobEntity) entity).playAmbientSound();
					}
					clearEntityData(stack);
					player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
					ItemEvents.sendPacket(player, 9, 0);

				}
				player.getEntityData().remove("overrideKey");
				return ActionResultType.SUCCESS;
			}
		}

		return ActionResultType.FAIL;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (hasEntityData(stack)) {
			if (getEntity(stack, world) == null)
				stack = ItemStack.EMPTY;

			if (entity instanceof LivingEntity) {
				if (entity instanceof PlayerEntity && Settings.slownessInCreative.get() ? false
						: ((PlayerEntity) entity).isCreative())
					return;

				((LivingEntity) entity).addPotionEffect(
						new EffectInstance(Effects.SLOWNESS, 1, potionLevel(stack, world), false, false));
			}

		} else {
			stack = ItemStack.EMPTY;
		}
	}

	public static void clearEntityData(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			tag.remove(ENTITY_DATA_KEY);
			tag.remove("entity");
		}
	}

	public static CompoundNBT getEntityData(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return tag.getCompound(ENTITY_DATA_KEY);
		}
		return null;
	}

	public static Entity getEntity(ItemStack stack, World world) {
		if (world == null)
			return null;

		String name = getEntityName(stack);

		CompoundNBT e = getEntityData(stack);
		Optional<EntityType<?>> type = EntityType.byKey(name);
		Entity entity = null;

		if (type.isPresent()) {
			entity = type.get().create(world);
		}

		if (entity != null)
			entity.deserializeNBT(e);

		return entity;
	}

	public static String getEntityName(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return tag.getString("entity");
		}
		return null;
	}

	public static String getCustomName(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			if (tag.contains("CustomName") && !tag.getString("CustomName").isEmpty()) {
				return tag.toString();
			} else {
				return tag.toString();
			}
		}
		return null;
	}

	public static EntityType<?> getEntityType(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			String name = tag.getString("entity");
			Optional<EntityType<?>> type = EntityType.byKey(name);
			if (type.isPresent())
				return type.get();
		}
		return null;
	}

	private int potionLevel(ItemStack stack, World world) {
		Entity e = getEntity(stack, world);
		if (e == null)
			return 1;

		int i = (int) (e.getHeight() * e.getWidth());
		if (i > 4)
			i = 4;

		if (!Settings.heavyEntities.get())
			i = 1;

		double multiplier = Settings.entitySlownessMultiplier.get();

		return (int) (multiplier * i);
	}
}
