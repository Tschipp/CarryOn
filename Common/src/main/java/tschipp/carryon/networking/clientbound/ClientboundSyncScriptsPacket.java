package tschipp.carryon.networking.clientbound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;
import tschipp.carryon.common.scripting.CarryOnScript;
import tschipp.carryon.common.scripting.ScriptManager;
import tschipp.carryon.networking.PacketBase;

import java.util.List;

public record ClientboundSyncScriptsPacket(Tag serialized) implements PacketBase
{
	public ClientboundSyncScriptsPacket(FriendlyByteBuf buf)
	{
		this(buf.readNbt().get("data"));
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		CompoundTag tag = new CompoundTag();
		tag.put("data", serialized);
		buf.writeNbt(tag);
	}

	@Override
	public void handle(Player player)
	{
		DataResult<List<CarryOnScript>> res = Codec.list(CarryOnScript.CODEC).parse(NbtOps.INSTANCE, serialized);
		List<CarryOnScript> scripts = res.getOrThrow(false, (s) -> {throw new RuntimeException("Failed deserializing carry on scripts on the client: " + s);});
		ScriptManager.SCRIPTS.clear();
		ScriptManager.SCRIPTS.addAll(scripts);
	}

	@Override
	public ResourceLocation id() {
		return Constants.PACKET_ID_SYNC_SCRIPTS;
	}
}
