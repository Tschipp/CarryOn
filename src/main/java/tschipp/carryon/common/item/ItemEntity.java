package tschipp.carryon.common.item;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.GameRegistry;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.network.client.CarrySlotPacket;

public class ItemEntity extends Item
{

	public static final String ENTITY_DATA_KEY = "entityData";

	public ItemEntity()
	{
		this.setUnlocalizedName("entity_item");
		this.setRegistryName(CarryOn.MODID, "entity_item");
		ForgeRegistries.ITEMS.register(this);
		this.setMaxStackSize(1);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		if (hasEntityData(stack))
		{	
			return I18n.translateToLocal("entity."+EntityList.getTranslationName(new ResourceLocation(getEntityName(stack))) + ".name");
		}

		return "";
	}

	public static boolean hasEntityData(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
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
		entityData = entity.writeToNBT(entityData);

		String name = EntityList.getKey(entity).toString();

		NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		if (tag.hasKey(ENTITY_DATA_KEY))
			return false;

		tag.setTag(ENTITY_DATA_KEY, entityData);
		tag.setString("entity", name);
		stack.setTagCompound(tag);
		return true;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		Block block = world.getBlockState(pos).getBlock();

		if (hasEntityData(stack))
		{
			BlockPos finalPos = pos;

			if (!block.isReplaceable(world, pos))
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
						((EntityLiving) entity).playLivingSound();
					}
					clearEntityData(stack);
					player.setHeldItem(hand, ItemStack.EMPTY);
					CarryOn.network.sendToAllAround(new CarrySlotPacket(9, player.getEntityId()), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 256));
				}
				player.getEntityData().removeTag("overrideKey");
				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.FAIL;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
	{
		if (hasEntityData(stack))
		{
			if(getEntity(stack, world) == null)
				stack = ItemStack.EMPTY;

			if (entity instanceof EntityLivingBase)
			{
				if(entity instanceof EntityPlayer && CarryOnConfig.settings.slownessInCreative ? false : ((EntityPlayer)entity).isCreative())
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
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			tag.removeTag(ENTITY_DATA_KEY);
			tag.removeTag("entity");
		}
	}

	public static NBTTagCompound getEntityData(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			return tag.getCompoundTag(ENTITY_DATA_KEY);
		}
		return null;
	}

	public static Entity getEntity(ItemStack stack, World world)
	{
		if (world == null)
			return null;

		String name = getEntityName(stack);

		NBTTagCompound e = getEntityData(stack);
		Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(name), world);
		if (entity != null)
			entity.readFromNBT(e);

		return entity;
	}

	public static String getEntityName(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			return tag.getString("entity");
		}
		return null;
	}

	public static String getCustomName(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			if (tag.hasKey("CustomName") && !tag.getString("CustomName").isEmpty()) {
				return tag.toString();
			} else {
				return tag.toString();
			}
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

		if (!CarryOnConfig.settings.heavyEntities)
			i = 1;

		return (int) (i * CarryOnConfig.settings.entitySlownessMultiplier);
	}
}
