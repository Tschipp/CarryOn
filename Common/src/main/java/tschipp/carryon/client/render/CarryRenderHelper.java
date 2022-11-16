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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
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

	public static void applyGeneralTransformations(Player player, float partialticks, PoseStack matrix)
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

	public static boolean doSneakCheck(Player player)
	{
		if (player.getAbilities().flying)
			return false;

		return player.isShiftKeyDown() || player.isCrouching();
	}
}
