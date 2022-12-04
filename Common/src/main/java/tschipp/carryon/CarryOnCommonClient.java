package tschipp.carryon;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class CarryOnCommonClient
{
	public static void checkForKeybinds()
	{
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if(player != null) {
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if ((CarryOnKeybinds.carryKey.isUnbound() ? player.isShiftKeyDown() : CarryOnKeybinds.carryKey.isDown()) && !carry.isKeyPressed()) {
				CarryOnKeybinds.onCarryKey(true);
				carry.setKeyPressed(true);
				CarryOnDataManager.setCarryData(player, carry);
			} else if (!(CarryOnKeybinds.carryKey.isUnbound() ? player.isShiftKeyDown() : CarryOnKeybinds.carryKey.isDown()) && carry.isKeyPressed()) {
				CarryOnKeybinds.onCarryKey(false);
				carry.setKeyPressed(false);
				CarryOnDataManager.setCarryData(player, carry);
			}
		}
	}

	public static void onCarryClientTick()
	{
		Player player = Minecraft.getInstance().player;
		if(player != null) {
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if(carry.isCarrying())
			{
				player.getInventory().selected = carry.getSelected();
			}
		}
	}

	public static Player getPlayer()
	{
		return Minecraft.getInstance().player;
	}
}
