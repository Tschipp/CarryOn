package tschipp.carryon.client.event;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.helper.ScriptParseHelper;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;

public class RenderEntityEvents
{
	/*
	 * Prevents the Player from scrolling
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onScroll(MouseEvent event) throws IllegalArgumentException, IllegalAccessException
	{
		if (event.getDwheel() > 0 || event.getDwheel() < 0 || Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isPressed())
		{
			ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItemMainhand();
			if (stack == null ? false : (stack.getItem() == RegistrationHandler.itemEntity))
			{
				if (ItemEntity.hasEntityData(stack))
				{
					event.setCanceled(true);
				}
			}
		}
	}

	/*
	 * Prevents the Player from opening Guis
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event)
	{
		if (event.getGui() != null)
		{
			boolean inventory = event.getGui() instanceof GuiContainer;
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (player != null)
			{
				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
				if (inventory && (stack != null ? stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack) : false))
				{
					event.setCanceled(true);
					Minecraft.getMinecraft().currentScreen = null;
				}
			}
		}
	}

	/*
	 * Prevents the Player from switching Slots
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void inputEvent(InputEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		Field field = KeyBinding.class.getDeclaredFields()[7];
		field.setAccessible(true);
		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItemMainhand();
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (stack != null ? (stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack)) : false)
		{
			if (settings.keyBindDrop.isPressed())
			{
				field.set(settings.keyBindDrop, false);
			}
			if (settings.keyBindSwapHands.isPressed())
			{
				field.set(settings.keyBindSwapHands, false);
			}
			for (KeyBinding keyBind : settings.keyBindsHotbar)
			{
				if (keyBind.isPressed())
				{
					field.set(keyBind, false);
				}
			}
		}

		int current = player.inventory.currentItem;

		if (player.getEntityData().hasKey("carrySlot") ? player.getEntityData().getInteger("carrySlot") != current : false)
		{
			player.inventory.currentItem = player.getEntityData().getInteger("carrySlot");
		}
	}

	/*
	 * Renders the Entity in First Person
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{

		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
		ItemStack stack = player.getHeldItemMainhand();
		int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;
		float partialticks = event.getPartialTicks();

		if (stack != null ? (stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack)) : false)
		{
			if(Loader.isModLoaded("realrender") || Loader.isModLoaded("rfpr"))
				return;
			
			
			Entity entity = ItemEntity.getEntity(stack, world);

			if (entity != null)
			{
				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialticks;
				double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialticks;
				double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialticks;

				entity.setPosition(d0, d1, d2);
				entity.rotationYaw = 0.0f;
				entity.prevRotationYaw = 0.0f;
				entity.setRotationYawHead(0.0f);

				float height = entity.height;
				float width = entity.width;
				GlStateManager.pushMatrix();
				GlStateManager.scale(.8, .8, .8);
				GlStateManager.rotate(180, 0, 1, 0);
				GlStateManager.translate(0.0, -height - .1, width + 0.1);
				GlStateManager.enableAlpha();

				if (perspective == 0)
				{
					RenderHelper.enableStandardItemLighting();
					Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);

					CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
					if (carryOverride != null)
					{
						double[] translation = ScriptParseHelper.getXYZArray(carryOverride.getRenderTranslation());
						double[] rotation = ScriptParseHelper.getXYZArray(carryOverride.getRenderRotation());
						double[] scale = ScriptParseHelper.getScale(carryOverride.getRenderScale());
						String entityname = carryOverride.getRenderNameEntity();
						if (entityname != null)
						{
							Entity newEntity = EntityList.createEntityByName(entityname, world);
							if (newEntity != null)
							{
								NBTTagCompound nbttag = carryOverride.getRenderNBT();
								if (nbttag != null)
									newEntity.readFromNBT(nbttag);
								entity = newEntity;
								entity.setPosition(d0, d1, d2);
								entity.rotationYaw = 0.0f;
								entity.prevRotationYaw = 0.0f;
								entity.setRotationYawHead(0.0f);
							}
						}

						GlStateManager.translate(translation[0], translation[1], translation[2]);
						GlStateManager.rotate((float) rotation[0], 1, 0, 0);
						GlStateManager.rotate((float) rotation[1], 0, 1, 0);
						GlStateManager.rotate((float) rotation[2], 0, 0, 1);
						GlStateManager.scale(scale[0], scale[1], scale[2]);

					}

					this.renderEntityStatic(entity);
					Minecraft.getMinecraft().getRenderManager().setRenderShadow(true);
				}

				GlStateManager.disableAlpha();
				GlStateManager.scale(1, 1, 1);
				GlStateManager.popMatrix();

				RenderHelper.disableStandardItemLighting();
				GlStateManager.disableRescaleNormal();
				GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
				GlStateManager.disableTexture2D();
				GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

				if (perspective == 0)
				{
					event.setCanceled(true);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void renderEntityStatic(Entity entity)
	{
		if (entity.ticksExisted == 0)
		{
			entity.lastTickPosX = entity.posX;
			entity.lastTickPosY = entity.posY;
			entity.lastTickPosZ = entity.posZ;
		}

		float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw);
		int i = this.getBrightnessForRender(entity, Minecraft.getMinecraft().thePlayer);

		if (entity.isBurning())
		{
			i = 15728880;
		}

		int j = i % 65536;
		int k = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		this.setLightmapDisabled(false);
		
		
		
		Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0.0D, 0.0D, 0.0D, f, 0.0F, true);
		this.setLightmapDisabled(true);
	}

	@SideOnly(Side.CLIENT)
	private int getBrightnessForRender(Entity entity, EntityPlayer player)
	{
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor_double(player.posX), 0, MathHelper.floor_double(player.posZ));

		if (entity.worldObj.isBlockLoaded(blockpos$mutableblockpos))
		{
			blockpos$mutableblockpos.setY(MathHelper.floor_double(player.posY + entity.getEyeHeight()));
			return entity.worldObj.getCombinedLight(blockpos$mutableblockpos, 0);
		}
		else
		{
			return 0;
		}
	}

	@SideOnly(Side.CLIENT)
	private void setLightmapDisabled(boolean disabled)
	{
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);

		if (disabled)
		{
			GlStateManager.disableTexture2D();
		}
		else
		{
			GlStateManager.enableTexture2D();
		}

		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	/*
	 * Renders the Block in Third Person
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPost(RenderPlayerEvent.Post event)
	{
		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayer player = event.getEntityPlayer();
		ModelPlayer modelPlayer = event.getRenderer().getMainModel();
		EntityPlayerSP clientPlayer = Minecraft.getMinecraft().thePlayer;
		ItemStack stack = player.getHeldItemMainhand();
		float partialticks = event.getPartialRenderTick();
		if (stack  != null ? stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack) : false)
		{
			Entity entity = ItemEntity.getEntity(stack, world);
			float rotation = -(player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialticks);

			if (entity != null)
			{
				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialticks;
				double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialticks;
				double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialticks;

				double c0 = clientPlayer.lastTickPosX + (clientPlayer.posX - clientPlayer.lastTickPosX) * partialticks;
				double c1 = clientPlayer.lastTickPosY + (clientPlayer.posY - clientPlayer.lastTickPosY) * partialticks;
				double c2 = clientPlayer.lastTickPosZ + (clientPlayer.posZ - clientPlayer.lastTickPosZ) * partialticks;

				double xOffset = d0 - c0;
				double yOffset = d1 - c1;
				double zOffset = d2 - c2;

				float height = entity.height;
				float width = entity.width;
				float multiplier = height * width;

				entity.setPosition(c0, c1, c2);
				entity.rotationYaw = 0.0f;
				entity.prevRotationYaw = 0.0f;
				entity.setRotationYawHead(0.0f);

				GlStateManager.pushMatrix();
				GlStateManager.translate(xOffset, yOffset, zOffset);
				GlStateManager.scale((10 - multiplier) * 0.08, (10 - multiplier) * 0.08, (10 - multiplier) * 0.08);
				GlStateManager.rotate(rotation, 0, 1f, 0);
				GlStateManager.translate(0.0, height / 2 + -(height / 2) + 1, width - 0.1 < 0.7 ? width - 0.1 + (0.7 - (width - 0.1)) : width - 0.1);

				if((Loader.isModLoaded("realrender") || Loader.isModLoaded("rfpr")) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
					GlStateManager.translate(0, 0, -0.3);
				
				if (player.isSneaking())
				{
					GlStateManager.translate(0, -0.3, 0);
				}

				Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);
				
				CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
				if (carryOverride != null)
				{
					double[] translation = ScriptParseHelper.getXYZArray(carryOverride.getRenderTranslation());
					double[] rot = ScriptParseHelper.getXYZArray(carryOverride.getRenderRotation());
					double[] scale = ScriptParseHelper.getScale(carryOverride.getRenderScale());
					String entityname = carryOverride.getRenderNameEntity();
					if (entityname != null)
					{
						Entity newEntity = EntityList.createEntityByName(entityname, world);
						if (newEntity != null)
						{
							NBTTagCompound nbttag = carryOverride.getRenderNBT();
							if (nbttag != null)
								newEntity.readFromNBT(nbttag);
							entity = newEntity;
							entity.setPosition(c0, c1, c2);
							entity.rotationYaw = 0.0f;
							entity.prevRotationYaw = 0.0f;
							entity.setRotationYawHead(0.0f);
						}
					}

					GlStateManager.translate(translation[0], translation[1], translation[2]);
					GlStateManager.rotate((float) rot[0], 1, 0, 0);
					GlStateManager.rotate((float) rot[1], 0, 1, 0);
					GlStateManager.rotate((float) rot[2], 0, 0, 1);
					GlStateManager.scale(scale[0], scale[1], scale[2]);

				}
				
				Minecraft.getMinecraft().getRenderManager().renderEntityStatic(entity, 0.0f, false);
				Minecraft.getMinecraft().getRenderManager().setRenderShadow(true);

				GlStateManager.scale(1, 1, 1);
				GlStateManager.popMatrix();
			}
		}
	}
}
