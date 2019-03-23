package tschipp.carryon.common.capabilities;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class PositionStorage implements IStorage<IPosition> {

	@Override
	public INBTBase writeNBT(Capability<IPosition> capability, IPosition instance, EnumFacing side) {

		NBTTagCompound tag = new NBTTagCompound();

		tag.setBoolean("blockActivated", instance.isBlockActivated());
		tag.setInt("x", instance.getPos().getX());
		tag.setInt("y", instance.getPos().getY());
		tag.setInt("z", instance.getPos().getZ());
		
		return tag;

	}

	@Override
	public void readNBT(Capability<IPosition> capability, IPosition instance, EnumFacing side, INBTBase nbt) {

		NBTTagCompound tag = (NBTTagCompound) nbt;

		int x = tag.getInt("x");
		int y = tag.getInt("y");
		int z = tag.getInt("z");
		
		BlockPos pos = new BlockPos(x,y,z);
		
		instance.setPos(pos);
		instance.setBlockActivated(tag.getBoolean("blockActivated"));
	}

	

}
