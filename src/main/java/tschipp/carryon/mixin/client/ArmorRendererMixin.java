package tschipp.carryon.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.class_3883;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.entity.ArmorBipedEntityRenderer;
import net.minecraft.client.render.entity.ArmorEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import tschipp.carryon.RegistryHandler;
import tschipp.carryon.render.ICarrying;

@Mixin(ArmorBipedEntityRenderer.class)
public abstract class ArmorRendererMixin<T extends LivingEntity, M extends Model<T>, A extends BipedEntityModel<T>> extends ArmorEntityRenderer<T, M, A> {

    protected ArmorRendererMixin(class_3883<T, M> var1, A var2, A var3) {
        super(var1, var2, var3);
    }

    //Used to be "render"
    @Inject(method = "method_17157", at = @At("HEAD"))
    private void onRenderArmor(LivingEntity living, float var2, float var3, float var4, float var5, float var6, float var7, float var8, CallbackInfo info) 
    {
        ItemStack stack = living.getMainHandStack();
        ICarrying model = (ICarrying)this.modelBody;

        if (!stack.isEmpty() && stack.getItem() == RegistryHandler.TILE_ITEM)
            model.setCarryingBlock(true);
        else
            model.setCarryingBlock(false);
           
        if (!stack.isEmpty() && stack.getItem() == RegistryHandler.ENTITY_ITEM)
            model.setCarryingEntity(true);
        else
            model.setCarryingEntity(false);
    }

    @Override
    public void method_17157(T var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
        super.method_17157(var1, var2, var3, var4, var5, var6, var7, var8);
    }

}