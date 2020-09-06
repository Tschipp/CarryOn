package tschipp.carryon.client.helper;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.common.helper.ScriptParseHelper;
import tschipp.carryon.common.scripting.CarryOnOverride;

public class CarryRenderHelper
{
	public static Vec3d getExactPos(Entity entity, float partialticks)
	{
		return new Vec3d(entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * partialticks, entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * partialticks, entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * partialticks);
	}

	public static float getExactBodyRotationDegrees(LivingEntity entity, float partialticks)
	{
		if (entity.getRidingEntity() != null && entity.getRidingEntity() instanceof LivingEntity)
			return -(entity.prevRotationYawHead + (entity.rotationYawHead - entity.prevRotationYawHead) * partialticks);
		else
			return -(entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * partialticks);
	}

	public static Quaternion getExactBodyRotation(LivingEntity entity, float partialticks)
	{
		return Vector3f.YP.rotationDegrees(getExactBodyRotationDegrees(entity, partialticks));
	}

	public static void performOverrideTransformation(MatrixStack matrix, CarryOnOverride override)
	{
		float[] translation = ScriptParseHelper.getXYZArray(override.getRenderTranslation());
		float[] rotation = ScriptParseHelper.getXYZArray(override.getRenderRotation());
		float[] scaled = ScriptParseHelper.getScaled(override.getRenderScaled());

		matrix.translate(translation[0], translation[1], translation[2]);
		Quaternion rot = Vector3f.XP.rotationDegrees(rotation[0]);
		rot.multiply(Vector3f.YP.rotationDegrees(rotation[1]));
		rot.multiply(Vector3f.ZP.rotationDegrees(rotation[2]));

		matrix.rotate(rot);
		matrix.scale(scaled[0], scaled[1], scaled[2]);
	}

	public static void renderItem(BlockState state, CompoundNBT tag, ItemStack stack, ItemStack tileStack, MatrixStack matrix, IRenderTypeBuffer buffer, int light, IBakedModel model)
	{
		if (ModelOverridesHandler.hasCustomOverrideModel(state, tag))
		{
			Object override = ModelOverridesHandler.getOverrideObject(state, tag);

			if (override instanceof ItemStack)
			{
				Minecraft.getInstance().getItemRenderer().renderItem((ItemStack) override, TransformType.NONE, false, matrix, buffer, light, 0xFFFFFF, model); //Note: I'm not sure what the second to last argument does, but it seems to work like this
				return;
			}
		}
		
		Minecraft.getInstance().getItemRenderer().renderItem(tileStack.isEmpty() ? stack : tileStack, TransformType.NONE, false, matrix, buffer, light, 0xFFFFFF, model);
	}
}
