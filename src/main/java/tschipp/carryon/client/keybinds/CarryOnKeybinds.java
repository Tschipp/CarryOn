package tschipp.carryon.client.keybinds;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CarryOnKeybinds
{

	public static final String KEYBIND_KEY = "carryOnKeyPressed";
	public static KeyBinding carryKey;
	
	@SideOnly(Side.CLIENT)
	public static void init()
	{
		carryKey = new KeyBinding("key.carry.desc", Keyboard.KEY_LSHIFT, "key.carry.category");
		
		ClientRegistry.registerKeyBinding(carryKey);
	}
	
	public static boolean isKeyPressed(EntityPlayer player)
	{
		NBTTagCompound tag = player.getEntityData();
		if(tag != null && tag.hasKey(KEYBIND_KEY))
		{
			return tag.getBoolean(KEYBIND_KEY);
		}
		return false;
	}
	
	public static void setKeyPressed(EntityPlayer player, boolean pressed)
	{
		NBTTagCompound tag = player.getEntityData();
		tag.setBoolean(KEYBIND_KEY, pressed);
	}
	
		
	
}
