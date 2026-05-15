/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.carryon.common.carry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.CarryOnCommon;
import tschipp.carryon.Constants;
import tschipp.carryon.common.config.ListHandler;
import tschipp.carryon.common.pickupcondition.PickupCondition;
import tschipp.carryon.common.pickupcondition.PickupConditionHandler;
import tschipp.carryon.common.scripting.CarryOnScript;
import tschipp.carryon.common.scripting.ScriptManager;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingOtherPlayerPacket;
import tschipp.carryon.platform.Services;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PickupHandler {

    public static boolean canCarryGeneral(ServerPlayer player, Vec3 pos)
    {
        if(!player.getMainHandItem().isEmpty())
            return false;

        if(player.position().distanceTo(pos) > Constants.COMMON_CONFIG.settings.maxDistance)
            return false;

        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if(carry.isCarrying())
            return false;

        if(!carry.isKeyPressed())
            return false;

        //Needed so that we don't pick up and place in the same tick
        if(player.tickCount == carry.getTick())
            return false;

        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
            return false;



        return true;
    }


    public static boolean tryPickUpBlock(ServerPlayer player, BlockPos pos, Level level, @Nullable BiFunction<BlockState, BlockPos, Boolean> pickupCallback)
    {
        if(!canCarryGeneral(player, Vec3.atCenterOf(pos)))
            return false;

        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
        CompoundTag nbt = null;
        if(blockEntity != null) {
            TagValueOutput output = TagValueOutput.createWithContext(new ProblemReporter.ScopedCollector(Constants.LOG), level.registryAccess());
            blockEntity.saveWithId(output);
            nbt = output.buildResult();
        }

        Optional<CarryOnScript> result =  ScriptManager.inspectBlock(state, level, pos, nbt);
        boolean overrideChecks = result.map(CarryOnScript::overrideChecks).orElse(false);

        if(!ListHandler.isPermitted(state.getBlock()))
            return false;

        // Reject pickup of Double blocks, if they use the vanilla property
        if(hasPropertyType(state, DoorBlock.HALF))
            return false;

        if(!overrideChecks && (state.getDestroySpeed(level, pos) == -1 && !player.isCreative() && !Constants.COMMON_CONFIG.settings.pickupUnbreakableBlocks))
            return false;

        if(!overrideChecks && (blockEntity == null && !Constants.COMMON_CONFIG.settings.pickupAllBlocks))
            return false;

        //Check if TE is locked
        if(blockEntity != null)
        {
            if(nbt.contains("Lock") && !nbt.getString("Lock").equals(""))
                return false;
        }

        Optional<PickupCondition> cond = PickupConditionHandler.getPickupCondition(state);
        if(cond.isPresent())
        {
            if(!cond.get().isFulfilled(player))
                return false;
        }

        boolean doPickup = pickupCallback == null ? true : pickupCallback.apply(state, pos);
        if(!doPickup)
            return false;

        if(result.isPresent())
        {
            CarryOnScript script = result.get();
            if(!script.fulfillsConditions(player))
                return false;

            carry.setActiveScript(script);

            String cmd = script.scriptEffects().commandInit();
            if(!cmd.isEmpty())
                player.level().getServer().getCommands().performPrefixedCommand(player.level().getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().name() + " run " + cmd);
        }

        carry.setBlock(state, blockEntity, player, pos);

        level.removeBlockEntity(pos);
        level.removeBlock(pos, false);

        CarryOnDataManager.setCarryData(player, carry);
        level.playSound(null, pos, state.getSoundType().getHitSound(), SoundSource.BLOCKS, 1.0f, 0.5f);
        player.swing(InteractionHand.MAIN_HAND, true);
        if (!player.isCreative() || Constants.COMMON_CONFIG.settings.slownessInCreative)
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100000000, CarryOnCommon.potionLevel(carry, player.level()), false, false));
        return true;
    }



    public static boolean tryPickupEntity(ServerPlayer player, Entity entity, @Nullable Function<Entity, Boolean> pickupCallback)
    {
        if(!canCarryGeneral(player, entity.position()))
            return false;

        if (entity.invulnerableTime != 0)
            return false;

        if(entity.isRemoved())
            return false;

        if (entity instanceof TamableAnimal tame)
        {
            EntityReference<LivingEntity> ref  = tame.getOwnerReference();
            if (ref != null) {
                UUID owner = ref.getUUID();
                UUID playerID = player.getGameProfile().id();
                if (!owner.equals(playerID))
                    return false;
            }
        }

        Optional<CarryOnScript> result =  ScriptManager.inspectEntity(entity);
        boolean overrideChecks = result.map(CarryOnScript::overrideChecks).orElse(false);

        if(!ListHandler.isPermitted(entity))
        {
            //We can pick up baby animals even if the grown up animal is blacklisted.
            if(!overrideChecks && (!(entity instanceof AgeableMob ageableMob && Constants.COMMON_CONFIG.settings.allowBabies && (ageableMob.getAge() < 0 || ageableMob.isBaby()))))
                return false;
        }

        //Non-Creative only guards
        if(!player.isCreative())
        {
            if(!overrideChecks && (!Constants.COMMON_CONFIG.settings.pickupHostileMobs && entity.getType().getCategory() == MobCategory.MONSTER))
                return false;

            if(Constants.COMMON_CONFIG.settings.maxEntityHeight < entity.getBbHeight() || Constants.COMMON_CONFIG.settings.maxEntityWidth < entity.getBbWidth())
                return false;
        }

        Optional<PickupCondition> cond = PickupConditionHandler.getPickupCondition(entity);
        if(cond.isPresent())
        {
            if(!cond.get().isFulfilled(player))
                return false;
        }

        boolean doPickup = pickupCallback == null || pickupCallback.apply(entity);
        if(!doPickup)
            return false;

        CarryOnData carry = CarryOnDataManager.getCarryData(player);

        if(result.isPresent())
        {
            CarryOnScript script = result.get();
            if(!script.fulfillsConditions(player))
                return false;

            carry.setActiveScript(script);
        }

        if (entity instanceof Player otherPlayer) {
            if (!Constants.COMMON_CONFIG.settings.pickupPlayers)
                return false;

            if (!player.isCreative() && otherPlayer.isCreative())
                return false;

            otherPlayer.ejectPassengers();
            otherPlayer.stopRiding();

            if (result.isPresent()) {
                String cmd = result.get().scriptEffects().commandInit();
                if (!cmd.isEmpty())
                    player.level().getServer().getCommands().performPrefixedCommand(player.level().getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().name() + " run " + cmd);
            }

            otherPlayer.startRiding(player, true, false);
            Services.PLATFORM.sendPacketToAllPlayers(Constants.PACKET_ID_START_RIDING_OTHER, new ClientboundStartRidingOtherPlayerPacket(player.getId(), otherPlayer.getId(), true), player.level());
            carry.setCarryingPlayer(otherPlayer);
            player.swing(InteractionHand.MAIN_HAND, true);
            player.level().playSound(null, player.getOnPos(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.AMBIENT, 1.0f, 0.5f);
            CarryOnDataManager.setCarryData(player, carry);
            if (!player.isCreative() || Constants.COMMON_CONFIG.settings.slownessInCreative)
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100000000, CarryOnCommon.potionLevel(carry, player.level()), false, false));
            return true;

        }

        entity.ejectPassengers();
        entity.stopRiding();
        if (entity instanceof Animal animal) {
            animal.dropLeash();
        }

        if(result.isPresent())
        {
            String cmd = result.get().scriptEffects().commandInit();
            if(!cmd.isEmpty())
                player.level().getServer().getCommands().performPrefixedCommand(player.level().getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().name() + " run " + cmd);
        }

        carry.setEntity(entity);
        entity.remove(RemovalReason.UNLOADED_WITH_PLAYER);

        player.level().playSound(null, player.getOnPos(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.AMBIENT, 1.0f, 0.5f);
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
        if (!player.isCreative() || Constants.COMMON_CONFIG.settings.slownessInCreative)
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100000000, CarryOnCommon.potionLevel(carry, player.level()), false, false));
        return true;
    }

    private static <T extends Comparable<T>> boolean hasPropertyType(BlockState state, Property<T> prop) {
        for (var p : state.getProperties()) {
            if(p.getValueClass().equals(prop.getValueClass()))
                return true;
        }
        return false;
    }

}
