package tschipp.carryon.client.event;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.helper.ScriptParseHelper;
import tschipp.carryon.common.helper.StringParser;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;
import tschipp.carryon.network.server.SyncKeybindPacket;

public class RenderEvents
{
	private static boolean initModels;

	/*
	 * Prevents the Player from scrolling
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onScroll(MouseEvent event)
	{
		if (event.getDwheel() > 0 || event.getDwheel() < 0 || Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isPressed())
		{
			ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();

			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile)
			{
				if (ItemTile.hasTileData(stack))
				{
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerTick(PlayerTickEvent event) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		EntityPlayer player = event.player;

		if (player != null && event.side == Side.CLIENT)
		{
			boolean keyPressed = CarryOnKeybinds.carryKey.isKeyDown();
			boolean playerKeyPressed = CarryOnKeybinds.isKeyPressed(player);

			if (keyPressed && !playerKeyPressed)
			{
				CarryOnKeybinds.setKeyPressed(player, true);
				CarryOn.network.sendToServer(new SyncKeybindPacket(true));
			}
			else if (!keyPressed && playerKeyPressed)
			{
				CarryOnKeybinds.setKeyPressed(player, false);
				CarryOn.network.sendToServer(new SyncKeybindPacket(false));
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntity();
			if (player.world.isRemote)
			{
				CarryOnKeybinds.setKeyPressed(player, false);
				CarryOn.network.sendToServer(new SyncKeybindPacket(false));
			}
		}
	}

	/*
	 * Prevents the Player from opening Guis
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiInit(InitGuiEvent.Pre event)
	{
		if (event.getGui() != null)
		{
			boolean inventory = event.getGui() instanceof GuiContainer;
			EntityPlayer player = Minecraft.getMinecraft().player;

			if (player != null && inventory)
			{
				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);

				if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
				{
					Minecraft.getMinecraft().player.closeScreen();
					Minecraft.getMinecraft().currentScreen = null;
					Minecraft.getMinecraft().setIngameFocus();

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
		Field field = KeyBinding.class.getDeclaredFields()[8];
		field.setAccessible(true);
		ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
		EntityPlayer player = Minecraft.getMinecraft().player;

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
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
	 * Renders the Block in First Person
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
		boolean f1 = Minecraft.getMinecraft().gameSettings.hideGUI;

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack) && perspective == 0 && !f1)
		{
			if (Loader.isModLoaded("realrender") || Loader.isModLoaded("rfpr"))
				return;

			Block block = ItemTile.getBlock(stack);
			NBTTagCompound tag = ItemTile.getTileData(stack);
			IBlockState state = ItemTile.getBlockState(stack);
			ItemStack tileStack = ItemTile.getItemStack(stack);

			GlStateManager.pushMatrix();
			GlStateManager.scale(2.5, 2.5, 2.5);
			GlStateManager.translate(0, -0.6, -1);
			GlStateManager.enableBlend();

			if (CarryOnConfig.settings.facePlayer ? !isChest(block) : isChest(block))
			{
				GlStateManager.rotate(180, 0, 1f, 0);
				GlStateManager.rotate(-8, 1f, 0, 0);
			}
			else
			{
				GlStateManager.rotate(8, 1f, 0, 0);
			}

			IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(tileStack, world, player);

			CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
			if (carryOverride != null)
			{
				double[] translation = ScriptParseHelper.getXYZArray(carryOverride.getRenderTranslation());
				double[] rotation = ScriptParseHelper.getXYZArray(carryOverride.getRenderRotation());
				double[] scale = ScriptParseHelper.getScale(carryOverride.getRenderScale());
				Block b = StringParser.getBlock(carryOverride.getRenderNameBlock());
				if (b != null)
				{
					ItemStack s = new ItemStack(b, 1, carryOverride.getRenderMeta());
					s.setTagCompound(carryOverride.getRenderNBT());
					model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(s, world, player);
				}

				GlStateManager.translate(translation[0], translation[1], translation[2]);
				GlStateManager.rotate((float) rotation[0], 1, 0, 0);
				GlStateManager.rotate((float) rotation[1], 0, 1, 0);
				GlStateManager.rotate((float) rotation[2], 0, 0, 1);
				GlStateManager.scale(scale[0], scale[1], scale[2]);

			}

			int i = this.getBrightnessForRender(Minecraft.getMinecraft().player);
			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.setLightmapDisabled(false);

			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			if (ModelOverridesHandler.hasCustomOverrideModel(state, tag))
			{
				Object override = ModelOverridesHandler.getOverrideObject(state, tag);

				if (override instanceof ItemStack)
				{

					Minecraft.getMinecraft().getRenderItem().renderItem((ItemStack) override, model);
				}
				else
				{
					Minecraft.getMinecraft().getRenderItem().renderItem(tileStack.isEmpty() ? stack : tileStack, model);
				}
			}
			else
			{
				Minecraft.getMinecraft().getRenderItem().renderItem(tileStack.isEmpty() ? stack : tileStack, model);
			}

			this.setLightmapDisabled(true);

			if (perspective == 0)
			{
				event.setCanceled(true);
			}

			GlStateManager.disableBlend();
			GlStateManager.scale(1, 1, 1);
			GlStateManager.popMatrix();

		}
	}

	@SideOnly(Side.CLIENT)
	private int getBrightnessForRender(EntityPlayer player)
	{
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor(player.posX), 0, MathHelper.floor(player.posZ));

		if (player.world.isBlockLoaded(blockpos$mutableblockpos))
		{
			blockpos$mutableblockpos.setY(MathHelper.floor(player.posY + player.getEyeHeight()));
			return player.world.getCombinedLight(blockpos$mutableblockpos, 0);
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
		World world = Minecraft.getMinecraft().world;
		EntityPlayer player = event.getEntityPlayer();
		EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;
		ItemStack stack = player.getHeldItemMainhand();
		float partialticks = event.getPartialRenderTick();

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
		{
			Block block = ItemTile.getBlock(stack);
			IBlockState state = ItemTile.getBlockState(stack);
			NBTTagCompound tag = ItemTile.getTileData(stack);
			ItemStack tileItem = ItemTile.getItemStack(stack);

			float rotation = 0f;

			if (player.isRiding() && player.getRidingEntity() instanceof EntityLivingBase)
				rotation = -(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialticks);
			else
				rotation = -(player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialticks);

			double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialticks;
			double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialticks;
			double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialticks;

			double c0 = clientPlayer.lastTickPosX + (clientPlayer.posX - clientPlayer.lastTickPosX) * partialticks;
			double c1 = clientPlayer.lastTickPosY + (clientPlayer.posY - clientPlayer.lastTickPosY) * partialticks;
			double c2 = clientPlayer.lastTickPosZ + (clientPlayer.posZ - clientPlayer.lastTickPosZ) * partialticks;

			double xOffset = d0 - c0;
			double yOffset = d1 - c1;
			double zOffset = d2 - c2;

			GlStateManager.pushMatrix();
			GlStateManager.translate(xOffset, yOffset, zOffset);
			GlStateManager.scale(0.6, 0.6, 0.6);
			GlStateManager.enableBlend();

			if (CarryOnConfig.settings.facePlayer ? !isChest(block) : isChest(block))
			{
				GlStateManager.rotate(rotation, 0, 1.0f, 0);
				GlStateManager.translate(0, 1.6, 0.65);
				if ((Loader.isModLoaded("realrender") || Loader.isModLoaded("rfpr")) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
					GlStateManager.translate(0, 0, -0.4);
			}
			else
			{
				GlStateManager.rotate(rotation + 180, 0, 1.0f, 0);
				GlStateManager.translate(0, 1.6, -0.65);
				if ((Loader.isModLoaded("realrender") || Loader.isModLoaded("rfpr")) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
					GlStateManager.translate(0, 0, 0.4);
			}

			if (player.isSneaking())
			{
				GlStateManager.translate(0, -0.3, 0);
			}

			IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(tileItem, world, player);

			CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
			if (carryOverride != null)
			{
				double[] translation = ScriptParseHelper.getXYZArray(carryOverride.getRenderTranslation());
				double[] rot = ScriptParseHelper.getXYZArray(carryOverride.getRenderRotation());
				double[] scale = ScriptParseHelper.getScale(carryOverride.getRenderScale());
				Block b = StringParser.getBlock(carryOverride.getRenderNameBlock());
				if (b != null)
				{
					ItemStack s = new ItemStack(b, 1, carryOverride.getRenderMeta());
					s.setTagCompound(carryOverride.getRenderNBT());
					model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(s, world, player);
				}

				GlStateManager.translate(translation[0], translation[1], translation[2]);
				GlStateManager.rotate((float) rot[0], 1, 0, 0);
				GlStateManager.rotate((float) rot[1], 0, 1, 0);
				GlStateManager.rotate((float) rot[2], 0, 0, 1);
				GlStateManager.scale(scale[0], scale[1], scale[2]);

			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			if (ModelOverridesHandler.hasCustomOverrideModel(state, tag))
			{
				Object override = ModelOverridesHandler.getOverrideObject(state, tag);

				if (override instanceof ItemStack)
				{
					Minecraft.getMinecraft().getRenderItem().renderItem((ItemStack) override, model);
				}
				else
				{
					Minecraft.getMinecraft().getRenderItem().renderItem(tileItem.isEmpty() ? stack : tileItem, model);
				}
			}
			else
			{
				Minecraft.getMinecraft().getRenderItem().renderItem(tileItem.isEmpty() ? stack : tileItem, model);
			}

			GlStateManager.disableBlend();
			GlStateManager.scale(1, 1, 1);
			GlStateManager.popMatrix();
		}
	}

	/*
	 * Renders correct arm rotation
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEvent(RenderPlayerEvent.Post event)
	{
		if(!CarryOnConfig.settings.renderArms)
			return;
		
		if (handleMobends() && !Loader.isModLoaded("obfuscate"))
		{
			EntityPlayer player = event.getEntityPlayer();
			EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;
			float partialticks = event.getPartialRenderTick();

			RenderPlayer render = event.getRenderer();
			
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
			{
				ModelPlayer model = event.getRenderer().getMainModel();
				float rotation = 0;

				if (player.isRiding() && player.getRidingEntity() instanceof EntityLivingBase)
					rotation = (player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialticks);
				else
					rotation = (player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialticks);

				AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
				ResourceLocation skinLoc = aplayer.getLocationSkin();

				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialticks;
				double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialticks;
				double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialticks;

				double c0 = clientPlayer.lastTickPosX + (clientPlayer.posX - clientPlayer.lastTickPosX) * partialticks;
				double c1 = clientPlayer.lastTickPosY + (clientPlayer.posY - clientPlayer.lastTickPosY) * partialticks;
				double c2 = clientPlayer.lastTickPosZ + (clientPlayer.posZ - clientPlayer.lastTickPosZ) * partialticks;

				double xOffset = d0 - c0;
				double yOffset = d1 - c1;
				double zOffset = d2 - c2;

				GlStateManager.pushMatrix();
				GlStateManager.translate(xOffset, yOffset, zOffset);

				Minecraft.getMinecraft().getTextureManager().bindTexture(skinLoc);

				CarryOnOverride overrider = ScriptChecker.getOverride(player);
				if (overrider != null)
				{
					double[] rotLeft = null;
					double[] rotRight = null;
					if (overrider.getRenderRotationLeftArm() != null)
						rotLeft = ScriptParseHelper.getXYZArray(overrider.getRenderRotationLeftArm());
					if (overrider.getRenderRotationRightArm() != null)
						rotRight = ScriptParseHelper.getXYZArray(overrider.getRenderRotationRightArm());

					boolean renderRight = overrider.isRenderRightArm();
					boolean renderLeft = overrider.isRenderLeftArm();

					if (renderLeft && rotLeft != null)
					{
						renderArmPost(model.bipedLeftArm, (float) rotLeft[0], (float) rotLeft[2], rotation, false, player.isSneaking());
						renderArmPost(model.bipedLeftArmwear, (float) rotLeft[0], (float) rotLeft[2], rotation, false, player.isSneaking());
					}
					else if (renderLeft)
					{
						renderArmPost(model.bipedLeftArm, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation, false, player.isSneaking());
						renderArmPost(model.bipedLeftArmwear, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation, false, player.isSneaking());
					}

					if (renderRight && rotRight != null)
					{
						renderArmPost(model.bipedRightArm, (float) rotRight[0], (float) rotRight[2], rotation, true, player.isSneaking());
						renderArmPost(model.bipedRightArmwear, (float) rotRight[0], (float) rotRight[2], rotation, true, player.isSneaking());
					}
					else if (renderRight)
					{
						renderArmPost(model.bipedRightArm, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation, true, player.isSneaking());
						renderArmPost(model.bipedRightArmwear, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation, true, player.isSneaking());
					}

				}
				else
				{
					renderArmPost(model.bipedRightArm, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation, true, player.isSneaking());
					renderArmPost(model.bipedLeftArm, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation, false, player.isSneaking());
					renderArmPost(model.bipedLeftArmwear, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation, false, player.isSneaking());
					renderArmPost(model.bipedRightArmwear, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation, true, player.isSneaking());
				}
				GlStateManager.popMatrix();
			}
		}
	}

	/*
	 * Hides the vanilla arm for rendering the rotation
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onEvent(RenderPlayerEvent.Pre event)
	{
		if(!CarryOnConfig.settings.renderArms)
			return;
		
		if (handleMobends() && !Loader.isModLoaded("obfuscate"))
		{
			EntityPlayer player = event.getEntityPlayer();
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
			{
				ModelPlayer model = event.getRenderer().getMainModel();
				
				CarryOnOverride overrider = ScriptChecker.getOverride(player);
				if (overrider != null)
				{
					boolean renderRight = overrider.isRenderRightArm();
					boolean renderLeft = overrider.isRenderLeftArm();

					if (renderRight)
					{
						renderArmPre(model.bipedRightArm);
						renderArmPre(model.bipedRightArmwear);
						
						
					}

					if (renderLeft)
					{
						renderArmPre(model.bipedLeftArm);
						renderArmPre(model.bipedLeftArmwear);
					}
				}
				else
				{
					renderArmPre(model.bipedRightArm);
					renderArmPre(model.bipedLeftArm);
					renderArmPre(model.bipedLeftArmwear);
					renderArmPre(model.bipedRightArmwear);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void renderArmPost(ModelRenderer arm, float x, float z, float rotation, boolean right, boolean sneaking)
	{
		arm.isHidden = false;
		if (right)
		{
			arm.rotationPointZ = -MathHelper.sin((float) Math.toRadians(rotation)) * 4.75F;
			arm.rotationPointX = -MathHelper.cos((float) Math.toRadians(rotation)) * 4.75F;
		}
		else
		{
			arm.rotationPointZ = MathHelper.sin((float) Math.toRadians(rotation)) * 4.75F;
			arm.rotationPointX = MathHelper.cos((float) Math.toRadians(rotation)) * 4.75F;
		}

		if (!sneaking)
			arm.rotationPointY = 20;
		else
			arm.rotationPointY = 15;

		arm.rotateAngleX = (float) x;
		arm.rotateAngleY = (float) -Math.toRadians(rotation);
		arm.rotateAngleZ = (float) z;
		arm.renderWithRotation(0.0625F);
		arm.rotationPointY = 2;
	}

	@SideOnly(Side.CLIENT)
	public void renderArmPre(ModelRenderer arm)
	{
		arm.isHidden = true;
	}

	public boolean handleMobends()
	{
		if (Loader.isModLoaded("mobends"))
		{
			Configuration config = new Configuration(new File(CarryOn.CONFIGURATION_FILE.getPath().substring(0, CarryOn.CONFIGURATION_FILE.getPath().length() - 16), "mobends.cfg"));

			boolean renderPlayer = config.get("animated", "player", true).getBoolean();
			return !renderPlayer;
		}
		return true;
	}

	public static boolean isChest(Block block)
	{
		return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void hideItems(RenderPlayerEvent.Specials.Pre event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();

		if (stack != null && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
		{
			event.setRenderItem(false);
		}
	}
}
