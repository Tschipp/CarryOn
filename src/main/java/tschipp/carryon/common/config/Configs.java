package tschipp.carryon.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

public class Configs {
	
	public static class Settings
	{
		@Comment("If the front of the Tile Entities should face the player or should face outward")
		public boolean facePlayer = false;
		
		@Comment("More complex Tile Entities slow down the player more")
		public boolean heavyTiles = true;
		
		@Comment("Allow all blocks to be picked up, not just Tile Entites")
		public boolean pickupAllBlocks = false;
		
		@Comment("Maximum distance from where Blocks can be picked up")
		public double maxDistance = 2.5;
	}
	
	public static class ForbiddenTiles
	{
		@Comment("Tile Entities that cannot be picked up")
    	public String[] forbiddenTiles = new String[]
    			{
    					"minecraft:end_portal",
    					"minecraft:end_gateway",
    					"minecraft:double_plant",
    					"minecraft:bed",
    					"animania:block_trough",
    					"animania:block_invisiblock",
    					"colossalchests:*",
    					"ic2:*",
    					"bigreactors:*",
    					"forestry:*",
    					"tconstruct:*",
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
    					"embers:inferno_forge",
    					"storagedrawers:framingtable",
    			};
	}
	
	public static class ModelOverrides
	{
		@Comment("Model Overrides based on NBT or on Meta. Advanced Users Only!")
    	public String[] modelOverrides = new String[]
    			{
    				"minecraft:lit_furnace->minecraft:furnace",
    				"minecraft:bed->minecraft:bed",
    	            "quark:custom_chest{type:\"spruce\"}->quark:custom_chest;0",
    	            "quark:custom_chest{type:\"birch\"}->quark:custom_chest;1",
    	            "quark:custom_chest{type:\"jungle\"}->quark:custom_chest;2",
    	            "quark:custom_chest{type:\"acacia\"}->quark:custom_chest;3",
    	            "quark:custom_chest{type:\"dark_oak\"}->quark:custom_chest;4",
    	            "quark:custom_chest_trap{type:\"spruce\"}->quark:custom_chest_trap;0",
    	            "quark:custom_chest_trap{type:\"birch\"}->quark:custom_chest_trap;1",
    	            "quark:custom_chest_trap{type:\"jungle\"}->quark:custom_chest_trap;2",
    	            "quark:custom_chest_trap{type:\"acacia\"}->quark:custom_chest_trap;3",
    	            "quark:custom_chest_trap{type:\"dark_oak\"}->quark:custom_chest_trap;4",
    	            "storagedrawers:basicdrawers;0{Mat:\"spruce\"}->storagedrawers:basicdrawers;0{material:\"spruce\"}",
    	            "storagedrawers:basicdrawers;0{Mat:\"birch\"}->storagedrawers:basicdrawers;0{material:\"birch\"}",
    	            "storagedrawers:basicdrawers;0{Mat:\"jungle\"}->storagedrawers:basicdrawers;0{material:\"jungle\"}",
    	            "storagedrawers:basicdrawers;0{Mat:\"acacia\"}->storagedrawers:basicdrawers;0{material:\"acacia\"}",
    	            "storagedrawers:basicdrawers;0{Mat:\"dark_oak\"}->storagedrawers:basicdrawers;0{material:\"dark_oak\"}",
    	            "storagedrawers:basicdrawers;1{Mat:\"spruce\"}->storagedrawers:basicdrawers;1{material:\"spruce\"}",
    	            "storagedrawers:basicdrawers;1{Mat:\"birch\"}->storagedrawers:basicdrawers;1{material:\"birch\"}",
    	            "storagedrawers:basicdrawers;1{Mat:\"jungle\"}->storagedrawers:basicdrawers;1{material:\"jungle\"}",
    	            "storagedrawers:basicdrawers;1{Mat:\"acacia\"}->storagedrawers:basicdrawers;1{material:\"acacia\"}",
    	            "storagedrawers:basicdrawers;1{Mat:\"dark_oak\"}->storagedrawers:basicdrawers;1{material:\"dark_oak\"}",
    	            "storagedrawers:basicdrawers;2{Mat:\"spruce\"}->storagedrawers:basicdrawers;2{material:\"spruce\"}",
    	            "storagedrawers:basicdrawers;2{Mat:\"birch\"}->storagedrawers:basicdrawers;2{material:\"birch\"}",
    	            "storagedrawers:basicdrawers;2{Mat:\"jungle\"}->storagedrawers:basicdrawers;2{material:\"jungle\"}",
    	            "storagedrawers:basicdrawers;2{Mat:\"acacia\"}->storagedrawers:basicdrawers;2{material:\"acacia\"}",
    	            "storagedrawers:basicdrawers;2{Mat:\"dark_oak\"}->storagedrawers:basicdrawers;2{material:\"dark_oak\"}",
    	            "storagedrawers:basicdrawers;3{Mat:\"spruce\"}->storagedrawers:basicdrawers;3{material:\"spruce\"}",
    	            "storagedrawers:basicdrawers;3{Mat:\"birch\"}->storagedrawers:basicdrawers;3{material:\"birch\"}",
    	            "storagedrawers:basicdrawers;3{Mat:\"jungle\"}->storagedrawers:basicdrawers;3{material:\"jungle\"}",
    	            "storagedrawers:basicdrawers;3{Mat:\"acacia\"}->storagedrawers:basicdrawers;3{material:\"acacia\"}",
    	            "storagedrawers:basicdrawers;3{Mat:\"dark_oak\"}->storagedrawers:basicdrawers;3{material:\"dark_oak\"}",
    	            "storagedrawers:basicdrawers;4{Mat:\"spruce\"}->storagedrawers:basicdrawers;4{material:\"spruce\"}",
    	            "storagedrawers:basicdrawers;4{Mat:\"birch\"}->storagedrawers:basicdrawers;4{material:\"birch\"}",
    	            "storagedrawers:basicdrawers;4{Mat:\"jungle\"}->storagedrawers:basicdrawers;4{material:\"jungle\"}",
    	            "storagedrawers:basicdrawers;4{Mat:\"acacia\"}->storagedrawers:basicdrawers;4{material:\"acacia\"}",
    	            "storagedrawers:basicdrawers;4{Mat:\"dark_oak\"}->storagedrawers:basicdrawers;4{material:\"dark_oak\"}"
    			};
	}

}
