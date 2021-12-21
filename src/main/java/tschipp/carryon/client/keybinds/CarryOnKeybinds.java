package tschipp.carryon.client.keybinds;

import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

public class CarryOnKeybinds
{

	public static final String KEYBIND_KEY = "carryOnKeyPressed";
	public static KeyMapping carryKey;

	@OnlyIn(Dist.CLIENT)
	public static void init()
	{
		carryKey = new KeyMapping("key.carry.desc", 340, "key.carry.category");

		ClientRegistry.registerKeyBinding(carryKey);
	}

	public static boolean isKeyPressed(Player player)
	{
		CompoundTag tag = player.getPersistentData();
		if (tag != null && tag.contains(KEYBIND_KEY))
		{
			return tag.getBoolean(KEYBIND_KEY);
		}
		return false;
	}

	public static void setKeyPressed(Player player, boolean pressed)
	{
		CompoundTag tag = player.getPersistentData();
		tag.putBoolean(KEYBIND_KEY, pressed);
	}

}
