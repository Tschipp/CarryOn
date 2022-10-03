package tschipp.carryon.mixin;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mixin(Player.class)
public abstract class PlayerMixin extends Entity {

    public PlayerMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(at = @At("RETURN"), method = "defineSynchedData()V")
    private void onDefineSynchedData(CallbackInfo info) {
        this.entityData.define(CarryOnDataManager.CARRY_DATA_KEY, new CompoundTag());
    }

}
