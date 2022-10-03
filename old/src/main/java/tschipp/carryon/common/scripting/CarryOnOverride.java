package tschipp.carryon.common.scripting;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import tschipp.carryon.common.helper.InvalidConfigException;

public class CarryOnOverride
{
	// BLOCKS
	private CompoundTag typeBlockTag = new CompoundTag();
	private String typeNameBlock = "";
	private String typeMaterial = "";
	private String typeHardness = "";
	private String typeResistance = "";

	// ENTITIES
	private CompoundTag typeEntityTag;
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
	private CompoundTag renderNBT = new CompoundTag();
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
		this.resourceLocation = loc.toString();

		if (jsonElem != null && jsonElem.isJsonObject())
		{
			try
			{
				JsonObject json = jsonElem.getAsJsonObject();
				JsonObject object = (JsonObject) json.get("object");
				JsonObject conditions = (JsonObject) json.get("conditions");
				JsonObject render = (JsonObject) json.get("render");
				JsonObject effects = (JsonObject) json.get("effects");

				if (object != null && (conditions != null || render != null || effects != null))
				{
					JsonObject block = (JsonObject) object.get("block");
					JsonObject entity = (JsonObject) object.get("entity");

					if (block == null && entity == null || block != null && entity != null)
						errored = true;

					if (!errored)
					{
						if (block != null)
						{
							this.setBlock(true);
							JsonElement name = block.get("name");
							JsonElement material = block.get("material");
							JsonElement hardness = block.get("hardness");
							JsonElement resistance = block.get("resistance");
							JsonObject nbt = (JsonObject) block.get("nbt");

							if (name != null)
								this.setTypeNameBlock(name.getAsString());
							if (material != null)
								this.setTypeMaterial(material.getAsString());
							if (hardness != null)
								this.setTypeHardness(hardness.getAsString());
							if (resistance != null)
								this.setTypeResistance(resistance.getAsString());
							if (nbt != null)
								this.setTypeBlockTag(TagParser.parseTag(nbt.toString()));
						}
						else
						{
							this.setEntity(true);
							JsonElement name = entity.get("name");
							JsonElement health = entity.get("health");
							JsonElement height = entity.get("height");
							JsonElement width = entity.get("width");
							JsonObject nbt = (JsonObject) entity.get("nbt");

							if (name != null)
								this.setTypeNameEntity(name.getAsString());
							if (health != null)
								this.setTypeHealth(health.getAsString());
							if (height != null)
								this.setTypeHeight(height.getAsString());
							if (width != null)
								this.setTypeWidth(width.getAsString());
							if (nbt != null)
								this.setTypeEntityTag(TagParser.parseTag(nbt.toString()));
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
								this.setConditionGamestage(gamestage.getAsString());
							if (achievement != null)
								this.setConditionAchievement(achievement.getAsString());
							if (xp != null)
								this.setConditionXp(xp.getAsString());
							if (gamemode != null)
								this.setConditionGamemode(gamemode.getAsString());
							if (scoreboard != null)
								this.setConditionScoreboard(scoreboard.getAsString());
							if (position != null)
								this.setConditionPosition(position.getAsString());
							if (potionEffects != null)
								this.setConditionEffects(potionEffects.getAsString());
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
								this.setRenderNameBlock(name_block.getAsString());
							if (name_entity != null)
								this.setRenderNameEntity(name_entity.getAsString());
							if (translation != null)
								this.setRenderTranslation(translation.getAsString());
							if (rotation != null)
								this.setRenderRotation(rotation.getAsString());
							if (scaled != null)
								this.setRenderscaled(scaled.getAsString());
							if (nbt != null)
								this.setRenderNBT(TagParser.parseTag(nbt.toString()));
							if (rotationLeftArm != null)
								this.setRenderRotationLeftArm(rotationLeftArm.getAsString());
							if (rotationRightArm != null)
								this.setRenderRotationRightArm(rotationRightArm.getAsString());
							if (renderLeftArm != null)
								this.setRenderLeftArm(renderLeftArm.getAsBoolean());
							if (renderRightArm != null)
								this.setRenderRightArm(renderRightArm.getAsBoolean());
						}

						if (effects != null)
						{
							JsonElement commandInit = effects.get("commandPickup");
							JsonElement commandLoop = effects.get("commandLoop");
							JsonElement commandPlace = effects.get("commandPlace");

							if (commandInit != null)
								this.setCommandInit(commandInit.getAsString());
							if (commandLoop != null)
								this.setCommandLoop(commandLoop.getAsString());
							if (commandPlace != null)
								this.setCommandPlace(commandPlace.getAsString());
						}
					}
					else
						this.isInvalid = true;
				}
			}
			catch (Exception e)
			{
				this.isInvalid = true;
			}
		}
		else
			this.isInvalid = true;

