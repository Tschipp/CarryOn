package tschipp.carryon.common.capabilities.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
		if (event.getObject() instanceof PlayerEntity)
		{
			event.addCapability(new ResourceLocation(CarryOn.MODID, "position"), new PositionProvider());
		}

	}

	@SubscribeEvent
	public void onBlockRight(PlayerInteractEvent.RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		PlayerEntity player = event.getEntityPlayer();

		if (event.isCanceled())
			return;

		if (player == null)
			return;

		if (player instanceof FakePlayer)
			return;

		TileEntity te = world.getTileEntity(pos);
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
