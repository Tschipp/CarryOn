package tschipp.carryon.common.carry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.Constants;

import java.util.UUID;

public class PickupHandler {

    public static boolean canCarryGeneral(ServerPlayer player, Vec3 pos)
    {
        //TODO: Check carry key
        if(!player.isShiftKeyDown())
            return false;

        if(!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())
            return false;

        if(player.position().distanceTo(pos) > Constants.COMMON_CONFIG.settings.maxDistance)
            return false;

        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if(carry.isCarrying())
            return false;

        //Needed so that we don't pick up and place in the same tick
        if(player.tickCount == carry.getTick())
            return false;

        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
            return false;



        return true;
    }


    public static boolean tryPickUpBlock(ServerPlayer player, BlockPos pos, Level level)
    {
        if(!canCarryGeneral(player, Vec3.atCenterOf(pos)))
            return false;

        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);

        if(!ListHandler.isPermitted(state.getBlock()))
            return false;

        if(state.getDestroySpeed(level, pos) == -1 && !player.isCreative())
            return false;

        if(blockEntity == null && !Constants.COMMON_CONFIG.settings.pickupAllBlocks)
            return false;

        //Check if TE is locked
        if(blockEntity != null)
        {
            CompoundTag nbt = blockEntity.saveWithId();
            if(nbt.contains("Lock") && !nbt.getString("Lock").equals(""))
                return false;
        }

        //TODO: Script conditions

        //TODO: Gamestages conditions check

        //TODO: Protections

        carry.setBlock(state, blockEntity);

        level.removeBlockEntity(pos);
        level.removeBlock(pos, false);

        CarryOnDataManager.setCarryData(player, carry);
        level.playSound(null, pos, state.getSoundType().getHitSound(), SoundSource.BLOCKS, 1.0f, 0.5f);
        player.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }



    public static boolean tryPickupEntity(ServerPlayer player, Entity entity)
    {
        if(!canCarryGeneral(player, entity.position()))
            return false;

        if (entity.invulnerableTime != 0)
            return false;

        if (entity instanceof Player)
            return false;

        if (entity instanceof TamableAnimal tame)
        {
            UUID owner = tame.getOwnerUUID();
            UUID playerID = player.getGameProfile().getId();
            if (owner != null && !owner.equals(playerID))
                return false;
        }

        if(!ListHandler.isPermitted(entity))
        {
            //We can pick up baby animals even if the grown up animal is blacklisted.
            if(!(entity instanceof AgeableMob ageableMob && Constants.COMMON_CONFIG.settings.allowBabies && (ageableMob.getAge() < 0 || ageableMob.isBaby())))
                return false;
        }

        //Non-Creative only guards
        if(!player.isCreative())
        {
            if(!Constants.COMMON_CONFIG.settings.pickupHostileMobs && entity.getType().getCategory() == MobCategory.MONSTER)
                return false;

            if(Constants.COMMON_CONFIG.settings.maxEntityHeight > entity.getBbHeight() || Constants.COMMON_CONFIG.settings.maxEntityWidth > entity.getBbWidth())
                return false;
        }

        //TODO: Script conditions

        //TODO: Gamestages conditions check

        //TODO: Protections

        CarryOnData carry = CarryOnDataManager.getCarryData(player);

        entity.ejectPassengers();
        if (entity instanceof Animal animal)
            animal.dropLeash(true, true);

        carry.setEntity(entity);
        entity.remove(RemovalReason.UNLOADED_WITH_PLAYER);

        player.level.playSound(null, player.getOnPos(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.AMBIENT, 1.0f, 0.5f);
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }

}
