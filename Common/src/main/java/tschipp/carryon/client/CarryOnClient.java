package tschipp.carryon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class CarryOnClient
{
	public static Player getPlayer()
	{
		return Minecraft.getInstance().player;
	}
}
