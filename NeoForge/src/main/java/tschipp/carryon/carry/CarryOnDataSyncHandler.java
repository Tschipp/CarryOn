package tschipp.carryon.carry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;
import tschipp.carryon.common.carry.CarryOnData;

public class CarryOnDataSyncHandler implements AttachmentSyncHandler<CarryOnData> {
    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, CarryOnData carryOnData, boolean b) {
        CarryOnData.STREAM_CODEC.encode(registryFriendlyByteBuf, carryOnData);
    }

    @Override
    public @Nullable CarryOnData read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable CarryOnData carryOnData) {
        return CarryOnData.STREAM_CODEC.decode(registryFriendlyByteBuf);
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        ServerPlayer player = (ServerPlayer) holder;
        // the isAlive check avoids us syncing attachment data about dead players. Which causes a disconnect
        // player.tickCount <= 0 avoids us syncing attachment data about players the instant they spawn. 
        // Which also causes a disconnect as the player entity may not be synced yet.
        // The same checks apply to the recipient: syncing to a dead or just-spawned player
        // causes a NPE in the RegistryFriendlyByteBuf internals (wrapped == null), kicking all players.
        if (to.connection == null || !player.isAlive() || player.tickCount <= 0 || player.isRemoved())
            return false;
        if (!to.isAlive() || to.isRemoved() || to.tickCount <= 0)
            return false;
        return AttachmentSyncHandler.super.sendToPlayer(holder, to);
    }
}
