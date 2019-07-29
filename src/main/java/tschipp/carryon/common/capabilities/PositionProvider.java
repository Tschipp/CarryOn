package tschipp.carryon.common.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PositionProvider implements ICapabilitySerializable<CompoundNBT> {

	@CapabilityInject(IPosition.class)
	public static final Capability<IPosition> POSITION_CAPABILITY = null;
	
	private IPosition instance = POSITION_CAPABILITY.getDefaultInstance();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return (LazyOptional<T>) LazyOptional.of(() -> {return new TEPosition();});
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		return (CompoundNBT) POSITION_CAPABILITY.getStorage().writeNBT(POSITION_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		POSITION_CAPABILITY.getStorage().readNBT(POSITION_CAPABILITY, instance, null, nbt);		
	}
	
}
