package tschipp.carryon.common.scripting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import tschipp.carryon.common.helper.InvalidConfigException;

public class CarryOnOverride
{
	// BLOCKS
	private CompoundNBT typeBlockTag = new CompoundNBT();
	private String typeNameBlock = "";
	private String typeMaterial = "";
	private String typeHardness = "";
	private String typeResistance = "";

	// ENTITIES
	private CompoundNBT typeEntityTag;
	private String typeNameEntity = "";
	private String typeHeight = "";
	private String typeWidth = "";
	private String typeHealth = "";

	// CONDITIONS
	private String conditionGamestage = "";
	private String conditionAchievement = "";
	private String conditionXp = "";
	private String conditionGamemode = "";
	private String conditionScoreboard = "";
	private String conditionPosition = "";
	private String conditionEffects = "";

	// RENDER
	private String renderNameBlock = "";
	private String renderNameEntity = "";
	private CompoundNBT renderNBT = new CompoundNBT();
	private String renderTranslation = "";
	private String renderRotation = "";
	private String renderscaled = "";
	private String renderRotationLeftArm = "";
	private String renderRotationRightArm = "";
	private boolean renderLeftArm = true;
	private boolean renderRightArm = true;

	// EFFECTS
	private String commandInit = "";
	private String commandLoop = "";
	private String commandPlace = "";

	private boolean isBlock;
	private boolean isEntity;
	private String resourceLocation = "";
	public boolean isInvalid = false;

	public CarryOnOverride(String path)
	{
		this.resourceLocation = path;
	}

