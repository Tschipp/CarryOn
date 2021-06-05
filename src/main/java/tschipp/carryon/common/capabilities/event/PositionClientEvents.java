package tschipp.carryon.common.capabilities.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import tschipp.carryon.common.capabilities.IPosition;
import tschipp.carryon.common.capabilities.PositionProvider;
import tschipp.carryon.common.capabilities.TEPosition;

public class PositionClientEvents
{

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onGui(GuiScreenEvent.DrawScreenEvent event)
	{
		if (event.getGui() != null)
		{
			PlayerEntity player = Minecraft.getInstance().player;
			boolean inventory = event.getGui() instanceof ContainerScreen;
			
			if (player != null && inventory)
			{
				if(player.getCapability(PositionProvider.POSITION_CAPABILITY).isPresent())
				{
					IPosition cap = player.getCapability(PositionProvider.POSITION_CAPABILITY).orElse(new TEPosition());
					if(cap.isBlockActivated())
					{
						World world = player.level;
						BlockPos pos = cap.getPos();
						if(world != null)
						{
							TileEntity te = world.getBlockEntity(pos);
							if(te == null)
							{
//								player.openContainer = null;
								Minecraft.getInstance().screen = null;
//								Minecraft.getInstance().fo;
								cap.setBlockActivated(false);
								cap.setPos(new BlockPos(0,0,0));
							}
						}
					}
				}
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onGuiClose(PlayerContainerEvent.Close event)
	{
		PlayerEntity player = event.getPlayer();
		if(player.getCapability(PositionProvider.POSITION_CAPABILITY).isPresent())
		{
			IPosition cap = player.getCapability(PositionProvider.POSITION_CAPABILITY).orElse(new TEPosition());
			cap.setBlockActivated(false);
			cap.setPos(new BlockPos(0,0,0));
		}
	}
	
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if (event.side == LogicalSide.CLIENT)
		{
			PlayerEntity player = event.player;
			if(player.getCapability(PositionProvider.POSITION_CAPABILITY).isPresent())
			{
				IPosition cap = player.getCapability(PositionProvider.POSITION_CAPABILITY).orElse(new TEPosition());
				if (cap.isBlockActivated() && Minecraft.getInstance().screen == null)
				{
					cap.setBlockActivated(false);
					cap.setPos(new BlockPos(0, 0, 0));
				}
			}
		}
	}
	
	
}
