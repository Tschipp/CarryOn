package tschipp.carryon.common.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.config.CarryOnConfig;

public class ScriptReader
{
	private static ArrayList<File> scripts = new ArrayList<File>();
	public static HashMap<Integer, CarryOnOverride> OVERRIDES = new HashMap<Integer, CarryOnOverride>();
	
	//public static HashSet<CarryOnOverride> OVERRIDES = new HashSet<CarryOnOverride>();

	public static void preInit(FMLPreInitializationEvent event)
	{
		CarryOn.CONFIGURATION_FILE = new File(event.getModConfigurationDirectory(), "carryon-scripts/");
		if (!CarryOn.CONFIGURATION_FILE.exists())
			CarryOn.CONFIGURATION_FILE.mkdir();

		for (File file : CarryOn.CONFIGURATION_FILE.listFiles())
		{
			if (file.getName().endsWith(".json"))
				scripts.add(file);
		}

	}

	public static void parseScripts() throws JsonIOException, JsonSyntaxException, FileNotFoundException, NBTException
	{
		if(!CarryOnConfig.settings.useScripts)
			return;
			
		for (File file : scripts)
		{
			boolean errored = false;
			int hash = file.getAbsolutePath().hashCode();

			JsonParser parser = new JsonParser();
			JsonObject json = (JsonObject) parser.parse(new FileReader(file.getAbsolutePath()));

			JsonObject object = (JsonObject) json.get("object");
			JsonObject conditions = (JsonObject) json.get("conditions");
			JsonObject render = (JsonObject) json.get("render");

			if ((object != null && conditions != null) || (object != null && render != null))
			{
				JsonObject block = (JsonObject) object.get("block");
				JsonObject entity = (JsonObject) object.get("entity");

				if ((block == null && entity == null) || (block != null && entity != null))
					errored = true;
				
				if (!errored)
				{
					CarryOnOverride override = new CarryOnOverride();
					
					if (block != null)
					{
						override.setBlock(true);
						JsonElement name = block.get("name");
						JsonElement meta = block.get("meta");
						JsonElement material = block.get("material");
						JsonElement hardness = block.get("hardness");
						JsonElement resistance = block.get("resistance");
						JsonObject nbt = (JsonObject) block.get("nbt");

						if (name != null)
							override.setTypeNameBlock(name.getAsString());
						if (meta != null)
							override.setTypeMeta(meta.getAsString());
						if (material != null)
							override.setTypeMaterial(material.getAsString());
						if (hardness != null)
							override.setTypeHardness(hardness.getAsString());
						if (resistance != null)
							override.setTypeResistance(resistance.getAsString());
						if (nbt != null)
							override.setTypeBlockTag(JsonToNBT.getTagFromJson(nbt.toString()));
					}
					else
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
						JsonElement achievement = conditions.get("achievement");
						JsonElement xp = conditions.get("xp");
						JsonElement gamemode = conditions.get("gamemode");
						JsonElement scoreboard = conditions.get("scoreboard");
						JsonElement position = conditions.get("position");

						if(gamestage != null)
							override.setConditionGamestage(gamestage.getAsString());
						if(achievement != null)
							override.setConditionAchievement(achievement.getAsString());
						if(xp != null)
							override.setConditionXp(xp.getAsString());
						if(gamemode != null)
							override.setConditionGamemode(gamemode.getAsString());
						if(scoreboard != null)
							override.setConditionScoreboard(scoreboard.getAsString());
						if(position != null)
							override.setConditionPosition(position.getAsString());
					}
					
					if (render != null)
					{
						JsonElement name_block = render.get("name_block");
						JsonElement name_entity = render.get("name_entity");
						JsonElement meta = render.get("meta");
						JsonObject nbt = (JsonObject) render.get("nbt");
						JsonElement translation = render.get("translation");
						JsonElement rotation = render.get("rotation");
						JsonElement scale = render.get("scale");
						JsonElement rotationLeftArm = render.get("rotation_left_arm");
						JsonElement rotationRightArm = render.get("rotation_right_arm");
						JsonElement renderLeftArm = render.get("render_left_arm");
						JsonElement renderRightArm = render.get("render_right_arm");

						if(name_block != null)
							override.setRenderNameBlock(name_block.getAsString());
						if(name_entity != null)
							override.setRenderNameEntity(name_entity.getAsString());
						if(meta != null)
							override.setRenderMeta(meta.getAsInt());
						if(translation != null)
							override.setRenderTranslation(translation.getAsString());
						if(rotation != null)
							override.setRenderRotation(rotation.getAsString());
						if(scale != null)
							override.setRenderScale(scale.getAsString());
						if (nbt != null)
							override.setRenderNBT(JsonToNBT.getTagFromJson(nbt.toString()));
						if(rotationLeftArm != null)
							override.setRenderRotationLeftArm(rotationLeftArm.getAsString());
						if(rotationRightArm != null)
							override.setRenderRotationRightArm(rotationRightArm.getAsString());
						if(renderLeftArm != null)
							override.setRenderLeftArm(renderLeftArm.getAsBoolean());
						if(renderRightArm != null)
							override.setRenderRightArm(renderRightArm.getAsBoolean());
					}
					
					OVERRIDES.put(override.hashCode(), override);

				}
			}
		}
		
		System.out.println("Successfully parsed scripts!");
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
		}
		catch (JsonIOException | JsonSyntaxException | FileNotFoundException | NBTException e)
		{
			e.printStackTrace();
		}
	}

}
