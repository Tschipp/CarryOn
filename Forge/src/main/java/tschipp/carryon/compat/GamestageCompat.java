package tschipp.carryon.compat;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.world.entity.player.Player;

public class GamestageCompat
{
	public static boolean hasStage(Player player, String stage)
	{
		return GameStageHelper.hasStage(player, stage);
	}
}
