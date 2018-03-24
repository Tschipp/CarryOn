package tschipp.carryon.common.capabilities;

import net.minecraft.util.math.BlockPos;

public interface IPosition {

	public BlockPos getPos();
	
	public void setPos(BlockPos pos);
	
	public boolean isBlockActivated();
	
	public void setBlockActivated(boolean b);
	
	
}
