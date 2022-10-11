package tschipp.carryon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CarryRenderHelper
{
	public static Vec3 getExactPos(Entity entity, float partialticks)
	{
		return new Vec3(entity.xOld + (entity.getX() - entity.xOld) * partialticks, entity.yOld + (entity.getY() - entity.yOld) * partialticks, entity.zOld + (entity.getZ() - entity.zOld) * partialticks);
	}

	public static float getExactBodyRotationDegrees(LivingEntity entity, float partialticks)
	{
		if (entity.getVehicle() != null && entity.getVehicle() instanceof LivingEntity)
			return -(entity.yHeadRotO + (entity.yHeadRot - entity.yHeadRotO) * partialticks);
		else
			return -(entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO) * partialticks);
	}

	public static Quaternion getExactBodyRotation(LivingEntity entity, float partialticks)
	{
		return Vector3f.YP.rotationDegrees(getExactBodyRotationDegrees(entity, partialticks));
	}

	//TODO: Scripting
//	public static void performOverrideTransformation(PoseStack matrix, CarryOnOverride override)
//	{
//		int perspective = getPerspective();
//
//		float[] translation = ScriptParseHelper.getXYZArray(override.getRenderTranslation());
//		float[] rotation = ScriptParseHelper.getXYZArray(override.getRenderRotation());
//		float[] scaled = ScriptParseHelper.getScaled(override.getRenderScaled());
//
//		Quaternion rot = Vector3f.XP.rotationDegrees(rotation[0]);
//		rot.mul(Vector3f.YP.rotationDegrees(rotation[1]));
//		rot.mul(Vector3f.ZP.rotationDegrees(rotation[2]));
//		matrix.mulPose(rot);
//
//		matrix.translate(translation[0], translation[1], perspective == 1 && override.isBlock() ? -translation[2] : translation[2]);
//
//		matrix.scale(scaled[0], scaled[1], scaled[2]);
//	}

	public static void renderItem(BlockState state, CompoundTag tag, ItemStack stack, PoseStack matrix, MultiBufferSource buffer, int light, BakedModel model)
	{
		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
//		if (ModelOverridesHandler.hasCustomOverrideModel(state, tag))
//		{
//			Object override = ModelOverridesHandler.getOverrideObject(state, tag);

//			if (override instanceof ItemStack)
//			{
//				renderer.render((ItemStack) override, TransformType.NONE, false, matrix, buffer, light, OverlayTexture.NO_OVERLAY, model);
//				return;
////			}
//		}

		renderer.render(stack, TransformType.NONE, false, matrix, buffer, light, OverlayTexture.NO_OVERLAY, model);
	}

	@SuppressWarnings("resource")
	public static int getPerspective()
	{
		boolean isThirdPerson = !Minecraft.getInstance().options.getCameraType().isFirstPerson(); // isThirdPerson
		boolean isThirdPersonReverse = Minecraft.getInstance().options.getCameraType().isMirrored();

		if (!isThirdPerson && !isThirdPersonReverse)
			return 0;
		if (isThirdPerson && !isThirdPersonReverse)
			return 1;
		return 2;
	}
}
