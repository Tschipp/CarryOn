package tschipp.carryon.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.client.render.CarryRenderHelper;
import tschipp.carryon.client.render.ICarryOnRenderState;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
            shift = At.Shift.AFTER
    ), method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;")
    private void onCreateRenderState(Entity entity, float $$1, CallbackInfoReturnable<EntityRenderState> cir, @Local(ordinal = 0) EntityRenderState state) {
        if (entity instanceof Player player) {
            CarryOnData carry = CarryOnDataManager.getCarryData(player);

            ICarryOnRenderState carryOnRenderState = (ICarryOnRenderState) state;
            carryOnRenderState.setCarryOnData(carry);
            carryOnRenderState.setRenderWidth(CarryRenderHelper.getRenderWidth(player));

            carryOnRenderState.setPlayer(player);
        }
    }
}