	public CarryOnOverride(JsonElement jsonElem, ResourceLocation loc)
	{
		boolean errored = false;
		resourceLocation = loc.toString();

		if (jsonElem != null && jsonElem.isJsonObject())
		{
			try
			{
				JsonObject json = jsonElem.getAsJsonObject();
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
						if (block != null)
						{
							setBlock(true);
							JsonElement name = block.get("name");
							JsonElement material = block.get("material");
							JsonElement hardness = block.get("hardness");
							JsonElement resistance = block.get("resistance");
							JsonObject nbt = (JsonObject) block.get("nbt");

							if (name != null)
								setTypeNameBlock(name.getAsString());
							if (material != null)
								setTypeMaterial(material.getAsString());
							if (hardness != null)
								setTypeHardness(hardness.getAsString());
							if (resistance != null)
								setTypeResistance(resistance.getAsString());
							if (nbt != null)
								setTypeBlockTag(JsonToNBT.getTagFromJson(nbt.toString()));
						}
						else
						{
							setEntity(true);
							JsonElement name = entity.get("name");
							JsonElement health = entity.get("health");
							JsonElement height = entity.get("height");
							JsonElement width = entity.get("width");
							JsonObject nbt = (JsonObject) entity.get("nbt");

							if (name != null)
								setTypeNameEntity(name.getAsString());
							if (health != null)
								setTypeHealth(health.getAsString());
							if (height != null)
								setTypeHeight(height.getAsString());
							if (width != null)
								setTypeWidth(width.getAsString());
							if (nbt != null)
								setTypeEntityTag(JsonToNBT.getTagFromJson(nbt.toString()));
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
								setConditionGamestage(gamestage.getAsString());
							if (achievement != null)
								setConditionAchievement(achievement.getAsString());
							if (xp != null)
								setConditionXp(xp.getAsString());
							if (gamemode != null)
								setConditionGamemode(gamemode.getAsString());
							if (scoreboard != null)
								setConditionScoreboard(scoreboard.getAsString());
							if (position != null)
								setConditionPosition(position.getAsString());
							if (potionEffects != null)
								setConditionEffects(potionEffects.getAsString());
						}

						if (render != null)
						{
							JsonElement name_block = render.get("name_block");
							JsonElement name_entity = render.get("name_entity");
							JsonObject nbt = (JsonObject) render.get("nbt");
							JsonElement translation = render.get("translation");
							JsonElement rotation = render.get("rotation");
							JsonElement scaled = render.get("scale");
							JsonElement rotationLeftArm = render.get("rotation_left_arm");
							JsonElement rotationRightArm = render.get("rotation_right_arm");
							JsonElement renderLeftArm = render.get("render_left_arm");
							JsonElement renderRightArm = render.get("render_right_arm");

							if (name_block != null)
								setRenderNameBlock(name_block.getAsString());
							if (name_entity != null)
								setRenderNameEntity(name_entity.getAsString());
							if (translation != null)
								setRenderTranslation(translation.getAsString());
							if (rotation != null)
								setRenderRotation(rotation.getAsString());
							if (scaled != null)
								setRenderscaled(scaled.getAsString());
							if (nbt != null)
								setRenderNBT(JsonToNBT.getTagFromJson(nbt.toString()));
							if (rotationLeftArm != null)
								setRenderRotationLeftArm(rotationLeftArm.getAsString());
							if (rotationRightArm != null)
								setRenderRotationRightArm(rotationRightArm.getAsString());
							if (renderLeftArm != null)
								setRenderLeftArm(renderLeftArm.getAsBoolean());
							if (renderRightArm != null)
								setRenderRightArm(renderRightArm.getAsBoolean());
						}

						if (effects != null)
						{
							JsonElement commandInit = effects.get("commandPickup");
							JsonElement commandLoop = effects.get("commandLoop");
							JsonElement commandPlace = effects.get("commandPlace");

							if (commandInit != null)
								setCommandInit(commandInit.getAsString());
							if (commandLoop != null)
								setCommandLoop(commandLoop.getAsString());
							if (commandPlace != null)
								setCommandPlace(commandPlace.getAsString());
						}
					}
					else
						isInvalid = true;
				}
			}
			catch (Exception e)
			{
				isInvalid = true;
			}
		}
		else
			isInvalid = true;
		
		if(!isBlock && !isEntity)
			isInvalid = true;
		
		if(isInvalid)
			new InvalidConfigException("The script parsing for " + resourceLocation + " has failed! Please double check!").printException();
	}

	public String getCommandInit()
	{
		return commandInit;
	}

	public void setCommandInit(String commandInit)
	{
		this.commandInit = commandInit;
	}

	public String getCommandLoop()
	{
		return commandLoop;
	}

	public void setCommandLoop(String commandLoop)
	{
		this.commandLoop = commandLoop;
	}

	public String getConditionEffects()
	{
		return conditionEffects;
	}

	public void setConditionEffects(String conditionEffects)
	{
		this.conditionEffects = conditionEffects;
	}

	public String getRenderRotationLeftArm()
	{
		return renderRotationLeftArm;
	}

	public void setRenderRotationLeftArm(String renderRotationLeftArm)
	{
		this.renderRotationLeftArm = renderRotationLeftArm;
	}

	public String getRenderRotationRightArm()
	{
		return renderRotationRightArm;
	}

	public void setRenderRotationRightArm(String renderRotationRightArm)
	{
		this.renderRotationRightArm = renderRotationRightArm;
	}

	public boolean isRenderLeftArm()
	{
		return renderLeftArm;
	}

	public void setRenderLeftArm(boolean renderLeftArm)
	{
		this.renderLeftArm = renderLeftArm;
	}

	public boolean isRenderRightArm()
	{
		return renderRightArm;
	}

	public void setRenderRightArm(boolean renderRightArm)
	{
		this.renderRightArm = renderRightArm;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceLocation == null) ? 0 : resourceLocation.hashCode());
		return result;
	}

	@Override
	public String toString()
	{
		return "Code: " + this.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CarryOnOverride other = (CarryOnOverride) obj;
		if (commandInit == null)
		{
			if (other.commandInit != null)
				return false;
		}
		else if (!commandInit.equals(other.commandInit))
			return false;
		if (commandLoop == null)
		{
			if (other.commandLoop != null)
				return false;
		}
		else if (!commandLoop.equals(other.commandLoop))
			return false;
		if (commandPlace == null)
		{
			if (other.commandPlace != null)
				return false;
		}
		else if (!commandPlace.equals(other.commandPlace))
			return false;
		if (conditionAchievement == null)
		{
			if (other.conditionAchievement != null)
				return false;
		}
		else if (!conditionAchievement.equals(other.conditionAchievement))
			return false;
		if (conditionEffects == null)
		{
			if (other.conditionEffects != null)
				return false;
		}
		else if (!conditionEffects.equals(other.conditionEffects))
			return false;
		if (conditionGamemode == null)
		{
			if (other.conditionGamemode != null)
				return false;
		}
		else if (!conditionGamemode.equals(other.conditionGamemode))
			return false;
		if (conditionGamestage == null)
		{
			if (other.conditionGamestage != null)
				return false;
		}
		else if (!conditionGamestage.equals(other.conditionGamestage))
			return false;
		if (conditionPosition == null)
		{
			if (other.conditionPosition != null)
				return false;
		}
		else if (!conditionPosition.equals(other.conditionPosition))
			return false;
		if (conditionScoreboard == null)
		{
			if (other.conditionScoreboard != null)
				return false;
		}
		else if (!conditionScoreboard.equals(other.conditionScoreboard))
			return false;
		if (conditionXp == null)
		{
			if (other.conditionXp != null)
				return false;
		}
		else if (!conditionXp.equals(other.conditionXp))
			return false;
		if (isBlock != other.isBlock)
			return false;
		if (isEntity != other.isEntity)
			return false;
		if (resourceLocation == null)
		{
			if (other.resourceLocation != null)
				return false;
		}
		else if (!resourceLocation.equals(other.resourceLocation))
			return false;
		if (renderLeftArm != other.renderLeftArm)
			return false;
		if (renderNBT == null)
		{
			if (other.renderNBT != null)
				return false;
		}
		else if (!renderNBT.equals(other.renderNBT))
			return false;
		if (renderNameBlock == null)
		{
			if (other.renderNameBlock != null)
				return false;
		}
		else if (!renderNameBlock.equals(other.renderNameBlock))
			return false;
		if (renderNameEntity == null)
		{
			if (other.renderNameEntity != null)
				return false;
		}
		else if (!renderNameEntity.equals(other.renderNameEntity))
			return false;
		if (renderRightArm != other.renderRightArm)
			return false;
		if (renderRotation == null)
		{
			if (other.renderRotation != null)
				return false;
		}
		else if (!renderRotation.equals(other.renderRotation))
			return false;
		if (renderRotationLeftArm == null)
		{
			if (other.renderRotationLeftArm != null)
				return false;
		}
		else if (!renderRotationLeftArm.equals(other.renderRotationLeftArm))
			return false;
		if (renderRotationRightArm == null)
		{
			if (other.renderRotationRightArm != null)
				return false;
		}
		else if (!renderRotationRightArm.equals(other.renderRotationRightArm))
			return false;
		if (renderscaled == null)
		{
			if (other.renderscaled != null)
				return false;
		}
		else if (!renderscaled.equals(other.renderscaled))
			return false;
		if (renderTranslation == null)
		{
			if (other.renderTranslation != null)
				return false;
		}
		else if (!renderTranslation.equals(other.renderTranslation))
			return false;
		if (typeBlockTag == null)
		{
			if (other.typeBlockTag != null)
				return false;
		}
		else if (!typeBlockTag.equals(other.typeBlockTag))
			return false;
		if (typeEntityTag == null)
		{
			if (other.typeEntityTag != null)
				return false;
		}
		else if (!typeEntityTag.equals(other.typeEntityTag))
			return false;
		if (typeHardness == null)
		{
			if (other.typeHardness != null)
				return false;
		}
		else if (!typeHardness.equals(other.typeHardness))
			return false;
		if (typeHealth == null)
		{
			if (other.typeHealth != null)
				return false;
		}
		else if (!typeHealth.equals(other.typeHealth))
			return false;
		if (typeHeight == null)
		{
			if (other.typeHeight != null)
				return false;
		}
		else if (!typeHeight.equals(other.typeHeight))
			return false;
		if (typeMaterial == null)
		{
			if (other.typeMaterial != null)
				return false;
		}
		else if (!typeMaterial.equals(other.typeMaterial))
			return false;
		if (typeNameBlock == null)
		{
			if (other.typeNameBlock != null)
				return false;
		}
		else if (!typeNameBlock.equals(other.typeNameBlock))
			return false;
		if (typeNameEntity == null)
		{
			if (other.typeNameEntity != null)
				return false;
		}
		else if (!typeNameEntity.equals(other.typeNameEntity))
			return false;
		if (typeResistance == null)
		{
			if (other.typeResistance != null)
				return false;
		}
		else if (!typeResistance.equals(other.typeResistance))
			return false;
		if (typeWidth == null)
		{
			if (other.typeWidth != null)
				return false;
		}
		else if (!typeWidth.equals(other.typeWidth))
			return false;
		return true;
	}

	public boolean isBlock()
	{
		return isBlock;
	}

	public void setBlock(boolean isBlock)
	{
		this.isBlock = isBlock;
	}

	public boolean isEntity()
	{
		return isEntity;
	}

	public void setEntity(boolean isEntity)
	{
		this.isEntity = isEntity;
	}

	public CompoundNBT getTypeBlockTag()
	{
		return typeBlockTag;
	}

	public String getTypeNameBlock()
	{
		return typeNameBlock;
	}

	public String getTypeMaterial()
	{
		return typeMaterial;
	}

	public String getTypeHardness()
	{
		return typeHardness;
	}

	public String getTypeResistance()
	{
		return typeResistance;
	}

	public CompoundNBT getTypeEntityTag()
	{
		return typeEntityTag;
	}

	public String getTypeNameEntity()
	{
		return typeNameEntity;
	}

	public String getTypeHeight()
	{
		return typeHeight;
	}

	public String getTypeWidth()
	{
		return typeWidth;
	}

	public String getTypeHealth()
	{
		return typeHealth;
	}

	public String getConditionGamestage()
	{
		return conditionGamestage;
	}

	public String getConditionAchievement()
	{
		return conditionAchievement;
	}

	public String getConditionXp()
	{
		return conditionXp;
	}

	public String getConditionGamemode()
	{
		return conditionGamemode;
	}

	public String getConditionScoreboard()
	{
		return conditionScoreboard;
	}

	public String getConditionPosition()
	{
		return conditionPosition;
	}

	public String getRenderNameBlock()
	{
		return renderNameBlock;
	}

	public String getRenderNameEntity()
	{
		return renderNameEntity;
	}

	public CompoundNBT getRenderNBT()
	{
		return renderNBT;
	}

	public String getRenderTranslation()
	{
		return renderTranslation;
	}

	public String getRenderRotation()
	{
		return renderRotation;
	}

	public String getRenderScaled()
	{
		return renderscaled;
	}

	public void setTypeBlockTag(CompoundNBT typeBlockTag)
	{
		this.typeBlockTag = typeBlockTag;
	}

	public void setTypeNameBlock(String typeNameBlock)
	{
		this.typeNameBlock = typeNameBlock;
	}

	public void setTypeMaterial(String typeMaterial)
	{
		this.typeMaterial = typeMaterial;
	}

	public void setTypeHardness(String typeHardness)
	{
		this.typeHardness = typeHardness;
	}

	public void setTypeResistance(String typeResistance)
	{
		this.typeResistance = typeResistance;
	}

	public void setTypeEntityTag(CompoundNBT typeEntityTag)
	{
		this.typeEntityTag = typeEntityTag;
	}

	public void setTypeNameEntity(String typeNameEntity)
	{
		this.typeNameEntity = typeNameEntity;
	}

	public void setTypeHeight(String typeHeight)
	{
		this.typeHeight = typeHeight;
	}

	public void setTypeWidth(String typeWidth)
	{
		this.typeWidth = typeWidth;
	}

	public void setTypeHealth(String typeHealth)
	{
		this.typeHealth = typeHealth;
	}

	public void setConditionGamestage(String conditionGamestage)
	{
		this.conditionGamestage = conditionGamestage;
	}

	public void setConditionAchievement(String conditionAchievement)
	{
		this.conditionAchievement = conditionAchievement;
	}

	public void setConditionXp(String conditionXp)
	{
		this.conditionXp = conditionXp;
	}

	public void setConditionGamemode(String conditionGamemode)
	{
		this.conditionGamemode = conditionGamemode;
	}

	public void setConditionScoreboard(String conditionScoreboard)
	{
		this.conditionScoreboard = conditionScoreboard;
	}

	public void setConditionPosition(String conditionPosition)
	{
		this.conditionPosition = conditionPosition;
	}

	public void setRenderNameBlock(String renderNameBlock)
	{
		this.renderNameBlock = renderNameBlock;
	}

	public void setRenderNameEntity(String renderNameEntity)
	{
		this.renderNameEntity = renderNameEntity;
	}

	public void setRenderNBT(CompoundNBT renderNBT)
	{
		this.renderNBT = renderNBT;
	}

	public void setRenderTranslation(String renderTranslation)
	{
		this.renderTranslation = renderTranslation;
	}

	public void setRenderRotation(String renderRotation)
	{
		this.renderRotation = renderRotation;
	}

	public void setRenderscaled(String renderscaled)
	{
		this.renderscaled = renderscaled;
	}

	public String getCommandPlace()
	{
		return commandPlace;
	}

	public void setCommandPlace(String commandPlace)
	{
		this.commandPlace = commandPlace;
	}

	public void serialize(PacketBuffer buf)
	{
		// BLOCKS
		buf.writeCompoundTag(typeBlockTag);
		buf.writeString(typeNameBlock);
		buf.writeString(typeMaterial);
		buf.writeString(typeHardness);
		buf.writeString(typeResistance);

		// ENTITIES
		buf.writeCompoundTag(typeEntityTag);
		buf.writeString(typeNameEntity);
		buf.writeString(typeHeight);
		buf.writeString(typeWidth);
		buf.writeString(typeHealth);

		// CONDITIONS
		buf.writeString(conditionGamestage);
		buf.writeString(conditionAchievement);
		buf.writeString(conditionXp);
		buf.writeString(conditionGamemode);
		buf.writeString(conditionScoreboard);
		buf.writeString(conditionPosition);
		buf.writeString(conditionEffects);

		// RENDER
		buf.writeString(renderNameBlock);
		buf.writeString(renderNameEntity);
		buf.writeCompoundTag(renderNBT);
		buf.writeString(renderTranslation);
		buf.writeString(renderRotation);
		buf.writeString(renderscaled);
		buf.writeString(renderRotationLeftArm);
		buf.writeString(renderRotationRightArm);
		buf.writeBoolean(renderLeftArm);
		buf.writeBoolean(renderRightArm);

		// EFFECTS
		buf.writeString(commandInit);
		buf.writeString(commandLoop);
		buf.writeString(commandPlace);

		buf.writeBoolean(isBlock);
		buf.writeBoolean(isEntity);
		buf.writeString(resourceLocation);
	}

	public static CarryOnOverride deserialize(PacketBuffer buf)
	{
		CarryOnOverride override = new CarryOnOverride("");
		override.typeBlockTag = buf.readCompoundTag();
		override.typeNameBlock = buf.readString();
		override.typeMaterial = buf.readString();
		override.typeHardness = buf.readString();
		override.typeResistance = buf.readString();

		// ENTITIES
		override.typeEntityTag = buf.readCompoundTag();
		override.typeNameEntity = buf.readString();
		override.typeHeight = buf.readString();
		override.typeWidth = buf.readString();
		override.typeHealth = buf.readString();

		// CONDITIONS
		override.conditionGamestage = buf.readString();
		override.conditionAchievement = buf.readString();
		override.conditionXp = buf.readString();
		override.conditionGamemode = buf.readString();
		override.conditionScoreboard = buf.readString();
		override.conditionPosition = buf.readString();
		override.conditionEffects = buf.readString();

		// RENDER
		override.renderNameBlock = buf.readString();
		override.renderNameEntity = buf.readString();
		override.renderNBT = buf.readCompoundTag();
		override.renderTranslation = buf.readString();
		override.renderRotation = buf.readString();
		override.renderscaled = buf.readString();
		override.renderRotationLeftArm = buf.readString();
		override.renderRotationRightArm = buf.readString();
		override.renderLeftArm = buf.readBoolean();
		override.renderRightArm = buf.readBoolean();

		// EFFECTS
		override.commandInit = buf.readString();
		override.commandLoop = buf.readString();
		override.commandPlace = buf.readString();

		override.isBlock = buf.readBoolean();
		override.isEntity = buf.readBoolean();
		override.resourceLocation = buf.readString();

		return override;
	}

}
