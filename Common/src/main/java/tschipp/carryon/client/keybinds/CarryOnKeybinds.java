package tschipp.carryon.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import tschipp.carryon.Constants;
import tschipp.carryon.networking.serverbound.ServerboundCarryKeyPressedPacket;
import tschipp.carryon.platform.Services;

import java.util.function.Consumer;

public class CarryOnKeybinds
{
	public static KeyMapping carryKey;

	public static void registerKeybinds(Consumer<KeyMapping> registrar)
	{
		carryKey = new ConflictFreeKeyMapping("key.carry.desc", Services.PLATFORM.getPlatformName().equals("Forge") ? InputConstants.KEY_LSHIFT : InputConstants.UNKNOWN.getValue(), "key.carry.category");
		registrar.accept(carryKey);
	}

	public static void onCarryKey(boolean pressed)
	{
		Services.PLATFORM.sendPacketToServer(Constants.PACKET_ID_KEY_PRESSED, new ServerboundCarryKeyPressedPacket(pressed));
	}

}
