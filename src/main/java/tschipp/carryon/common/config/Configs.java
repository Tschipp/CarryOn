package tschipp.carryon.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.ListHandler;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = CarryOn.MODID, bus = Bus.MOD)
public class Configs
{

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

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading event)
	{
		if (event.getConfig().getModId().equals(CarryOn.MODID)) {
			ListHandler.initConfigLists();

			CommentedConfig cfg = event.getConfig().getConfigData();

			if (cfg instanceof CommentedFileConfig)
				((CommentedFileConfig) cfg).load();
		}
	}

	@SubscribeEvent
	public static void onConfigChanged(ModConfigEvent.Reloading event)
	{
		if (event.getConfig().getModId().equals(CarryOn.MODID)) {
			ListHandler.initConfigLists();

			CommentedConfig cfg = event.getConfig().getConfigData();

			if (cfg instanceof CommentedFileConfig)
				((CommentedFileConfig) cfg).load();
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

			s.push("settings");
			c.push("settings");

			maxDistance = s.comment("Maximum distance from where Blocks and Entities can be picked up").defineInRange("maxDistance", 2.5, 0, Double.MAX_VALUE);

			maxEntityWidth = s.comment("Max width of entities that can be picked up in survival mode").defineInRange("maxEntityWidth", 1.5, 0, 10);

			maxEntityHeight = s.comment("Max height of entities that can be picked up in survival mode").defineInRange("maxEntityHeight", 2.0, 0, 10);

			blockSlownessMultiplier = s.comment("Slowness multiplier for blocks").defineInRange("blockSlownessMultiplier", 1, 0, Double.MAX_VALUE);

			entitySlownessMultiplier = s.comment("Slowness multiplier for entities").defineInRange("entitySlownessMultiplier", 1, 0, Double.MAX_VALUE);

			maxEntityStackLimit = s.comment("Maximum stack limit for entities").defineInRange("maxEntityStackLimit", 10, 1, Integer.MAX_VALUE);

			facePlayer = c.comment("If the front of the Tile Entities should face the player or should face outward").define("facePlayer", false);

			heavyTiles = s.comment("More complex Tile Entities slow down the player more").define("heavyTiles", true);

			pickupAllBlocks = s.comment("Allow all blocks to be picked up, not just Tile Entites").define("pickupAllBlocks", false);

			slownessInCreative = s.comment("Whether Blocks and Entities slow the creative player down when carried").define("slownessInCreative", true);

			pickupHostileMobs = s.comment("Whether hostile mobs should be able to picked up in survival mode").define("pickupHostileMobs", false);

			heavyEntities = s.comment("Larger Entities slow down the player more").define("heavyEntities", true);

			renderArms = c.comment("Arms should render on sides when carrying").define("renderArms", true);

			allowBabies = s.comment("Allow babies to be carried even when adult mob is blacklisted (or not whitelisted)").define("allowBabies", false);

			useWhitelistBlocks = s.comment("Use Whitelist instead of Blacklist for Blocks").define("useWhitelistBlocks", false);

			useWhitelistEntities = s.comment("Use Whitelist instead of Blacklist for Entities").define("useWhitelistEntities", false);

			useWhitelistStacking = s.comment("Use Whitelist instead of Blacklist for Stacking").define("useWhitelistStacking", false);

			hitWhileCarrying = s.comment("Whether the player can hit blocks and entities while carrying or not").define("hitWhileCarrying", false);

			dropCarriedWhenHit = s.comment("Whether the player drops the carried object when hit or not").define("dropCarriedWhenHit", false);

			useScripts = s.comment("Use custom Pickup Scripts. Having this set to false, will not allow you to run scripts, but will increase your performance").worldRestart().define("useScripts", false);

			stackableEntities = s.comment("Allows entities to be stacked using Carry On").define("stackableEntities", true);

			entitySizeMattersStacking = s.comment("Whether entities' size matters when stacking or not").define("stackableEntities", true);

			s.pop();
			c.pop();

		}

	}

	public static class WhiteList
	{
		public static ConfigValue<List<? extends String>> allowedEntities;

		public static ConfigValue<List<? extends String>> allowedBlocks;

		public static ConfigValue<List<? extends String>> allowedStacking;

		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			s.comment("Whitelist. Read about the format here: https://github.com/Tschipp/CarryOn/wiki/Black---and-Whitelist-Config");

			allowedEntities = s.comment("Entities that CAN be picked up (useWhitelistEntities must be true)").defineList("whitelist.allowedEntities", Arrays.asList(), obj -> obj instanceof String ? true : false);

			allowedBlocks = s.comment("Blocks that CAN be picked up (useWhitelistBlocks must be true)").defineList("whitelist.allowedBlocks", Arrays.asList(), obj -> obj instanceof String ? true : false);

			allowedStacking = s.comment("Entities that CAN have other entities stacked on top of them (useWhitelistStacking must be true)").defineList("whitelist.allowedStacking", Arrays.asList(), obj -> obj instanceof String ? true : false);
		}
	}

	public static class Blacklist
	{
		public static ConfigValue<List<? extends String>> forbiddenTiles;

		public static ConfigValue<List<? extends String>> forbiddenEntities;

		public static ConfigValue<List<? extends String>> forbiddenStacking;

		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			s.comment("Blacklist. Read about the format here: https://github.com/Tschipp/CarryOn/wiki/Black---and-Whitelist-Config");

			forbiddenTiles = s.comment("Blocks that cannot be picked up").defineList("blacklist.forbiddenTiles", Arrays.asList("#forge:immovable", "#forge:relocation_not_supported", "minecraft:end_portal", "minecraft:end_gateway", "minecraft:tall_grass", "minecraft:large_fern", "minecraft:peony", "minecraft:rose_bush", "minecraft:lilac", "minecraft:sunflower", "minecraft:*_bed", "minecraft:oak_door", "minecraft:iron_door", "minecraft:spruce_door", "minecraft:birch_door", "minecraft:jungle_door", "minecraft:acacia_door", "minecraft:dark_oak_door", "minecraft:waterlily", "minecraft:cake", "minecraft:nether_portal", "minecraft:tall_seagrass", "animania:block_trough", "animania:block_invisiblock", "colossalchests:*", "ic2:*", "bigreactors:*", "forestry:*", "tconstruct:*", "rustic:*", "botania:*", "astralsorcery:*", "quark:colored_bed_*", "immersiveengineering:*", "embers:block_furnace", "embers:ember_bore", "embers:ember_activator", "embers:mixer", "embers:heat_coil", "embers:large_tank", "embers:crystal_cell", "embers:alchemy_pedestal", "embers:boiler", "embers:combustor", "embers:catalzyer", "embers:field_chart", "embers:inferno_forge", "storagedrawers:framingtable", "skyresources:*", "lootbags:*", "exsartagine:*", "aquamunda:tank", "opencomputers:*", "malisisdoors:*", "industrialforegoing:*", "minecolonies:*", "thaumcraft:pillar*", "thaumcraft:infernal_furnace", "thaumcraft:placeholder*", "thaumcraft:infusion_matrix", "thaumcraft:golem_builder", "thaumcraft:thaumatorium*", "magneticraft:oil_heater", "magneticraft:solar_panel", "magneticraft:steam_engine", "magneticraft:shelving_unit", "magneticraft:grinder", "magneticraft:sieve", "magneticraft:solar_tower", "magneticraft:solar_mirror", "magneticraft:container", "magneticraft:pumpjack", "magneticraft:solar_panel", "magneticraft:refinery", "magneticraft:oil_heater", "magneticraft:hydraulic_press", "magneticraft:multiblock_gap", "refinedstorage:*", "mcmultipart:*", "enderstorage:*", "betterstorage:*", "practicallogistics2:*", "wearablebackpacks:*", "rftools:screen", "rftools:creative_screen", "create:*", "magic_doorknob:*", "iceandfire:*", "ftbquests:*", "waystones:*", "framedblocks:*", "securitycraft:*", "forgemultipartcbe:*", "integrateddynamics:cable",
					"mekanismgenerators:wind_generator", "vm:vending_machine"), obj -> obj instanceof String);

			forbiddenEntities = s.comment("Entities that cannot be picked up").defineList("blacklist.forbiddenEntities", Arrays.asList("minecraft:end_crystal", "minecraft:ender_dragon", "minecraft:ghast", "minecraft:shulker", "minecraft:leash_knot", "minecraft:armor_stand", "minecraft:item_frame", "minecraft:painting", "minecraft:shulker_bullet", "animania:hamster", "animania:ferret*", "animania:hedgehog*", "animania:cart", "animania:wagon", "mynko:*", "pixelmon:*", "mocreatures:*", "quark:totem", "vehicle:*",
					"securitycraft:*", "taterzens:npc", "easy_npc:*", "minecolonies:*"), obj -> obj instanceof String ? true : false);

			forbiddenStacking = s.comment("Entities that cannot have other entities stacked on top of them").defineList("blacklist.forbiddenStacking", Arrays.asList("minecraft:horse"), obj -> obj instanceof String ? true : false);
		}

	}

	public static class ModelOverrides
	{
		public static ConfigValue<List<? extends String>> modelOverrides;

		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			c.comment("Model Overrides. Read about the format here: https://github.com/Tschipp/CarryOn/wiki/Model-Override-Config");

			modelOverrides = c.comment("Model Overrides based on NBT or on Meta. Advanced Users Only!").defineList("modeloverrides.overrides", Arrays.asList("minecraft:hopper->(block)minecraft:hopper", "minecraft:comparator->(block)minecraft:comparator", "minecraft:repeater->(block)minecraft:repeater", "minecraft:cauldron->(block)minecraft:cauldron", "minecraft:brewing_stand->(item)minecraft:brewing_stand", "minecraft:flower_pot->(block)minecraft:flower_pot", "minecraft:sugar_cane->(block)minecraft:sugar_cane", "minecraft:redstone_wire->(item)minecraft:redstone", "animania:block_nest->(block)animania:block_nest", "animania:cheese_mold;0->(block)animania:cheese_mold;0", "animania:cheese_mold;1->(block)animania:cheese_mold;1", "animania:cheese_mold;2->(block)animania:cheese_mold;2", "animania:cheese_mold;3->(block)animania:cheese_mold;3", "animania:cheese_mold;4->(block)animania:cheese_mold;4", "animania:cheese_mold;5->(block)animania:cheese_mold;5", "animania:cheese_mold;6->(block)animania:cheese_mold;6", "animania:cheese_mold;7->(block)animania:cheese_mold;7", "animania:cheese_mold;8->(block)animania:cheese_mold;8", "animania:cheese_mold;9->(block)animania:cheese_mold;9", "animania:cheese_mold;10->(block)animania:cheese_mold;10"), obj -> obj instanceof String ? true : false);
		}
	}

	public static class CustomPickupConditions
	{

		public static ConfigValue<List<? extends String>> customPickupConditionsBlocks;

		public static ConfigValue<List<? extends String>> customPickupConditionsEntities;

		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			s.comment("Custom Pickup Conditions. Read about the format here: https://github.com/Tschipp/CarryOn/wiki/Custom-Pickup-Condition-Config");

			customPickupConditionsBlocks = s.comment("Custom Pickup Conditions for Blocks").defineList("custom_pickup_conditions.customPickupConditionsBlocks", Arrays.asList(), obj -> obj instanceof String ? true : false);

			customPickupConditionsEntities = s.comment("Custom Pickup Conditions for Entities").defineList("custom_pickup_conditions.customPickupConditionsEntities", Arrays.asList(), obj -> obj instanceof String ? true : false);
		}
	}

}
