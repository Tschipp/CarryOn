package tschipp.carryon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tschipp.carryon.client.render.CarriedObjectRender;
import tschipp.carryon.client.render.CarryRenderHelper;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin
{

	@Shadow
	private void renderArmWithItem(AbstractClientPlayer player, float partialTicks, float f2, InteractionHand hand, float f3, ItemStack stack, float f4, PoseStack poseStack, MultiBufferSource source, int light) {throw new RuntimeException("CarryOn InHandRendererMixin failed");}


	@Redirect(at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"), method = "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V")
	private void onRenderHand(ItemInHandRenderer instance, AbstractClientPlayer player, float partialTicks, float f2, InteractionHand hand, float f3, ItemStack stack, float f4, PoseStack poseStack, MultiBufferSource source, int light)
	{
		if(!CarriedObjectRender.drawFirstPerson(player, source, poseStack, light, partialTicks) && CarryRenderHelper.getPerspective() == 0)
			renderArmWithItem(player, partialTicks, f2, hand, f3, stack, f4, poseStack, source, light);
	}

}
