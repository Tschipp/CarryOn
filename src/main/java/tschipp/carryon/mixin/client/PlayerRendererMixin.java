package tschipp.carryon.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.item.ItemStack;
import tschipp.carryon.RegistryHandler;
import tschipp.carryon.render.BlockRendererLayer;
import tschipp.carryon.render.EntityRendererLayer;
import tschipp.carryon.render.ICarrying;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>{

    private PlayerRendererMixin(EntityRenderDispatcher var1, PlayerEntityModel<AbstractClientPlayerEntity> var2, float var3) {
        super(var1, var2, var3);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstructed(EntityRenderDispatcher renderDispatcher, boolean slim, CallbackInfo info)
    {
        this.addLayer(new BlockRendererLayer(this));
        this.addLayer(new EntityRendererLayer(this));
    }

    @Inject(method = "method_4218", at = @At("RETURN"))
    private void onPreRender(AbstractClientPlayerEntity player, CallbackInfo info) 
    {
        ItemStack stack = player.getMainHandStack();
        ICarrying model = (ICarrying)this.method_4038();

        if (!stack.isEmpty() && stack.getItem() == RegistryHandler.TILE_ITEM)
            model.setCarryingBlock(true);
        else
            model.setCarryingBlock(false);
           
        if (!stack.isEmpty() && stack.getItem() == RegistryHandler.ENTITY_ITEM)
            model.setCarryingEntity(true);
        else
            model.setCarryingEntity(false);
    }
}