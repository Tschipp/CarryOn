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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import tschipp.carryon.Constants;
import tschipp.carryon.common.scripting.CarryOnScript;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class CarryOnData {

    private CarryType type;
    private CompoundTag nbt;
    private boolean keyPressed = false;
    private CarryOnScript activeScript;
    private int selectedSlot = 0;
    private static final ProblemReporter problemReporter = new ProblemReporter.ScopedCollector(Constants.LOG);


    public static final Codec<CarryOnData> CODEC = CompoundTag.CODEC.flatXmap(
            tag -> {
                try {
                    return DataResult.success(new CarryOnData(tag));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            },
            carry -> {
                try {
                    return DataResult.success(carry.getNbt());
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CarryOnData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final String SERIALIZATION_KEY = "CarryOnData";

    public CarryOnData(CompoundTag data)
    {
        if(data.contains("type"))
            this.type = CarryType.valueOf(data.getStringOr("type", "INVALID"));
        else
            this.type = CarryType.INVALID;

        this.nbt = data;

        this.keyPressed = data.getBooleanOr("keyPressed", false);

        if(data.contains("activeScript"))
        {
            DataResult<CarryOnScript> res = CarryOnScript.CODEC.parse(NbtOps.INSTANCE, data.get("activeScript"));
            this.activeScript = res.getOrThrow((s) -> {throw new RuntimeException("Failed to decode activeScript during CarryOnData serialization: " + s);});
        }

        this.selectedSlot = data.getIntOr("selected", 0);

    }

    public CarryType getType()
    {
        return this.type;
    }

    public CompoundTag getNbt()
    {
        // ROOT FIX for server crash (crash-2026-07-15_13.32.29):
        // Fabric's fabric:attachment_sync_v1 calls this method on a Netty IO thread.
        // The original code wrote directly to this.nbt (putString/putBoolean/putInt),
        // which raced with server-thread reads of this.nbt in clone() → nbt.copy().
        // Concurrent write+read on a fastutil Object2ObjectOpenHashMap (non-thread-safe)
        // corrupts its backing array → NPE: "this.wrapped is null".
        // Fix: write into a snapshot copy only, never mutating the live this.nbt here.
        CompoundTag out = nbt.copy();
        out.putString("type", type.toString());
        out.putBoolean("keyPressed", keyPressed);
        if(activeScript != null)
        {
            DataResult<Tag> res = CarryOnScript.CODEC.encodeStart(NbtOps.INSTANCE, activeScript);
            Tag tag = res.getOrThrow((s) -> {throw new RuntimeException("Failed to encode activeScript during CarryOnData serialization: " + s);});
            out.put("activeScript", tag);
        }
        out.putInt("selected", this.selectedSlot);
        return out;
    }

    public CompoundTag getContentNbt()
    {
        if(type == CarryType.BLOCK && nbt.contains("block"))
            return nbt.getCompoundOrEmpty("block");
        else if(type == CarryType.ENTITY && nbt.contains("entity"))
            return nbt.getCompoundOrEmpty("entity");
        return null;
    }

    public void setBlock(BlockState state, @Nullable BlockEntity tile, ServerPlayer player, BlockPos pos)
    {
        this.type = CarryType.BLOCK;

        if(state.hasProperty(BlockStateProperties.WATERLOGGED))
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);

        CompoundTag stateData = NbtUtils.writeBlockState(state);
        nbt.put("block", stateData);

        if(tile != null)
        {
            TagValueOutput output = TagValueOutput.createWithContext(problemReporter, player.registryAccess());
            tile.saveWithId(output);
            Tag tileData = output.buildResult();
            nbt.put("tile", tileData);
        }
    }

    public BlockState getBlock()
    {
        if(this.type != CarryType.BLOCK)
            throw new IllegalStateException("Called getBlock on data that contained " + this.type);

        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK, nbt.getCompoundOrEmpty("block"));
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos, HolderLookup.Provider lookup)
    {
        if(this.type != CarryType.BLOCK)
            throw new IllegalStateException("Called getBlockEntity on data that contained " + this.type);

        if(!nbt.contains("tile"))
            return null;

        return BlockEntity.loadStatic(pos, this.getBlock(), nbt.getCompoundOrEmpty("tile"), lookup);
    }

    public void setEntity(Entity entity)
    {
        this.type = CarryType.ENTITY;
        TagValueOutput output = TagValueOutput.createWithContext(new ProblemReporter.ScopedCollector(Constants.LOG), entity.registryAccess());
        entity.save(output);
        Tag entityData = output.buildResult();
        nbt.put("entity", entityData);
    }

    public Entity getEntity(Level level)
    {
        if(this.type != CarryType.ENTITY)
            throw new IllegalStateException("Called getEntity on data that contained " + this.type);

        ValueInput in = TagValueInput.create(problemReporter, level.registryAccess(), nbt.getCompoundOrEmpty("entity"));
        // MC 26.2: EntityType#create now takes an EntitySpawnRequest instead of a raw EntitySpawnReason.
        var optionalEntity = EntityType.create(in, level, new EntitySpawnRequest(EntitySpawnReason.BUCKET, false));
        if(optionalEntity.isPresent())
            return optionalEntity.get();

        Constants.LOG.error("Called EntityType#create even though no entity data was present. Data: " + nbt.toString());
        this.clear();
        return new AreaEffectCloud(level, 0, 0, 0);
    }

    public Optional<CarryOnScript> getActiveScript()
    {
        if(activeScript == null)
            return Optional.empty();
        return Optional.of(activeScript);
    }

    public void setActiveScript(CarryOnScript script)
    {
        this.activeScript = script;
    }

    public void setCarryingPlayer(Player player) 
    {
        this.type = CarryType.PLAYER;
        nbt.putString("player",  player.getStringUUID().toString());
    }

    public Player getCarryingPlayer(Level level) 
    {
        if(this.type != CarryType.PLAYER)
            throw new IllegalStateException("Called getCarryingPlayer on data that contained " + this.type);
        if(!nbt.contains("player"))
            return null;
        UUID uuid = UUID.fromString(nbt.getString("player").get());
        return level.getServer().getPlayerList().getPlayer(uuid);
    }

    public boolean isCarrying()
    {
        return this.type != CarryType.INVALID;
    }

    public boolean isCarrying(CarryType type)
    {
        return this.type == type;
    }

    public boolean isKeyPressed() {return this.keyPressed;}

    public void setKeyPressed(boolean val) {
        this.keyPressed = val;
        this.nbt.putBoolean("keyPressed", val);
    }

    public void setSelected(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public int getSelected() {
        return this.selectedSlot;
    }

    public void clear()
    {
        this.type = CarryType.INVALID;
        this.nbt = new CompoundTag();
        this.activeScript = null;
    }

    public CarryOnData clone() {
        try {
            return new CarryOnData(nbt.copy());
        } catch (NullPointerException e) {
            // Safety net: nbt.copy() iterates a fastutil Object2ObjectOpenHashMap whose
            // backing array can be null if a concurrent write raced with this read. Returning
            // empty carry data drops the carried item but prevents a server crash.
            Constants.LOG.error("CarryOnData.clone() caught NPE in nbt.copy() — returning empty carry state to prevent server crash", e);
            return new CarryOnData(new CompoundTag());
        }
    }

    public int getTick()
    {
        return this.nbt.getIntOr("tick", -1);
    }

    public void setTick(int tick) {
        this.nbt.putInt("tick", tick);
    }


    public enum CarryType {
        BLOCK,
        ENTITY,
        PLAYER,
        INVALID
    }
}
