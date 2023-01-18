package tschipp.carryon.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptRender;
import tschipp.carryon.platform.Services;

import java.util.Optional;

public class CarriedObjectRender
{

	public static boolean drawFirstPerson(Player player, MultiBufferSource buffer, PoseStack matrix, int light, float partialTicks)
	{
		if(Services.PLATFORM.isModLoaded("firstperson") || Services.PLATFORM.isModLoaded("firstpersonmod"))
			return false;

		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		try {
			if (carry.isCarrying(CarryType.BLOCK))
				drawFirstPersonBlock(player, buffer, matrix, light, CarryRenderHelper.getRenderState(player));
			else if (carry.isCarrying(CarryType.ENTITY))
				drawFirstPersonEntity(player, buffer, matrix, light, partialTicks);
		}
		catch (Exception e)
		{
			//hehe
		}

		if(carry.getActiveScript().isPresent())
		{
			ScriptRender render = carry.getActiveScript().get().scriptRender();
			if(!render.renderLeftArm() && player.getMainArm() == HumanoidArm.LEFT)
				return false;

			if(!render.renderRightArm() && player.getMainArm() == HumanoidArm.RIGHT)
				return false;
		}

		return carry.isCarrying();
	}

	private static void drawFirstPersonBlock(Player player, MultiBufferSource buffer, PoseStack matrix, int light, BlockState state)
	{
		matrix.pushPose();
		matrix.scale(2.5f, 2.5f, 2.5f);
		matrix.translate(0, -0.5, -1);
		RenderSystem.enableBlend();
		RenderSystem.disableCull();

		CarryOnData carry = CarryOnDataManager.getCarryData(player);

		if (Constants.CLIENT_CONFIG.facePlayer != CarryRenderHelper.isChest(state.getBlock())) {
			matrix.mulPose(Vector3f.YP.rotationDegrees(180));
			matrix.mulPose(Vector3f.XN.rotationDegrees(8));
		} else {
			matrix.mulPose(Vector3f.XP.rotationDegrees(8));
		}

		if(carry.getActiveScript().isPresent())
			CarryRenderHelper.performScriptTransformation(matrix, carry.getActiveScript().get());

		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

		ItemStack stack = new ItemStack(state.getBlock().asItem());
		BakedModel model = CarryRenderHelper.getRenderBlock(player);
		CarryRenderHelper.renderBakedModel(stack, matrix, buffer, light, model);

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		matrix.popPose();
	}

	private static void drawFirstPersonEntity(Player player, MultiBufferSource buffer, PoseStack matrix, int light, float partialTicks) {
		EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
		Entity entity = CarryRenderHelper.getRenderEntity(player);
		CarryOnData carry = CarryOnDataManager.getCarryData(player);

		if (entity != null)
		{
			Vec3 playerpos = CarryRenderHelper.getExactPos(player, partialTicks);

			entity.setPos(playerpos.x, playerpos.y, playerpos.z);
			entity.xRotO = 0.0f;
			entity.yRotO = 0.0f;
			entity.setYHeadRot(0.0f);

			float height = entity.getBbHeight();
			float width = entity.getBbWidth();

			matrix.pushPose();
			matrix.scale(0.8f, 0.8f, 0.8f);
			matrix.mulPose(Vector3f.YP.rotationDegrees(180));
			matrix.translate(0.0, -height - .1, width + 0.1);

			manager.setRenderShadow(false);

			Optional<CarryOnScript> res = carry.getActiveScript();
			if(res.isPresent())
			{
				CarryOnScript script = res.get();
				CarryRenderHelper.performScriptTransformation(matrix, script);
			}

			if (entity instanceof LivingEntity)
				((LivingEntity) entity).hurtTime = 0;

			try {
				manager.render(entity, 0, 0, 0, 0f, 0, matrix, buffer, light);
			}
			catch (Exception e)
			{
			}
			manager.setRenderShadow(true);
		}

		// RenderSystem.disableAlphaTest();
		matrix.popPose();
	}

	/**
	 * Draws the third person view of entities and blocks
	 * @param partialticks
	 * @param matrix
	 */
	public static void drawThirdPerson(float partialticks, PoseStack matrix) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		int light = 0;
		int perspective = CarryRenderHelper.getPerspective();
		EntityRenderDispatcher manager = mc.getEntityRenderDispatcher();

		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();

		for (Player player : level.players())
		{
			try {
				CarryOnData carry = CarryOnDataManager.getCarryData(player);

				if (perspective == 0 && player == mc.player && !(Services.PLATFORM.isModLoaded("firstperson") || Services.PLATFORM.isModLoaded("firstpersonmod")))
					continue;

				light = manager.getPackedLightCoords(player, partialticks);

				if (carry.isCarrying(CarryType.BLOCK)) {
					BlockState state = CarryRenderHelper.getRenderState(player);

					CarryRenderHelper.applyBlockTransformations(player, partialticks, matrix, state.getBlock());

					ItemStack tileItem = new ItemStack(state.getBlock().asItem());
					BakedModel model = CarryRenderHelper.getRenderBlock(player);

					//ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, level, player) : tileItem.isEmpty() ? mc.getBlockRenderer().getBlockModel(state) : mc.getItemRenderer().getModel(tileItem, level, player, 0);
//
					Optional<CarryOnScript> res = carry.getActiveScript();
					if (res.isPresent()) {
						CarryOnScript script = res.get();
						CarryRenderHelper.performScriptTransformation(matrix, script);
					}

					RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
					RenderSystem.enableCull();

					PoseStack.Pose p = matrix.last();
					PoseStack copy = new PoseStack();
					copy.mulPoseMatrix(p.pose());
					matrix.popPose();

					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

					CarryRenderHelper.renderBakedModel(tileItem, copy, buffer, light, model);
					buffer.endBatch();

					matrix.popPose();
				} else if (carry.isCarrying(CarryType.ENTITY)) {
					Entity entity = CarryRenderHelper.getRenderEntity(player);

					if (entity != null) {
						CarryRenderHelper.applyEntityTransformations(player, partialticks, matrix, entity);

						manager.setRenderShadow(false);

						Optional<CarryOnScript> res = carry.getActiveScript();
						if (res.isPresent()) {
							CarryOnScript script = res.get();
							CarryRenderHelper.performScriptTransformation(matrix, script);
						}

						if (entity instanceof LivingEntity le)
							le.hurtTime = 0;

						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

						manager.render(entity, 0, 0, 0, 0f, 0, matrix, buffer, light);
						buffer.endBatch();

						matrix.popPose();
						manager.setRenderShadow(true);
						matrix.popPose();
					}
				}
			}
			catch (Exception e)
			{
			}

		}
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

}

