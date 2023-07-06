package tschipp.carryon.events;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import tschipp.carryon.CarryOnCommon;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PickupHandler;
import tschipp.carryon.common.carry.PlacementHandler;
import tschipp.carryon.common.scripting.ScriptReloadListener;
import tschipp.carryon.compat.ArchitecturyCompat;
import tschipp.carryon.config.ConfigLoader;
import tschipp.carryon.scripting.IdentifiableScriptReloadListener;

public class CommonEvents {

    public static void registerEvents() {

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if(!client)
                ConfigLoader.onConfigLoaded();
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if(world.isClientSide)
                return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            Direction facing = hitResult.getDirection();

            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            if(!carry.isCarrying())
            {
                if (PickupHandler.tryPickUpBlock((ServerPlayer) player, pos, world, (pState, pPos) -> {
                    boolean success = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pPos, pState, world.getBlockEntity(pPos));
                    return success;
                }))
                    return InteractionResult.SUCCESS;
                return InteractionResult.PASS;
            }
            else
            {
                if(carry.isCarrying(CarryOnData.CarryType.BLOCK))
                {
                    if(PlacementHandler.tryPlaceBlock((ServerPlayer) player, pos, facing, (pState, pPos) -> {
                        return ArchitecturyCompat.sendPlaceEvent(world, pState, pPos, player);
                    }))
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
                if (PickupHandler.tryPickupEntity((ServerPlayer) player, entity, null)) {
                    return InteractionResult.SUCCESS;
                }
            }
            else if(carry.isCarrying(CarryOnData.CarryType.ENTITY) || carry.isCarrying(CarryType.PLAYER))
            {
                PlacementHandler.tryStackEntity((ServerPlayer) player, entity);
            }

            return InteractionResult.PASS;
        });


        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            CarryOnCommon.registerCommands(dispatcher);
        }));


        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableScriptReloadListener());


        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            ScriptReloadListener.syncScriptsWithClient(player);
        });


        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for(ServerPlayer player : server.getPlayerList().getPlayers())
                CarryOnCommon.onCarryTick(player);
        });


        ServerPlayerEvents.COPY_FROM.register(((oldPlayer, newPlayer, alive) -> {
            PlacementHandler.placeCarriedOnDeath(oldPlayer, newPlayer, !alive);
        }));


        PlayerBlockBreakEvents.BEFORE.register(((world, player, pos, state, blockEntity) -> {
            if(!CarryOnCommon.onTryBreakBlock(player))
                return false;
            return true;
        }));

        AttackBlockCallback.EVENT.register(((player, world, hand, pos, direction) -> {
            if(!CarryOnCommon.onTryBreakBlock(player))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        }));

        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if(!CarryOnCommon.onAttackedByPlayer(player))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        }));

        //TODO: drop carried when attacked
    }

}
