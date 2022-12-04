package tschipp.carryon.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PlacementHandler;

@Mixin(Player.class)
public class PlayerMixinFabric
{
	@Inject(at = @At("HEAD"), method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	private void onHurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
		if(Constants.COMMON_CONFIG.settings.dropCarriedWhenHit)
		{
			Player player = ((Player)(Object)this);
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if(carry.isCarrying() && !player.level.isClientSide)
				PlacementHandler.placeCarried((ServerPlayer)player);
		}

	}
}
