package tschipp.carryon.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.InterModComms.IMCMessage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import tschipp.carryon.CarryOnCommon;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.*;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.scripting.ScriptReloadListener;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Constants.MOD_ID)
public class CommonEvents
{
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onBlockClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (event.isCanceled())
			return;

		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();

		if (level.isClientSide)
			return;

		boolean success = false;

		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if (!carry.isCarrying()) {
			if (PickupHandler.tryPickUpBlock((ServerPlayer) player, pos, level)) {
				success = true;
			}
		} else {
			if (carry.isCarrying(CarryType.BLOCK)) {
				if (PlacementHandler.tryPlaceBlock((ServerPlayer) player, pos, event.getFace(), (pos2, state) -> {
					BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos2);
					EntityPlaceEvent event1 = new EntityPlaceEvent(snapshot, level.getBlockState(pos), player);
					MinecraftForge.EVENT_BUS.post(event1);
					return !event1.isCanceled();
				})) {
					success = true;
				}
			} else {
				//TODO: Entity place perms
				if (PlacementHandler.tryPlaceEntity((ServerPlayer) player,pos, event.getFace(), null))
				{
					success = true;
				}
			}
		}

		if(success)
		{
			event.setUseBlock(Event.Result.DENY);
			event.setUseItem(Event.Result.DENY);
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityRightClick(PlayerInteractEvent.EntityInteract event)
	{
		if (event.isCanceled())
			return;

		Player player = event.getEntity();
		Level level = event.getLevel();
		Entity target = event.getTarget();

		if (level.isClientSide)
			return;

		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		if (!carry.isCarrying()) {
			if (PickupHandler.tryPickupEntity((ServerPlayer) player, target)) {
				event.setResult(Result.DENY);
				event.setCancellationResult(InteractionResult.SUCCESS);
				event.setCanceled(true);
				return;
			}
		}
		else if(carry.isCarrying(CarryType.ENTITY) || carry.isCarrying(CarryType.PLAYER))
		{
			PlacementHandler.tryStackEntity((ServerPlayer)player, target);
		}
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event)
	{
		CarryOnCommon.registerCommands(event.getDispatcher());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void serverLoad(FMLDedicatedServerSetupEvent event)
	{
		Stream<IMCMessage> messages = InterModComms.getMessages(Constants.MOD_ID);

		messages.forEach(msg -> {

			String method = msg.method();
			Object obj = msg.messageSupplier().get();

			if (!(obj instanceof String str))
				return;

			switch (method)
			{
				case "blacklistBlock":
					ListHandler.addForbiddenTiles(str);
					break;
				case "blacklistEntity":
					ListHandler.addForbiddenEntities(str);
					break;
				case "whitelistBlock":
					ListHandler.addAllowedTiles(str);
					break;
				case "whitelistEntity":
					ListHandler.addAllowedEntities(str);
					break;
				case "blacklistStacking":
					ListHandler.addForbiddenStacking(str);
					break;
				case "whitelistStacking":
					ListHandler.addAllowedStacking(str);
					break;
					//TODO
//				case "addModelOverride":
//					ModelOverridesHandler.parseOverride(str, 0);
//					break;
			}

		});

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
		if(player == null)
		{
			for(ServerPlayer p : event.getPlayerList().getPlayers())
				ScriptReloadListener.syncScriptsWithClient(p);
		}
		else
			ScriptReloadListener.syncScriptsWithClient(player);
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.END)
			for(ServerPlayer player : event.getServer().getPlayerList().getPlayers())
				PickupHandler.onCarryTick(player);
	}

}
