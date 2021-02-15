package tschipp.carryon.common.helper;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.PlayerEntity;

public class CarryonGamestageHelper
{
	public static boolean hasGamestage(String stage, PlayerEntity player)
	{
		return GameStageHelper.hasStage(player, stage);
	}
}
