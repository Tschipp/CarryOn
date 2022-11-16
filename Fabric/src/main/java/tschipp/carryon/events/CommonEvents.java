package tschipp.carryon.events;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PickupHandler;
import tschipp.carryon.common.carry.PlacementHandler;

public class CommonEvents {

    public static void registerEvents() {

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if(world.isClientSide)
                return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            Direction facing = hitResult.getDirection();

            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            if(!carry.isCarrying())
            {
                if (PickupHandler.tryPickUpBlock((ServerPlayer) player, pos, world))
                    return InteractionResult.SUCCESS;
                return InteractionResult.PASS;
            }
            else
            {
                if(carry.isCarrying(CarryOnData.CarryType.BLOCK))
                {
                    if(PlacementHandler.tryPlaceBlock((ServerPlayer) player, pos, facing, null))
                        return InteractionResult.SUCCESS;
                }
                else
                {
                    if(PlacementHandler.tryPlaceEntity((ServerPlayer) player, pos, facing, null))
                        return InteractionResult.SUCCESS;
                }

                //Fail here, so that we don't interact with placed things
                return InteractionResult.FAIL;
            }
        });




        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {

            if(level.isClientSide)
                return InteractionResult.PASS;

            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            if (!carry.isCarrying()) {
                if (PickupHandler.tryPickupEntity((ServerPlayer) player, entity)) {
                    return InteractionResult.SUCCESS;
                }
            }
            else if(carry.isCarrying(CarryOnData.CarryType.ENTITY))
            {
                //TODO: Stacking
            }

            return InteractionResult.PASS;
        });

    }

}
