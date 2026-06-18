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

package tschipp.carryon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptRender;
import tschipp.carryon.platform.Services;

public class CarriedObjectRender
{
	public static boolean draw(Player player, PoseStack matrix, int light, float partialTicks,SubmitNodeCollector nodeCollector, boolean firstPerson)
	{
		if(Services.PLATFORM.isModLoaded("firstperson") || Services.PLATFORM.isModLoaded("firstpersonmod") || player == null)
			return false;

		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		try {
			if (carry.isCarrying(CarryType.ENTITY))
				drawEntity(player, matrix, light, partialTicks, nodeCollector, firstPerson);
			else {
				CarryRenderHelper.clearRenderEntity(player);
				if (carry.isCarrying(CarryType.BLOCK))
					drawBlock(player,  matrix, light, CarryRenderHelper.getRenderState(player), nodeCollector, firstPerson, partialTicks);
			}
		}
		catch (Exception e)
		{
			//hehe
		}

		if(carry.getActiveScript().isPresent())
		{
			ScriptRender render = carry.getActiveScript().get().scriptRender();
			if(!render.renderLeftArm() && player.getMainArm() == HumanoidArm.LEFT)
				return false;

			if(!render.renderRightArm() && player.getMainArm() == HumanoidArm.RIGHT)
				return false;
		}

		return carry.isCarrying();
	}

	private static void drawBlock(Player player, PoseStack matrix, int light, BlockState state, SubmitNodeCollector nodeCollector, boolean firstPerson, float partialTicks)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		ItemStackRenderState renderState = new ItemStackRenderState();

		matrix.pushPose();

		PoseStack renderPose = CarryRenderHelper.setupBlockTransformations(player, matrix, carry, firstPerson);

		ItemStack renderStack = CarryRenderHelper.getRenderItemStack(player);
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, renderStack, ItemDisplayContext.NONE, player.level(), null, 0);
		renderState.submit(renderPose, nodeCollector, light,  OverlayTexture.NO_OVERLAY, 0);
		matrix.popPose();
	}

	private static void drawEntity(Player player, PoseStack matrix, int light, float partialTicks,SubmitNodeCollector nodeCollector, boolean firstPerson) {
		EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();

		Entity entity = CarryRenderHelper.getRenderEntity(player);
		CarryOnData carry = CarryOnDataManager.getCarryData(player);

		if (entity == null)
			return;

        Vec3 playerpos = CarryRenderHelper.getExactPos(player, partialTicks);

        entity.setPos(playerpos.x, playerpos.y, playerpos.z);
        entity.xRotO = 0.0f;
        entity.yRotO = 0.0f;
        entity.setYHeadRot(0.0f);

        matrix.pushPose();

		CarryRenderHelper.setupEntityTransformations(player, matrix, carry, firstPerson);

        if (entity instanceof LivingEntity)
            ((LivingEntity) entity).hurtTime = 0;

        try {
            EntityRenderState renderState = manager.extractEntity(entity, partialTicks);
            renderState.shadowPieces.clear();
			renderState.displayFireAnimation = false;
			renderState.nameTag = null;
			renderState.scoreText = null;
			renderState.leashStates = null;
			renderState.lightCoords = light;
			manager.submit(renderState, new CameraRenderState(), 0.0, 0.0, 0.0, matrix, nodeCollector);
        }
        catch (Exception ignored)
        {
        }

        matrix.popPose();
		if(!firstPerson)
			matrix.popPose();
    }
}

