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
		
		@Comment("Slowness multiplier for blocks")
		public double blockSlownessMultiplier = 1.0;
		
		@Comment("Slowness multiplier for entities")
		public double entitySlownessMultiplier = 1.0;
		
		@Comment("Arms should render on sides when carrying")
		public boolean renderArms = true;
		
		@Comment("Allow babies to be carried even when adult mob is blacklisted (or not whitelisted)")
		public boolean allowBabies = false;
		
		@Comment("Use Whitelist instead of Blacklist for Blocks")
		public boolean useWhitelistBlocks=false;
		
		@Comment("Use Whitelist instead of Blacklist for Entities")
		public boolean useWhitelistEntities=false;
		
		@Comment("Use Whitelist instead of Blacklist for Stacking")
		public boolean useWhitelistStacking=false;

		@Comment("All blocks that aren't a Tile Entities can be picked up as long as they're allowed to in the whitelist, even if other settings disable whitelist or picking of normal blocks.")
		public boolean useWhitelistForNormalBlocks=false;
		
		@Comment("Whether the player can hit blocks and entities while carrying or not")
		public boolean hitWhileCarrying=false;
		
		@Comment("Whether the player drops the carried object when hit or not")
		public boolean dropCarriedWhenHit=false;
		
		@Config.RequiresMcRestart()
		@Comment("Use custom Pickup Scripts. Having this set to false, will not allow you to run scripts, but will increase your performance")
		public boolean useScripts=false;
		
		@Comment("Allows entities to be stacked using Carry On")
		public boolean stackableEntities = true;
		
		@Config.RangeInt(min = 1)
		@Comment("Maximum stack limit for entities")
		public int maxEntityStackLimit = 10;
		
		@Comment("Whether entities' size matters when stacking or not")
		public boolean entitySizeMattersStacking = true;
	}
	
	public static class WhiteList
	{
		@Comment("Entities that CAN be picked up")
		public String[] allowedEntities=new String[]
				{
				};
		
		@Comment("Blocks that CAN be picked up")
		public String[] allowedBlocks=new String[]
				{
				};
		
		@Comment("Entities that CAN have other entities stacked on top of them")
		public String[] allowedStacking = new String[]
				{	
				};
	}
	
	public static class Blacklist
	{
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
    					"minecraft:waterlily",
    					"minecraft:cake",
    					"animania:block_trough",
    					"animania:block_invisiblock",
    					"colossalchests:*",
    					"ic2:*",
    					"bigreactors:*",
    					"forestry:*",
    					"tconstruct:*",
    					"rustic:*",
    					"botania:*",
    					"astralsorcery:*",
    					"quark:colored_bed_*",
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
    					"skyresources:*",
    					"lootbags:*",
    					"exsartagine:*",
    					"aquamunda:tank",
    					"opencomputers:*",
    					"malisisdoors:*",
    					"industrialforegoing:*",
    					"minecolonies:*",
    					"thaumcraft:pillar*",
    					"thaumcraft:infernal_furnace",
    					"thaumcraft:placeholder*",
    					"thaumcraft:infusion_matrix",
    					"thaumcraft:golem_builder",
    					"thaumcraft:thaumatorium*",
    					"magneticraft:oil_heater",
    					"magneticraft:solar_panel",
    					"magneticraft:steam_engine",
    					"magneticraft:shelving_unit",
    					"magneticraft:grinder",
    					"magneticraft:sieve",
    					"magneticraft:solar_tower",
    					"magneticraft:solar_mirror",
    					"magneticraft:container",
    					"magneticraft:pumpjack",
    					"magneticraft:solar_panel",
    					"magneticraft:refinery",
    					"magneticraft:oil_heater",
    					"magneticraft:hydraulic_press",
    					"magneticraft:multiblock_gap",
    					"refinedstorage:*",
    					"practicallogistics2:*",
    					"mcmultipart:*",
    					"enderstorage:*",
    					"betterstorage:*",
    					"practicallogistics2:*"

    			};
		
		@Comment("Entities that cannot be picked up")
		public String[] forbiddenEntities = new String[]
				{
						"minecraft:ender_crystal",
						"minecraft:ender_dragon",
						"minecraft:ghast",
						"minecraft:shulker",
						"minecraft:leash_knot",
						"minecraft:armor_stand",
						"minecraft:item_frame",
						"minecraft:painting",
						"minecraft:shulker_bullet",
						"animania:textures/entity/pigs/hamster_tarou.png",
						"animania:hamster",
						"animania:ferret*",
						"animania:hedgehog*",
						"animania:cart",
						"animania:wagon",
						"mynko:*"
				};
		
		
		@Comment("Entities that cannot have other entities stacked on top of them")
		public String[] forbiddenStacking = new String[]
				{
						"minecraft:horse"
				};
		
	}
	
	public static class ModelOverrides
	{
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
    				"minecraft:leaves2->(item)minecraft:leaves2",
    				"minecraft:reeds->(block)minecraft:reeds",
    				"minecraft:daylight_detector_inverted->minecraft:daylight_detector",
    	            "minecraft:standing_sign->(item)minecraft:sign",
    				"minecraft:wall_sign->(item)minecraft:sign",
    	            "minecraft:redstone_wire->(item)minecraft:redstone",
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
    	            "storagedrawers:basicdrawers;4{Mat:\"dark_oak\"}->storagedrawers:basicdrawers;4{material:\"dark_oak\"}",
    	            "animania:block_nest->(block)animania:block_nest",
    	            "animania:cheese_mold;0->(block)animania:cheese_mold;0",
    	            "animania:cheese_mold;1->(block)animania:cheese_mold;1",
    	            "animania:cheese_mold;2->(block)animania:cheese_mold;2",
    	            "animania:cheese_mold;3->(block)animania:cheese_mold;3",
    	            "animania:cheese_mold;4->(block)animania:cheese_mold;4",
    	            "animania:cheese_mold;5->(block)animania:cheese_mold;5",
    	            "animania:cheese_mold;6->(block)animania:cheese_mold;6",
    	            "animania:cheese_mold;7->(block)animania:cheese_mold;7",
    	            "animania:cheese_mold;8->(block)animania:cheese_mold;8",
    	            "animania:cheese_mold;9->(block)animania:cheese_mold;9",
    	            "animania:cheese_mold;10->(block)animania:cheese_mold;10",
    			};
	}
	
	
	
	public static class CustomPickupConditions
	{
		@Config.RequiresMcRestart()
		@Comment("Custom Pickup Conditions for Blocks")
    	public String[] customPickupConditionsBlocks = new String[]
    			{
    					
    			};	
		
		@Config.RequiresMcRestart()
		@Comment("Custom Pickup Conditions for Entities")
    	public String[] customPickupConditionsEntities = new String[]
    			{
    					
    			};	
	}

}
