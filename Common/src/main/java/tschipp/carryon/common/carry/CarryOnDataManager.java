package tschipp.carryon.common.carry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;

public class CarryOnDataManager {

    public static final EntityDataAccessor<CompoundTag> CARRY_DATA_KEY = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);


    public CarryOnData getCarryData(Player player)
    {
        CompoundTag data = player.getEntityData().get(CARRY_DATA_KEY);
        return new CarryOnData(data);
    }

    public void setCarryData(Player player, CarryOnData data)
    {
        player.getEntityData().set(CARRY_DATA_KEY, data.getNbt());
    }

}
