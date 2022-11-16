package tschipp.carryon.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {

    @Shadow
    public ModelPart rightArm;

    @Shadow
    public ModelPart leftArm;

    @Inject(at = @At("RETURN"), method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V")
    private void onSetupAnimations(LivingEntity living, float f1, float f2, float f3, float f4, float f5, CallbackInfo ci)
    {
        System.out.println("Reached model injectiob");
        if(living instanceof Player player)
        {
            System.out.println("Model is player");

            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            if(carry.isCarrying() && !player.isVisuallySwimming() && !player.isFallFlying())
            {
                System.out.println("Player is carrying");

                boolean sneaking = !player.getAbilities().flying && player.isShiftKeyDown() || player.isCrouching();

                float x = 1.0f + (sneaking ? 0.2f : 0.0f) - (carry.isCarrying(CarryOnData.CarryType.BLOCK) ? 0.0f : 0.3f);
                float z = 0.15f;

                rightArm.yRot = 0;
                leftArm.yRot = 0;

                rightArm.xRot = -x;
                leftArm.xRot = -x;

                rightArm.zRot = z;
                leftArm.zRot = -z;
            }

        }
    }

}
