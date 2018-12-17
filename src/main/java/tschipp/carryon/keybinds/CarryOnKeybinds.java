package tschipp.carryon.keybinds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import tschipp.carryon.interfaces.ICarryOnData;


public class CarryOnKeybinds
{

	public static final String KEYBIND_KEY = "carryOnKeyPressed";
	public static KeyBinding carryKey;
	
	@Environment(EnvType.CLIENT)
	public static void init()
	{
		carryKey = new KeyBinding("key.carry.desc", InputUtil.Type.KEY_KEYBOARD, 340, "key.carry.category");		
	}
	
	public static boolean isKeyPressed(PlayerEntity player)
	{
		ICarryOnData data = (ICarryOnData)player;
		CompoundTag tag = data.getCarryOnData();
		if(tag != null && tag.containsKey(KEYBIND_KEY))
		{
			return tag.getBoolean(KEYBIND_KEY);
		}
		return false;
	}
	
	public static void setKeyPressed(PlayerEntity player, boolean pressed)
	{
		ICarryOnData data = (ICarryOnData)player;
		CompoundTag tag = data.getCarryOnData();
		tag.putBoolean(KEYBIND_KEY, pressed);
		data.setCarryOnData(tag);
	}
	
		
	
}
