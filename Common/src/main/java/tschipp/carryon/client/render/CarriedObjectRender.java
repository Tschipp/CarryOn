package tschipp.carryon.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;

import java.util.Optional;

public class CarriedObjectRender
{

	public static boolean drawFirstPerson(Player player, MultiBufferSource buffer, PoseStack matrix, int light, float partialTicks)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if(carry.isCarrying(CarryType.BLOCK))
			drawFirstPersonBlock(player, buffer, matrix, light, carry.getBlock());
		else if (carry.isCarrying(CarryType.ENTITY))
			drawFirstPersonEntity(player, buffer, matrix, light, carry.getEntity(player.level), partialTicks);

		return carry.isCarrying();
	}

	private static void drawFirstPersonBlock(Player player, MultiBufferSource buffer, PoseStack matrix, int light, BlockState state)
	{
		matrix.pushPose();
		matrix.scale(2.5f, 2.5f, 2.5f);
		matrix.translate(0, -0.5, -1);
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		int perspective = CarryRenderHelper.getPerspective();

		if (Constants.CLIENT_CONFIG.facePlayer && isChest(state.getBlock())) {
			matrix.mulPose(Vector3f.YP.rotationDegrees(180));
			matrix.mulPose(Vector3f.XN.rotationDegrees(8));
		} else {
			matrix.mulPose(Vector3f.XP.rotationDegrees(8));
		}

//
//		CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
//		if (carryOverride != null) {
//			CarryRenderHelper.performOverrideTransformation(matrix, carryOverride);
//
//			if (!carryOverride.getRenderNameBlock().isEmpty()) {
//				Block b = StringParser.getBlock(carryOverride.getRenderNameBlock());
//				if (b != null) {
//					ItemStack s = new ItemStack(b, 1);
//					s.setTag(carryOverride.getRenderNBT());
//					model = Minecraft.getInstance().getItemRenderer().getModel(s, level, player, 0);
//				}
//			}
//		}

		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

		ItemStack stack = new ItemStack(state.getBlock().asItem());
		//TODO: Model overrides
		BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, player.level, player, 0);
		CarryRenderHelper.renderItem(state, null, stack, matrix, buffer, light, model);

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		matrix.popPose();
	}

	private static void drawFirstPersonEntity(Player player, MultiBufferSource buffer, PoseStack matrix, int light, Entity entity, float partialTicks) {
		EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();

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

			// RenderSystem.enableAlphaTest();

				// Lighting.en
				manager.setRenderShadow(false);

				//TODO: Scripts
				/*
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
							newEntity = type.get().create(level);

						if (newEntity != null)
						{
							CompoundTag nbttag = carryOverride.getRenderNBT();
							if (nbttag != null)
								newEntity.deserializeNBT(nbttag);
							entity = newEntity;
							entity.setPos(playerpos.x, playerpos.y, playerpos.z);
							entity.xRotO = 0.0f;
							entity.yRotO = 0.0f;
							entity.setYHeadRot(0.0f);
						}
					}
				}
				*/


				if (entity instanceof LivingEntity)
					((LivingEntity) entity).hurtTime = 0;

				manager.render(entity, 0, 0, 0, 0f, 0, matrix, buffer, light);
				manager.setRenderShadow(true);
			}

			// RenderSystem.disableAlphaTest();
			matrix.popPose();
	}


	private static boolean isChest(Block block)
	{
		return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
	}
}

