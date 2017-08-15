package tschipp.carryon.client.event;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;

public class RenderEntityEvents
{

	/*
	 * Prevents the Player from scrolling
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onScroll(MouseEvent event)
	{
		if (event.getDwheel() > 0 || event.getDwheel() < 0)
		{
			ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity)
			{
				if (ItemEntity.hasEntityData(stack))
					event.setCanceled(true);
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
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player != null)
			{
				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
				if (inventory && !stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
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
	public void inputEvent(InputEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		Field field = KeyBinding.class.getDeclaredFields()[7];
		field.setAccessible(true);
		ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
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
	}

	/*
	 * Renders the Entity in First Person
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{
		World world = Minecraft.getMinecraft().world;
		EntityPlayer player = Minecraft.getMinecraft().player;
		AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
		ItemStack stack = player.getHeldItemMainhand();
		int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;
		float partialticks = event.getPartialTicks();

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
		{
			BlockPos pos = player.getPosition();
			Entity entity = ItemEntity.getEntity(stack, world);
			if (entity != null)
			{
				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialticks;
				double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialticks;
				double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialticks;
				
				entity.setPosition(d0, d1, d2);
				entity.rotationYaw = 0.0f;
				entity.prevRotationYaw = 0.0f;
				entity.setRotationYawHead(0.0f);
				
				float height = entity.height;
				float width = entity.width;
				float multiplier = height * width;
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(1, 1, 1);
				GlStateManager.rotate(180, 0, 1, 0);
				GlStateManager.translate(0.0, -height, width + 0.1);
				GlStateManager.disableLighting();
				GlStateManager.enableAlpha();
				GlStateManager.color(0, 0, 0, 0);
				
				if (perspective == 0)			
					Minecraft.getMinecraft().getRenderManager().renderEntityStatic(entity, 0.0f, false);
				
				GlStateManager.enableLighting();
				GlStateManager.disableAlpha();
				GlStateManager.scale(1, 1, 1);
				GlStateManager.popMatrix();

				if (perspective == 0)
					event.setCanceled(true);
			}
		}
		else
		{
			event.setCanceled(false);
			Minecraft mc = Minecraft.getMinecraft();
			RenderManager manager = mc.getRenderManager();
			RenderPlayer renderPlayer = manager.getSkinMap().get(aplayer.getSkinType());
			ModelPlayer modelPlayer = renderPlayer.getMainModel();
			modelPlayer.bipedLeftArm.isHidden = false;
			modelPlayer.bipedRightArm.isHidden = false;
		}
	}

	/*
	 * Renders the Block in Third Person
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPost(RenderPlayerEvent.Post event)
	{
		World world = Minecraft.getMinecraft().world;
		EntityPlayer player = event.getEntityPlayer();
		ModelPlayer modelPlayer = event.getRenderer().getMainModel();
		EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;
		ItemStack stack = player.getHeldItemMainhand();
		float partialticks = event.getPartialRenderTick();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
		{
			Entity entity = ItemEntity.getEntity(stack, world);

			float rotation = -player.renderYawOffset;
			int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;

			if (entity != null)
			{
				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialticks;
				double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialticks;
				double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialticks;

				double c0 = clientPlayer.lastTickPosX + (clientPlayer.posX - clientPlayer.lastTickPosX) * (double) partialticks;
				double c1 = clientPlayer.lastTickPosY + (clientPlayer.posY - clientPlayer.lastTickPosY) * (double) partialticks;
				double c2 = clientPlayer.lastTickPosZ + (clientPlayer.posZ - clientPlayer.lastTickPosZ) * (double) partialticks;
				
				double xOffset = d0 - c0;
				double yOffset = d1 - c1;
				double zOffset = d2 - d2;
				
				float height = entity.height;
				float width = entity.width;
				float multiplier = height * width;
					
				entity.setPosition(d0, d1, d2);
				entity.rotationYaw = 0.0f;
				entity.prevRotationYaw = 0.0f;
				entity.setRotationYawHead(0.0f);
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(xOffset, yOffset, zOffset);
				GlStateManager.scale((10 - multiplier) * 0.08, (10 - multiplier) * 0.08, (10 - multiplier) * 0.08);
				GlStateManager.rotate(rotation, 0, 1f, 0);
				GlStateManager.translate(0.0, (height / 2) + -(height / 2) + 1, (width - 0.1) < 0.7 ? (width - 0.1) + (0.7 - (width - 0.1)) : (width - 0.1));

				if (player.isSneaking())
					GlStateManager.translate(0, -0.3, 0);

				Minecraft.getMinecraft().getRenderManager().renderEntityStatic(entity, 0.0f, false);

				GlStateManager.scale(1, 1, 1);
				GlStateManager.popMatrix();
			}
		}
		else
		{
			modelPlayer.bipedLeftArm.isHidden = false;
			modelPlayer.bipedRightArm.isHidden = false;
		}

	}

	/*
	 * Renders correct arm rotation
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPre(RenderPlayerEvent.Pre event)
	{
		EntityPlayer player = event.getEntityPlayer();
		AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
		ItemStack stack = player.getHeldItemMainhand();
		ModelPlayer model = event.getRenderer().getMainModel();
		EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;

		ResourceLocation skinLoc = DefaultPlayerSkin.getDefaultSkin(player.getPersistentID());

		ModelRenderer fakeLeftArm = new ModelRenderer(model, 32, 48);
		ModelRenderer fakeRightArm = new ModelRenderer(model, 40, 16);

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
		{
			if (model.bipedBody.childModels != null && !model.bipedBody.childModels.isEmpty())
				model.bipedBody.childModels.clear();

			model.bipedLeftArm.isHidden = true;
			model.bipedRightArm.isHidden = true;

			Minecraft.getMinecraft().getTextureManager().bindTexture(skinLoc);
			float rotation = -player.renderYawOffset;
			if (aplayer.getSkinType().equals("default"))
			{
				fakeLeftArm.addBox(model.bipedLeftArm.offsetX + 4.2F, model.bipedLeftArm.offsetY, model.bipedLeftArm.offsetZ, 4, 12, 4, .08F);
			}
			else
			{
				fakeLeftArm.addBox(model.bipedLeftArm.offsetX + 4.2F, model.bipedLeftArm.offsetY, model.bipedLeftArm.offsetZ, 3, 12, 4, .08F);
			}

			if (aplayer.getSkinType().equals("default"))
			{
				fakeRightArm.addBox(model.bipedRightArm.offsetX - 7.9F, model.bipedRightArm.offsetY, model.bipedRightArm.offsetZ, 4, 12, 4, .08F);
			}
			else
			{
				fakeRightArm.addBox(model.bipedRightArm.offsetX - 7.2F, model.bipedRightArm.offsetY, model.bipedRightArm.offsetZ, 3, 12, 4, .08F);
			}

			if (!player.isSneaking())
			{
				fakeRightArm.rotateAngleX = -1.2F;
				fakeLeftArm.rotateAngleX = -1.2F;
			}
			else
			{
				fakeRightArm.rotateAngleX = -1.7F;
				fakeLeftArm.rotateAngleX = -1.7F;
			}
			
			fakeRightArm.rotateAngleY = -0.15f;
			fakeLeftArm.rotateAngleY = 0.15f;

			model.bipedBody.addChild(fakeLeftArm);
			model.bipedBody.addChild(fakeRightArm);

		}
		else
		{
			model.bipedLeftArm.isHidden = false;
			model.bipedRightArm.isHidden = false;
			if (model.bipedBody.childModels != null && !model.bipedBody.childModels.isEmpty())
			{
				model.bipedBody.childModels.clear();
			}
		}

		if (stack.isEmpty() || stack.getItem() != RegistrationHandler.itemEntity || !ItemEntity.hasEntityData(stack))
		{
			model.bipedLeftArm.isHidden = false;
			model.bipedRightArm.isHidden = false;
		}

	}

	@SideOnly(Side.CLIENT)
	private static RenderPlayer getRenderPlayer(AbstractClientPlayer player)
	{
		Minecraft mc = Minecraft.getMinecraft();
		RenderManager manager = mc.getRenderManager();
		return manager.getSkinMap().get(player.getSkinType());
	}

	@SideOnly(Side.CLIENT)
	private static ModelPlayer getPlayerModel(AbstractClientPlayer player)
	{
		return getRenderPlayer(player).getMainModel();
	}

}
