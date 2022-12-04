package tschipp.carryon.platform;

import net.minecraft.world.entity.player.Player;
import tschipp.carryon.platform.services.IGamestagePlatformHelper;

public class FabricGamestagesHelper implements IGamestagePlatformHelper
{
	@Override
	public boolean hasStage(Player player, String stage)
	{
		return true;
	}
}
