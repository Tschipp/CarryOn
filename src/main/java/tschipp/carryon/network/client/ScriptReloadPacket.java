package tschipp.carryon.network.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptReader;

public class ScriptReloadPacket
{
	private List<CarryOnOverride> overrides = new ArrayList<>();

	public ScriptReloadPacket()
	{
	}

	public ScriptReloadPacket(Collection<CarryOnOverride> collection)
	{
		this.overrides.addAll(collection);
	}

	public ScriptReloadPacket(FriendlyByteBuf buf)
	{
		int size = buf.readInt();
		for (int i = 0; i < size; i++)
		{
			this.overrides.add(CarryOnOverride.deserialize(buf));
		}
	}

	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(this.overrides.size());
		this.overrides.forEach(override -> override.serialize(buf));
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				ScriptReader.OVERRIDES.clear();

				this.overrides.forEach(override -> {
					ScriptReader.OVERRIDES.put(override.hashCode(), override);
				});

				ctx.get().setPacketHandled(true);
			});
		}

	}
}
