package tschipp.carryon.common.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class PositionStorage implements IStorage<IPosition> {

	@Override
	public NBTBase writeNBT(Capability<IPosition> capability, IPosition instance, EnumFacing side) {

		NBTTagCompound tag = new NBTTagCompound();

		tag.setBoolean("blockActivated", instance.isBlockActivated());
		tag.setInteger("x", instance.getPos().getX());
		tag.setInteger("y", instance.getPos().getY());
		tag.setInteger("z", instance.getPos().getZ());
		
		return tag;

	}

	@Override
	public void readNBT(Capability<IPosition> capability, IPosition instance, EnumFacing side, NBTBase nbt) {

		NBTTagCompound tag = (NBTTagCompound) nbt;

		int x = tag.getInteger("x");
		int y = tag.getInteger("y");
		int z = tag.getInteger("z");
		
		BlockPos pos = new BlockPos(x,y,z);
		
		instance.setPos(pos);
		instance.setBlockActivated(tag.getBoolean("blockActivated"));
	}

}
