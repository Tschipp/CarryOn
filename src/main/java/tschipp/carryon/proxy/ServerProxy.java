package tschipp.carryon.proxy;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy implements IProxy
{

	@Override
	public void setup(FMLCommonSetupEvent event)
	{

	}

	@Override
	public Player getPlayer()
	{
		return null;
	}

	@Override
	public Level getLevel()
	{
		return null;
	}

}
