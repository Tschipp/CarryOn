package tschipp.carryon.common.capabilities;

import net.minecraft.util.math.BlockPos;

public class TEPosition implements IPosition {
	
	private BlockPos pos = new BlockPos(0, 0, 0);
	private boolean blockActivated = false;
	
	@Override
	public BlockPos getPos()
	{
		return pos;
	}
	@Override
	public void setPos(BlockPos pos)
	{
		this.pos = pos;
	}
	@Override
	public boolean isBlockActivated()
	{
		return blockActivated;
	}
	@Override
	public void setBlockActivated(boolean b)
	{
		this.blockActivated = b;
	}
	

}