		if (!this.isBlock && !this.isEntity)
			this.isInvalid = true;

		if (this.isInvalid)
			new InvalidConfigException("The script parsing for " + this.resourceLocation + " has failed! Please double check!").printException();
	}

	public String getCommandInit()
	{
		return this.commandInit;
	}

	public void setCommandInit(String commandInit)
	{
		this.commandInit = commandInit;
	}

	public String getCommandLoop()
	{
		return this.commandLoop;
	}

	public void setCommandLoop(String commandLoop)
	{
		this.commandLoop = commandLoop;
	}

	public String getConditionEffects()
	{
		return this.conditionEffects;
	}

	public void setConditionEffects(String conditionEffects)
	{
		this.conditionEffects = conditionEffects;
	}

	public String getRenderRotationLeftArm()
	{
		return this.renderRotationLeftArm;
	}

	public void setRenderRotationLeftArm(String renderRotationLeftArm)
	{
		this.renderRotationLeftArm = renderRotationLeftArm;
	}

	public String getRenderRotationRightArm()
	{
		return this.renderRotationRightArm;
	}

	public void setRenderRotationRightArm(String renderRotationRightArm)
	{
		this.renderRotationRightArm = renderRotationRightArm;
	}

	public boolean isRenderLeftArm()
	{
		return this.renderLeftArm;
	}

	public void setRenderLeftArm(boolean renderLeftArm)
	{
		this.renderLeftArm = renderLeftArm;
	}

	public boolean isRenderRightArm()
	{
		return this.renderRightArm;
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
		return prime * result + (this.resourceLocation == null ? 0 : this.resourceLocation.hashCode());
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
		if (obj == null || this.getClass() != obj.getClass())
			return false;
		CarryOnOverride other = (CarryOnOverride) obj;
		if (!Objects.equals(this.commandInit, other.commandInit) || !Objects.equals(this.commandLoop, other.commandLoop) || !Objects.equals(this.commandPlace, other.commandPlace) || !Objects.equals(this.conditionAchievement, other.conditionAchievement))
		{
			return false;
		}
		if (!Objects.equals(this.conditionEffects, other.conditionEffects) || !Objects.equals(this.conditionGamemode, other.conditionGamemode) || !Objects.equals(this.conditionGamestage, other.conditionGamestage) || !Objects.equals(this.conditionPosition, other.conditionPosition))
		{
			return false;
		}
		if (!Objects.equals(this.conditionScoreboard, other.conditionScoreboard))
		{
			return false;
		}
		if (!Objects.equals(this.conditionXp, other.conditionXp))
		{
			return false;
		}
		if (this.isBlock != other.isBlock)
			return false;
		if (this.isEntity != other.isEntity)
			return false;
		if (!Objects.equals(this.resourceLocation, other.resourceLocation))
		{
			return false;
		}
		if (this.renderLeftArm != other.renderLeftArm)
			return false;
		if (!Objects.equals(this.renderNBT, other.renderNBT))
		{
			return false;
		}
		if (!Objects.equals(this.renderNameBlock, other.renderNameBlock))
		{
			return false;
		}
		if (!Objects.equals(this.renderNameEntity, other.renderNameEntity))
		{
			return false;
		}
		if (this.renderRightArm != other.renderRightArm)
			return false;
		if (!Objects.equals(this.renderRotation, other.renderRotation))
		{
			return false;
		}
		if (!Objects.equals(this.renderRotationLeftArm, other.renderRotationLeftArm))
		{
			return false;
		}
		if (!Objects.equals(this.renderRotationRightArm, other.renderRotationRightArm))
		{
			return false;
		}
		if (!Objects.equals(this.renderscaled, other.renderscaled))
		{
			return false;
		}
		if (!Objects.equals(this.renderTranslation, other.renderTranslation))
		{
			return false;
		}
		if (!Objects.equals(this.typeBlockTag, other.typeBlockTag))
		{
			return false;
		}
		if (!Objects.equals(this.typeEntityTag, other.typeEntityTag))
		{
			return false;
		}
		if (!Objects.equals(this.typeHardness, other.typeHardness))
		{
			return false;
		}
		if (!Objects.equals(this.typeHealth, other.typeHealth))
		{
			return false;
		}
		if (!Objects.equals(this.typeHeight, other.typeHeight))
		{
			return false;
		}
		if (!Objects.equals(this.typeMaterial, other.typeMaterial))
		{
			return false;
		}
		if (!Objects.equals(this.typeNameBlock, other.typeNameBlock))
		{
			return false;
		}
		if (!Objects.equals(this.typeNameEntity, other.typeNameEntity))
		{
			return false;
		}
		if (!Objects.equals(this.typeResistance, other.typeResistance))
		{
			return false;
		}
		if (!Objects.equals(this.typeWidth, other.typeWidth))
		{
			return false;
		}
		return true;
	}

	public boolean isBlock()
	{
		return this.isBlock;
	}

	public void setBlock(boolean isBlock)
	{
		this.isBlock = isBlock;
	}

	public boolean isEntity()
	{
		return this.isEntity;
	}

	public void setEntity(boolean isEntity)
	{
		this.isEntity = isEntity;
	}

	public CompoundTag getTypeBlockTag()
	{
		return this.typeBlockTag;
	}

	public String getTypeNameBlock()
	{
		return this.typeNameBlock;
	}

	public String getTypeMaterial()
	{
		return this.typeMaterial;
	}

	public String getTypeHardness()
	{
		return this.typeHardness;
	}

	public String getTypeResistance()
	{
		return this.typeResistance;
	}

	public CompoundTag getTypeEntityTag()
	{
		return this.typeEntityTag;
	}

	public String getTypeNameEntity()
	{
		return this.typeNameEntity;
	}

	public String getTypeHeight()
	{
		return this.typeHeight;
	}

	public String getTypeWidth()
	{
		return this.typeWidth;
	}

	public String getTypeHealth()
	{
		return this.typeHealth;
	}

	public String getConditionGamestage()
	{
		return this.conditionGamestage;
	}

	public String getConditionAchievement()
	{
		return this.conditionAchievement;
	}

	public String getConditionXp()
	{
		return this.conditionXp;
	}

	public String getConditionGamemode()
	{
		return this.conditionGamemode;
	}

	public String getConditionScoreboard()
	{
		return this.conditionScoreboard;
	}

	public String getConditionPosition()
	{
		return this.conditionPosition;
	}

	public String getRenderNameBlock()
	{
		return this.renderNameBlock;
	}

	public String getRenderNameEntity()
	{
		return this.renderNameEntity;
	}

	public CompoundTag getRenderNBT()
	{
		return this.renderNBT;
	}

	public String getRenderTranslation()
	{
		return this.renderTranslation;
	}

	public String getRenderRotation()
	{
		return this.renderRotation;
	}

	public String getRenderScaled()
	{
		return this.renderscaled;
	}

	public void setTypeBlockTag(CompoundTag typeBlockTag)
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

	public void setTypeEntityTag(CompoundTag typeEntityTag)
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

	public void setRenderNBT(CompoundTag renderNBT)
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
		return this.commandPlace;
	}

	public void setCommandPlace(String commandPlace)
	{
		this.commandPlace = commandPlace;
	}

	public void serialize(FriendlyByteBuf buf)
	{
		// BLOCKS
		buf.writeNbt(this.typeBlockTag);
		buf.writeUtf(this.typeNameBlock);
		buf.writeUtf(this.typeMaterial);
		buf.writeUtf(this.typeHardness);
		buf.writeUtf(this.typeResistance);

		// ENTITIES
		buf.writeNbt(this.typeEntityTag);
		buf.writeUtf(this.typeNameEntity);
		buf.writeUtf(this.typeHeight);
		buf.writeUtf(this.typeWidth);
		buf.writeUtf(this.typeHealth);

		// CONDITIONS
		buf.writeUtf(this.conditionGamestage);
		buf.writeUtf(this.conditionAchievement);
		buf.writeUtf(this.conditionXp);
		buf.writeUtf(this.conditionGamemode);
		buf.writeUtf(this.conditionScoreboard);
		buf.writeUtf(this.conditionPosition);
		buf.writeUtf(this.conditionEffects);

		// RENDER
		buf.writeUtf(this.renderNameBlock);
		buf.writeUtf(this.renderNameEntity);
		buf.writeNbt(this.renderNBT);
		buf.writeUtf(this.renderTranslation);
		buf.writeUtf(this.renderRotation);
		buf.writeUtf(this.renderscaled);
		buf.writeUtf(this.renderRotationLeftArm);
		buf.writeUtf(this.renderRotationRightArm);
		buf.writeBoolean(this.renderLeftArm);
		buf.writeBoolean(this.renderRightArm);

		// EFFECTS
		buf.writeUtf(this.commandInit);
		buf.writeUtf(this.commandLoop);
		buf.writeUtf(this.commandPlace);

		buf.writeBoolean(this.isBlock);
		buf.writeBoolean(this.isEntity);
		buf.writeUtf(this.resourceLocation);
	}

	public static CarryOnOverride deserialize(FriendlyByteBuf buf)
	{
		CarryOnOverride override = new CarryOnOverride("");
		override.typeBlockTag = buf.readNbt();
		override.typeNameBlock = buf.readUtf();
		override.typeMaterial = buf.readUtf();
		override.typeHardness = buf.readUtf();
		override.typeResistance = buf.readUtf();

		// ENTITIES
		override.typeEntityTag = buf.readNbt();
		override.typeNameEntity = buf.readUtf();
		override.typeHeight = buf.readUtf();
		override.typeWidth = buf.readUtf();
		override.typeHealth = buf.readUtf();

		// CONDITIONS
		override.conditionGamestage = buf.readUtf();
		override.conditionAchievement = buf.readUtf();
		override.conditionXp = buf.readUtf();
		override.conditionGamemode = buf.readUtf();
		override.conditionScoreboard = buf.readUtf();
		override.conditionPosition = buf.readUtf();
		override.conditionEffects = buf.readUtf();

		// RENDER
		override.renderNameBlock = buf.readUtf();
		override.renderNameEntity = buf.readUtf();
		override.renderNBT = buf.readNbt();
		override.renderTranslation = buf.readUtf();
		override.renderRotation = buf.readUtf();
		override.renderscaled = buf.readUtf();
		override.renderRotationLeftArm = buf.readUtf();
		override.renderRotationRightArm = buf.readUtf();
		override.renderLeftArm = buf.readBoolean();
		override.renderRightArm = buf.readBoolean();

		// EFFECTS
		override.commandInit = buf.readUtf();
		override.commandLoop = buf.readUtf();
		override.commandPlace = buf.readUtf();

		override.isBlock = buf.readBoolean();
		override.isEntity = buf.readBoolean();
		override.resourceLocation = buf.readUtf();

		return override;
	}

}
