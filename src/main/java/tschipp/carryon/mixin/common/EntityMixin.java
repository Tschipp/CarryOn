package tschipp.carryon.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import tschipp.carryon.PickupHandler;
import tschipp.carryon.RegistryHandler;
import tschipp.carryon.items.ItemEntity;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public boolean field_6037;

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> info) {
        
            ItemStack main = player.getMainHandStack();
            ItemStack off = player.getOffHandStack();
            World world = player.world;
            Entity entity = ((Entity) (Object) this);

            if (main.isEmpty() && off.isEmpty() && player.isSneaking()) {
                ItemStack stack = new ItemStack(RegistryHandler.ENTITY_ITEM);

                if (!this.field_6037) {
                    if (entity instanceof AnimalEntity)
                        ((AnimalEntity) entity).detachLeash(true, true);

                    if (PickupHandler.canPlayerPickUpEntity(player, entity)) {
                        if (ItemEntity.storeEntityData(entity, world, stack)) {
                            // if (entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                            //     IItemHandler handler = entity
                            //             .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                            //     for (int i = 0; i < handler.getSlots(); i++) {
                            //         handler.extractItem(i, 64, false);
                            //     }
                            // }

                            // CarryOnOverride override = ScriptChecker.inspectEntity(entity);
                            // int overrideHash = 0;
                            // if (override != null)
                            //     overrideHash = override.hashCode();

                            // ItemEvents.sendPacket(player, player.inventory.currentItem, overrideHash);

                            // if (entity instanceof LivingEntity)
                            //     ((LivingEntity) entity).setHealth(0);

                            entity.invalidate();
                            player.setStackInHand(Hand.MAIN, stack);
                            info.cancel();
                            info.setReturnValue(false);
                        }
                    }
                }

            }
        
    }

}