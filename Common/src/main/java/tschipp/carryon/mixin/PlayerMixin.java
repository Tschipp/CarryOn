package tschipp.carryon.mixin;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    private PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData()V", at = @At("RETURN"))
    private void onDefineSynchedData(CallbackInfo info) {
        this.entityData.define(CarryOnDataManager.CARRY_DATA_KEY, new CompoundTag());
        System.out.println("Added Carry Data!");
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void onAddAdditionalSaveData(CompoundTag tag, CallbackInfo info)
    {
        CarryOnData carry = CarryOnDataManager.getCarryData((Player)(Object)this);
        tag.put("CarryOnData", carry.getNbt());
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void onReadAdditionalSaveData(CompoundTag tag, CallbackInfo info)
    {
        if (tag.contains("CarryOnData")) {
            CarryOnData data = new CarryOnData(tag.getCompound("CarryOnData"));
            CarryOnDataManager.setCarryData((Player) (Object) this, data);
        }
    }

}
