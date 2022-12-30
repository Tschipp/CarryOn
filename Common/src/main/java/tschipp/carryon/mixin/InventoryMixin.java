package tschipp.carryon.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mixin(Inventory.class)
public class InventoryMixin
{
	private static final ItemStack DUMMY_STACK = new ItemStack(Blocks.COBBLESTONE, 1);

	@Shadow
	public Player player;

	@Shadow
	public int selected;

	@Shadow
	public NonNullList<ItemStack> items;

	@Redirect(method = "getFreeSlot()I", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"))
	private <E> E getFreeSlotEmptyCheck(NonNullList<E> instance, int i)
	{
		if(i == selected && CarryOnDataManager.getCarryData(player).isCarrying())
		{
			return (E) DUMMY_STACK;
		}
		else
			return instance.get(i);
	}

	@Inject(method = "setPickedItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
	private void onPickBlock(CallbackInfo info)
	{
		if(CarryOnDataManager.getCarryData(player).isCarrying())
			info.cancel();
	}

	@Inject(method = "pickSlot(I)V", at = @At("HEAD"), cancellable = true)
	private void onPickSlot(int slot, CallbackInfo info)
	{
		if(CarryOnDataManager.getCarryData(player).isCarrying())
			info.cancel();
	}

	@Inject(method = "swapPaint(D)V", at = @At("HEAD"), cancellable = true)
	private void onSwapPaint(double direction, CallbackInfo info)
	{
		if(CarryOnDataManager.getCarryData(player).isCarrying())
			info.cancel();
	}
}
