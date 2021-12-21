// package tschipp.carryon.compat.obfuscate;
//
// import com.mrcrayfish.obfuscate.client.event.PlayerModelEvent;
//
// import net.minecraft.client.model.PlayerModel;
// import net.minecraft.client.renderer.model.ModelRenderer;
// import net.minecraft.entity.player.PlayerEntity;
// import net.minecraft.world.item.ItemStack;
// import net.minecraftforge.api.distmarker.Dist;
// import net.minecraftforge.api.distmarker.OnlyIn;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import tschipp.carryon.common.config.Configs.Settings;
// import tschipp.carryon.common.handler.RegistrationHandler;
// import tschipp.carryon.common.helper.ScriptParseHelper;
// import tschipp.carryon.common.item.ItemCarryonBlock;
// import tschipp.carryon.common.item.ItemCarryonEntity;
// import tschipp.carryon.common.scripting.CarryOnOverride;
// import tschipp.carryon.common.scripting.ScriptChecker;
//
// public class ObfuscateEvents
// {
//
// @SubscribeEvent
// public void preModelPlayerEvent(PlayerModelEvent.SetupAngles.Post event)
// {
// if(!Settings.renderArms.get())
// return;
//
// PlayerEntity player = event.getPlayer();
//
// Pose pose = player.getPose();
// if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING)
// return;
//
// PlayerModel<?> model = event.getModelPlayer();
// ItemStack stack = player.getMainHandItem();
// if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile &&
// ItemCarryonBlock.hasTileData(stack) || stack.getItem() ==
// RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))
// {
//
// float rotation = 0;
//
// CarryOnOverride overrider = ScriptChecker.getOverride(player);
// if (overrider != null)
// {
// float[] rotLeft = null;
// float[] rotRight = null;
// if (overrider.getRenderRotationLeftArm() != null)
// rotLeft =
// ScriptParseHelper.getXYZArray(overrider.getRenderRotationLeftArm());
// if (overrider.getRenderRotationRightArm() != null)
// rotRight =
// ScriptParseHelper.getXYZArray(overrider.getRenderRotationRightArm());
//
// boolean renderRight = overrider.isRenderRightArm();
// boolean renderLeft = overrider.isRenderLeftArm();
//
// if (renderLeft && rotLeft != null)
// {
// renderArmPre(model.leftArm, (float) rotLeft[0], (float) rotLeft[2],
// rotation);
// renderArmPre(model.leftSleeve, (float) rotLeft[0], (float) rotLeft[2],
// rotation);
// }
// else if (renderLeft)
// {
// renderArmPre(model.leftArm, 0.8F + (player.isShiftKeyDown() ? 0.2f : 0f) -
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0),
// (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation);
// renderArmPre(model.leftSleeve, 0.8F + (player.isShiftKeyDown() ? 0.2f : 0f) -
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0),
// (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation);
// }
//
// if (renderRight && rotRight != null)
// {
// renderArmPre(model.rightArm, (float) rotRight[0], (float) rotRight[2],
// rotation);
// renderArmPre(model.rightSleeve, (float) rotRight[0], (float) rotRight[2],
// rotation);
// }
// else if (renderRight)
// {
// renderArmPre(model.rightArm, 0.8F + (player.isShiftKeyDown() ? 0.2f : 0f) -
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0),
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation);
// renderArmPre(model.rightSleeve, 0.8F + (player.isShiftKeyDown() ? 0.2f : 0f)
// - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0),
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation);
// }
//
// }
// else
// {
// renderArmPre(model.rightArm, 0.8F + (player.isShiftKeyDown() ? 0.2f : 0f) -
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0),
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation);
// renderArmPre(model.rightSleeve, 0.8F + (player.isShiftKeyDown() ? 0.2f : 0f)
// - (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0),
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.15f : 0), rotation);
// renderArmPre(model.leftArm, 0.8F + (player.isShiftKeyDown() ? 0.2f : 0f) -
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0),
// (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation);
// renderArmPre(model.leftSleeve, 0.8F + (player.isShiftKeyDown() ? 0.2f : 0f) -
// (stack.getItem() == RegistrationHandler.itemEntity ? -0.2f : 0),
// (stack.getItem() == RegistrationHandler.itemEntity ? 0.15f : 0), rotation);
// }
//
// }
//
// }
//
//
// @OnlyIn(Dist.CLIENT)
// private void renderArmPre(ModelRenderer arm, float x, float z, float
// rotation)
// {
// arm.xRot = (float) -x;
// arm.yRot = (float) -Math.toRadians(rotation);
// arm.zRot = (float) z;
// }
//
//
// }
