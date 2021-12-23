package tschipp.carryon.client.event;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
		@SuppressWarnings("resource")
		Player player = Minecraft.getInstance().player;

		if (player != null)
		{
			ItemStack stack = player.getMainHandItem();

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
		Player player = event.player;

		if (player != null && event.side == LogicalSide.CLIENT)
		{
			boolean keyPressed = CarryOnKeybinds.carryKey.isDown();
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
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			if (player.level.isClientSide)
			{
				CarryOnKeybinds.setKeyPressed(player, false);
				CarryOn.network.sendToServer(new SyncKeybindPacket(false));

				if (CarryOn.FINGERPRINT_VIOLATED)
				{
					TextComponent cf = new TextComponent(ChatFormatting.AQUA + "Curseforge" + ChatFormatting.RED);
					cf.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, "https://minecraft.curseforge.com/projects/carry-on"));

					player.displayClientMessage(new TextComponent(ChatFormatting.RED + "[CarryOn] WARNING! Invalid fingerprint detected! The Carry On mod file may have been tampered with! If you didn't download the file from ").append(cf).append(ChatFormatting.RED + " or through any kind of mod launcher, immediately delete the file and re-download it from ").append(cf), false);
				}
			}

		}
	}

	/*
	 * Prevents the Player from opening Guis
	 */
	@SuppressWarnings("resource")
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onGuiInit(InitGuiEvent.Pre event)
	{
		if (event.getGui() != null)
		{
			boolean inventory = event.getGui() instanceof AbstractContainerScreen;
			Player player = Minecraft.getInstance().player;

			if (player != null && inventory)
			{
				ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

				if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack)))
				{
					Minecraft.getInstance().player.closeContainer();
					Minecraft.getInstance().screen = null;
					Minecraft.getInstance().mouseHandler.grabMouse();

				}

			}
		}
	}

	/*
	 * Prevents the Player from switching Slots
	 */
	@SuppressWarnings("resource")
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void inputEvent(KeyPressedEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Options settings = Minecraft.getInstance().options;
		int key = event.key;
		int scancode = event.scancode;
		Player player = Minecraft.getInstance().player;

		if (player != null)
		{
			ItemStack stack = Minecraft.getInstance().player.getMainHandItem();

			if (!stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack)))
			{
				if (settings.keyDrop.matches(key, scancode))
				{
					event.setCanceled(true);
				}
				if (settings.keySwapOffhand.matches(key, scancode))
				{
					event.setCanceled(true);
				}
				if (settings.keyPickItem.matches(key, scancode))
				{
					event.setCanceled(true);
				}
				for (KeyMapping keyBind : settings.keyHotbarSlots)
				{
					if (keyBind.matches(key, scancode))
					{
						event.setCanceled(true);
					}
				}
			}

			int current = player.getInventory().selected;

			if (player.getPersistentData().contains("carrySlot") ? player.getPersistentData().getInt("carrySlot") != current : false)
			{
				player.getInventory().selected = player.getPersistentData().getInt("carrySlot");
			}
		}
	}

	/*
	 * Renders the Block in First Person
	 */
	@SuppressWarnings({ "resource", "deprecation" })
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{
		Level world = Minecraft.getInstance().level;
		Player player = Minecraft.getInstance().player;
		ItemStack stack = player.getMainHandItem();
		int perspective = CarryRenderHelper.getPerspective();
		boolean f1 = Minecraft.getInstance().options.hideGui;
		MultiBufferSource buffer = event.getBuffers();
		PoseStack matrix = event.getMatrixStack();
		int light = event.getLight();

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) && perspective == 0 && !f1)
		{
			if (ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr"))
				return;

			Block block = ItemCarryonBlock.getBlock(stack);
			CompoundTag tag = ItemCarryonBlock.getTileData(stack);
			BlockState state = ItemCarryonBlock.getBlockState(stack);
			ItemStack tileStack = ItemCarryonBlock.getItemStack(stack);

			matrix.pushPose();
			matrix.scale(2.5f, 2.5f, 2.5f);
			matrix.translate(0, -0.5, -1);
			RenderSystem.enableBlend();
			RenderSystem.disableCull();

			if (Settings.facePlayer.get() ? !isChest(block) : isChest(block))
			{
				matrix.mulPose(Vector3f.YP.rotationDegrees(180));
				matrix.mulPose(Vector3f.XN.rotationDegrees(8));
			}
			else
			{
				matrix.mulPose(Vector3f.XP.rotationDegrees(8));
			}

			BakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : tileStack.isEmpty() ? Minecraft.getInstance().getBlockRenderer().getBlockModel(state) : Minecraft.getInstance().getItemRenderer().getModel(tileStack, world, player, 0);

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
						model = Minecraft.getInstance().getItemRenderer().getModel(s, world, player, 0);
					}
				}
			}

			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

			CarryRenderHelper.renderItem(state, tag, stack, tileStack, matrix, buffer, light, model);

			if (perspective == 0)
			{
				event.setCanceled(true);
			}

			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			matrix.popPose();
		}
	}

	// @SubscribeEvent
	// public void onJoinServer(LoggedInEvent event)
	// {
	// ListHandler.initConfigLists();
	// }

	/*
	 * Render blocks and entities in third person
	 */
	@SuppressWarnings({ "deprecation", "resource" })
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onRenderWorld(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		float partialticks = event.getPartialTicks();
		BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		PoseStack matrix = event.getMatrixStack();
		int light = 0;
		int perspective = CarryRenderHelper.getPerspective();
		EntityRenderDispatcher manager = mc.getEntityRenderDispatcher();

		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();

		for (Player player : world.players())
		{
			if (perspective == 0 && player == mc.player)
				continue;

			light = manager.getPackedLightCoords(player, partialticks);
			ItemStack stack = player.getMainHandItem();

			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack))
			{
				Block block = ItemCarryonBlock.getBlock(stack);
				BlockState state = ItemCarryonBlock.getBlockState(stack);
				CompoundTag tag = ItemCarryonBlock.getTileData(stack);
				ItemStack tileItem = ItemCarryonBlock.getItemStack(stack);

				applyBlockTransformations(player, partialticks, matrix, block);

				BakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : tileItem.isEmpty() ? mc.getBlockRenderer().getBlockModel(state) : mc.getItemRenderer().getModel(tileItem, world, player, 0);

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
							model = mc.getItemRenderer().getModel(s, world, player, 0);
						}
					}
				}

				RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
				RenderSystem.enableCull();

				PoseStack.Pose p = matrix.last();
				PoseStack copy = new PoseStack();
				copy.mulPoseMatrix(p.pose());
				matrix.popPose();
				drawArms(player, partialticks, matrix, buffer, light);

				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

				CarryRenderHelper.renderItem(state, tag, stack, tileItem, copy, buffer, light, model);
				buffer.endBatch();

				matrix.popPose();
			}
			else if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))
			{
				Entity entity = RenderEntityEvents.getEntity(stack, world);

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

							Optional<EntityType<?>> type = EntityType.byString(entityname);
							if (type.isPresent())
								newEntity = type.get().create(world);

							if (newEntity != null)
							{
								CompoundTag nbttag = carryOverride.getRenderNBT();
								if (nbttag != null)
									newEntity.deserializeNBT(nbttag);
								entity = newEntity;
								entity.yo = 0.0f;
								entity.yRotO = 0.0f;
								entity.setYHeadRot(0.0f);
								entity.xo = 0.0f;
								entity.xRotO = 0.0f;
							}
						}
					}

					if (entity instanceof LivingEntity le)
						le.hurtTime = 0;

					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

					manager.render(entity, 0, 0, 0, 0f, 0, matrix, buffer, light);
					buffer.endBatch();

					matrix.popPose();

					drawArms(player, partialticks, matrix, buffer, light);

					manager.setRenderShadow(true);

					matrix.popPose();
				}
			}

		}
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	@SuppressWarnings("resource")
	private void applyGeneralTransformations(Player player, float partialticks, PoseStack matrix)
	{
		int perspective = CarryRenderHelper.getPerspective();
		Quaternion playerrot = CarryRenderHelper.getExactBodyRotation(player, partialticks);
		Vec3 playerpos = CarryRenderHelper.getExactPos(player, partialticks);
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vec3 offset = playerpos.subtract(cameraPos);
		Pose pose = player.getPose();

		matrix.pushPose();
		matrix.translate(offset.x, offset.y, offset.z);

		if (perspective == 2)
			playerrot.mul(Vector3f.YP.rotationDegrees(180));
		matrix.mulPose(playerrot);

		matrix.pushPose();
		matrix.scale(0.6f, 0.6f, 0.6f);

		if (perspective == 2)
			matrix.translate(0, 0, -1.35);

		if (doSneakCheck(player))
		{
			matrix.translate(0, -0.4, 0);
		}

		if (pose == Pose.SWIMMING)
		{
			float f = player.getSwimAmount(partialticks);
			float f3 = player.isInWater() ? -90.0F - player.xRotO : -90.0F;
			float f4 = Mth.lerp(f, 0.0F, f3);
			if (perspective == 2)
			{
				matrix.translate(0, 0, 1.35);
				matrix.mulPose(Vector3f.XP.rotationDegrees(f4));
			}
			else
				matrix.mulPose(Vector3f.XN.rotationDegrees(f4));

			matrix.translate(0, -1.5, -1.848);
			if (perspective == 2)
				matrix.translate(0, 0, 2.38);
		}

		if (pose == Pose.FALL_FLYING)
		{
			float f1 = player.getFallFlyingTicks() + partialticks;
			float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
			if (!player.isAutoSpinAttack())
			{
				if (perspective == 2)
					matrix.translate(0, 0, 1.35);

				if (perspective == 2)
					matrix.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - player.xRotO)));
				else
					matrix.mulPose(Vector3f.XN.rotationDegrees(f2 * (-90.0F - player.xRotO)));
			}

			Vec3 Vector3d = player.getViewVector(partialticks);
			Vec3 Vector3d1 = player.getDeltaMovement();
			double d0 = Vector3d1.horizontalDistanceSqr();
			double d1 = Vector3d1.horizontalDistanceSqr();
			if (d0 > 0.0D && d1 > 0.0D)
			{
				double d2 = (Vector3d1.x * Vector3d.x + Vector3d1.z * Vector3d.z) / (Math.sqrt(d0) * Math.sqrt(d1));
				double d3 = Vector3d1.x * Vector3d.z - Vector3d1.z * Vector3d.x;

				matrix.mulPose(Vector3f.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
			}

			if (perspective != 2)
				matrix.translate(0, 0, -1.35);
			matrix.translate(0, -0.2, 0);
		}

		matrix.translate(0, 1.6, 0.65);
	}

	private void applyBlockTransformations(Player player, float partialticks, PoseStack matrix, Block block)
	{
		int perspective = CarryRenderHelper.getPerspective();

		applyGeneralTransformations(player, partialticks, matrix);

		if (Settings.facePlayer.get() ? !isChest(block) : isChest(block))
		{
			if ((ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr")) && perspective == 0)
				matrix.translate(0, 0, -0.4);
			matrix.mulPose(Vector3f.YP.rotationDegrees(180));
		}
		else if ((ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr")) && perspective == 0)
			matrix.translate(0, 0, 0.4);
	}

	private void applyEntityTransformations(Player player, float partialticks, PoseStack matrix, Entity entity)
	{
		int perspective = CarryRenderHelper.getPerspective();
		Pose pose = player.getPose();

		applyGeneralTransformations(player, partialticks, matrix);

		if (perspective == 2)
			matrix.translate(0, -1.6, 0.65);
		else
			matrix.translate(0, -1.6, -0.65);
		matrix.scale(1.666f, 1.666f, 1.666f);

		float height = entity.getBbHeight();
		float width = entity.getBbWidth();
		float multiplier = height * width;
		entity.yo = 0.0f;
		entity.yRotO = 0.0f;
		entity.setYHeadRot(0.0f);
		entity.xo = 0.0f;
		entity.xRotO = 0.0f;

		if (perspective == 2)
			matrix.mulPose(Vector3f.YP.rotationDegrees(180));

		matrix.scale((10 - multiplier) * 0.08f, (10 - multiplier) * 0.08f, (10 - multiplier) * 0.08f);
		matrix.translate(0.0, height / 2 + -(height / 2) + 1, width - 0.1 < 0.7 ? width - 0.1 + (0.7 - (width - 0.1)) : width - 0.1);

		if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING)
		{
			matrix.mulPose(Vector3f.XN.rotationDegrees(90));
			matrix.translate(0, -0.2 * height, 0);

			if (pose == Pose.FALL_FLYING)
				matrix.translate(0, 0, 0.2);
		}

	}

	/*
	 * Renders correct arm rotation
	 */
	@OnlyIn(Dist.CLIENT)
	public void drawArms(Player player, float partialticks, PoseStack matrix, MultiBufferSource buffer, int light)
	{
		int perspective = CarryRenderHelper.getPerspective();
		Pose pose = player.getPose();

		if (!Settings.renderArms.get() || pose == Pose.SWIMMING || pose == Pose.FALL_FLYING)
			return;

		if (handleMobends() && !ModList.get().isLoaded("obfuscate"))
		{
			ItemStack stack = player.getMainHandItem();
			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))
			{
				PlayerModel<AbstractClientPlayer> model = getPlayerModel((AbstractClientPlayer) player);

				AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
				ResourceLocation skinLoc = aplayer.getSkinTextureLocation();

				matrix.pushPose();
				if (perspective == 2)
					matrix.mulPose(Vector3f.YP.rotationDegrees(180));

				RenderSystem.setShaderTexture(0, skinLoc);

				CarryOnOverride overrider = ScriptChecker.getOverride(player);
				VertexConsumer builder = buffer.getBuffer(RenderType.entityCutout(skinLoc));

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
						renderArmPost(model.leftArm, rotLeft[0], rotLeft[2], false, doSneakCheck(player), light, matrix, builder);
						renderArmPost(model.leftSleeve, rotLeft[0], rotLeft[2], false, doSneakCheck(player), light, matrix, builder);
					}
					else if (renderLeft)
					{
						renderArmPost(model.leftArm, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0, false, doSneakCheck(player), light, matrix, builder);
						renderArmPost(model.leftSleeve, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0, false, doSneakCheck(player), light, matrix, builder);
					}

					if (renderRight && rotRight != null)
					{
						renderArmPost(model.rightArm, rotRight[0], rotRight[2], true, doSneakCheck(player), light, matrix, builder);
						renderArmPost(model.rightSleeve, rotRight[0], rotRight[2], true, doSneakCheck(player), light, matrix, builder);
					}
					else if (renderRight)
					{
						renderArmPost(model.rightArm, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0, true, doSneakCheck(player), light, matrix, builder);
						renderArmPost(model.rightSleeve, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0, true, doSneakCheck(player), light, matrix, builder);
					}
				}
				else
				{
					renderArmPost(model.rightArm, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0, true, doSneakCheck(player), light, matrix, builder);
					renderArmPost(model.leftArm, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0, false, doSneakCheck(player), light, matrix, builder);
					renderArmPost(model.leftSleeve, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0, false, doSneakCheck(player), light, matrix, builder);
					renderArmPost(model.rightSleeve, 2.0F + (doSneakCheck(player) ? 0f : 0.2f) - (stack.getItem() == RegistrationHandler.itemEntity ? 0.3f : 0), stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0, true, doSneakCheck(player), light, matrix, builder);
				}

				if (buffer instanceof BufferSource)
					((BufferSource) buffer).endBatch();

				matrix.popPose();
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
			Player player = event.getPlayer();
			Pose pose = player.getPose();
			ItemStack stack = player.getMainHandItem();
			if (pose != Pose.SWIMMING && pose != Pose.FALL_FLYING && !stack.isEmpty() && (stack.getItem() == RegistrationHandler.itemTile && ItemCarryonBlock.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack)))
			{
				PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

				CarryOnOverride overrider = ScriptChecker.getOverride(player);
				if (overrider != null)
				{
					boolean renderRight = overrider.isRenderRightArm();
					boolean renderLeft = overrider.isRenderLeftArm();

					if (renderRight)
					{
						renderArmPre(model.rightArm);
						renderArmPre(model.rightSleeve);

					}

					if (renderLeft)
					{
						renderArmPre(model.leftArm);
						renderArmPre(model.leftSleeve);
					}
				}
				else
				{
					renderArmPre(model.rightArm);
					renderArmPre(model.leftArm);
					renderArmPre(model.leftSleeve);
					renderArmPre(model.rightSleeve);
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void renderArmPost(ModelPart arm, float x, float z, boolean right, boolean sneaking, int light, PoseStack matrix, VertexConsumer builder)
	{
		matrix.pushPose();
		arm.visible = true;
		if (right)
			matrix.translate(0.015, 0, 0);
		else
			matrix.translate(-0.015, 0, 0);

		if (!sneaking)
			arm.y = 20;
		else
			arm.y = 15;

		arm.xRot = x;
		arm.yRot = 0;
		arm.zRot = -z;
		arm.render(matrix, builder, light, 655360);
		arm.y = 2;
		matrix.popPose();
	}

	@OnlyIn(Dist.CLIENT)
	private void renderArmPre(ModelPart arm)
	{
		arm.visible = false;
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

	public static boolean doSneakCheck(Player player)
	{
		if (player.getAbilities().flying)
			return false;

		return player.isShiftKeyDown() || player.isCrouching();
	}

	public static boolean isChest(Block block)
	{
		return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
	}

	@OnlyIn(Dist.CLIENT)
	private static PlayerRenderer getRenderPlayer(AbstractClientPlayer player)
	{
		Minecraft mc = Minecraft.getInstance();
		EntityRenderDispatcher manager = mc.getEntityRenderDispatcher();
		return (PlayerRenderer) manager.getSkinMap().get(player.getModelName());
	}

	@OnlyIn(Dist.CLIENT)
	private static PlayerModel<AbstractClientPlayer> getPlayerModel(AbstractClientPlayer player)
	{
		return getRenderPlayer(player).getModel();
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
