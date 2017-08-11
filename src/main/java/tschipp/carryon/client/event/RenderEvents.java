package tschipp.carryon.client.event;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemTile;

public class RenderEvents
{

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onScroll(MouseEvent event)
	{
		if (event.getDwheel() > 0 || event.getDwheel() < 0)
		{
			ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
			if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile)
			{
				if (ItemTile.hasTileData(stack))
					event.setCanceled(true);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event)
	{
		if (event.getGui() != null)
		{
			boolean inventory = event.getGui() instanceof GuiContainer;
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player != null)
			{
				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
				if (inventory && !stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
				{
					event.setCanceled(true);
					Minecraft.getMinecraft().currentScreen = null;
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void inputEvent(InputEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		Field field = KeyBinding.class.getDeclaredField("pressed");
		field.setAccessible(true);
		ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
		{
			if (settings.keyBindDrop.isPressed())
			{
				field.set(settings.keyBindDrop, false);
			}
			if (settings.keyBindSwapHands.isPressed())
			{
				field.set(settings.keyBindSwapHands, false);
			}
			for (KeyBinding keyBind : settings.keyBindsHotbar)
			{
				if (keyBind.isPressed())
				{
					field.set(keyBind, false);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{

		World world = Minecraft.getMinecraft().world;
		EntityPlayer player = Minecraft.getMinecraft().player;
		ItemStack stack = player.getHeldItemMainhand();
		int pass = event.getRenderPass();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
		{
			Block block = ItemTile.getBlock(stack);
			BlockPos pos = player.getPosition();
			stack = ItemTile.getItemStack(stack);

			
			int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;

			GlStateManager.pushMatrix();
			GlStateManager.scale(2.5, 2.5, 2.5);
			GlStateManager.translate(0, -0.6, -1);

			if (CarryOnConfig.settings.facePlayer ? !isChest(block) : isChest(block))
			{
				GlStateManager.rotate(180, 0, 1f, 0);
				GlStateManager.rotate(-8, 1f, 0, 0);
			}
			else
				GlStateManager.rotate(8, 1f, 0, 0);


			if (perspective == 0)
				Minecraft.getMinecraft().getRenderItem().renderItem(stack, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, world, player));
			
			GlStateManager.scale(1, 1, 1);
			GlStateManager.popMatrix();

			event.setCanceled(true);

		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPost(RenderPlayerEvent.Post event)
	{
		World world = Minecraft.getMinecraft().world;
		EntityPlayer player = Minecraft.getMinecraft().player;
		ItemStack stack = player.getHeldItemMainhand();

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
		{
			Block block = ItemTile.getBlock(stack);
			stack = ItemTile.getItemStack(stack);

			EntityItem entityItem = new EntityItem(Minecraft.getMinecraft().world, 0, 0, 0);
			entityItem.hoverStart = 0;

			entityItem.setEntityItemStack(stack);
			float rotation = -player.renderYawOffset;
			int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;

			GlStateManager.pushMatrix();
			GlStateManager.scale(2.2, 2.2, 2.2);

			if (CarryOnConfig.settings.facePlayer ? !isChest(block) : isChest(block))
			{
				GlStateManager.rotate(rotation, 0, 1.0f, 0);
				GlStateManager.translate(0, 0.1, 0.2);
			}
			else
			{
				GlStateManager.rotate(rotation + 180, 0, 1.0f, 0);
				GlStateManager.translate(0, 0.1, -0.2);
			}

			if (player.isSneaking())
				GlStateManager.translate(0, -0.08, 0);

			Minecraft.getMinecraft().getRenderManager().doRenderEntity(entityItem, event.getX(), event.getY(), event.getZ(), 0, 0, true);
			GlStateManager.scale(1, 1, 1);
			GlStateManager.popMatrix();

		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPre(RenderPlayerEvent.Pre event)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;
		ItemStack stack = player.getHeldItemMainhand();
		RenderPlayer renderer = event.getRenderer();

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
		{

		}
	}

	private boolean isChest(Block block)
	{
		return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
	}

}
