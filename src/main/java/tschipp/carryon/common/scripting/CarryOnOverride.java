package tschipp.carryon.common.scripting;

import net.minecraft.nbt.NBTTagCompound;

public class CarryOnOverride
{
	//BLOCKS
	private NBTTagCompound typeBlockTag;
	private String typeMeta;
	private String typeNameBlock;
	private String typeMaterial;
	private String typeHardness;
	private String typeResistance;

	//ENTITIES
	private NBTTagCompound typeEntityTag;
	private String typeNameEntity;
	private String typeHeight;
	private String typeWidth;
	private String typeHealth;
	
	//CONDITIONS
	private String conditionGamestage;
	private String conditionAchievement;
	private String conditionXp;
	private String conditionGamemode;
	private String conditionScoreboard;
	private String conditionPosition;

	//RENDER
	private String renderNameBlock;
	private String renderNameEntity;
	private int renderMeta;
	private NBTTagCompound renderNBT;
	private String renderTranslation;
	private String renderRotation;
	private String renderScale;
	
	private boolean isBlock;
	private boolean isEntity;

	
	
	
	
	
	
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
	public NBTTagCompound getTypeBlockTag()
	{
		return typeBlockTag;
	}
	public String getTypeMeta()
	{
		return typeMeta;
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
	public NBTTagCompound getTypeEntityTag()
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
	public int getRenderMeta()
	{
		return renderMeta;
	}
	public NBTTagCompound getRenderNBT()
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
	public String getRenderScale()
	{
		return renderScale;
	}
	public void setTypeBlockTag(NBTTagCompound typeBlockTag)
	{
		this.typeBlockTag = typeBlockTag;
	}
	public void setTypeMeta(String typeMeta)
	{
		this.typeMeta = typeMeta;
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
	public void setTypeEntityTag(NBTTagCompound typeEntityTag)
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
	public void setRenderMeta(int renderMeta)
	{
		this.renderMeta = renderMeta;
	}
	public void setRenderNBT(NBTTagCompound renderNBT)
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
	public void setRenderScale(String renderScale)
	{
		this.renderScale = renderScale;
	}

	
	
}
