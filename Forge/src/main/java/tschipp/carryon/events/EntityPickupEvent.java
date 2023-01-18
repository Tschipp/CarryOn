package tschipp.carryon.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class EntityPickupEvent extends Event {

    public final ServerPlayer player;
    public final Entity target;

    public EntityPickupEvent(ServerPlayer player, Entity target) {
        this.player = player;
        this.target = target;
    }
}
