package tschipp.carryon.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PickupHandler;
import tschipp.carryon.common.carry.PlacementHandler;

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
		else if(carry.isCarrying(CarryType.ENTITY))
		{
			//TODO: Stacking
		}
	}

}
