package tschipp.carryon.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Mouse;
import net.minecraft.item.ItemStack;
import tschipp.carryon.CarryOn;
import tschipp.carryon.RegistryHandler;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "(JDD)V", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolling(long var1, double var3, double var5, CallbackInfo info) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ItemStack held = player.getMainHandStack();

        CarryOn.LOGGER.info(player);
        CarryOn.LOGGER.info(held);

        if (!held.isEmpty() && held.getItem() == RegistryHandler.TILE_ITEM
                || held.getItem() == RegistryHandler.ENTITY_ITEM)
            info.cancel();
    }

    @Inject(method = "(JIII)V", at = @At("HEAD"), cancellable = true)
    private void onMouseButtonPress(long var1, int code, int var4, int var5, CallbackInfo info) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            ItemStack held = player.getMainHandStack();

            CarryOn.LOGGER.info(player);
            CarryOn.LOGGER.info(held);

            if (!held.isEmpty() && held.getItem() == RegistryHandler.TILE_ITEM
                    || held.getItem() == RegistryHandler.ENTITY_ITEM)
                if (MinecraftClient.getInstance().options.keyPickItem.matches(code))
                    info.cancel();
        }
    }
}