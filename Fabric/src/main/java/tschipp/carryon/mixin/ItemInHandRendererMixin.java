package tschipp.carryon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.client.render.CarriedObjectRender;
import tschipp.carryon.client.render.CarryRenderHelper;
import tschipp.carryon.platform.Services;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin
{
	@Inject(at = @At(value = "HEAD"), method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true)
	private void onRenderHand(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci)
	{
		if(CarryRenderHelper.getPerspective() == 0 && CarriedObjectRender.drawFirstPerson(player, buffer, poseStack, light, partialTicks))
			ci.cancel();
	}

}
