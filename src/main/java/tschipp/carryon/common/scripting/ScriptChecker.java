package tschipp.carryon.common.scripting;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.handler.ListHandler;
import tschipp.carryon.common.helper.ScriptParseHelper;

public class ScriptChecker
{
	@Nullable
	public static CarryOnOverride inspectBlock(BlockState state, Level level, BlockPos pos, @Nullable CompoundTag tag)
	{
		if (!Settings.useScripts.get())
			return null;

		Block block = state.getBlock();
		Material material = state.getMaterial();
		float hardness = state.getDestroySpeed(level, pos);
		@SuppressWarnings("deprecation")
		float resistance = block.getExplosionResistance();
		CompoundTag nbt = tag;

		boolean isAllowed = Settings.useWhitelistBlocks.get() ? ListHandler.isAllowed(block) : !ListHandler.isForbidden(block);

		if (isAllowed)
		{
			for (CarryOnOverride override : ScriptReader.OVERRIDES.values())
			{
				if (override.isBlock() && matchesAll(override, block, material, hardness, resistance, nbt))
					return override;
			}
		}

		return null;
	}

	@Nullable
	public static CarryOnOverride inspectEntity(Entity entity)
	{
		if (!Settings.useScripts.get())
			return null;

		String name = entity.getType().getRegistryName().toString();
		float height = entity.getBbHeight();
		float width = entity.getBbWidth();
		float health = entity instanceof LivingEntity ? ((LivingEntity) entity).getHealth() : 0.0f;
		CompoundTag tag = entity.serializeNBT();

		boolean isAllowed = Settings.useWhitelistEntities.get() ? ListHandler.isAllowed(entity) : !ListHandler.isForbidden(entity);

		if (isAllowed)
		{
			for (CarryOnOverride override : ScriptReader.OVERRIDES.values())
			{
				if (override.isEntity() && matchesAll(override, name, height, width, health, tag))
					return override;
			}
		}

		return null;
	}

	public static boolean matchesAll(CarryOnOverride override, String name, float height, float width, float health, CompoundTag tag)
	{
		boolean matchname = override.getTypeNameEntity().isEmpty() ? true : name.equals(override.getTypeNameEntity());
		boolean matchheight = ScriptParseHelper.matches(height, override.getTypeHeight());
		boolean matchwidth = ScriptParseHelper.matches(width, override.getTypeWidth());
		boolean matchhealth = ScriptParseHelper.matches(health, override.getTypeHealth());
		boolean matchnbt = ScriptParseHelper.matches(tag, override.getTypeEntityTag());

		return matchname && matchheight && matchwidth && matchhealth && matchnbt;
	}

	public static boolean matchesAll(CarryOnOverride override, Block block, Material material, float hardness, float resistance, CompoundTag nbt)
	{
		boolean matchnbt = ScriptParseHelper.matches(nbt, override.getTypeBlockTag());
		boolean matchblock = ScriptParseHelper.matches(block, override.getTypeNameBlock());
		boolean matchmaterial = ScriptParseHelper.matches(material, override.getTypeMaterial());
		boolean matchhardness = ScriptParseHelper.matches(hardness, override.getTypeHardness());
		boolean matchresistance = ScriptParseHelper.matches(resistance, override.getTypeResistance());

		return matchnbt && matchblock && matchmaterial && matchhardness && matchresistance;
	}

	public static boolean fulfillsConditions(CarryOnOverride override, Player player)
	{
		ServerAdvancementManager manager = ((ServerPlayer) player).server.getAdvancements();
		Advancement adv = manager.getAdvancement(new ResourceLocation(override.getConditionAchievement().isEmpty() ? "" : override.getConditionAchievement()));

		boolean achievement = adv == null ? true : ((ServerPlayer) player).getAdvancements().getOrStartProgress(adv).isDone();
		boolean gamemode = ScriptParseHelper.matches(((ServerPlayer) player).gameMode.getGameModeForPlayer().getId(), override.getConditionGamemode());
		boolean gamestage = true;
		if (ModList.get().isLoaded("gamestages") && !override.getConditionGamestage().isEmpty())
		{
			try
			{
				Class<?> gameStageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
				Class<?> iStageData = Class.forName("net.darkhax.gamestages.data.IStageData");

				Method getPlayerData = ObfuscationReflectionHelper.findMethod(gameStageHelper, "getPlayerData", Player.class);
				Method hasStage = ObfuscationReflectionHelper.findMethod(iStageData, "hasStage", String.class);

				Object stageData = getPlayerData.invoke(null, player);
				String condition = override.getConditionGamestage();
				gamestage = (boolean) hasStage.invoke(stageData, condition);
			}
			catch (Exception e)
			{
				try
				{
					Class<?> playerDataHandler = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler");
					Class<?> iStageData = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler$IStageData");

					Method getStageData = ObfuscationReflectionHelper.findMethod(playerDataHandler, "getStageData", Player.class);
					Method hasUnlockedStage = ObfuscationReflectionHelper.findMethod(iStageData, "hasUnlockedStage", String.class);

					Object stageData = getStageData.invoke(null, player);
					String condition = override.getConditionGamestage();
					gamestage = (boolean) hasUnlockedStage.invoke(stageData, condition);

				}
				catch (Exception ex)
				{
				}
			}

		}

		boolean position = ScriptParseHelper.matches(player.blockPosition(), override.getConditionPosition());
		boolean xp = ScriptParseHelper.matches(player.experienceLevel, override.getConditionXp());
		boolean scoreboard = ScriptParseHelper.matchesScore(player, override.getConditionScoreboard());
		boolean effects = ScriptParseHelper.hasEffects(player, override.getConditionEffects());

		return achievement && gamemode && gamestage && position && xp && scoreboard && effects;
	}

	@Nullable
	public static CarryOnOverride getOverride(Player player)
	{
		CompoundTag tag = player.getPersistentData();

		if (tag != null && tag.contains("overrideKey"))
		{
			int key = tag.getInt("overrideKey");

			return ScriptReader.OVERRIDES.get(key);
		}

		return null;
	}

	public static void setCarryOnOverride(Player player, int i)
	{
		CompoundTag tag = player.getPersistentData();

		if (tag != null)
			tag.putInt("overrideKey", i);
	}

}
