package tschipp.carryon.client.keybinds;

import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tschipp.carryon.CarryOn;

@EventBusSubscriber(modid = CarryOn.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class CarryOnKeybinds
{

	public static final String KEYBIND_KEY = "carryOnKeyPressed";
	public static KeyMapping carryKey;

	
	@SubscribeEvent
	public static void registerKeybinds(RegisterKeyMappingsEvent event)
	{
		carryKey = new KeyMapping("key.carry.desc", 340, "key.carry.category");

		event.register(carryKey);
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
