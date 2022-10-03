package tschipp.carryon.common.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PositionProvider implements ICapabilitySerializable<CompoundTag>
{

	public static final Capability<IPosition> POSITION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

	private IPosition instance = new TEPosition();// POSITION_CAPABILITY.getDefaultInstance();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == POSITION_CAPABILITY)
			return (LazyOptional<T>) LazyOptional.of(TEPosition::new);

		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();

		tag.putBoolean("blockActivated", this.instance.isBlockActivated());
		tag.putInt("x", this.instance.getPos().getX());
		tag.putInt("y", this.instance.getPos().getY());
		tag.putInt("z", this.instance.getPos().getZ());

		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		CompoundTag tag = nbt;

		int x = tag.getInt("x");
		int y = tag.getInt("y");
		int z = tag.getInt("z");

		BlockPos pos = new BlockPos(x, y, z);

		this.instance.setPos(pos);
		this.instance.setBlockActivated(tag.getBoolean("blockActivated"));
	}

}
