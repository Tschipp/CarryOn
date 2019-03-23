package tschipp.carryon.common.item;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.event.ItemEvents;

public class ItemEntity extends Item
{

	public static final String ENTITY_DATA_KEY = "entityData";

	public ItemEntity()
	{
		super(new Item.Properties().maxStackSize(1));
		this.setRegistryName(CarryOn.MODID, "entity_item");
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack)
	{
		if (hasEntityData(stack)) {
			
			return new TextComponentTranslation(getEntityType(stack).getTranslationKey());
		}

		return new TextComponentString("");
	}

	public static boolean hasEntityData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			return tag.hasKey(ENTITY_DATA_KEY) && tag.hasKey("entity");
		}
		return false;
	}

	public static boolean storeEntityData(@Nonnull Entity entity, World world, ItemStack stack)
	{
		if (entity == null)
			return false;

		if (stack.isEmpty())
			return false;

		NBTTagCompound entityData = new NBTTagCompound();
		entityData = entity.serializeNBT();

		String name = EntityType.getId(entity.getType()).toString();

		NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();
		if (tag.hasKey(ENTITY_DATA_KEY))
			return false;

		tag.setTag(ENTITY_DATA_KEY, entityData);
		tag.setString("entity", name);
		stack.setTag(tag);
		return true;
	}

	@Override
	public EnumActionResult onItemUse(ItemUseContext context)
	{
		EntityPlayer player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		EnumFacing facing = context.getFace();
		
		ItemStack stack = context.getItem();
		
		IBlockState state = world.getBlockState(pos);

		if(ModList.get().isLoaded("betterplacement"))
		{
			if(CarryOnKeybinds.isKeyPressed(player))
				return EnumActionResult.FAIL;
		}
		
		if (hasEntityData(stack))
		{
			BlockPos finalPos = pos;

			if (!state.isReplaceable(new BlockItemUseContext(context)))
			{
				finalPos = pos.offset(facing);
			}

			Entity entity = getEntity(stack, world);
			if (entity != null)
			{
				if (!world.isRemote)
				{
					entity.setPositionAndRotation(finalPos.getX() + 0.5, finalPos.getY(), finalPos.getZ() + 0.5, 180 + player.rotationYawHead, 0.0f);
					world.spawnEntity(entity);
					if (entity instanceof EntityLiving)
					{
						((EntityLiving) entity).playAmbientSound();
					}
					clearEntityData(stack);
					player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
					ItemEvents.sendPacket(player, 9, 0);
				
				}
				player.getEntityData().removeTag("overrideKey");
				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.FAIL;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
	{
		if (hasEntityData(stack))
		{
			if(getEntity(stack, world) == null)
				stack = ItemStack.EMPTY;

			if (entity instanceof EntityLivingBase)
			{
				if(entity instanceof EntityPlayer && Settings.slownessInCreative.get() ? false : ((EntityPlayer)entity).isCreative())
					return;

				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 1, potionLevel(stack, world), false, false));
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
			NBTTagCompound tag = stack.getTag();
			tag.removeTag(ENTITY_DATA_KEY);
			tag.removeTag("entity");
		}
	}

	public static NBTTagCompound getEntityData(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			return tag.getCompound(ENTITY_DATA_KEY);
		}
		return null;
	}

	public static Entity getEntity(ItemStack stack, World world)
	{
		if (world == null)
			return null;

		String name = getEntityName(stack);

		NBTTagCompound e = getEntityData(stack);
		Entity entity = EntityType.create(world, new ResourceLocation(name));
		if (entity != null)
			entity.deserializeNBT(e);

		return entity;
	}

	public static String getEntityName(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			return tag.getString("entity");
		}
		return null;
	}

	public static String getCustomName(ItemStack stack)
	{
		if (stack.hasTag())
		{
			NBTTagCompound tag = stack.getTag();
			if (tag.hasKey("CustomName") && !tag.getString("CustomName").isEmpty()) {
				return tag.toString();
			} else {
				return tag.toString();
			}
		}
		return null;
	}

	public static EntityType<?> getEntityType(ItemStack stack) {
		if (stack.hasTag()) {
			NBTTagCompound tag = stack.getTag();
			String name = tag.getString("entity");
			EntityType<?> type = EntityType.getById(name);
			return type;
		}
		return null;
	}
	
	private int potionLevel(ItemStack stack, World world)
	{
		Entity e = getEntity(stack, world);
		if(e == null)
			return 1;
		
		int i = (int)(e.height * e.width);
		if (i > 4)
			i = 4;

		if (!Settings.heavyEntities.get())
			i = 1;

		double multiplier = Settings.entitySlownessMultiplier.get();
		
		return (int) (multiplier * i);
	}
}
