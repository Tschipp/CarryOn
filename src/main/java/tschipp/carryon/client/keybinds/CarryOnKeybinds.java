package tschipp.carryon.client.keybinds;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class CarryOnKeybinds
{

	public static final String KEYBIND_KEY = "carryOnKeyPressed";
	public static KeyBinding carryKey;
	
	@OnlyIn(Dist.CLIENT)
	public static void init()
	{
		carryKey = new KeyBinding("key.carry.desc", 340, "key.carry.category");
		
		ClientRegistry.registerKeyBinding(carryKey);
	}
	
	public static boolean isKeyPressed(PlayerEntity player)
	{
		CompoundNBT tag = player.getEntityData();
		if(tag != null && tag.contains(KEYBIND_KEY))
		{
			return tag.getBoolean(KEYBIND_KEY);
		}
		return false;
	}
	
	public static void setKeyPressed(PlayerEntity player, boolean pressed)
	{
		CompoundNBT tag = player.getEntityData();
		tag.putBoolean(KEYBIND_KEY, pressed);
	}
	
		
	
}
