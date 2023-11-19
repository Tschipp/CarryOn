package tschipp.carryon.common.config;

import tschipp.carryon.config.PropertyType;
import tschipp.carryon.config.annotations.Category;
import tschipp.carryon.config.annotations.Config;
import tschipp.carryon.config.annotations.Property;

public class CarryConfig
{

	@Config("carryon-common")
	public static class Common
	{
		//Settings
		@Property(
				type = PropertyType.CATEGORY,
				description = "General Settings"
		)
		public Settings settings = new Settings();

		@Category("settings")
		public static class Settings
		{
			@Property(
					type = PropertyType.DOUBLE,
					description = "Maximum distance from where Blocks and Entities can be picked up",
					minD = 0
			)
			public double maxDistance = 2.5;

			@Property(
					type = PropertyType.DOUBLE,
					description = "Max width of entities that can be picked up in survival mode",
					minD = 0,
					maxD = 10
			)
			public double maxEntityWidth = 1.5;

			@Property(
					type = PropertyType.DOUBLE,
					description = "Max height of entities that can be picked up in survival mode",
					minD = 0,
					maxD = 10
			)
			public double maxEntityHeight = 2.5;

			@Property(
					type = PropertyType.DOUBLE,
					description = "Slowness multiplier for blocks",
					minD = 0
			)
			public double blockSlownessMultiplier = 1;

			@Property(
					type = PropertyType.DOUBLE,
					description = "Slowness multiplier for entities",
					minD = 0
			)
			public double entitySlownessMultiplier = 1;

