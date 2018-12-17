package tschipp.carryon;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PickupHandler {

    public static boolean canPlayerPickUpBlock(PlayerEntity player, BlockEntity te, World world, BlockPos pos)
    {
        return true;
    }

    public static boolean canPlayerPickUpEntity(PlayerEntity player, Entity entity)
    {
        return true;
    }

}