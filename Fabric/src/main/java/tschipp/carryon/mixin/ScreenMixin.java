package tschipp.carryon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mixin(Screen.class)
public class ScreenMixin
{
	@Inject(at = @At(value = "TAIL"), method = "init(Lnet/minecraft/client/Minecraft;II)V")
	private void onInit(Minecraft mc, int i, int j, CallbackInfo ci)
	{
		Player player = mc.player;
		if((Object)this instanceof AbstractContainerScreen && player != null)
		{
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if(carry.isCarrying())
				mc.setScreen((Screen)null);
		}
	}
}
