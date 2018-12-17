package tschipp.carryon.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.FirstPersonRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import tschipp.carryon.RegistryHandler;
import tschipp.carryon.render.BlockRendererLayer;
import tschipp.carryon.render.EntityRendererLayer;

@Mixin(FirstPersonRenderer.class)
public class FirstPersonMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    public void onRenderItem(float partialTicks,  CallbackInfo info)
    {   
        PlayerEntity player = MinecraftClient.getInstance().player;
        ItemStack stack = player.getMainHandStack();

        if(!stack.isEmpty() && stack.getItem() == RegistryHandler.TILE_ITEM)
            BlockRendererLayer.renderFirstPerson(player, stack, partialTicks);
        else if(!stack.isEmpty() && stack.getItem() == RegistryHandler.ENTITY_ITEM)
            EntityRendererLayer.renderFirstPerson(player, stack, partialTicks);
    }

}