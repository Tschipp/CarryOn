package tschipp.carryon.mixin;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tschipp.carryon.client.render.ICarryOnRenderState;
import tschipp.carryon.common.carry.CarryOnData;

@Mixin(HumanoidRenderState.class)
public class PlayerRenderStateMixin implements ICarryOnRenderState {

    @Unique
    public CarryOnData carryOnData = null;

    @Unique
    public float renderWidth = 0f;


    @Unique
    public Player player = null;

    @Unique
    @Override
    public CarryOnData getCarryOnData() {
        return carryOnData;
    }

    @Unique
    @Override
    public void setCarryOnData(CarryOnData data) {
        carryOnData = data;
    }

    @Unique
    @Override
    public float getRenderWidth() {
        return renderWidth;
    }

    @Unique
    @Override
    public void setRenderWidth(float val) {
        renderWidth = val;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(Player player) {
      this.player = player;
    }
}
