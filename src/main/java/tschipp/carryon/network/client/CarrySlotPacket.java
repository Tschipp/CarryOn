package tschipp.carryon.network.client;

import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.scripting.ScriptChecker;

public class CarrySlotPacket
{
	public int slot;
	public int carryOverride = 0;
	public int entityid;

	public CarrySlotPacket(ByteBuf buf)
	{
		this.slot = buf.readInt();
		this.carryOverride = buf.readInt();
		this.entityid = buf.readInt();
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

	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(slot);
		buf.writeInt(carryOverride);
		buf.writeInt(entityid);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {

			World world = CarryOn.proxy.getWorld();

			if (world != null)
			{
				Entity e = world.getEntityByID(entityid);

				if (e != null && e instanceof EntityPlayer)
				{
					EntityPlayer player = (EntityPlayer) e;

					ctx.get().setPacketHandled(true);
					
					if (slot >= 9)
					{
						player.getEntityData().removeTag("carrySlot");
						player.getEntityData().removeTag("overrideKey");
					} else
					{

						player.getEntityData().setInt("carrySlot", slot);
						if (carryOverride != 0)
							ScriptChecker.setCarryOnOverride(player, carryOverride);
					}
				}

			}
		});
	}

}
