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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PlacementHandler;
import tschipp.carryon.common.command.CommandCarryOn;
import tschipp.carryon.config.ConfigLoader;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingOtherPlayerPacket;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingPacket;
import tschipp.carryon.networking.clientbound.ClientboundSyncScriptsPacket;
import tschipp.carryon.networking.serverbound.ServerboundCarryKeyPressedPacket;
import tschipp.carryon.platform.Services;
import tschipp.carryon.utils.SizeHelper;

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
			// Dumb Fix to sync Carry Data after a respawn with KeepInventory, because we can't sync in the first tick.
			if(player.tickCount == 1)
				CarryOnDataManager.setCarryData(player, carry);

	        if(carry.getActiveScript().isPresent())
	        {
	            String cmd = carry.getActiveScript().get().scriptEffects().commandLoop();
	            if(!cmd.isEmpty())
	                player.level().getServer().getCommands().performPrefixedCommand(player.level().getServer().createCommandSourceStack(), "/execute as " + player.getGameProfile().name() + " run " + cmd);
	        }

		    Inventory inv = player.getInventory();
			inv.setSelectedSlot(carry.getSelected());
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
		if (Constants.COMMON_CONFIG.settings.dropCarriedWhenHit && !player.level().isClientSide())
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


	public static int potionLevel(CarryOnData carry, Player player)
	{
		if(carry.isCarrying(CarryType.PLAYER))
			return 1;
		if(carry.isCarrying(CarryType.ENTITY))
		{
			Entity entity = carry.getEntity(player.level());
			int i = (int) (SizeHelper.getRelativeEntityArea(player, entity));
				if (i > 4)
					i = 4;
			if (!Constants.COMMON_CONFIG.settings.heavyEntities) // why not check this first?
				i = 1;
			return (int) (i * Constants.COMMON_CONFIG.settings.entitySlownessMultiplier);
		}
		if(carry.isCarrying(CarryType.BLOCK))
		{
			String nbt = carry.getNbt().toString();
			int i = nbt.length() / 500;

			if (i > 4)
				i = 4;

			if (!Constants.COMMON_CONFIG.settings.heavyTiles)
				i = 1;

			return (int) (i * Constants.COMMON_CONFIG.settings.blockSlownessMultiplier);
		}
		return 0;
	}
}
