package tschipp.carryon.common.capabilities;

import net.minecraft.core.BlockPos;

public class TEPosition implements IPosition
{

	private BlockPos pos = new BlockPos(0, 0, 0);
	private boolean blockActivated = false;

	@Override
	public BlockPos getPos()
	{
		return this.pos;
	}

	@Override
	public void setPos(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public boolean isBlockActivated()
	{
		return this.blockActivated;
	}

	@Override
	public void setBlockActivated(boolean b)
	{
		this.blockActivated = b;
	}

}
