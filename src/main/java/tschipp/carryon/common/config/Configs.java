package tschipp.carryon.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

public class Configs {
	
	public static class Settings
	{
		@Comment("If the front of the Tile Entities should face the player or should face outward")
		public boolean facePlayer = false;
		
		@Comment("More complex Tile Entities slow down the player more")
		public boolean heavyTiles = false;
	}
	
	public static class ForbiddenTiles
	{
		@Config.RequiresMcRestart()
		@Comment("Tile Entities that cannot be picked up")
    	public String[] forbiddenTiles = new String[]
    			{
    					"animania:block_trough",
    					"animania:block_invisiblock",
    					"ic2:*",
    					"immersiveengineering:*",
    					"embers:block_furnace",
    					"embers:ember_bore",
    					"embers:ember_activator",
    					"embers:mixer",
    					"embers:heat_coil",
    					"embers:large_tank",
    					"embers:crystal_cell",
    					"embers:alchemy_pedestal",
    					"embers:boiler",
    					"embers:combustor",
    					"embers:catalzyer",
    					"embers:field_chart",
    					"embers:inferno_forge"

    			};
	
	}

}
