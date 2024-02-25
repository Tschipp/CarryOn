package tschipp.carryon.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class EntityPickupEvent extends Event implements ICancellableEvent {

    public final ServerPlayer player;
    public final Entity target;

    public EntityPickupEvent(ServerPlayer player, Entity target) {
        this.player = player;
        this.target = target;
    }
}
