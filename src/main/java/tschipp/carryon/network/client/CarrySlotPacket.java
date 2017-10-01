package tschipp.carryon.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CarrySlotPacket implements IMessage
{
	public int slot;
	public int carryOverride = 0;
	
	public CarrySlotPacket()
	{
	}
	
	public CarrySlotPacket(int slot)
	{
		this.slot = slot;
	}
	
	public CarrySlotPacket(int slot, int carryOverride)
	{
		this.slot = slot;
		this.carryOverride = carryOverride;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		NBTTagCompound tag =  ByteBufUtils.readTag(buf);
		
		this.slot = tag.getInteger("slot");
		this.carryOverride = tag.getInteger("override");
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("slot", slot);
		tag.setInteger("override", carryOverride);
		ByteBufUtils.writeTag(buf, tag);

	}

}
