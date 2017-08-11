package tschipp.carryon.common.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import tschipp.carryon.common.config.CarryOnConfig;

public class ForbiddenTileHandler
{
	
	public static final List<String> FORBIDDEN_TILES;
	
	public static boolean isForbidden(Block block)
	{
		return FORBIDDEN_TILES.contains(block.getRegistryName().toString());
	}
	
	static
	{
		String[] forbidden = CarryOnConfig.forbiddenTiles.forbiddenTiles;
		
		FORBIDDEN_TILES = new ArrayList<String>();
		
		for(int i = 0; i < forbidden.length; i++)
		{
			if(forbidden[i].contains("*"))
			{
				String modid = forbidden[i].replace("*", "");
				for(int k = 0; k < Block.REGISTRY.getKeys().size(); k++)
				{
					if(Block.REGISTRY.getKeys().toArray()[k].toString().contains(modid))
					{
						FORBIDDEN_TILES.add(Block.REGISTRY.getKeys().toArray()[k].toString());
					}
				}
			}
			FORBIDDEN_TILES.add(forbidden[i]);
		}
	}
}
