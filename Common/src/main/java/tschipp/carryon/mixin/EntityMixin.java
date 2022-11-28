package tschipp.carryon.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingPacket;
import tschipp.carryon.platform.Services;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow
	public boolean hasPassenger(Entity pEntity) {throw new IllegalStateException("EntityMixin application failed");}

	@Shadow public abstract void onPassengerTurned(Entity $$0);

	@Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At("HEAD"), cancellable = true)
	private void onPositionPassenger(Entity entity, MoveFunction move, CallbackInfo ci)
	{
		if((Object)this instanceof Player thisPlayer && entity instanceof Player otherPlayer)
		{
			if(hasPassenger(otherPlayer) && CarryOnDataManager.getCarryData(thisPlayer).isCarrying(CarryType.PLAYER))
			{
				Vec3 forward = new Vec3(0, 0, 0.6);
				Vec3 otherPos = thisPlayer.position().add(forward.yRot((float) Math.toRadians(-thisPlayer.yBodyRot)));
				otherPos = otherPos.add(0, 0.4,0);
				move.accept(otherPlayer, otherPos.x, otherPos.y, otherPos.z);
				((Entity)((Object)this)).onPassengerTurned(otherPlayer);
				ci.cancel();
			}
		}

	}

	@Inject(method = "getDismountLocationForPassenger(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"))
	private void onDismountPassenger(LivingEntity living, CallbackInfoReturnable<Vec3> cir)
	{
		if((Object)this instanceof Player thisPlayer && living instanceof Player otherPlayer)
		{
			CarryOnData carry = CarryOnDataManager.getCarryData(thisPlayer);
			if(carry.isCarrying(CarryType.PLAYER))
			{
				carry.clear();
				CarryOnDataManager.setCarryData(thisPlayer, carry);
				Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_START_RIDING, new ClientboundStartRidingPacket(otherPlayer.getId(), false), (ServerPlayer) thisPlayer);
			}
		}
	}

	@Inject(method = "onPassengerTurned(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
	private void onPassengerTurned(Entity toUpdate, CallbackInfo ci)
	{
		if((Object)this instanceof Player thisPlayer && toUpdate instanceof Player otherPlayer)
		{
			CarryOnData carry = CarryOnDataManager.getCarryData(thisPlayer);
			if(carry.isCarrying(CarryType.PLAYER)) {
				this.clampRotation(toUpdate);
			}
		}
	}

	@Unique
	private void clampRotation(Entity pEntityToUpdate) {
		Entity thisEntity = (Entity)((Object)this);
		pEntityToUpdate.setYBodyRot(thisEntity.getYRot());
		float f = Mth.wrapDegrees(pEntityToUpdate.getYRot() - thisEntity.getYRot());
		float f1 = Mth.clamp(f, -30.0F, 30.0F);
		pEntityToUpdate.yRotO += f1 - f;
		pEntityToUpdate.setYRot(pEntityToUpdate.getYRot() + f1 - f);
		pEntityToUpdate.setYHeadRot(pEntityToUpdate.getYRot());
	}
}
