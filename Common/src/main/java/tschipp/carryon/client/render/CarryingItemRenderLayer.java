package tschipp.carryon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public class CarryingItemRenderLayer<M extends PlayerModel> extends RenderLayer<AvatarRenderState, M> {
    public CarryingItemRenderLayer(RenderLayerParent<AvatarRenderState, M> renderer) {
        super(renderer);
    }
    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight,
        AvatarRenderState renderState, float yRot, float xRot) {
        if (renderState instanceof ICarryOnRenderState carryOnRenderState){
            CarriedObjectRender.draw(carryOnRenderState.getPlayer(), poseStack, packedLight, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), nodeCollector,false);
        }
    }
}
