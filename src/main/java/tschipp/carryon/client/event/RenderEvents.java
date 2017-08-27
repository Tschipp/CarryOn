package tschipp.carryon.client.event;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
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
import net.minecraft.item.Item;
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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.carryon.CarryOn;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;
import tschipp.carryon.network.server.SyncKeybindPacket;

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
			ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItemMainhand();
			if (stack != null && stack.getItem() == RegistrationHandler.itemTile)
			{
				if (ItemTile.hasTileData(stack))
					event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerTick(PlayerTickEvent event)
	{
		EntityPlayer player = event.player;
		if (player != null && event.side == Side.CLIENT)
		{
			
			boolean keyPressed = CarryOnKeybinds.carryKey.isKeyDown();
			boolean playerKeyPressed = CarryOnKeybinds.isKeyPressed(player);
			
			if (keyPressed && !playerKeyPressed)
			{
				CarryOnKeybinds.setKeyPressed(player, true);
				CarryOn.network.sendToServer(new SyncKeybindPacket(true));
			}
			else if(!keyPressed && playerKeyPressed)
			{
				CarryOnKeybinds.setKeyPressed(player, false);
				CarryOn.network.sendToServer(new SyncKeybindPacket(false));
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
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (player != null)
			{
				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
				if (inventory && (stack != null ? stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack) : false))
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
		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItemMainhand();
		if (stack != null ? stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack) : false)
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
		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
		ItemStack stack = player.getHeldItemMainhand();
		int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;

		if (stack != null ? stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack) : false)
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
			{
				IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(tileStack == null ? stack : tileStack, world, player);
				if (ModelOverridesHandler.hasCustomOverrideModel(state, tag))
				{
					Object override = ModelOverridesHandler.getOverrideObject(state, tag);
					if (override instanceof ItemStack)
					{
						Minecraft.getMinecraft().getRenderItem().renderItem((ItemStack) override, model);
					}
					else
						Minecraft.getMinecraft().getRenderItem().renderItem(tileStack == null ? stack : tileStack, model);
				}
				else
					Minecraft.getMinecraft().getRenderItem().renderItem(tileStack == null ? stack : tileStack, model);
			}
			GlStateManager.scale(1, 1, 1);
			GlStateManager.popMatrix();

			if (perspective == 0)
				event.setCanceled(true);
		}
		else
		{
			if (stack == null ? true : stack.getItem() != RegistrationHandler.itemEntity)
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
	}

	/*
	 * Renders the Block in Third Person
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPost(RenderPlayerEvent.Post event)
	{
		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayer player = event.getEntityPlayer();
		ModelPlayer modelPlayer = event.getRenderer().getMainModel();
		EntityPlayerSP clientPlayer = Minecraft.getMinecraft().thePlayer;
		ItemStack stack = player.getHeldItemMainhand();
		float partialticks = event.getPartialRenderTick();
		if (stack != null ? stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack) : false)
		{
			Block block = ItemTile.getBlock(stack);
			IBlockState state = ItemTile.getBlockState(stack);
			NBTTagCompound tag = ItemTile.getTileData(stack);

			ItemStack tileItem = ItemTile.getItemStack(stack);

			EntityItem entityItem = new EntityItem(Minecraft.getMinecraft().theWorld, 0, 0, 0);
			entityItem.hoverStart = 0;

			entityItem.setEntityItemStack(tileItem);
			float rotation = -player.renderYawOffset;
			int perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;

			double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialticks;
			double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialticks;
			double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialticks;

			double c0 = clientPlayer.lastTickPosX + (clientPlayer.posX - clientPlayer.lastTickPosX) * (double) partialticks;
			double c1 = clientPlayer.lastTickPosY + (clientPlayer.posY - clientPlayer.lastTickPosY) * (double) partialticks;
			double c2 = clientPlayer.lastTickPosZ + (clientPlayer.posZ - clientPlayer.lastTickPosZ) * (double) partialticks;

			double xOffset = d0 - c0;
			double yOffset = d1 - c1;
			double zOffset = d2 - c2;

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

			IBakedModel model = ModelOverridesHandler.hasCustomOverrideModel(state, tag) ? ModelOverridesHandler.getCustomOverrideModel(state, tag, world, player) : Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(tileItem == null ? stack : tileItem, world, player);
			if (ModelOverridesHandler.hasCustomOverrideModel(state, tag))
			{
				Object override = ModelOverridesHandler.getOverrideObject(state, tag);
				if (override instanceof ItemStack)
				{
					Minecraft.getMinecraft().getRenderItem().renderItem((ItemStack) override, model);
				}
				else
					Minecraft.getMinecraft().getRenderItem().renderItem(tileItem == null ? stack : tileItem, model);
			}
			else
				Minecraft.getMinecraft().getRenderItem().renderItem(tileItem == null ? stack : tileItem, model);
			GlStateManager.scale(1, 1, 1);

			GlStateManager.popMatrix();
		}

	}

	/*
	 * Renders correct arm rotation
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRenderPre(RenderPlayerEvent.Pre event)
	{

		if (!Loader.isModLoaded("mobends") && CarryOnConfig.settings.renderArms)
		{

			EntityPlayer player = event.getEntityPlayer();
			AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
			ItemStack stack = player.getHeldItemMainhand();
			ModelPlayer model = event.getRenderer().getMainModel();
			EntityPlayerSP clientPlayer = Minecraft.getMinecraft().thePlayer;

			ResourceLocation skinLoc = DefaultPlayerSkin.getDefaultSkin(player.getPersistentID());

			ModelRenderer fakeLeftArm = new ModelRenderer(model, 32, 48);
			ModelRenderer fakeRightArm = new ModelRenderer(model, 40, 16);
			
			player.setArrowCountInEntity(0); //TODO Temporary Fix
			
			if (stack != null ? ((stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack)) || (stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack))) : false)
			{
				if (model.bipedBody.childModels != null && !model.bipedBody.childModels.isEmpty())
				{

					for (int k = 0; k < model.bipedBody.childModels.size(); k++)
					{
						float chkRot = model.bipedBody.childModels.get(k).rotateAngleX;
						if (chkRot == -0.9001F || chkRot == -1.2001F || chkRot == -1.4001F || chkRot == -1.7001F)
						{
							model.bipedBody.childModels.remove(k);
							k = k - 1;
						}
					}

				}

				Item item = stack.getItem();

				model.bipedLeftArm.isHidden = true;
				model.bipedRightArm.isHidden = true;

				Minecraft.getMinecraft().getTextureManager().bindTexture(skinLoc);
				float rotation = -player.renderYawOffset;

				if (aplayer.getSkinType().equals("default"))
				{
					fakeLeftArm.addBox(model.bipedLeftArm.offsetX + 4.2F, model.bipedLeftArm.offsetY, model.bipedLeftArm.offsetZ, 4, 12, 4, .08F);
				}
				else
				{
					fakeLeftArm.addBox(model.bipedLeftArm.offsetX + 4.2F, model.bipedLeftArm.offsetY, model.bipedLeftArm.offsetZ, 3, 12, 4, .08F);
				}

				if (aplayer.getSkinType().equals("default"))
				{
					fakeRightArm.addBox(model.bipedRightArm.offsetX - 7.9F, model.bipedRightArm.offsetY, model.bipedRightArm.offsetZ, 4, 12, 4, .08F);
				}
				else
				{
					fakeRightArm.addBox(model.bipedRightArm.offsetX - 7.2F, model.bipedRightArm.offsetY, model.bipedRightArm.offsetZ, 3, 12, 4, .08F);
				}

				if (item == RegistrationHandler.itemTile)
				{
					if (!player.isSneaking())
					{
						fakeRightArm.rotateAngleX = -.9001F;
						fakeLeftArm.rotateAngleX = -.9001F;
					}
					else
					{
						fakeRightArm.rotateAngleX = -1.4001F;
						fakeLeftArm.rotateAngleX = -1.4001F;
					}
				}
				else
				{
					if (!player.isSneaking())
					{
						fakeRightArm.rotateAngleX = -1.2001F;
						fakeLeftArm.rotateAngleX = -1.2001F;
					}
					else
					{
						fakeRightArm.rotateAngleX = -1.7001F;
						fakeLeftArm.rotateAngleX = -1.7001F;
					}

					fakeRightArm.rotateAngleY = -0.15f;
					fakeLeftArm.rotateAngleY = 0.15f;

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
					for (int k = 0; k < model.bipedBody.childModels.size(); k++)
					{
						float chkRot = model.bipedBody.childModels.get(k).rotateAngleX;
						if (chkRot == -0.9001F || chkRot == -1.2001F || chkRot == -1.4001F || chkRot == -1.7001F)
						{
							model.bipedBody.childModels.remove(k);
							k = k - 1;
						}
					}
				}
			}

			if (stack == null ||  (stack != null && stack.getItem() != RegistrationHandler.itemTile && stack.getItem() != RegistrationHandler.itemEntity))
			{
				model.bipedLeftArm.isHidden = false;
				model.bipedRightArm.isHidden = false;
			}
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void hideItems(RenderPlayerEvent.Specials.Pre event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();

		if (stack != null && (stack.getItem() == RegistrationHandler.itemTile || stack.getItem() == RegistrationHandler.itemEntity))
		{
			event.setRenderItem(false);
		}
	}

}
