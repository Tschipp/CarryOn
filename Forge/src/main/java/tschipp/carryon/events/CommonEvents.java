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

package tschipp.carryon.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Result;
import net.minecraftforge.event.*;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tschipp.carryon.CarryOnCommon;
import tschipp.carryon.Constants;
import tschipp.carryon.carry.CarryOnDataCapabilityProvider;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PickupHandler;
import tschipp.carryon.common.carry.PlacementHandler;
import tschipp.carryon.common.scripting.ScriptReloadListener;
import tschipp.carryon.config.ConfigLoader;
import tschipp.carryon.networking.ClientboundSyncCarryDataPacket;
import tschipp.carryon.platform.Services;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Constants.MOD_ID)
public class CommonEvents
{
	@SubscribeEvent(priority = Priority.HIGH)
	public static boolean onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();

		if (level.isClientSide())
			return false;

		boolean success = false;

		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if (!carry.isCarrying()) {
			if (PickupHandler.tryPickUpBlock((ServerPlayer) player, pos, level, (pState, pPos) -> {
				BlockEvent.BreakEvent breakEvent = new BreakEvent(level, pPos, pState, player, Result.DEFAULT);
				return !BreakEvent.BUS.post(breakEvent);
			})) {
				success = true;
			}
		} else {
			if (carry.isCarrying(CarryType.BLOCK)) {
				PlacementHandler.tryPlaceBlock((ServerPlayer) player, pos, event.getFace(), (pos2, state) -> {
					BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos2);
					EntityPlaceEvent event1 = new EntityPlaceEvent(snapshot, level.getBlockState(pos), player);
					return !EntityPlaceEvent.BUS.post(event1);
				});
			} else {
				PlacementHandler.tryPlaceEntity((ServerPlayer) player, pos, event.getFace(), (pPos, toPlace) -> {
					if (toPlace instanceof Mob mob) {
						FinalizeSpawn checkSpawn = new FinalizeSpawn(mob, (ServerLevelAccessor) level, pPos.x, pPos.y, pPos.z, ((ServerLevelAccessor) level).getCurrentDifficultyAt(new BlockPos((int) pPos.x, (int) pPos.y, (int) pPos.z)), EntitySpawnReason.EVENT, null, null, null);
						return !FinalizeSpawn.BUS.post(checkSpawn);
					}
					return true;
				});
			}
			success = true;
		}

		if (success) {
			event.setUseBlock(Result.DENY);
			event.setUseItem(Result.DENY);
			event.setCancellationResult(InteractionResult.SUCCESS);
			return true;
		}
		return false;
	}

	// Forge 65.0.0: EntityInteract was removed from PlayerInteractEvent (sealed class).
	// EntityInteractSpecific is the replacement — fires on right-click with a known hit vector.
	@SubscribeEvent(priority = Priority.HIGH)
	public static boolean onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific event)
	{
		Player player = event.getEntity();
		Level level = event.getLevel();
		Entity target = event.getTarget();

		if (level.isClientSide())
			return false;

		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if (!carry.isCarrying()) {
			if (PickupHandler.tryPickupEntity((ServerPlayer) player, target, (toPickup) -> {
				EntityPickupEvent pickupEvent = new EntityPickupEvent((ServerPlayer) player, toPickup);
				return !EntityPickupEvent.BUS.post(pickupEvent);
			})) {
				event.setCancellationResult(InteractionResult.SUCCESS);
				return true;
			}
		} else if (carry.isCarrying(CarryType.ENTITY) || carry.isCarrying(CarryType.PLAYER)) {
			PlacementHandler.tryStackEntity((ServerPlayer) player, target);
		}

		return false;
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event)
	{
		CarryOnCommon.registerCommands(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onDatapackRegister(AddReloadListenerEvent event)
	{
		event.addListener(new ScriptReloadListener());
	}

	@SubscribeEvent
	public static void onDatapackSync(OnDatapackSyncEvent event)
	{
		ServerPlayer player = event.getPlayer();
		if (player == null) {
			for (ServerPlayer p : event.getPlayerList().getPlayers())
				ScriptReloadListener.syncScriptsWithClient(p);
		} else
			ScriptReloadListener.syncScriptsWithClient(player);
	}

	@SubscribeEvent
	public static void onTagsUpdate(TagsUpdatedEvent event)
	{
		ConfigLoader.onConfigLoaded(event.getRegistryAccess());
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event)
	{
		for (ServerPlayer player : event.server().getPlayerList().getPlayers())
			CarryOnCommon.onCarryTick(player);
	}

	@SubscribeEvent
	public static void onClone(Clone event)
	{
		if (!event.getOriginal().level().isClientSide()) {
			Player newPlayer = event.getEntity();
			Player oldPlayer = event.getOriginal();
			oldPlayer.reviveCaps();

			PlacementHandler.placeCarriedOnDeath((ServerPlayer) oldPlayer, (ServerPlayer) newPlayer, event.isWasDeath());

			oldPlayer.invalidateCaps();

		}
	}

	@SubscribeEvent
	public static void harvestSpeed(BreakSpeed event)
	{
		if (!CarryOnCommon.onTryBreakBlock(event.getEntity()))
			event.setNewSpeed(0);
	}

	@SubscribeEvent
	public static boolean attackEntity(AttackEntityEvent event)
	{
        return !CarryOnCommon.onAttackedByPlayer(event.getEntity());
    }

	@SubscribeEvent
	public static boolean onBreakBlock(BreakEvent event)
	{
        return !CarryOnCommon.onTryBreakBlock(event.getPlayer());
    }

	@SubscribeEvent
	public static void playerAttack(LivingAttackEvent event)
	{
		if(event.getEntity() instanceof Player player)
			CarryOnCommon.onPlayerAttacked(player);
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if(event.getEntity() instanceof ServerPlayer player)
			CarryOnCommon.onRiderDisconnected(player);
	}

	@SubscribeEvent
	public static void onAttachCapabilities(AttachCapabilitiesEvent.Entities event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "carry_on_data"), new CarryOnDataCapabilityProvider());
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if(event.getEntity() instanceof ServerPlayer sp && event.getTarget() instanceof ServerPlayer target) {
			Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_SYNC_CARRY_ON_DATA, new ClientboundSyncCarryDataPacket(sp.getId(), CarryOnDataManager.getCarryData(sp)), target);
		}
	}

	@SubscribeEvent
	public static void onJoinWorld(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ServerPlayer sp) {
			Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_SYNC_CARRY_ON_DATA, new ClientboundSyncCarryDataPacket(sp.getId(), CarryOnDataManager.getCarryData(sp)), sp);
		}
	}

	@SubscribeEvent
	public static void onPlayerDie(LivingDeathEvent event) {
		if(event.getEntity() instanceof ServerPlayer sp) {
			CarryOnCommon.onRiderDisconnected(sp);
		}
	}

}
