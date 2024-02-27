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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.TickEvent;
import tschipp.carryon.CarryOnCommonClient;
import tschipp.carryon.Constants;
import tschipp.carryon.client.render.CarriedObjectRender;
import tschipp.carryon.client.render.CarryRenderHelper;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void renderHand(RenderHandEvent event)
	{
		Player player = Minecraft.getInstance().player;
		MultiBufferSource buffer = event.getMultiBufferSource();
		PoseStack matrix = event.getPoseStack();
		int light = event.getPackedLight();
		float partialTicks = event.getPartialTick();

		if(CarriedObjectRender.drawFirstPerson(player, buffer, matrix, light, partialTicks) && CarryRenderHelper.getPerspective() == 0)
			event.setCanceled(true);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event)
	{
		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			CarriedObjectRender.drawThirdPerson(event.getPartialTick(), event.getPoseStack());
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onGuiInit(ScreenEvent.Init.Pre event)
	{
		if (event.getScreen() != null)
		{
			boolean inventory = event.getScreen() instanceof AbstractContainerScreen;
			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;

			if (player != null && inventory)
			{
				CarryOnData carry = CarryOnDataManager.getCarryData(player);
				if (carry.isCarrying())
				{
					mc.player.closeContainer();
					mc.screen = null;
					mc.mouseHandler.grabMouse();
					event.setCanceled(true);
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == TickEvent.Phase.END)
		{
			CarryOnCommonClient.checkForKeybinds();
			CarryOnCommonClient.onCarryClientTick();
		}

	}
}
