package tschipp.carryon.client.render;

import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;

public interface ICarryOnRenderState {

    CarryOnData getCarryOnData();

    void setCarryOnData(CarryOnData data);

    float getRenderWidth();

    void setRenderWidth(float val);

    Player getPlayer();

    void setPlayer(Player player);

}
