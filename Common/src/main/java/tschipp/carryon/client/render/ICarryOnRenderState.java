package tschipp.carryon.client.render;

import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;

// MC 26.2: Mixed into HumanoidRenderState by PlayerRenderStateMixin. Allows EntityRendererMixin
// and CarryingItemRenderLayer to exchange carry data across the extraction/submission boundary
// of the deferred pipeline without unsafe raw casts on HumanoidRenderState directly.
public interface ICarryOnRenderState {

    CarryOnData getCarryOnData();

    void setCarryOnData(CarryOnData data);

    float getRenderWidth();

    void setRenderWidth(float val);

    Player getPlayer();

    void setPlayer(Player player);

}
