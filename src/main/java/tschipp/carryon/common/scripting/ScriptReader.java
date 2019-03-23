package tschipp.carryon.common.scripting;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.nbt.JsonToNBT;
import net.minecraftforge.fml.loading.FMLPaths;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.config.Configs.Settings;

public class ScriptReader
{
	private static ArrayList<File> scripts = new ArrayList<File>();
	public static HashMap<Integer, CarryOnOverride> OVERRIDES = new HashMap<Integer, CarryOnOverride>();

	// public static HashSet<CarryOnOverride> OVERRIDES = new
	// HashSet<CarryOnOverride>();

	public static void preInit()
	{
		scripts.clear();
		
		CarryOn.CONFIGURATION_FILE = new File(FMLPaths.CONFIGDIR.get().toString(), "/carryon-scripts/");

		if (!CarryOn.CONFIGURATION_FILE.exists())
			CarryOn.CONFIGURATION_FILE.mkdir();

		for (File file : CarryOn.CONFIGURATION_FILE.listFiles())
		{
			if (file.getName().endsWith(".json"))
				scripts.add(file);
		}

	}

	public static void parseScripts() 
	{
		OVERRIDES.clear();
		
		try
		{
			if (!Settings.useScripts.get())
				return;

			for (File file : scripts)
			{
				boolean errored = false;
				JsonParser parser = new JsonParser();
				JsonObject json = (JsonObject) parser.parse(new FileReader(file.getAbsolutePath()));

				JsonObject object = (JsonObject) json.get("object");
				JsonObject conditions = (JsonObject) json.get("conditions");
				JsonObject render = (JsonObject) json.get("render");
				JsonObject effects = (JsonObject) json.get("effects");

				if ((object != null && conditions != null) || (object != null && render != null) || (object != null && effects != null))
				{
					JsonObject block = (JsonObject) object.get("block");
					JsonObject entity = (JsonObject) object.get("entity");

					if ((block == null && entity == null) || (block != null && entity != null))
						errored = true;

					if (!errored)
					{
						CarryOnOverride override = new CarryOnOverride(file.getAbsolutePath());

						if (block != null)
						{
							override.setBlock(true);
							JsonElement name = block.get("name");
							JsonElement material = block.get("material");
							JsonElement hardness = block.get("hardness");
							JsonElement resistance = block.get("resistance");
							JsonObject nbt = (JsonObject) block.get("nbt");

							if (name != null)
								override.setTypeNameBlock(name.getAsString());
							if (material != null)
								override.setTypeMaterial(material.getAsString());
							if (hardness != null)
								override.setTypeHardness(hardness.getAsString());
							if (resistance != null)
								override.setTypeResistance(resistance.getAsString());
							if (nbt != null)
								override.setTypeBlockTag(JsonToNBT.getTagFromJson(nbt.toString()));
						} else
						{
							override.setEntity(true);
							JsonElement name = entity.get("name");
							JsonElement health = entity.get("health");
							JsonElement height = entity.get("height");
							JsonElement width = entity.get("width");
							JsonObject nbt = (JsonObject) entity.get("nbt");

							if (name != null)
								override.setTypeNameEntity(name.getAsString());
							if (health != null)
								override.setTypeHealth(health.getAsString());
							if (height != null)
								override.setTypeHeight(height.getAsString());
							if (width != null)
								override.setTypeWidth(width.getAsString());
							if (nbt != null)
								override.setTypeEntityTag(JsonToNBT.getTagFromJson(nbt.toString()));
						}

						if (conditions != null)
						{
							JsonElement gamestage = conditions.get("gamestage");
							JsonElement achievement = conditions.get("advancement");
							JsonElement xp = conditions.get("xp");
							JsonElement gamemode = conditions.get("gamemode");
							JsonElement scoreboard = conditions.get("scoreboard");
							JsonElement position = conditions.get("position");
							JsonElement potionEffects = conditions.get("effects");

							if (gamestage != null)
								override.setConditionGamestage(gamestage.getAsString());
							if (achievement != null)
								override.setConditionAchievement(achievement.getAsString());
							if (xp != null)
								override.setConditionXp(xp.getAsString());
							if (gamemode != null)
								override.setConditionGamemode(gamemode.getAsString());
							if (scoreboard != null)
								override.setConditionScoreboard(scoreboard.getAsString());
							if (position != null)
								override.setConditionPosition(position.getAsString());
							if (potionEffects != null)
								override.setConditionEffects(potionEffects.getAsString());
						}

						if (render != null)
						{
							JsonElement name_block = render.get("name_block");
							JsonElement name_entity = render.get("name_entity");
							JsonObject nbt = (JsonObject) render.get("nbt");
							JsonElement translation = render.get("translation");
							JsonElement rotation = render.get("rotation");
							JsonElement scaled = render.get("scaled");
							JsonElement rotationLeftArm = render.get("rotation_left_arm");
							JsonElement rotationRightArm = render.get("rotation_right_arm");
							JsonElement renderLeftArm = render.get("render_left_arm");
							JsonElement renderRightArm = render.get("render_right_arm");

							if (name_block != null)
								override.setRenderNameBlock(name_block.getAsString());
							if (name_entity != null)
								override.setRenderNameEntity(name_entity.getAsString());
							if (translation != null)
								override.setRenderTranslation(translation.getAsString());
							if (rotation != null)
								override.setRenderRotation(rotation.getAsString());
							if (scaled != null)
								override.setRenderscaled(scaled.getAsString());
							if (nbt != null)
								override.setRenderNBT(JsonToNBT.getTagFromJson(nbt.toString()));
							if (rotationLeftArm != null)
								override.setRenderRotationLeftArm(rotationLeftArm.getAsString());
							if (rotationRightArm != null)
								override.setRenderRotationRightArm(rotationRightArm.getAsString());
							if (renderLeftArm != null)
								override.setRenderLeftArm(renderLeftArm.getAsBoolean());
							if (renderRightArm != null)
								override.setRenderRightArm(renderRightArm.getAsBoolean());
						}

						if (effects != null)
						{
							JsonElement commandInit = effects.get("commandPickup");
							JsonElement commandLoop = effects.get("commandLoop");
							JsonElement commandPlace = effects.get("commandPlace");

							if (commandInit != null)
								override.setCommandInit(commandInit.getAsString());
							if (commandLoop != null)
								override.setCommandLoop(commandLoop.getAsString());
							if (commandPlace != null)
								override.setCommandPlace(commandPlace.getAsString());
						}

						OVERRIDES.put(override.hashCode(), override);
					}
				}
			}

			System.out.println("Successfully parsed scripts!");
		} catch (Exception e)
		{
			CarryOn.LOGGER.error(e);
		}
	}

	public static void reloadScripts()
	{
		scripts.clear();
		OVERRIDES.clear();

		for (File file : CarryOn.CONFIGURATION_FILE.listFiles())
		{
			if (file.getName().endsWith(".json"))
				scripts.add(file);
		}

		try
		{
			parseScripts();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
