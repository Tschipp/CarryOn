package tschipp.carryon.common.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class PositionStorage implements IStorage<IPosition> {

	@Override
	public INBT writeNBT(Capability<IPosition> capability, IPosition instance, Direction side) {

		CompoundNBT tag = new CompoundNBT();

		tag.putBoolean("blockActivated", instance.isBlockActivated());
		tag.putInt("x", instance.getPos().getX());
		tag.putInt("y", instance.getPos().getY());
		tag.putInt("z", instance.getPos().getZ());
		
		return tag;

	}

	@Override
	public void readNBT(Capability<IPosition> capability, IPosition instance, Direction side, INBT nbt) {

		CompoundNBT tag = (CompoundNBT) nbt;

		int x = tag.getInt("x");
		int y = tag.getInt("y");
		int z = tag.getInt("z");
		
		BlockPos pos = new BlockPos(x,y,z);
		
		instance.setPos(pos);
		instance.setBlockActivated(tag.getBoolean("blockActivated"));
	}


	

}
