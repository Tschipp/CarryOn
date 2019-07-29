package tschipp.carryon.client.event;

import java.lang.reflect.InvocationTargetException;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.helper.KeyboardCallbackWrapper.KeyPressedEvent;
import tschipp.carryon.common.helper.ScriptParseHelper;
import tschipp.carryon.common.helper.ScrollCallbackWrapper.MouseScrolledEvent;
import tschipp.carryon.common.helper.StringParser;
import tschipp.carryon.common.item.ItemCarryonBlock;
import tschipp.carryon.common.item.ItemCarryonEntity;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;
import tschipp.carryon.network.server.SyncKeybindPacket;

public class RenderEvents
{
	/*
	 * Prevents the Player from scrolling
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onScroll(MouseScrolledEvent event)
	{
		PlayerEntity player = Minecraft.getInstance().player;

		if (player != null)
		{
			ItemStack stack = player.getHeldItemMainhand();

			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
			{
				if (ItemCarryonBlock.hasTileData(stack) || ItemCarryonEntity.hasEntityData(stack))
				{
					event.setCanceled(true);
				}
			}
		}

	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerTick(PlayerTickEvent event) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		PlayerEntity player = event.player;

		if (player != null && event.side == LogicalSide.CLIENT)
		{
			boolean keyPressed = CarryOnKeybinds.carryKey.isKeyDown();
			boolean playerKeyPressed = CarryOnKeybinds.isKeyPressed(player);

			if (keyPressed && !playerKeyPressed)
			{
				CarryOnKeybinds.setKeyPressed(player, true);
				CarryOn.network.sendToServer(new SyncKeybindPacket(true));
			} else if (!keyPressed && playerKeyPressed)
			{
				CarryOnKeybinds.setKeyPressed(player, false);
				CarryOn.network.sendToServer(new SyncKeybindPacket(false));
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.getEntity() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) event.getEntity();
			if (player.world.isRemote)
			{
				CarryOnKeybinds.setKeyPressed(player, false);
				CarryOn.network.sendToServer(new SyncKeybindPacket(false));

				if (CarryOn.FINGERPRINT_VIOLATED)
				{
					StringTextComponent cf = new StringTextComponent(TextFormatting.AQUA + "Curseforge" + TextFormatting.RED);
					cf.getStyle().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://minecraft.curseforge.com/projects/carry-on"));

					player.sendMessage(new StringTextComponent(TextFormatting.RED + "[CarryOn] WARNING! Invalid fingerprint detected! The Carry On mod file may have been tampered with! If you didn't download the file from ").appendSibling(cf).appendText(TextFormatting.RED + " or through any kind of mod launcher, immediately delete the file and re-download it from ").appendSibling(cf));
				}
			}

		}
	}

	/*
	 * Prevents the Player from opening Guis
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onGuiInit(InitGuiEvent.Pre event)
	{
		if (event.getGui() != null)
		{
			boolean inventory = event.getGui() instanceof ContainerScreen;
			PlayerEntity player = Minecraft.getInstance().player;

			if (player != null && inventory)
			{
				ItemStack stack = player.getHeldItem(Hand.MAIN_HAND);

				if (!stack.isEmpty() && ((stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack)) || (stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))))
				{
					Minecraft.getInstance().player.closeScreen();
					Minecraft.getInstance().currentScreen = null;
					Minecraft.getInstance().mouseHelper.grabMouse();

				}

			}
		}
	}

	/*
	 * Prevents the Player from switching Slots
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void inputEvent(KeyPressedEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		GameSettings settings = Minecraft.getInstance().gameSettings;
		int key = event.key;
		int scancode = event.scancode;
		PlayerEntity player = Minecraft.getInstance().player;

		if (player != null)
		{
			ItemStack stack = Minecraft.getInstance().player.getHeldItemMainhand();

			if (!stack.isEmpty() && ((stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack)) || (stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))))
			{
				if (settings.keyBindDrop.matchesKey(key, scancode))
				{
					event.setCanceled(true);
				}
				if (settings.keyBindSwapHands.matchesKey(key, scancode))
				{
					event.setCanceled(true);
				}
				if (settings.keyBindPickBlock.matchesKey(key, scancode))
				{
					event.setCanceled(true);
				}
				for (KeyBinding keyBind : settings.keyBindsHotbar)
				{
					if (keyBind.matchesKey(key, scancode))
					{
						event.setCanceled(true);
					}
				}
			}

			int current = player.inventory.currentItem;

			if (player.getEntityData().contains("carrySlot") ? player.getEntityData().getInt("carrySlot") != current : false)
			{
				player.inventory.currentItem = player.getEntityData().getInt("carrySlot");
			}
		}
	}

	/*
	 * Renders the Block in First Person
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{
		World world = Minecraft.getInstance().world;
		PlayerEntity player = Minecraft.getInstance().player;
		ItemStack stack = player.getHeldItemMainhand();
		int perspective = Minecraft.getInstance().gameSettings.thirdPersonView;
		boolean f1 = Minecraft.getInstance().gameSettings.hideGUI;

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) && perspective == 0 && !f1)
		{
			if (ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr"))
				return;

			Block block = ItemCarryonBlock.getBlock(stack);
			CompoundNBT tag = ItemCarryonBlock.getTileData(stack);
			BlockState state = ItemCarryonBlock.getBlockState(stack);
			ItemStack tileStack = ItemCarryonBlock.getItemStack(stack);

			GlStateManager.pushMatrix();
			GlStateManager.scaled(2.5, 2.5, 2.5);
			GlStateManager.translated(0, -0.6, -1);
			GlStateManager.enableBlend();

			if (Settings.facePlayer.get() ? !isChest(block) : isChest(block))
			{
				GlStateManager.rotatef(180, 0, 1f, 0);
				GlStateManager.rotatef(-8, 1f, 0, 0);
			} else
			{
				GlStateManager.rotatef(8, 1f, 0, 0);
			}

			IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : (tileStack.isEmpty() ? Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state) : Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(tileStack, world, player));

			CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
			if (carryOverride != null)
			{
				double[] translation = ScriptParseHelper.getXYZArray(carryOverride.getRenderTranslation());
				double[] rotation = ScriptParseHelper.getXYZArray(carryOverride.getRenderRotation());
				double[] scaled = ScriptParseHelper.getscaled(carryOverride.getRenderscaled());
				Block b = StringParser.getBlock(carryOverride.getRenderNameBlock());
				if (b != null)
				{
					ItemStack s = new ItemStack(b, 1);
					s.setTag(carryOverride.getRenderNBT());
					model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(s, world, player);
				}

				GlStateManager.translated(translation[0], translation[1], translation[2]);
				GlStateManager.rotatef((float) rotation[0], 1, 0, 0);
				GlStateManager.rotatef((float) rotation[1], 0, 1, 0);
				GlStateManager.rotatef((float) rotation[2], 0, 0, 1);
				GlStateManager.scaled(scaled[0], scaled[1], scaled[2]);

			}

			int i = this.getBrightnessForRender(Minecraft.getInstance().player);
			int j = i % 65536;
			int k = i / 65536;
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, j, k);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.setLightmapDisabled(false);

			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

			if (ModelOverridesHandler.hasCustomOverrideModel(state, tag))
			{
				Object override = ModelOverridesHandler.getOverrideObject(state, tag);

				if (override instanceof ItemStack)
				{

					Minecraft.getInstance().getItemRenderer().renderItem((ItemStack) override, model);
				} else
				{
					Minecraft.getInstance().getItemRenderer().renderItem(tileStack.isEmpty() ? stack : tileStack, model);
				}
			} else
			{
				Minecraft.getInstance().getItemRenderer().renderItem(tileStack.isEmpty() ? stack : tileStack, model);
			}

			this.setLightmapDisabled(true);

			if (perspective == 0)
			{
				event.setCanceled(true);
			}

			GlStateManager.disableBlend();
			GlStateManager.scaled(1, 1, 1);
			GlStateManager.popMatrix();

		}
	}

	@SuppressWarnings("deprecation")
	@OnlyIn(Dist.CLIENT)
	private int getBrightnessForRender(PlayerEntity player)
	{
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor(player.posX), 0, MathHelper.floor(player.posZ));

		if (player.world.isBlockLoaded(blockpos$mutableblockpos))
		{
			blockpos$mutableblockpos.setY(MathHelper.floor(player.posY + player.getEyeHeight()));
			return player.world.getCombinedLight(blockpos$mutableblockpos, 0);
		} else
		{
			return 0;
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void setLightmapDisabled(boolean disabled)
	{
		GlStateManager.activeTexture(GLX.GL_TEXTURE1);

		if (disabled)
		{
			GlStateManager.disableTexture();;
		} else
		{
			GlStateManager.enableTexture();
		}

		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
	}

	/*
	 * Renders the Block in Third Person
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPost(RenderPlayerEvent.Post event)
	{
		World world = Minecraft.getInstance().world;
		PlayerEntity player = event.getEntityPlayer();
		ClientPlayerEntity clientPlayer = Minecraft.getInstance().player;
		ItemStack stack = player.getHeldItemMainhand();
		float partialticks = event.getPartialRenderTick();

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack))
		{
			Block block = ItemCarryonBlock.getBlock(stack);
			BlockState state = ItemCarryonBlock.getBlockState(stack);
			CompoundNBT tag = ItemCarryonBlock.getTileData(stack);
			ItemStack tileItem = ItemCarryonBlock.getItemStack(stack);

			float rotation = 0f;

			if (player.getRidingEntity() != null && player.getRidingEntity() instanceof LivingEntity)
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
			GlStateManager.translated(xOffset, yOffset, zOffset);
			GlStateManager.scaled(0.6, 0.6, 0.6);
			GlStateManager.enableBlend();

			if (Settings.facePlayer.get() ? !isChest(block) : isChest(block))
			{
				GlStateManager.rotatef(rotation, 0, 1.0f, 0);
				GlStateManager.translated(0, 1.6, 0.65);
				if ((ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr")) && Minecraft.getInstance().gameSettings.thirdPersonView == 0)
					GlStateManager.translated(0, 0, -0.4);
			} else
			{
				GlStateManager.rotatef(rotation + 180, 0, 1.0f, 0);
				GlStateManager.translated(0, 1.6, -0.65);
				if ((ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr")) && Minecraft.getInstance().gameSettings.thirdPersonView == 0)
					GlStateManager.translated(0, 0, 0.4);
			}

			if (player.isSneaking())
			{
				GlStateManager.translated(0, -0.3, 0);
			}

			IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : (tileItem.isEmpty() ? Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state) : Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(tileItem, world, player));

			CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
			if (carryOverride != null)
			{
				double[] translation = ScriptParseHelper.getXYZArray(carryOverride.getRenderTranslation());
				double[] rot = ScriptParseHelper.getXYZArray(carryOverride.getRenderRotation());
				double[] scaled = ScriptParseHelper.getscaled(carryOverride.getRenderscaled());
				Block b = StringParser.getBlock(carryOverride.getRenderNameBlock());
				if (b != null)
				{
					ItemStack s = new ItemStack(b, 1);
					s.setTag(carryOverride.getRenderNBT());
					model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(s, world, player);
				}

				GlStateManager.translated(translation[0], translation[1], translation[2]);
				GlStateManager.rotatef((float) rot[0], 1, 0, 0);
				GlStateManager.rotatef((float) rot[1], 0, 1, 0);
				GlStateManager.rotatef((float) rot[2], 0, 0, 1);
				GlStateManager.scaled(scaled[0], scaled[1], scaled[2]);

			}

			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

			if (ModelOverridesHandler.hasCustomOverrideModel(state, tag))
			{
				Object override = ModelOverridesHandler.getOverrideObject(state, tag);

				if (override instanceof ItemStack)
				{
					Minecraft.getInstance().getItemRenderer().renderItem((ItemStack) override, model);
				} else
				{
					Minecraft.getInstance().getItemRenderer().renderItem(tileItem.isEmpty() ? stack : tileItem, model);
				}
			} else
			{
				Minecraft.getInstance().getItemRenderer().renderItem(tileItem.isEmpty() ? stack : tileItem, model);
			}

			GlStateManager.disableBlend();
			GlStateManager.scaled(1, 1, 1);
			GlStateManager.popMatrix();
		}
	}

	/*
	 * Renders correct arm rotation
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEvent(RenderPlayerEvent.Post event)
	{
		if (!Settings.renderArms.get())
			return;

		if (handleMobends() && !ModList.get().isLoaded("obfuscate"))
		{
			PlayerEntity player = event.getEntityPlayer();
			ClientPlayerEntity clientPlayer = Minecraft.getInstance().player;
			float partialticks = event.getPartialRenderTick();

			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))
			{
				PlayerModel<AbstractClientPlayerEntity> model = event.getRenderer().getEntityModel();
				float rotation = 0;

				if (player.getRidingEntity() != null && player.getRidingEntity() instanceof LivingEntity)
					rotation = (player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialticks);
				else
					rotation = (player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialticks);

				AbstractClientPlayerEntity aplayer = (AbstractClientPlayerEntity) player;
				ResourceLocation skinLoc = aplayer.getLocationSkin();

				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialticks;
				double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialticks;
				double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialticks;

				double c0 = clientPlayer.lastTickPosX + (clientPlayer.posX - clientPlayer.lastTickPosX) * partialticks;
				double c1 = clientPlayer.lastTickPosY + (clientPlayer.posY - clientPlayer.lastTickPosY) * partialticks;
				double c2 = clientPlayer.lastTickPosZ + (clientPlayer.posZ - clientPlayer.lastTickPosZ) * partialticks;

				Vec3d cameraPos =  Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
				
				double xOffset = d0 - cameraPos.getX();
				double yOffset = d1 - cameraPos.getY();
				double zOffset = d2 - cameraPos.getZ();

				GlStateManager.pushMatrix();
				GlStateManager.translated(xOffset, yOffset, zOffset);

				Minecraft.getInstance().getTextureManager().bindTexture(skinLoc);

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
					} else if (renderLeft)
					{
						renderArmPost(model.bipedLeftArm, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation, false, player.isSneaking());
						renderArmPost(model.bipedLeftArmwear, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation, false, player.isSneaking());
					}

					if (renderRight && rotRight != null)
					{
						renderArmPost(model.bipedRightArm, (float) rotRight[0], (float) rotRight[2], rotation, true, player.isSneaking());
						renderArmPost(model.bipedRightArmwear, (float) rotRight[0], (float) rotRight[2], rotation, true, player.isSneaking());
					} else if (renderRight)
					{
						renderArmPost(model.bipedRightArm, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation, true, player.isSneaking());
						renderArmPost(model.bipedRightArmwear, 2.0F + (player.isSneaking() ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation, true, player.isSneaking());
					}

				} else
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
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onEvent(RenderPlayerEvent.Pre event)
	{
		if (!Settings.renderArms.get())
			return;

		if (handleMobends() && !ModList.get().isLoaded("obfuscate"))
		{
			PlayerEntity player = event.getEntityPlayer();
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))
			{
				PlayerModel<AbstractClientPlayerEntity> model = event.getRenderer().getEntityModel();

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
				} else
				{
					renderArmPre(model.bipedRightArm);
					renderArmPre(model.bipedLeftArm);
					renderArmPre(model.bipedLeftArmwear);
					renderArmPre(model.bipedRightArmwear);
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void renderArmPost(RendererModel arm, float x, float z, float rotation, boolean right, boolean sneaking)
	{
		arm.isHidden = false;
		if (right)
		{
			arm.rotationPointZ = -MathHelper.sin((float) Math.toRadians(rotation)) * 4.75F;
			arm.rotationPointX = -MathHelper.cos((float) Math.toRadians(rotation)) * 4.75F;
		} else
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

	@OnlyIn(Dist.CLIENT)
	public void renderArmPre(RendererModel arm)
	{
		arm.isHidden = true;
	}

	public boolean handleMobends()
	{
		// TODO MOBENDS
		// if (ModList.get().isLoaded("mobends"))
		// {
		// Configuration config = new Configuration(new
		// File(CarryOn.CONFIGURATION_FILE.getPath().substring(0,
		// CarryOn.CONFIGURATION_FILE.getPath().length() - 16), "mobends.cfg"));
		//
		// boolean renderPlayer = config.get("animated", "player",
		// true).getBoolean();
		// return !renderPlayer;
		// }
		return true;
	}

	public static boolean isChest(Block block)
	{
		return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
	}

	@OnlyIn(Dist.CLIENT)
	private static PlayerRenderer getRenderPlayer(AbstractClientPlayerEntity player)
	{
		Minecraft mc = Minecraft.getInstance();
		EntityRendererManager manager = mc.getRenderManager();
		return manager.getSkinMap().get(player.getSkinType());
	}

	@OnlyIn(Dist.CLIENT)
	private static PlayerModel<AbstractClientPlayerEntity> getPlayerModel(AbstractClientPlayerEntity player)
	{
		return getRenderPlayer(player).getEntityModel();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void hideItems(RenderSpecificHandEvent event)
	{
		ItemStack stack = event.getItemStack();

		if (stack != null && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
		{
			event.setCanceled(true);
		}
	}
}
