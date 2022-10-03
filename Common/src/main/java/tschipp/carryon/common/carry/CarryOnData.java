package tschipp.carryon.common.carry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CarryOnData {

    private CarryType type;
    private CompoundTag nbt;

    public CarryOnData(CompoundTag data)
    {
        if(data.contains("type"))
            this.type = CarryType.valueOf(data.getString("type"));
        else
            this.type = CarryType.INVALID;

        this.nbt = data;
    }

    public CompoundTag getNbt()
    {
        nbt.putString("type", type.toString());
        return nbt;
    }

    public void setBlock(BlockState state, @Nullable BlockEntity tile)
    {
        this.type = CarryType.BLOCK;
        CompoundTag stateData = NbtUtils.writeBlockState(state);
        nbt.put("block", stateData);

        if(tile != null)
        {
            CompoundTag tileData = tile.saveWithId();
            nbt.put("tile", tileData);
        }
    }

    public BlockState getBlock()
    {
        if(this.type != CarryType.BLOCK)
            throw new IllegalStateException("Called getBlock on data that contained " + this.type);

        return NbtUtils.readBlockState(nbt.getCompound("block"));
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos)
    {
        if(this.type != CarryType.BLOCK)
            throw new IllegalStateException("Called getBlockEntity on data that contained " + this.type);

        if(!nbt.contains("tile"))
            return null;

        return BlockEntity.loadStatic(pos, this.getBlock(), nbt.getCompound("tile"));
    }

    public void setEntity(Entity entity)
    {
        this.type = CarryType.ENTITY;
        CompoundTag entityData = new CompoundTag();
        entity.save(entityData);
        nbt.put("entity", entityData);
    }

    public Entity getEntity(Level level)
    {
        if(this.type != CarryType.ENTITY)
            throw new IllegalStateException("Called getEntity on data that contained " + this.type);

        return EntityType.create(nbt.getCompound("entity"), level).orElseThrow();
    }

    public enum CarryType {
        BLOCK,
        ENTITY,
        INVALID
    }
}
