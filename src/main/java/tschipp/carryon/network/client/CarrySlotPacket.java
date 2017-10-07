package tschipp.carryon.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CarrySlotPacket implements IMessage
{
	public int slot;
	public int carryOverride = 0;
	public int entityid;
	
	public CarrySlotPacket()
	{
		this.slot = 9;
		this.entityid = 0;
	}
	
	public CarrySlotPacket(int slot, int entityid)
	{
		this.slot = slot;
		this.entityid = entityid;
	}
	
	public CarrySlotPacket(int slot, int entityid, int carryOverride)
	{
		this.slot = slot;
		this.carryOverride = carryOverride;
		this.entityid = entityid;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		NBTTagCompound tag =  ByteBufUtils.readTag(buf);
		
		this.slot = tag.getInteger("slot");
		this.carryOverride = tag.getInteger("override");
		this.entityid = tag.getInteger("entityid");
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("slot", slot);
		tag.setInteger("override", carryOverride);
		tag.setInteger("entityid", entityid);
		ByteBufUtils.writeTag(buf, tag);

	}

}
