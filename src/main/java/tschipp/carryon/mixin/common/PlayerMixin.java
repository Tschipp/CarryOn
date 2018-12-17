package tschipp.carryon.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import tschipp.carryon.interfaces.ICarryOnData;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntity implements ICarryOnData {

    private PlayerMixin(World var1) {
        super(EntityType.PLAYER, var1);
    }

    private static final TrackedData<CompoundTag> CARRYON_DATA = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);;

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    public void onInitDataTracker(CallbackInfo info) {
        this.dataTracker.startTracking(CARRYON_DATA, new CompoundTag());
    }

    @Override
    public CompoundTag getCarryOnData() {
        return (CompoundTag)this.dataTracker.get(CARRYON_DATA);
    }

    @Override
    public void setCarryOnData(CompoundTag tag) {
        this.dataTracker.set(CARRYON_DATA, tag);
    }

}