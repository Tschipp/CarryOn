package tschipp.carryon.common.capabilities.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.capabilities.IPosition;
import tschipp.carryon.common.capabilities.PositionProvider;
import tschipp.carryon.common.capabilities.TEPosition;

public class PositionCommonEvents
{

	@SubscribeEvent
	public void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof Player)
		{
			event.addCapability(new ResourceLocation(CarryOn.MODID, "position"), new PositionProvider());
		}

	}

	@SubscribeEvent
	public void onBlockRight(PlayerInteractEvent.RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		Level world = event.getWorld();
		Player player = event.getPlayer();

		if (event.isCanceled())
			return;

		if (player == null)
			return;

		if (player instanceof FakePlayer)
			return;

		BlockEntity te = world.getBlockEntity(pos);
		if (te != null)
		{
			if(player.getCapability(PositionProvider.POSITION_CAPABILITY).isPresent())
			{
				IPosition cap = player.getCapability(PositionProvider.POSITION_CAPABILITY).orElse(new TEPosition());
				cap.setBlockActivated(true);
				cap.setPos(pos);
			}
		}
	}

	

}
