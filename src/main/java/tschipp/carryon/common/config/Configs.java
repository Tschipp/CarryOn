package tschipp.carryon.common.config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.ListHandler;

@Mod.EventBusSubscriber
public class Configs {
	
	private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

	public static final ForgeConfigSpec SERVER_CONFIG;
	public static final ForgeConfigSpec CLIENT_CONFIG;

	static {
		
		Settings.init(SERVER_BUILDER, CLIENT_BUILDER);
		Blacklist.init(SERVER_BUILDER, CLIENT_BUILDER);
		WhiteList.init(SERVER_BUILDER, CLIENT_BUILDER);
		ModelOverrides.init(SERVER_BUILDER, CLIENT_BUILDER);
		CustomPickupConditions.init(SERVER_BUILDER, CLIENT_BUILDER);
		
		SERVER_CONFIG = SERVER_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	public static void loadConfig(ForgeConfigSpec spec, Path path)
	{
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.autosave()
				.autoreload()
				.writingMode(WritingMode.REPLACE)
				.build();
		
		CarryOn.LOGGER.debug("Loading CarryOn Config");
		configData.load();
		spec.setConfig(configData);
	}
	
	
	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading event)
	{
	}
	
	@SubscribeEvent
	public static void onChange(final ModConfig.ConfigReloading event)
	{
		if(event.getConfig().getModId().equals(CarryOn.MODID))
		{
			ListHandler.initLists();
		}
	}
	
	
	public static class Settings
	{
		public static BooleanValue facePlayer;
		
		public static BooleanValue heavyTiles;
		
		public static BooleanValue pickupAllBlocks;
		
		public static BooleanValue slownessInCreative;
		
		public static DoubleValue maxDistance;
		
		public static DoubleValue maxEntityWidth;
		
		public static DoubleValue maxEntityHeight;
		
		public static BooleanValue pickupHostileMobs;
		
		public static BooleanValue heavyEntities;
		
		public static DoubleValue blockSlownessMultiplier;
		
		public static DoubleValue entitySlownessMultiplier;
		
		public static BooleanValue renderArms;
		
		public static BooleanValue allowBabies;
		
		public static BooleanValue useWhitelistBlocks;
		
		public static BooleanValue useWhitelistEntities;
		
		public static BooleanValue useWhitelistStacking;
		
		public static BooleanValue hitWhileCarrying;
		
		public static BooleanValue dropCarriedWhenHit;
		
		public static BooleanValue useScripts;
		
		public static BooleanValue stackableEntities;
		
		public static IntValue maxEntityStackLimit;
		
		public static BooleanValue entitySizeMattersStacking;
		
		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			c.comment("Settings");
			s.comment("Settings");

			
			maxDistance = s
					.comment("Maximum distance from where Blocks and Entities can be picked up")
					.defineInRange("settings.maxDistance", 2.5, 0, Double.MAX_VALUE);
			
			maxEntityWidth = s
					.comment("Max width of entities that can be picked up in survival mode")
					.defineInRange("settings.maxEntityWidth", 1.5, 0, 10);
			
			maxEntityHeight = s
					.comment("Max height of entities that can be picked up in survival mode")
					.defineInRange("settings.maxEntityHeight", 1.5, 0, 10);
			
			maxEntityWidth = s
					.comment("Max width of entities that can be picked up in survival mode")
					.defineInRange("settings.maxEntityWidth", 1.5, 0, 10);
			
			blockSlownessMultiplier = s
					.comment("Slowness multiplier for blocks")
					.defineInRange("settings.blockSlownessMultiplier", 1, 0, Double.MAX_VALUE);
			
			entitySlownessMultiplier = s
					.comment("Slowness multiplier for entities")
					.defineInRange("settings.entitySlownessMultiplier", 1, 0, Double.MAX_VALUE);
			
			maxEntityStackLimit = s
					.comment("Maximum stack limit for entities")
					.defineInRange("settings.maxEntityStackLimit", 10, 1, Integer.MAX_VALUE);
			
			facePlayer = c
					.comment("If the front of the Tile Entities should face the player or should face outward")
					.define("settings.facePlayer", false);
			
			heavyTiles = s
					.comment("More complex Tile Entities slow down the player more")
					.define("settings.heavyTiles", true);
				
			pickupAllBlocks = s
					.comment("Allow all blocks to be picked up, not just Tile Entites")
					.define("settings.pickupAllBlocks", false);
			
			slownessInCreative = s
					.comment("Whether Blocks and Entities slow the creative player down when carried")
					.define("settings.slownessInCreative", true);
			
			pickupHostileMobs = s
					.comment("Whether hostile mobs should be able to picked up in survival mode")
					.define("settings.pickupHostileMobs", false);
			
			heavyEntities = s
					.comment("Larger Entities slow down the player more")
					.define("settings.heavyEntities", true);
			
			renderArms = c
					.comment("Arms should render on sides when carrying")
					.define("settings.renderArms", true);
			
			allowBabies = s
					.comment("Allow babies to be carried even when adult mob is blacklisted (or not whitelisted)")
					.define("settings.allowBabies", false);
			
			useWhitelistBlocks = s
					.comment("Use Whitelist instead of Blacklist for Blocks")
					.define("settings.useWhitelistBlocks", false);
			
			useWhitelistEntities = s
					.comment("Use Whitelist instead of Blacklist for Entities")
					.define("settings.useWhitelistEntities", false);
			
