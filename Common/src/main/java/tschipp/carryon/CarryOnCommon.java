package tschipp.carryon;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import tschipp.carryon.common.command.CommandCarryOn;
import tschipp.carryon.config.ConfigLoader;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingPacket;
import tschipp.carryon.networking.clientbound.ClientboundSyncScriptsPacket;
import tschipp.carryon.networking.serverbound.ServerboundCarryKeyPressedPacket;
import tschipp.carryon.platform.Services;

public class CarryOnCommon
{
	public static void registerServerPackets()
	{
		Services.PLATFORM.registerServerboundPacket(
				Constants.PACKET_ID_KEY_PRESSED,
				0,
				ServerboundCarryKeyPressedPacket.class,
				ServerboundCarryKeyPressedPacket::toBytes,
				ServerboundCarryKeyPressedPacket::new,
				ServerboundCarryKeyPressedPacket::handle
		);
	}

	public static void registerClientPackets()
	{
		Services.PLATFORM.registerClientboundPacket(
				Constants.PACKET_ID_START_RIDING,
				1,
				ClientboundStartRidingPacket.class,
				ClientboundStartRidingPacket::toBytes,
				ClientboundStartRidingPacket::new,
				ClientboundStartRidingPacket::handle
		);

		Services.PLATFORM.registerClientboundPacket(
				Constants.PACKET_ID_SYNC_SCRIPTS,
				2,
				ClientboundSyncScriptsPacket.class,
				ClientboundSyncScriptsPacket::toBytes,
				ClientboundSyncScriptsPacket::new,
				ClientboundSyncScriptsPacket::handle
		);
	}

	public static void registerConfig()
	{
		ConfigLoader.registerConfig(Constants.COMMON_CONFIG);
		ConfigLoader.registerConfig(Constants.CLIENT_CONFIG);
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		CommandCarryOn.register(dispatcher);
	}

}
