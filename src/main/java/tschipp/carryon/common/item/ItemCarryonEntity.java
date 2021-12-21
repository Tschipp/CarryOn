package tschipp.carryon.common.item;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.event.ItemEvents;

public class ItemCarryonEntity extends Item
{

	private static final Method initGoals;

	static
	{
		initGoals = ObfuscationReflectionHelper.findMethod(Mob.class, "m_8099_");
		initGoals.setAccessible(true);
	}

	public static final String ENTITY_DATA_KEY = "entityData";

	public ItemCarryonEntity()
	{
		super(new Item.Properties().stacksTo(1));
		this.setRegistryName(CarryOn.MODID, "entity_item");
	}

	@Override
	public Component getName(ItemStack stack)
	{
		if (hasEntityData(stack))
		{

			return new TranslatableComponent(getEntityType(stack).getDescriptionId());
		}

		return new TextComponent("");
	}

	public static boolean hasEntityData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			return tag.contains(ENTITY_DATA_KEY) && tag.contains("entity");
		}
		return false;
	}

	public static boolean storeEntityData(@Nonnull Entity entity, Level world, ItemStack stack)
	{
		if (entity == null || stack.isEmpty())
			return false;

		CompoundTag entityData = new CompoundTag();
		entityData = entity.serializeNBT();

		String name = EntityType.getKey(entity.getType()).toString();

		CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
		if (tag.contains(ENTITY_DATA_KEY))
			return false;

		tag.put(ENTITY_DATA_KEY, entityData);
		tag.putString("entity", name);
		stack.setTag(tag);
		return true;
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction facing = context.getClickedFace();

		ItemStack stack = context.getItemInHand();

		BlockState state = world.getBlockState(pos);

		if (ModList.get().isLoaded("betterplacement") && CarryOnKeybinds.isKeyPressed(player))
			return InteractionResult.FAIL;

		if (hasEntityData(stack))
		{
			BlockPos finalPos = pos;

			if (!state.canBeReplaced(new BlockPlaceContext(context)))
			{
				finalPos = pos.relative(facing);
			}

			Entity entity = getEntity(stack, world);
			if (entity != null)
			{
				if (!world.isClientSide)
				{
					entity.absMoveTo(finalPos.getX() + 0.5, finalPos.getY(), finalPos.getZ() + 0.5, 180 + player.yHeadRot, 0.0f);
					world.addFreshEntity(entity);
					if (entity instanceof Mob)
					{
						((Mob) entity).playAmbientSound();
					}
					clearEntityData(stack);
					player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
					ItemEvents.sendPacket(player, 9, 0);

				}
				player.getPersistentData().remove("overrideKey");
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.FAIL;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
	{
		if (hasEntityData(stack))
		{
			if (getEntity(stack, world) == null)
				stack = ItemStack.EMPTY;

			if (entity instanceof LivingEntity)
			{
				if (entity instanceof Player && Settings.slownessInCreative.get() ? false : ((Player) entity).isCreative())
					return;

				((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1, this.potionLevel(stack, world), false, false));
			}

		}
		else
		{
			stack = ItemStack.EMPTY;
		}
	}

	public static void clearEntityData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			tag.remove(ENTITY_DATA_KEY);
			tag.remove("entity");
		}
	}

	public static CompoundTag getPersistentData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			return tag.getCompound(ENTITY_DATA_KEY);
		}
		return null;
	}

	public static Entity getEntity(ItemStack stack, Level world)
	{
		if (world == null)
			return null;

		String name = getEntityName(stack);

		CompoundTag e = getPersistentData(stack);
		Optional<EntityType<?>> type = EntityType.byString(name);
		Entity entity = null;

		if (type.isPresent())
		{
			entity = type.get().create(world);
		}

		if (entity != null)
		{
			try
			{
				initGoals.invoke(entity);
				entity.deserializeNBT(e);
			}
			catch (Exception e1)
			{
			}
		}

		return entity;
	}

	public static String getEntityName(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			return tag.getString("entity");
		}
		return null;
	}

	public static String getCustomName(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			if (tag.contains("CustomName") && !tag.getString("CustomName").isEmpty())
			{
				return tag.toString();
			}
			else
			{
				return tag.toString();
			}
		}
		return null;
	}

	public static EntityType<?> getEntityType(ItemStack stack)
	{
		if (stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			String name = tag.getString("entity");
			Optional<EntityType<?>> type = EntityType.byString(name);
			if (type.isPresent())
				return type.get();
		}
		return null;
	}

	private int potionLevel(ItemStack stack, Level world)
	{
		Entity e = getEntity(stack, world);
		if (e == null)
			return 1;

		int i = (int) (e.getBbHeight() * e.getBbWidth());
		if (i > 4)
			i = 4;

		if (!Settings.heavyEntities.get())
			i = 1;

		double multiplier = Settings.entitySlownessMultiplier.get();

		return (int) (multiplier * i);
	}
}
