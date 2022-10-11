package tschipp.carryon.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mixin(Inventory.class)
public class InventoryMixin
{
	@Shadow
	public Player player;

//	@Redirect(method = "selected:I", at = @At())
//	private void setSelected(Inventory inv, int value)
//	{
//
//	}

	@Inject(method = "setPickedItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
	private void onPickBlock(CallbackInfo info)
	{
		System.out.println("onPickBlock");
		if(CarryOnDataManager.getCarryData(player).isCarrying())
			info.cancel();
	}

	@Inject(method = "pickSlot(I)V", at = @At("HEAD"), cancellable = true)
	private void onPickSlot(int slot, CallbackInfo info)
	{
		System.out.println("onPickSlot");
		if(CarryOnDataManager.getCarryData(player).isCarrying())
			info.cancel();
	}

	@Inject(method = "swapPaint(D)V", at = @At("HEAD"), cancellable = true)
	private void onSwapPaint(double direction, CallbackInfo info)
	{
		System.out.println(player);
		System.out.println(CarryOnDataManager.getCarryData(player).getNbt());
		System.out.println("onSwapPaint");
		if(CarryOnDataManager.getCarryData(player).isCarrying())
			info.cancel();
	}
}
