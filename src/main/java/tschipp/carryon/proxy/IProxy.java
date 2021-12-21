package tschipp.carryon.proxy;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface IProxy
{
	
	public void setup(final FMLCommonSetupEvent event);
	
	public Player getPlayer();
	
	public Level getWorld();
}
