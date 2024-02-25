package tschipp.carryon.platform;

import net.minecraft.world.entity.player.Player;
import tschipp.carryon.compat.GamestageCompat;
import tschipp.carryon.platform.services.IGamestagePlatformHelper;

public class NeoForgeGamestagesHelper implements IGamestagePlatformHelper
{
	@Override
	public boolean hasStage(Player player, String stage)
	{
		if(!Services.PLATFORM.isModLoaded("gamestages"))
			return true;

		return GamestageCompat.hasStage(player, stage);
	}
}
