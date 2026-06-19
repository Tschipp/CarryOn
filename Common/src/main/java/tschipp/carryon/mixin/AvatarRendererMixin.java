package tschipp.carryon.mixin;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.client.render.CarryingItemRenderLayer;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> extends  LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel>  {

    public AvatarRendererMixin(Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }
   
    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V", at = @At("RETURN"))
    public void init(EntityRendererProvider.Context context, boolean slim, CallbackInfo info) {
        // MC 26.2: The deferred render pipeline removed event-based player render hooks
        // (RenderPlayerEvent on NeoForge, equivalent on Fabric). Render layers added to
        // AvatarRenderer are called instead. This registers our carry render layer.
        this.addLayer(new CarryingItemRenderLayer<>(this));
    }
}
