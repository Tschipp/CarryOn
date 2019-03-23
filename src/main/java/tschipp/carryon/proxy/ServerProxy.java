package tschipp.carryon.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy implements IProxy
{

	@Override
	public void setup(FMLCommonSetupEvent event)
	{
		
	}

	@Override
	public EntityPlayer getPlayer()
	{
		return null;
	}

	@Override
	public World getWorld()
	{
		return null;
	}

}
