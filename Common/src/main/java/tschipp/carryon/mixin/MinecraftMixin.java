package tschipp.carryon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	@Redirect(method = "handleKeybinds()V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", ordinal = 0, opcode = 181)) //Opcode for PUTFIELD
	private void onSlotSelected(Inventory inv,int slot)
	{
		if(!CarryOnDataManager.getCarryData(inv.player).isCarrying())
			inv.selected = slot;
	}
}
