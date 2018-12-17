package tschipp.carryon.render;

public interface ICarrying {

    public boolean isCarryingBlock();
    public boolean isCarryingEntity();

    public void setCarryingBlock(boolean isCarrying);
    public void setCarryingEntity(boolean isCarrying);

}