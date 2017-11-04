package tschipp.carryon.common.scripting;

import javax.annotation.Nullable;

import net.darkhax.gamestages.capabilities.PlayerDataHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.helper.ScriptParseHelper;

public class ScriptChecker
{
	@Nullable
	public static CarryOnOverride inspectBlock(IBlockState state, World world, BlockPos pos, @Nullable NBTTagCompound tag)
	{
		if (!CarryOnConfig.settings.useScripts)
			return null;

		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		Material material = state.getMaterial();
		float hardness = state.getBlockHardness(world, pos);
		float resistance = block.getExplosionResistance(null);
		NBTTagCompound nbt = tag;

		for (CarryOnOverride override : ScriptReader.OVERRIDES.values())
		{
			if (override.isBlock())
			{
				if (matchesAll(override, block, meta, material, hardness, resistance, nbt))
					return override;
			}
		}

		return null;
	}

	@Nullable
	public static CarryOnOverride inspectEntity(Entity entity)
	{
		if (!CarryOnConfig.settings.useScripts)
			return null;

		String name = EntityList.getKey(entity).toString();
		float height = entity.height;
		float width = entity.width;
		float health = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).getHealth() : 0.0f;
		NBTTagCompound tag = new NBTTagCompound();
		entity.writeToNBT(tag);

		for (CarryOnOverride override : ScriptReader.OVERRIDES.values())
		{
			if (override.isEntity())
			{
				if (matchesAll(override, name, height, width, health, tag))
					return override;
			}
		}

		return null;
	}

	public static boolean matchesAll(CarryOnOverride override, String name, float height, float width, float health, NBTTagCompound tag)
	{
		boolean matchname = override.getTypeNameEntity() == null ? true : name.equals(override.getTypeNameEntity());
		boolean matchheight = ScriptParseHelper.matches(height, override.getTypeHeight());
		boolean matchwidth = ScriptParseHelper.matches(width, override.getTypeWidth());
		boolean matchhealth = ScriptParseHelper.matches(health, override.getTypeHealth());
		boolean matchnbt = ScriptParseHelper.matches(tag, override.getTypeEntityTag());

		return (matchname && matchheight && matchwidth && matchhealth && matchnbt);
	}

	public static boolean matchesAll(CarryOnOverride override, Block block, int meta, Material material, float hardness, float resistance, NBTTagCompound nbt)
	{
		boolean matchnbt = ScriptParseHelper.matches(nbt, override.getTypeBlockTag());
		boolean matchblock = ScriptParseHelper.matches(block, override.getTypeNameBlock());
		boolean matchmeta = ScriptParseHelper.matches(meta, override.getTypeMeta());
		boolean matchmaterial = ScriptParseHelper.matches(material, override.getTypeMaterial());
		boolean matchhardness = ScriptParseHelper.matches(hardness, override.getTypeHardness());
		boolean matchresistance = ScriptParseHelper.matches(resistance, override.getTypeResistance());

		return (matchnbt && matchblock && matchmeta && matchmaterial && matchhardness && matchresistance);
	}

	public static boolean fulfillsConditions(CarryOnOverride override, EntityPlayer player)
	{
		AdvancementManager manager = ((WorldServer)((EntityPlayerMP)player).world).getAdvancementManager();
		Advancement adv = manager.getAdvancement(new ResourceLocation((override.getConditionAchievement()) == null ? "" : override.getConditionAchievement()));
		
		boolean achievement = adv == null ? true : ((EntityPlayerMP)player).getAdvancements().getProgress(adv).isDone();
		boolean gamemode = ScriptParseHelper.matches(((EntityPlayerMP) player).interactionManager.getGameType().getID(), override.getConditionGamemode());
		boolean gamestage = Loader.isModLoaded("gamestages") ? (override.getConditionGamestage() != null ? PlayerDataHandler.getStageData(player).hasUnlockedStage(override.getConditionGamestage()) : true) : true;
		boolean position = ScriptParseHelper.matches(player.getPosition(), override.getConditionPosition());
		boolean xp = ScriptParseHelper.matches(player.experienceLevel, override.getConditionXp());
		boolean scoreboard = ScriptParseHelper.matchesScore(player, override.getConditionScoreboard());
		boolean effects = ScriptParseHelper.hasEffects(player, override.getConditionEffects());
		
		return (achievement && gamemode && gamestage && position && xp && scoreboard && effects);
	}

	@Nullable
	public static CarryOnOverride getOverride(EntityPlayer player)
	{
		NBTTagCompound tag = player.getEntityData();

		if (tag != null && tag.hasKey("overrideKey"))
		{
			int key = tag.getInteger("overrideKey");
			
			return ScriptReader.OVERRIDES.get(key);
		}

		return null;
	}
	
	public static void setCarryOnOverride(EntityPlayer player, int i)
	{
		NBTTagCompound tag = player.getEntityData();

		if (tag != null)
			tag.setInteger("overrideKey", i);
	}
	
}
