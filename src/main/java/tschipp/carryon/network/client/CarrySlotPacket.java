package tschipp.carryon.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tschipp.carryon.common.scripting.ScriptChecker;
import tschipp.carryon.proxy.ClientProxy;

import java.util.function.Supplier;

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
		buf.writeInt(this.slot);
		buf.writeInt(this.carryOverride);
		buf.writeInt(this.entityid);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				Level level = ClientProxy.getLevel();

				if (level != null)
				{
					Entity e = level.getEntity(this.entityid);

					if (e instanceof Player player)
					{
						ctx.get().setPacketHandled(true);

						if (this.slot >= 9)
						{
							player.getPersistentData().remove("carrySlot");
							player.getPersistentData().remove("overrideKey");
						}
						else
						{

							player.getPersistentData().putInt("carrySlot", this.slot);
							if (this.carryOverride != 0)
								ScriptChecker.setCarryOnOverride(player, this.carryOverride);
						}
					}

				}
			});
		}
	}

}
