/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.carryon;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PlacementHandler;
import tschipp.carryon.common.command.CommandCarryOn;
import tschipp.carryon.config.ConfigLoader;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingOtherPlayerPacket;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingPacket;
import tschipp.carryon.networking.clientbound.ClientboundSyncCarryDataPacket;
import tschipp.carryon.networking.clientbound.ClientboundSyncScriptsPacket;
import tschipp.carryon.networking.serverbound.ServerboundCarryKeyPressedPacket;
import tschipp.carryon.platform.Services;

public class CarryOnCommon
{
	public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder();

	public static HolderLookup.Provider createLookup() {
		RegistryAccess.Frozen registryaccess$frozen = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		HolderLookup.Provider holderlookup$provider = BUILDER.build(registryaccess$frozen);
		return holderlookup$provider;
	}

	public static void registerServerPackets(Object... args)
	{
		Services.PLATFORM.registerServerboundPacket(
				ServerboundCarryKeyPressedPacket.TYPE,
				ServerboundCarryKeyPressedPacket.class,
				ServerboundCarryKeyPressedPacket.CODEC,
				ServerboundCarryKeyPressedPacket::handle,
				args
		);
	}

	public static void registerClientPackets(Object... args)
	{
		Services.PLATFORM.registerClientboundPacket(
				ClientboundStartRidingPacket.TYPE,
				ClientboundStartRidingPacket.class,
				ClientboundStartRidingPacket.CODEC,
				ClientboundStartRidingPacket::handle,
				args
		);

		Services.PLATFORM.registerClientboundPacket(
				ClientboundSyncScriptsPacket.TYPE,
				ClientboundSyncScriptsPacket.class,
				ClientboundSyncScriptsPacket.CODEC,
				ClientboundSyncScriptsPacket::handle,
				args
		);

		Services.PLATFORM.registerClientboundPacket(
				ClientboundStartRidingOtherPlayerPacket.TYPE,
				ClientboundStartRidingOtherPlayerPacket.class,
				ClientboundStartRidingOtherPlayerPacket.CODEC,
				ClientboundStartRidingOtherPlayerPacket::handle,
				args
		);

		Services.PLATFORM.registerClientboundPacket(
				ClientboundSyncCarryDataPacket.TYPE,
				ClientboundSyncCarryDataPacket.class,
				ClientboundSyncCarryDataPacket.CODEC,
				ClientboundSyncCarryDataPacket::handle,
				args
		);
	}

	public static void registerConfig()
	{
		ConfigLoader.registerConfig(Constants.COMMON_CONFIG);
		ConfigLoader.registerConfig(Constants.CLIENT_CONFIG);
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		CommandCarryOn.register(dispatcher);
	}


	public static void onCarryTick(ServerPlayer player)
	{
	    CarryOnData carry = CarryOnDataManager.getCarryData(player);
	    if(carry.isCarrying())
	    {
			//Dirty Hack to sync carry data 1 tick after respawning
			if(player.tickCount == 1)
				CarryOnDataManager.setCarryData(player, carry);

	        if(carry.getActiveScript().isPresent())
	        {
	            String cmd = carry.getActiveScript().get().scriptEffects().commandLoop();
	            if(!cmd.isEmpty())
	                player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().getName() + " run " + cmd);
	        }

		    if (!Constants.COMMON_CONFIG.settings.slownessInCreative && player.isCreative())
			    return;

		    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1, potionLevel(carry, player.level()), false, false));

		    Inventory inv = player.getInventory();
			inv.selected = carry.getSelected();
	    }
	}

	/**
	 * Returns true if the block can be broken.
	 */
	public static boolean onTryBreakBlock(Player player)
	{
		if (player != null && !Constants.COMMON_CONFIG.settings.hitWhileCarrying)
		{
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if(carry.isCarrying())
				return false;
		}
		return true;
	}

	/**
	 * Returns true of the entity can be attacked
	 */
	public static boolean onAttackedByPlayer(Player player)
	{
		if (player != null && !Constants.COMMON_CONFIG.settings.hitWhileCarrying)
		{
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if(carry.isCarrying())
				return false;
		}
		return true;
	}

	public static void onPlayerAttacked(Player player)
	{
		if (Constants.COMMON_CONFIG.settings.dropCarriedWhenHit && !player.level().isClientSide)
		{
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if (carry.isCarrying())
			{
				PlacementHandler.placeCarried((ServerPlayer) player);
			}

		}
	}

	public static void onRiderDisconnected(Player rider)
	{
		if(rider.getVehicle() instanceof ServerPlayer vehicle) {
			CarryOnData data = CarryOnDataManager.getCarryData(vehicle);
			if(data.isCarrying(CarryType.PLAYER)) {
				PlacementHandler.placeCarried(vehicle);
			}
		}
	}


	public static int potionLevel(CarryOnData carry, Level level)
	{
		if(carry.isCarrying(CarryType.PLAYER))
			return 1;
		if(carry.isCarrying(CarryType.ENTITY))
		{
			Entity entity = carry.getEntity(level);
			int i = (int) (entity.getBbHeight() * entity.getBbWidth());
			if (i > 4)
				i = 4;
			if (!Constants.COMMON_CONFIG.settings.heavyEntities)
				i = 1;
			return (int) (i * Constants.COMMON_CONFIG.settings.entitySlownessMultiplier);
		}
	if(carry.isCarrying(CarryType.BLOCK))
	{
		try
		{
			CompoundTag nbtTag = carry.getNbt();
			if(nbtTag == null)
				return (int) Constants.COMMON_CONFIG.settings.blockSlownessMultiplier;
			
			String nbt = nbtTag.toString();
			int i = nbt.length() / 500;

			if (i > 4)
				i = 4;

			if (!Constants.COMMON_CONFIG.settings.heavyTiles)
				i = 1;

			return (int) (i * Constants.COMMON_CONFIG.settings.blockSlownessMultiplier);
		}
		catch (NullPointerException e)
		{
			// Handle case where NBT contains null values that cause sorting to fail
			// Return default slowness level to prevent crash
			Constants.LOG.warn("Failed to calculate potion level from NBT due to null values, using default", e);
			return (int) Constants.COMMON_CONFIG.settings.blockSlownessMultiplier;
		}
	}
		return 0;
	}
}
