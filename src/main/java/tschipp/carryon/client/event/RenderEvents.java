package tschipp.carryon.client.event;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemTile;

public class RenderEvents
{

	/*
	 * Prevents the Player from scrolling
	 */
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

	/*
	 * Prevents the Player from opening Guis
	 */
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

	/*
	 * Prevents the Player from switching Slots
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void inputEvent(InputEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		Field field = KeyBinding.class.getDeclaredFields()[7];
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

	/*
	 * Renders the Block in First Person
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{
		World world = Minecraft.getMinecraft().world;
		EntityPlayer player = Minecraft.getMinecraft().player;
		AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
		ItemStack stack = player.getHeldItemMainhand();
		int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
		{
			Block block = ItemTile.getBlock(stack);
			BlockPos pos = player.getPosition();
			NBTTagCompound tag = ItemTile.getTileData(stack);
			IBlockState state = ItemTile.getBlockState(stack);
			ItemStack tileStack = ItemTile.getItemStack(stack);

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
				Minecraft.getMinecraft().getRenderItem().renderItem(tileStack.isEmpty() ? stack : tileStack, ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag) : Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(tileStack, world, player));

			GlStateManager.scale(1, 1, 1);
			GlStateManager.popMatrix();

			if (perspective == 0)
				event.setCanceled(true);
		}
		else
		{
			event.setCanceled(false);
			Minecraft mc = Minecraft.getMinecraft();
			RenderManager manager = mc.getRenderManager();
			RenderPlayer renderPlayer = manager.getSkinMap().get(aplayer.getSkinType());
			ModelPlayer modelPlayer = renderPlayer.getMainModel();
			modelPlayer.bipedLeftArm.isHidden = false;
			modelPlayer.bipedRightArm.isHidden = false;
		}
	}

	/*
	 * Renders the Block in Third Person
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPost(RenderPlayerEvent.Post event)
	{
		World world = Minecraft.getMinecraft().world;
		EntityPlayer player = event.getEntityPlayer();
		ModelPlayer modelPlayer = event.getRenderer().getMainModel();
		EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
		{
			Block block = ItemTile.getBlock(stack);
			IBlockState state = ItemTile.getBlockState(stack);
			NBTTagCompound tag = ItemTile.getTileData(stack);

			ItemStack tileItem = ItemTile.getItemStack(stack);

			EntityItem entityItem = new EntityItem(Minecraft.getMinecraft().world, 0, 0, 0);
			entityItem.hoverStart = 0;

			entityItem.setEntityItemStack(tileItem);
			float rotation = -player.renderYawOffset;
			int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;

			double xOffset = (double) player.posX - (double) clientPlayer.posX;
			double yOffset = (double) player.posY - (double) clientPlayer.posY;
			double zOffset = (double) player.posZ - (double) clientPlayer.posZ;

			GlStateManager.pushMatrix();
			GlStateManager.translate(xOffset, yOffset, zOffset);
			GlStateManager.scale(0.6, 0.6, 0.6);

			if (CarryOnConfig.settings.facePlayer ? !isChest(block) : isChest(block))
			{
				GlStateManager.rotate(rotation, 0, 1.0f, 0);
				GlStateManager.translate(0, 1.6, 0.65);
			}
			else
			{
				GlStateManager.rotate(rotation + 180, 0, 1.0f, 0);
				GlStateManager.translate(0, 1.6, -0.65);
			}

			if (player.isSneaking())
				GlStateManager.translate(0, -0.3, 0);

			IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag) : Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(tileItem, world, player);
			Minecraft.getMinecraft().getRenderItem().renderItem(tileItem.isEmpty() ? stack : tileItem, model);

			GlStateManager.scale(1, 1, 1);

			GlStateManager.popMatrix();
		}
		else
		{
			modelPlayer.bipedLeftArm.isHidden = false;
			modelPlayer.bipedRightArm.isHidden = false;
		}

	}

	/*
	 * Renders correct arm rotation
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPre(RenderPlayerEvent.Pre event)
	{
		EntityPlayer player = event.getEntityPlayer();
		AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
		ItemStack stack = player.getHeldItemMainhand();
		ModelPlayer model = event.getRenderer().getMainModel();
		EntityPlayerSP clientPlayer = Minecraft.getMinecraft().player;

		ResourceLocation skinLoc = DefaultPlayerSkin.getDefaultSkin(player.getPersistentID());

		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack))
		{
			model.bipedLeftArm.isHidden = true;
			model.bipedRightArm.isHidden = true;

			Minecraft.getMinecraft().getTextureManager().bindTexture(skinLoc);
			float rotation = -player.renderYawOffset;
			ModelRenderer fakeLeftArm = new ModelRenderer(model, 32, 48);
			if (aplayer.getSkinType().equals("default"))
			{
				fakeLeftArm.addBox(model.bipedLeftArm.offsetX + 4.2F, model.bipedLeftArm.offsetY, model.bipedLeftArm.offsetZ, 4, 12, 4, .08F);
			}
			else
			{
				fakeLeftArm.addBox(model.bipedLeftArm.offsetX + 4.2F, model.bipedLeftArm.offsetY, model.bipedLeftArm.offsetZ, 3, 12, 4, .08F);
			}

			ModelRenderer fakeRightArm = new ModelRenderer(model, 40, 16);
			if (aplayer.getSkinType().equals("default"))
			{
				fakeRightArm.addBox(model.bipedRightArm.offsetX - 7.9F, model.bipedRightArm.offsetY, model.bipedRightArm.offsetZ, 4, 12, 4, .08F);
			}
			else
			{
				fakeRightArm.addBox(model.bipedRightArm.offsetX - 7.2F, model.bipedRightArm.offsetY, model.bipedRightArm.offsetZ, 3, 12, 4, .08F);
			}

			if (!player.isSneaking())
			{
				fakeRightArm.rotateAngleX = -.9F;
				fakeLeftArm.rotateAngleX = -.9F;
			}
			else
			{	
				fakeRightArm.rotateAngleX = -1.3F;
				fakeLeftArm.rotateAngleX = -1.3F;
			}
			model.bipedBody.addChild(fakeLeftArm);
			model.bipedBody.addChild(fakeRightArm);

		}
		else
		{
			model.bipedLeftArm.isHidden = false;
			model.bipedRightArm.isHidden = false;
			if (model.bipedBody.childModels != null && !model.bipedBody.childModels.isEmpty())
			{
				model.bipedBody.childModels.clear();
			}
		}

		if (stack.isEmpty() || stack.getItem() != RegistrationHandler.itemTile || !ItemTile.hasTileData(stack))
		{
			model.bipedLeftArm.isHidden = false;
			model.bipedRightArm.isHidden = false;
		}

	}

	public static boolean isChest(Block block)
	{
		return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
	}

	@SideOnly(Side.CLIENT)
	private static RenderPlayer getRenderPlayer(AbstractClientPlayer player)
	{
		Minecraft mc = Minecraft.getMinecraft();
		RenderManager manager = mc.getRenderManager();
		return manager.getSkinMap().get(player.getSkinType());
	}

	@SideOnly(Side.CLIENT)
	private static ModelPlayer getPlayerModel(AbstractClientPlayer player)
	{
		return getRenderPlayer(player).getMainModel();
	}

}
