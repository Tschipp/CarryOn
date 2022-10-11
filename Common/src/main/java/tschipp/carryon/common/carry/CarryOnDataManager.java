package tschipp.carryon.common.carry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.ObjectUtils;

public class CarryOnDataManager {

    public static final EntityDataAccessor<CompoundTag> CARRY_DATA_KEY = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);


    public static CarryOnData getCarryData(Player player)
    {
        CompoundTag data = player.getEntityData().get(CARRY_DATA_KEY);
        return new CarryOnData(data.copy());
    }

    public static void setCarryData(Player player, CarryOnData data)
    {
        CompoundTag nbt = data.getNbt();
        nbt.putInt("tick", player.tickCount);
        System.out.println(player.getEntityData().isDirty());
        System.out.println("Old: " + player.getEntityData().get(CARRY_DATA_KEY) + ", New: " + nbt + ", NotEqual: " + ObjectUtils.notEqual(player.getEntityData().get(CARRY_DATA_KEY), nbt));
        player.getEntityData().set(CARRY_DATA_KEY, nbt);
        System.out.println(player.getEntityData().isDirty());
    }

}
