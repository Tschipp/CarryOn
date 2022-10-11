package tschipp.carryon.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class CarriedObjectRender
{

	public static boolean drawFirstPerson(Player player, MultiBufferSource buffer, PoseStack matrix, int light)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if(carry.isCarrying(CarryType.BLOCK))
			drawFirstPersonBlock(player, buffer, matrix, light, carry.getBlock());
		else
			;

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

		//TODO: FacePlayer config
		if (isChest(state.getBlock())) {
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


	public static boolean isChest(Block block)
	{
		return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
	}
}

