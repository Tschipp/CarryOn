package tschipp.carryon.client.event;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.helper.CarryRenderHelper;
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
			}
			else if (!keyPressed && playerKeyPressed)
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

					player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "[CarryOn] WARNING! Invalid fingerprint detected! The Carry On mod file may have been tampered with! If you didn't download the file from ").append(cf).appendString(TextFormatting.RED + " or through any kind of mod launcher, immediately delete the file and re-download it from ").append(cf), false);
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

			if (player.getPersistentData().contains("carrySlot") ? player.getPersistentData().getInt("carrySlot") != current : false)
			{
				player.inventory.currentItem = player.getPersistentData().getInt("carrySlot");
			}
		}
	}

	/*
	 * Renders the Block in First Person
	 */
	@SuppressWarnings("deprecation")
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{
		World world = Minecraft.getInstance().world;
		PlayerEntity player = Minecraft.getInstance().player;
		ItemStack stack = player.getHeldItemMainhand();
		int perspective = CarryRenderHelper.getPerspective();
		boolean f1 = Minecraft.getInstance().gameSettings.hideGUI;
		IRenderTypeBuffer buffer = event.getBuffers();
		MatrixStack matrix = event.getMatrixStack();
		int light = event.getLight();

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) && perspective == 0 && !f1)
		{
			if (ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr"))
				return;

			Block block = ItemCarryonBlock.getBlock(stack);
			CompoundNBT tag = ItemCarryonBlock.getTileData(stack);
			BlockState state = ItemCarryonBlock.getBlockState(stack);
			ItemStack tileStack = ItemCarryonBlock.getItemStack(stack);

			matrix.push();
			matrix.scale(2.5f, 2.5f, 2.5f);
			matrix.translate(0, -0.5, -1);
			RenderSystem.enableBlend();
			RenderSystem.disableCull();

			if (Settings.facePlayer.get() ? !isChest(block) : isChest(block))
			{
				matrix.rotate(Vector3f.YP.rotationDegrees(180));
				matrix.rotate(Vector3f.XN.rotationDegrees(8));
			}
			else
			{
				matrix.rotate(Vector3f.XP.rotationDegrees(8));
			}

			IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : (tileStack.isEmpty() ? Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state) : Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(tileStack, world, player));

			CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
			if (carryOverride != null)
			{
				CarryRenderHelper.performOverrideTransformation(matrix, carryOverride);

				if (!carryOverride.getRenderNameBlock().isEmpty())
				{
					Block b = StringParser.getBlock(carryOverride.getRenderNameBlock());
					if (b != null)
					{
						ItemStack s = new ItemStack(b, 1);
						s.setTag(carryOverride.getRenderNBT());
						model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(s, world, player);
					}
				}
			}

			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

			CarryRenderHelper.renderItem(state, tag, stack, tileStack, matrix, buffer, light, model);

			if (perspective == 0)
			{
				event.setCanceled(true);
			}

			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			matrix.pop();
		}
	}

	/*
	 * Render blocks and entities in third person
	 */
	@SuppressWarnings("deprecation")
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onRenderWorld(RenderWorldLastEvent event)
	{
		World world = Minecraft.getInstance().world;
		float partialticks = event.getPartialTicks();
		Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		MatrixStack matrix = event.getMatrixStack();
		int light = 0;
		int perspective = CarryRenderHelper.getPerspective();
		EntityRendererManager manager = Minecraft.getInstance().getRenderManager();

		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();

		for (PlayerEntity player : world.getPlayers())
		{
			if (perspective == 0 && player == Minecraft.getInstance().player)
				continue;

			light = Minecraft.getInstance().getRenderManager().getPackedLight(player, partialticks);
			ItemStack stack = player.getHeldItemMainhand();

			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack))
			{
				Block block = ItemCarryonBlock.getBlock(stack);
				BlockState state = ItemCarryonBlock.getBlockState(stack);
				CompoundNBT tag = ItemCarryonBlock.getTileData(stack);
				ItemStack tileItem = ItemCarryonBlock.getItemStack(stack);

				applyBlockTransformations(player, partialticks, matrix, block);

				IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : (tileItem.isEmpty() ? Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state) : Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(tileItem, world, player));

				CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
				if (carryOverride != null)
				{
					CarryRenderHelper.performOverrideTransformation(matrix, carryOverride);

					if (!carryOverride.getRenderNameBlock().isEmpty())
					{
						Block b = StringParser.getBlock(carryOverride.getRenderNameBlock());
						if (b != null)
						{
							ItemStack s = new ItemStack(b, 1);
							s.setTag(carryOverride.getRenderNBT());
							model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(s, world, player);
						}
					}
				}

				Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
				CarryRenderHelper.renderItem(state, tag, stack, tileItem, matrix, buffer, light, model);
				buffer.finish();

				matrix.pop();

				drawArms(player, partialticks, matrix, buffer, light);

				matrix.pop();
			}
			else if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))
			{
				Entity entity = ItemCarryonEntity.getEntity(stack, world);

				if (entity != null)
				{
					applyEntityTransformations(player, partialticks, matrix, entity);

					manager.setRenderShadow(false);

					CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
					if (carryOverride != null)
					{
						CarryRenderHelper.performOverrideTransformation(matrix, carryOverride);

						String entityname = carryOverride.getRenderNameEntity();
						if (entityname != null)
						{
							Entity newEntity = null;

							Optional<EntityType<?>> type = EntityType.byKey(entityname);
							if (type.isPresent())
								newEntity = type.get().create(world);

							if (newEntity != null)
							{
								CompoundNBT nbttag = carryOverride.getRenderNBT();
								if (nbttag != null)
									newEntity.deserializeNBT(nbttag);
								entity = newEntity;
								entity.rotationYaw = 0.0f;
								entity.prevRotationYaw = 0.0f;
								entity.setRotationYawHead(0.0f);
								entity.rotationPitch = 0.0f;
								entity.prevRotationPitch = 0.0f;
							}
						}
					}

					if (entity instanceof LivingEntity)
						((LivingEntity) entity).hurtTime = 0;

					manager.renderEntityStatic(entity, 0, 0, 0, 0f, 0, matrix, buffer, light);
					buffer.finish();

					matrix.pop();

					drawArms(player, partialticks, matrix, buffer, light);

					manager.setRenderShadow(true);

					matrix.pop();
				}
			}

		}
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private void applyGeneralTransformations(PlayerEntity player, float partialticks, MatrixStack matrix)
	{
		int perspective = CarryRenderHelper.getPerspective();
		Quaternion playerrot = CarryRenderHelper.getExactBodyRotation(player, partialticks);
		Vector3d playerpos = CarryRenderHelper.getExactPos(player, partialticks);
		Vector3d cameraPos = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		Vector3d offset = playerpos.subtract(cameraPos);
		Pose pose = player.getPose();

		matrix.push();
		matrix.translate(offset.x, offset.y, offset.z);

		if (perspective == 2)
			playerrot.multiply(Vector3f.YP.rotationDegrees(180));
		matrix.rotate(playerrot);

		matrix.push();
		matrix.scale(0.6f, 0.6f, 0.6f);

		if (perspective == 2)
			matrix.translate(0, 0, -1.35);

		if (doSneakCheck(player))
		{
			matrix.translate(0, -0.4, 0);
		}

		if (pose == Pose.SWIMMING)
		{
			float f = player.getSwimAnimation(partialticks);
			float f3 = player.isInWater() ? -90.0F - player.rotationPitch : -90.0F;
			float f4 = MathHelper.lerp(f, 0.0F, f3);
			if (perspective == 2)
			{
				matrix.translate(0, 0, 1.35);
				matrix.rotate(Vector3f.XP.rotationDegrees(f4));
			}
			else
				matrix.rotate(Vector3f.XN.rotationDegrees(f4));

			matrix.translate(0, -1.5, -1.848);
			if (perspective == 2)
				matrix.translate(0, 0, 2.38);
		}

		if (pose == Pose.FALL_FLYING)
		{
			float f1 = (float) player.getTicksElytraFlying() + partialticks;
			float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
			if (!player.isSpinAttacking())
			{
				if (perspective == 2)
					matrix.translate(0, 0, 1.35);

				if (perspective == 2)
					matrix.rotate(Vector3f.XP.rotationDegrees(f2 * (-90.0F - player.rotationPitch)));
				else
					matrix.rotate(Vector3f.XN.rotationDegrees(f2 * (-90.0F - player.rotationPitch)));
			}

			Vector3d Vector3d = player.getLook(partialticks);
			Vector3d Vector3d1 = player.getMotion();
			double d0 = Entity.horizontalMag(Vector3d1);
			double d1 = Entity.horizontalMag(Vector3d);
			if (d0 > 0.0D && d1 > 0.0D)
			{
				double d2 = (Vector3d1.x * Vector3d.x + Vector3d1.z * Vector3d.z) / (Math.sqrt(d0) * Math.sqrt(d1));
				double d3 = Vector3d1.x * Vector3d.z - Vector3d1.z * Vector3d.x;

				matrix.rotate(Vector3f.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
			}

			if (perspective != 2)
				matrix.translate(0, 0, -1.35);
			matrix.translate(0, -0.2, 0);
		}

		matrix.translate(0, 1.6, 0.65);
	}

	private void applyBlockTransformations(PlayerEntity player, float partialticks, MatrixStack matrix, Block block)
	{
		int perspective = CarryRenderHelper.getPerspective();

		applyGeneralTransformations(player, partialticks, matrix);

		if (Settings.facePlayer.get() ? !isChest(block) : isChest(block))
		{
			if ((ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr")) && perspective == 0)
				matrix.translate(0, 0, -0.4);
			matrix.rotate(Vector3f.YP.rotationDegrees(180));
		}
		else
		{
			if ((ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr")) && perspective == 0)
				matrix.translate(0, 0, 0.4);
		}
	}

	private void applyEntityTransformations(PlayerEntity player, float partialticks, MatrixStack matrix, Entity entity)
	{
		int perspective = CarryRenderHelper.getPerspective();
		Pose pose = player.getPose();

		applyGeneralTransformations(player, partialticks, matrix);

		if (perspective == 2)
			matrix.translate(0, -1.6, 0.65);
		else
			matrix.translate(0, -1.6, -0.65);
		matrix.scale(1.666f, 1.666f, 1.666f);

		float height = entity.getHeight();
		float width = entity.getWidth();
		float multiplier = height * width;
		entity.rotationYaw = 0.0f;
		entity.prevRotationYaw = 0.0f;
		entity.setRotationYawHead(0.0f);
		entity.rotationPitch = 0.0f;
		entity.prevRotationPitch = 0.0f;

		if (perspective == 2)
			matrix.rotate(Vector3f.YP.rotationDegrees(180));

		matrix.scale((10 - multiplier) * 0.08f, (10 - multiplier) * 0.08f, (10 - multiplier) * 0.08f);
		matrix.translate(0.0, height / 2 + -(height / 2) + 1, width - 0.1 < 0.7 ? width - 0.1 + (0.7 - (width - 0.1)) : width - 0.1);

		if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING)
		{
			matrix.rotate(Vector3f.XN.rotationDegrees(90));
			matrix.translate(0, -0.2 * height, 0);

			if (pose == Pose.FALL_FLYING)
				matrix.translate(0, 0, 0.2);
		}

	}

	/*
	 * Renders correct arm rotation
	 */
	@OnlyIn(Dist.CLIENT)
	public void drawArms(PlayerEntity player, float partialticks, MatrixStack matrix, IRenderTypeBuffer buffer, int light)
	{
		int perspective = CarryRenderHelper.getPerspective();
		Pose pose = player.getPose();

		if (!Settings.renderArms.get())
			return;

		if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING)
			return;

		if (handleMobends() && !ModList.get().isLoaded("obfuscate"))
		{
			ItemStack stack = player.getHeldItemMainhand();
			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))
			{
				PlayerModel<AbstractClientPlayerEntity> model = getPlayerModel((AbstractClientPlayerEntity) player);

				AbstractClientPlayerEntity aplayer = (AbstractClientPlayerEntity) player;
				ResourceLocation skinLoc = aplayer.getLocationSkin();

				matrix.push();
				if (perspective == 2)
					matrix.rotate(Vector3f.YP.rotationDegrees(180));

				Minecraft.getInstance().getTextureManager().bindTexture(skinLoc);

				CarryOnOverride overrider = ScriptChecker.getOverride(player);
				IVertexBuilder builder = buffer.getBuffer(RenderType.getEntityCutout(skinLoc));

				if (overrider != null)
				{
					float[] rotLeft = null;
					float[] rotRight = null;
					if (!overrider.getRenderRotationLeftArm().isEmpty())
						rotLeft = ScriptParseHelper.getXYZArray(overrider.getRenderRotationLeftArm());
					if (!overrider.getRenderRotationRightArm().isEmpty())
						rotRight = ScriptParseHelper.getXYZArray(overrider.getRenderRotationRightArm());

					boolean renderRight = overrider.isRenderRightArm();
					boolean renderLeft = overrider.isRenderLeftArm();

					if (renderLeft && rotLeft != null)
					{
						renderArmPost(model.bipedLeftArm, (float) rotLeft[0], (float) rotLeft[2], false, doSneakCheck(player), light, matrix, builder);
						renderArmPost(model.bipedLeftArmwear, (float) rotLeft[0], (float) rotLeft[2], false, doSneakCheck(player), light, matrix, builder);
					}
					else if (renderLeft)
					{
						renderArmPost(model.bipedLeftArm, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), false, doSneakCheck(player), light, matrix, builder);
						renderArmPost(model.bipedLeftArmwear, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), false, doSneakCheck(player), light, matrix, builder);
					}

					if (renderRight && rotRight != null)
					{
						renderArmPost(model.bipedRightArm, (float) rotRight[0], (float) rotRight[2], true, doSneakCheck(player), light, matrix, builder);
						renderArmPost(model.bipedRightArmwear, (float) rotRight[0], (float) rotRight[2], true, doSneakCheck(player), light, matrix, builder);
					}
					else if (renderRight)
					{
						renderArmPost(model.bipedRightArm, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), true, doSneakCheck(player), light, matrix, builder);
						renderArmPost(model.bipedRightArmwear, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), true, doSneakCheck(player), light, matrix, builder);
					}
				}
				else
				{
					renderArmPost(model.bipedRightArm, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), true, doSneakCheck(player), light, matrix, builder);
					renderArmPost(model.bipedLeftArm, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), false, doSneakCheck(player), light, matrix, builder);
					renderArmPost(model.bipedLeftArmwear, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), false, doSneakCheck(player), light, matrix, builder);
					renderArmPost(model.bipedRightArmwear, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), true, doSneakCheck(player), light, matrix, builder);
				}

				if (buffer instanceof Impl)
					((Impl) buffer).finish();

				matrix.pop();
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
			PlayerEntity player = event.getPlayer();
			Pose pose = player.getPose();
			ItemStack stack = player.getHeldItemMainhand();
			if (pose != Pose.SWIMMING && pose != Pose.FALL_FLYING && !stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack)))
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

	@OnlyIn(Dist.CLIENT)
	private void renderArmPost(ModelRenderer arm, float x, float z, boolean right, boolean sneaking, int light, MatrixStack matrix, IVertexBuilder builder)
	{
		matrix.push();
		arm.showModel = true;
		if (right)
			matrix.translate(0.015, 0, 0);
		else
			matrix.translate(-0.015, 0, 0);

		if (!sneaking)
			arm.rotationPointY = 20;
		else
			arm.rotationPointY = 15;

		arm.rotateAngleX = (float) x;
		arm.rotateAngleY = (float) 0;
		arm.rotateAngleZ = (float) -z;
		arm.render(matrix, builder, light, 655360);
		arm.rotationPointY = 2;
		matrix.pop();
	}

	@OnlyIn(Dist.CLIENT)
	private void renderArmPre(ModelRenderer arm)
	{
		arm.showModel = false;
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

	public static boolean doSneakCheck(PlayerEntity player)
	{
		if (player.abilities.isFlying)
			return false;

		return (player.isSneaking() || player.isCrouching());
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

	// @SubscribeEvent
	// @OnlyIn(Dist.CLIENT)
	// public void hideItems(RenderSpecificHandEvent event)
	// {
	// ItemStack stack = event.getItemStack();
	//
	// if (stack != null && (stack.getItem() == RegistrationHandler.itemTile ||
	// stack.getItem() == RegistrationHandler.itemEntity))
	// {
	// event.setCanceled(true);
	// }
	// }
}
