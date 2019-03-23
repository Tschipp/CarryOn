package tschipp.carryon.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface IProxy
{
	
	public void setup(final FMLCommonSetupEvent event);
	
	public EntityPlayer getPlayer();
	
	public World getWorld();
}
