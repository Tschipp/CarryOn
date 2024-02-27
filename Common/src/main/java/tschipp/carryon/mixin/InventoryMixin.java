/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.carryon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

	@WrapOperation(method = "getFreeSlot()I", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"))
	private Object getFreeSlotEmptyCheck(NonNullList<Object> instance, int slot, Operation<Object> original)
	{
		if(slot == selected && CarryOnDataManager.getCarryData(player).isCarrying())
		{
			return DUMMY_STACK;
		}
		else
			return original.call(instance, slot);
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
