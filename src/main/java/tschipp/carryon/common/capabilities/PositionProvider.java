package tschipp.carryon.common.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PositionProvider implements ICapabilitySerializable<NBTTagCompound> {

	@CapabilityInject(IPosition.class)
	public static final Capability<IPosition> POSITION_CAPABILITY = null;
	
	private IPosition instance = POSITION_CAPABILITY.getDefaultInstance();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, EnumFacing side)
	{
		return (LazyOptional<T>) LazyOptional.of(() -> {return new TEPosition();});
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		return (NBTTagCompound) POSITION_CAPABILITY.getStorage().writeNBT(POSITION_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		POSITION_CAPABILITY.getStorage().readNBT(POSITION_CAPABILITY, instance, null, nbt);		
	}
	
}
