package tschipp.carryon.common.carry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PickupHandler {

    //TODO: CONFIG
    private static final double range = 2.0;


    public static boolean canCarryGeneral(ServerPlayer player, Vec3 pos)
    {
        //TODO: Check carry key
        if(!player.isShiftKeyDown())
            return false;

        if(!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())
            return false;

        if(player.position().distanceTo(pos) >= range)
            return false;

        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if(carry.isCarrying())
            return false;

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

        //TODO: Whitelist/Blacklist checks


        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
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

        if (entity instanceof Animal)
            ((Animal) entity).dropLeash(true, true);

        //TODO: White and blacklist

        //TODO: Protections

        CarryOnData carry = CarryOnDataManager.getCarryData(player);


        entity.ejectPassengers();
        carry.setEntity(entity);
        entity.remove(RemovalReason.DISCARDED);

        player.level.playSound(null, player.getOnPos(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.AMBIENT, 1.0f, 0.5f);
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }

}