			@Property(
					type = PropertyType.INT,
					description = "Maximum stack limit for entities",
					min = 1
			)
			public int maxEntityStackLimit = 10;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "More complex Tile Entities slow down the player more"
			)
			public boolean heavyTiles = true;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Allow all blocks to be picked up, not just Tile Entites. White/Blacklist will still be respected."
			)
			public boolean pickupAllBlocks = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Whether Blocks and Entities slow the creative player down when carried"
			)
			public boolean slownessInCreative = true;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Whether hostile mobs should be able to picked up in survival mode"
			)
			public boolean pickupHostileMobs = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Larger Entities slow down the player more"
			)
			public boolean heavyEntities = true;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Allow babies to be carried even when adult mob is blacklisted (or not whitelisted)"
			)
			public boolean allowBabies = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Use Whitelist instead of Blacklist for Blocks"
			)
			public boolean useWhitelistBlocks = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Use Whitelist instead of Blacklist for Entities"
			)
			public boolean useWhitelistEntities = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Use Whitelist instead of Blacklist for Stacking"
			)
			public boolean useWhitelistStacking = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Whether the player can hit blocks and entities while carrying or not"
			)
			public boolean hitWhileCarrying = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Whether the player drops the carried object when hit or not"
			)
			public boolean dropCarriedWhenHit = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Use custom Pickup Scripts. Having this set to false, will not allow you to run scripts, but will increase your performance"
			)
			public boolean useScripts = false;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Allows entities to be stacked on top of each other"
			)
			public boolean stackableEntities = true;

			@Property(
					type = PropertyType.BOOLEAN,
					description = "Whether entities' size matters when stacking or not. This means that larger entities cannot be stacked on smaller ones"
			)
			public boolean entitySizeMattersStacking = true;

			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Usually all the block state information is retained when placing a block that was picked up. But some information is changed to a modified property, like rotation or orientation. In this list, add additional properties that should NOT be saved and instead be updated when placed. Format: modid:block[propertyname]. Note: You don't need to add an entry for every subtype of a same block. For example, we only add an entry for one type of slab, but the change is applied to all slabs."
			)
			public String[] placementStateExceptions = {
					"minecraft:chest[type]",
					"minecraft:stone_button[face]",
					"minecraft:vine[north,east,south,west,up]",
					"minecraft:creeper_head[rotation]",
					"minecraft:glow_lichen[north,east,south,west,up,down]",
					"minecraft:oak_sign[rotation]",
					"minecraft:oak_trapdoor[half]",
			};

            @Property(
                    type = PropertyType.BOOLEAN,
                    description = "Whether Players can be picked up. Creative players can't be picked up in Survival Mode"
            )
            public boolean pickupPlayers = true;

            @Property(
                    type = PropertyType.BOOLEAN,
                    description = "Whether players in Survival Mode can pick up unbreakable blocks. Creative players always can."
            )
            public boolean pickupUnbreakableBlocks = false;
        }

		@Property(
				type = PropertyType.CATEGORY,
				description = "Whitelist. Read about the format here: https://github.com/Tschipp/CarryOn/wiki/Black---and-Whitelist-Config"
		)

		//Whitelist
		public Whitelist whitelist = new Whitelist();

		@Category("whitelist")
		public static class Whitelist
		{
			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Entities that CAN be picked up (useWhitelistEntities must be true)"
			)
			public String[] allowedEntities = {};

			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Blocks that CAN be picked up (useWhitelistBlocks must be true)"
			)
			public String[] allowedBlocks = {};

			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Entities that CAN have other entities stacked on top of them (useWhitelistStacking must be true)"
			)
			public String[] allowedStacking = {};
		}

		//Blacklist
		@Property(
				type = PropertyType.CATEGORY,
				description = "Blacklist. Read about the format here: https://github.com/Tschipp/CarryOn/wiki/Black---and-Whitelist-Config"
		)
		public Blacklist blacklist = new Blacklist();

		@Category("blacklist")
		public static class Blacklist
		{
			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Blocks that cannot be picked up"
			)
			public String[] forbiddenTiles = {
					"#forge:immovable", "#forge:relocation_not_supported", "minecraft:end_portal", "minecraft:piston_head",
					"minecraft:end_gateway", "minecraft:tall_grass", "minecraft:large_fern", "minecraft:peony",
					"minecraft:rose_bush", "minecraft:lilac", "minecraft:sunflower", "minecraft:*_bed",
					"minecraft:*_door", "minecraft:big_dripleaf_stem", "minecraft:waterlily", "minecraft:cake",
					"minecraft:nether_portal", "minecraft:tall_seagrass", "animania:block_trough",
					"animania:block_invisiblock", "colossalchests:*", "ic2:*", "bigreactors:*", "forestry:*",
					"tconstruct:*", "rustic:*", "botania:*", "astralsorcery:*", "quark:colored_bed_*",
					"immersiveengineering:*", "embers:block_furnace", "embers:ember_bore",
					"embers:ember_activator", "embers:mixer", "embers:heat_coil", "embers:large_tank",
					"embers:crystal_cell", "embers:alchemy_pedestal", "embers:boiler", "embers:combustor",
					"embers:catalzyer", "embers:field_chart", "embers:inferno_forge",
					"storagedrawers:framingtable", "skyresources:*", "lootbags:*", "exsartagine:*",
					"aquamunda:tank", "opencomputers:*", "malisisdoors:*", "industrialforegoing:*",
					"minecolonies:*", "thaumcraft:pillar*", "thaumcraft:infernal_furnace",
					"thaumcraft:placeholder*", "thaumcraft:infusion_matrix", "thaumcraft:golem_builder",
					"thaumcraft:thaumatorium*", "magneticraft:oil_heater", "magneticraft:solar_panel",
					"magneticraft:steam_engine", "magneticraft:shelving_unit", "magneticraft:grinder",
					"magneticraft:sieve", "magneticraft:solar_tower", "magneticraft:solar_mirror",
					"magneticraft:container", "magneticraft:pumpjack", "magneticraft:solar_panel",
					"magneticraft:refinery", "magneticraft:oil_heater", "magneticraft:hydraulic_press",
					"magneticraft:multiblock_gap", "refinedstorage:*", "mcmultipart:*", "enderstorage:*",
					"betterstorage:*", "practicallogistics2:*", "wearablebackpacks:*", "rftools:screen",
					"rftools:creative_screen", "create:*", "magic_doorknob:*", "iceandfire:*", "ftbquests:*",
					"waystones:*", "contact:*", "framedblocks:*", "securitycraft:*", "forgemultipartcbe:*", "integrateddynamics:cable",
					"mekanismgenerators:wind_generator", "cookingforblockheads:cabinet", "cookingforblockheads:corner", "cookingforblockheads:counter",
					"cookingforblockheads:oven", "cookingforblockheads:toaster", "cookingforblockheads:milk_jar", "cookingforblockheads:cow_jar",
					"cookingforblockheads:fruit_basket", "cookingforblockheads:cooking_table", "cookingforblockheads:fridge", "cookingforblockheads:sink",
					"powah:*", "advancementtrophies:trophy", "mekanismgenerators:heat_generator", "mna:filler_block"
			};

			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Entities that cannot be picked up"
			)
			public String[] forbiddenEntities = {
					"minecraft:end_crystal", "minecraft:ender_dragon", "minecraft:ghast",
					"minecraft:shulker", "minecraft:leash_knot", "minecraft:armor_stand",
					"minecraft:item_frame", "minecraft:painting", "minecraft:shulker_bullet",
					"animania:hamster", "animania:ferret*", "animania:hedgehog*", "animania:cart",
					"animania:wagon", "mynko:*", "pixelmon:*", "mocreatures:*", "quark:totem", "vehicle:*",
					"securitycraft:*", "taterzens:npc", "easy_npc:*", "bodiesbodies:dead_body"
			};

			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Entities that cannot have other entities stacked on top of them"
			)
			public String[] forbiddenStacking = {
					"minecraft:horse"
			};
		}

		//Custom Pickup Conditions
		@Property(
				type = PropertyType.CATEGORY,
				description = "Custom Pickup Conditions. Read about the format here: https://github.com/Tschipp/CarryOn/wiki/Custom-Pickup-Condition-Config"
		)
		public CustomPickupConditions customPickupConditions = new CustomPickupConditions();

		@Category("customPickupConditions")
		public static class CustomPickupConditions
		{
			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Custom Pickup Conditions for Blocks"
			)
			public String[] customPickupConditionsBlocks = {};

			@Property(
					type = PropertyType.STRING_ARRAY,
					description = "Custom Pickup Conditions for Entities"
			)
			public String[] customPickupConditionsEntities = {};
		}
	}

	@Config("carryon-client")
	public static class Client
	{

		@Property(
				type = PropertyType.BOOLEAN,
				description = "If the front of the Tile Entities should face the player or should face outward"
		)
		public boolean facePlayer = false;

		@Property(
				type = PropertyType.BOOLEAN,
				description = "Arms should render on sides when carrying. Set to false if you experience issues with mods that replace the player model (like MoBends, etc)"
		)
		public boolean renderArms = true;

		@Property(
				type = PropertyType.STRING_ARRAY,
				description = "Model Overrides based on NBT or Meta. Advanced users only! Read about the format here: https://github.com/Tschipp/CarryOn/wiki/Model-Override-Config"
		)
		public String[] modelOverrides = {
				"minecraft:redstone_wire->(item)minecraft:redstone",
				"minecraft:bamboo_sapling->(block)minecraft:bamboo",
				"minecraft:candle_cake->(block)minecraft:cake"
		};
	}

}
