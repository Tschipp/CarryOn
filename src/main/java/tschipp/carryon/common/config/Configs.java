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
		
		@Comment("Whether Blocks and Entities slow the creative player down when carried")
		public boolean slownessInCreative = true;
		
		@Config.RangeDouble(min = 0)
		@Comment("Maximum distance from where Blocks and Entities can be picked up")
		public double maxDistance = 2.5;
		
		@Config.RangeDouble(min = 0, max = 10)
		@Comment("Max width of entities that can be picked up in survival mode")
		public float maxEntityWidth = 1.5f;
		
		@Config.RangeDouble(min = 0, max = 10)
		@Comment("Max height of entities that can be picked up in survival mode")
		public float maxEntityHeight = 1.5f;
		
		@Comment("Whether hostile mobs should be able to picked up in survival mode")
		public boolean pickupHostileMobs = false;
		
		@Comment("Larger Entities slow down the player more")
		public boolean heavyEntities = true;
	}
	
	public static class ForbiddenEntities
	{
		
		@Config.RequiresMcRestart()
		@Comment("Entities that cannot be picked up")
		public String[] forbiddenEntities = new String[]
				{
						"minecraft:ender_crystal",
						"minecraft:ender_dragon",
						"minecraft:ghast",
						"minecraft:shulker"
				};
	}
	
	
	public static class ForbiddenTiles
	{
		@Config.RequiresMcRestart()
		@Comment("Tile Entities that cannot be picked up")
    	public String[] forbiddenTiles = new String[]
    			{
    					"minecraft:end_portal",
    					"minecraft:end_gateway",
    					"minecraft:double_plant",
    					"minecraft:bed",
    					"minecraft:wooden_door",
    					"minecraft:iron_door",
    					"minecraft:spruce_door",
    					"minecraft:birch_door",
    					"minecraft:jungle_door",
    					"minecraft:acacia_door",
    					"minecraft:dark_oak_door",
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
		@Config.RequiresMcRestart()
		@Comment("Model Overrides based on NBT or on Meta. Advanced Users Only!")
    	public String[] modelOverrides = new String[]
    			{
    				"minecraft:lit_furnace->minecraft:furnace",
    				"minecraft:hopper->(block)minecraft:hopper",
    				"minecraft:unpowered_comparator->(block)minecraft:unpowered_comparator",
    				"minecraft:unpowered_repeater->(block)minecraft:unpowered_repeater",
    				"minecraft:powered_comparator->(block)minecraft:powered_comparator",
    				"minecraft:powered_repeater->(block)minecraft:powered_repeater",
    				"minecraft:cauldron->(block)minecraft:cauldron",
    				"minecraft:brewing_stand->(item)minecraft:brewing_stand",
    				"minecraft:tallgrass;1->(item)minecraft:tallgrass;1",
    				"minecraft:tallgrass;2->(item)minecraft:tallgrass;2",
    				"minecraft:flower_pot->(block)minecraft:flower_pot",
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
