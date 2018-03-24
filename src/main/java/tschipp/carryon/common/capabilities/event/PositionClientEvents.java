package tschipp.carryon.common.capabilities.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.carryon.common.capabilities.IPosition;
import tschipp.carryon.common.capabilities.PositionProvider;

public class PositionClientEvents
{

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGui(GuiScreenEvent.DrawScreenEvent event)
	{
		if (event.getGui() != null)
		{
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			boolean inventory = event.getGui() instanceof GuiContainer;
			
			if (player != null && inventory)
			{
				if(player.hasCapability(PositionProvider.POSITION_CAPABILITY, null))
				{
					IPosition cap = player.getCapability(PositionProvider.POSITION_CAPABILITY, null);
					if(cap.isBlockActivated())
					{
						World world = player.worldObj;
						BlockPos pos = cap.getPos();
						if(world != null)
						{
							TileEntity te = world.getTileEntity(pos);
							if(te == null)
							{
//								player.openContainer = null;
								Minecraft.getMinecraft().currentScreen = null;
								Minecraft.getMinecraft().setIngameFocus();
								cap.setBlockActivated(false);
								cap.setPos(new BlockPos(0,0,0));
							}
						}
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiClose(PlayerContainerEvent.Close event)
	{
		EntityPlayer player = event.getEntityPlayer();
		if(player.hasCapability(PositionProvider.POSITION_CAPABILITY, null))
		{
			IPosition cap = player.getCapability(PositionProvider.POSITION_CAPABILITY, null);
			cap.setBlockActivated(false);
			cap.setPos(new BlockPos(0,0,0));
		}
	}
	
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if (event.side == Side.CLIENT)
		{
			EntityPlayer player = event.player;
			if (player.hasCapability(PositionProvider.POSITION_CAPABILITY, null))
			{
				IPosition cap = player.getCapability(PositionProvider.POSITION_CAPABILITY, null);
				if (cap.isBlockActivated() && Minecraft.getMinecraft().currentScreen == null)
				{
					cap.setBlockActivated(false);
					cap.setPos(new BlockPos(0, 0, 0));
				}
			}
		}
	}
	
	
}
