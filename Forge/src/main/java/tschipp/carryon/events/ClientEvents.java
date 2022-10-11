package tschipp.carryon.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tschipp.carryon.Constants;
import tschipp.carryon.client.render.CarriedObjectRender;
import tschipp.carryon.client.render.CarryRenderHelper;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void renderHand(RenderHandEvent event)
	{
		Player player = Minecraft.getInstance().player;
		MultiBufferSource buffer = event.getMultiBufferSource();
		PoseStack matrix = event.getPoseStack();
		int light = event.getPackedLight();

		if(CarriedObjectRender.drawFirstPerson(player, buffer, matrix, light) && CarryRenderHelper.getPerspective() == 0)
			event.setCanceled(true);
	}
}
