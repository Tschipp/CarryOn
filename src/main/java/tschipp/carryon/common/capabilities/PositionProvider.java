package tschipp.carryon.common.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class PositionProvider implements ICapabilitySerializable {

	@CapabilityInject(IPosition.class)
	public static final Capability<IPosition> POSITION_CAPABILITY = null;
	
	private IPosition instance = POSITION_CAPABILITY.getDefaultInstance();
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == POSITION_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == POSITION_CAPABILITY ? POSITION_CAPABILITY.cast(instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return POSITION_CAPABILITY.getStorage().writeNBT(POSITION_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		POSITION_CAPABILITY.getStorage().readNBT(POSITION_CAPABILITY, instance, null, nbt);
	}

}