			useWhitelistStacking = s
					.comment("Use Whitelist instead of Blacklist for Stacking")
					.define("settings.useWhitelistStacking", false);
			
			hitWhileCarrying = s
					.comment("Whether the player can hit blocks and entities while carrying or not")
					.define("settings.hitWhileCarrying", false);
			
			dropCarriedWhenHit = s
					.comment("Whether the player drops the carried object when hit or not")
					.define("settings.dropCarriedWhenHit", false);
			
			useScripts = s
					.comment("Use custom Pickup Scripts. Having this set to false, will not allow you to run scripts, but will increase your performance")
					.worldRestart()
					.define("settings.useScripts", false);
			
			stackableEntities = s
					.comment("Allows entities to be stacked using Carry On")
					.define("settings.stackableEntities", true);
			
			entitySizeMattersStacking = s
					.comment("Whether entities' size matters when stacking or not")
					.define("settings.stackableEntities", true);
			
		
			
		}
		
	}
	
	public static class WhiteList
	{
		public static ConfigValue<List<? extends String>> allowedEntities;
		
		public static ConfigValue<List<? extends String>> allowedBlocks;
		
		public static ConfigValue<List<? extends String>> allowedStacking;
		
		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			s.comment("Whitelist");
			
			allowedEntities = s
					.comment("Entities that CAN be picked up (useWhitelistEntities must be true)")
					.defineList("whitelist.allowedEntities", Arrays.asList(new String[]{}), (obj) -> obj instanceof String ? true : false);
			
			allowedBlocks = s
					.comment("Blocks that CAN be picked up (useWhitelistBlocks must be true)")
					.defineList("whitelist.allowedBlocks", Arrays.asList(new String[]{}), (obj) -> obj instanceof String ? true : false);
			
			allowedStacking = s
					.comment("Entities that CAN have other entities stacked on top of them (useWhitelistStacking must be true)")
					.defineList("whitelist.allowedStacking", Arrays.asList(new String[]{}), (obj) -> obj instanceof String ? true : false);
		}
	}
	
	public static class Blacklist
	{
		public static ConfigValue<List<? extends String>> forbiddenTiles;
		
		public static ConfigValue<List<? extends String>> forbiddenEntities;
		
		public static ConfigValue<List<? extends String>> forbiddenStacking;
		
		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			s.comment("Blacklist");
			
			forbiddenTiles = s
					.comment("Tile Entities that cannot be picked up")
					.defineList("blacklist.forbiddenTiles", Arrays.asList(new String[]
			    			{
			    					"minecraft:end_portal",
			    					"minecraft:end_gateway",
			    					"minecraft:tall_grass",
			    					"minecraft:large_fern",
			    					"minecraft:peony",
			    					"minecraft:rose_bush",
			    					"minecraft:lilac",
			    					"minecraft:sunflower",
			    					"minecraft:*_bed",
			    					"minecraft:wooden_door",
			    					"minecraft:iron_door",
			    					"minecraft:spruce_door",
			    					"minecraft:birch_door",
			    					"minecraft:jungle_door",
			    					"minecraft:acacia_door",
			    					"minecraft:dark_oak_door",
			    					"minecraft:waterlily",
			    					"minecraft:cake",
			    					"minecraft:portal",
			    					"minecraft:tall_seagrass",
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
			    					"mcmultipart:*",
			    					"enderstorage:*",
			    					"betterstorage:*",
			    					"practicallogistics2:*",
			    					"wearablebackpacks:*"

			    			}), (obj) -> obj instanceof String ? true : false);
			
			forbiddenEntities = s
					.comment("Entities that cannot be picked up")
					.defineList("blacklist.forbiddenEntities", Arrays.asList(new String[]
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
									"animania:hamster",
									"animania:ferret*",
									"animania:hedgehog*",
									"animania:cart",
									"animania:wagon",
									"mynko:*"
							}), (obj) -> obj instanceof String ? true : false);
			
			forbiddenStacking = s
					.comment("Entities that cannot have other entities stacked on top of them")
					.defineList("blacklist.forbiddenStacking", Arrays.asList(new String[]
							{
									"minecraft:horse"
							}), (obj) -> obj instanceof String ? true : false);
		}
		
	}
	
	public static class ModelOverrides
	{
		public static ConfigValue<List<? extends String>> modelOverrides;
		
		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			c.comment("Model Overrides");

			
			modelOverrides = c
					.comment("Model Overrides based on NBT or on Meta. Advanced Users Only!")
					.defineList("modeloverrides.overrides", Arrays.asList(new String[]
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
			        			}), (obj) -> obj instanceof String ? true : false);
		}
	}
	
	
	
	public static class CustomPickupConditions
	{

		public static ConfigValue<List<? extends String>> customPickupConditionsBlocks;
		
		public static ConfigValue<List<? extends String>> customPickupConditionsEntities;
		
		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			s.comment("Custom Pickup Conditions");

			
			customPickupConditionsBlocks = s
					.comment("Custom Pickup Conditions for Blocks")
					.defineList("custom_pickup_conditions.customPickupConditionsBlocks", Arrays.asList(new String[]{}), (obj) -> obj instanceof String ? true : false);
			
			customPickupConditionsEntities = s
					.comment("Custom Pickup Conditions for Entities")
					.defineList("custom_pickup_conditions.customPickupConditionsEntities", Arrays.asList(new String[]{}), (obj) -> obj instanceof String ? true : false);
		}
	}

}
