package tschipp.carryon.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface IProxy
{
	
	public void setup(final FMLCommonSetupEvent event);
	
	public PlayerEntity getPlayer();
	
	public World getWorld();
}
