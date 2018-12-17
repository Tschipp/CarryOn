package tschipp.carryon.mixin.client;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.settings.GameOptions;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.Keyboard;
import net.minecraft.item.ItemStack;
import tschipp.carryon.CarryOn;
import tschipp.carryon.RegistryHandler;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "(JIIII)V", at = @At("HEAD"), cancellable = true)
    public void onKeyPressed(long var1, int code1, int code2, int var5, int var6, CallbackInfo info) 
    {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        GameOptions options = MinecraftClient.getInstance().options;
        if (player != null) {
            ItemStack held = player.getMainHandStack();

            CarryOn.LOGGER.info(player);
            CarryOn.LOGGER.info(held);

            if (!held.isEmpty() && held.getItem() == RegistryHandler.TILE_ITEM || held.getItem() == RegistryHandler.ENTITY_ITEM)
            {
                boolean cancel = false;
                if(options.keyInventory.matches(code1, code2))
                    cancel = true;
                else if(options.keySwapHands.matches(code1, code2))
                    cancel = true;    
                else if(options.keyDrop.matches(code1, code2))
                    cancel = true;            
                else if(options.keyLoadToolbarActivator.matches(code1, code2))
                    cancel = true;  
                else if(options.keySaveToolbarActivator.matches(code1, code2))
                    cancel = true;
                else if(options.keyPickItem.matches(code1, code2))
                    cancel = true;
                else {
                    for(KeyBinding key : options.keysHotbar)
                    {
                        if(key.matches(code1, code2))
                            cancel = true;
                    }
                } 
                
                if(cancel)
                    info.cancel();
            }
        }
    }
}