package tschipp.carryon.common.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PositionProvider implements ICapabilitySerializable<CompoundTag>
{

	@CapabilityInject(IPosition.class)
	public static final Capability<IPosition> POSITION_CAPABILITY = null;

	private IPosition instance = new TEPosition();//POSITION_CAPABILITY.getDefaultInstance();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == POSITION_CAPABILITY)
			return (LazyOptional<T>) LazyOptional.of(() -> {
				return new TEPosition();
			});
		
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();

		tag.putBoolean("blockActivated", instance.isBlockActivated());
		tag.putInt("x", instance.getPos().getX());
		tag.putInt("y", instance.getPos().getY());
		tag.putInt("z", instance.getPos().getZ());
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		CompoundTag tag = (CompoundTag) nbt;

		int x = tag.getInt("x");
		int y = tag.getInt("y");
		int z = tag.getInt("z");
		
		BlockPos pos = new BlockPos(x,y,z);
		
		instance.setPos(pos);
		instance.setBlockActivated(tag.getBoolean("blockActivated"));
	}

}
