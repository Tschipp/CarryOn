package tschipp.carryon.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import tschipp.carryon.render.ICarrying;

@Mixin(BipedEntityModel.class)
public class BipedModelMixin implements ICarrying {

    private boolean isCarryingBlock;
    private boolean isCarryingEntity;

    @Shadow
    public Cuboid armRight;
    @Shadow
    public Cuboid armLeft;

    @Shadow
    public boolean isSneaking;


    public boolean isCarryingBlock()
    {
        return isCarryingBlock;
    }

    public boolean isCarryingEntity()
    {
        return isCarryingEntity;
    }

    public void setCarryingBlock(boolean isCarrying)
    {
        this.isCarryingBlock = isCarrying;
    }

    public void setCarryingEntity(boolean isCarrying)
    {
        this.isCarryingEntity = isCarrying;
    }

    //setRotationAngles
    @Inject(method = "method_17087", at = @At("RETURN"))
    public void onSetAngles(LivingEntity var1, float var2, float var3, float var4, float var5, float var6, float var7, CallbackInfo info) 
    {
        if(this.isCarryingBlock())
        {
            armRight.pitch = -1F + (this.isSneaking ? 0f : 0.2f);
            armLeft.pitch = -1F + (this.isSneaking ? 0f : 0.2f);
            armRight.roll = 0f;
            armLeft.roll = 0f;
            armRight.yaw = 0f;
            armLeft.yaw = 0f;
        }
        else if (this.isCarryingEntity())
        {
            armRight.pitch = -1.2F + (this.isSneaking ? 0f : 0.2f);
            armLeft.pitch = -1.2F + (this.isSneaking ? 0f : 0.2f);
            armRight.roll = -0.15f;
            armLeft.roll = 0.15f;
            armRight.yaw = 0f;
            armLeft.yaw = 0f;
        }
    }


}