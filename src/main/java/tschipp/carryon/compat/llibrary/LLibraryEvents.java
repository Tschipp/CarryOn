package tschipp.carryon.compat.llibrary;

import net.ilexiconn.llibrary.client.event.PlayerModelEvent;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.helper.ScriptParseHelper;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;

public class LLibraryEvents
{

	@SubscribeEvent
	public void setAngles(PlayerModelEvent.SetRotationAngles event)
	{
		if (!CarryOnConfig.settings.renderArms)
			return;

		EntityPlayer player = event.getEntityPlayer();
		if (player != null)
		{
			ModelBiped modelBiped = event.getModel();
			if (modelBiped instanceof ModelPlayer)
			{
				ItemStack stack = player.getHeldItemMainhand();
				ModelPlayer model = (ModelPlayer) modelBiped;
				
				if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack) || stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))
				{

					float rotation = 0;

					if (player.isRiding() && player.getRidingEntity() instanceof EntityLivingBase)
						rotation = 0;
					else
						rotation = 0;

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
							render(model.bipedLeftArm, (float) rotLeft[0], (float) rotLeft[2], rotation);
							render(model.bipedLeftArmwear, (float) rotLeft[0], (float) rotLeft[2], rotation);
						}
						else if (renderLeft)
						{
							render(model.bipedLeftArm, 0.8F + (player.isSneaking() ? 0.2f : 0f) - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation);
							render(model.bipedLeftArmwear, 0.8F + (player.isSneaking() ? 0.2f : 0f) - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation);
						}

						if (renderRight && rotRight != null)
						{
							render(model.bipedRightArm, (float) rotRight[0], (float) rotRight[2], rotation);
							render(model.bipedRightArmwear, (float) rotRight[0], (float) rotRight[2], rotation);
						}
						else if (renderRight)
						{
							render(model.bipedRightArm, 0.8F + (player.isSneaking() ? 0.2f : 0f) - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation);
							render(model.bipedRightArmwear, 0.8F + (player.isSneaking() ? 0.2f : 0f) - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation);
						}

					}
					else
					{
						render(model.bipedRightArm, 0.8F + (player.isSneaking() ? 0.2f : 0f) - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation);
						render(model.bipedRightArmwear, 0.8F + (player.isSneaking() ? 0.2f : 0f) - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation);
						render(model.bipedLeftArm, 0.8F + (player.isSneaking() ? 0.2f : 0f) - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation);
						render(model.bipedLeftArmwear, 0.8F + (player.isSneaking() ? 0.2f : 0f) - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0), (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation);
					}

				}

			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void render(ModelRenderer arm, float x, float z, float rotation)
	{
		arm.rotateAngleX = (float) -x;
		arm.rotateAngleY = (float) -Math.toRadians(rotation);
		arm.rotateAngleZ = (float) z;
	}

}
