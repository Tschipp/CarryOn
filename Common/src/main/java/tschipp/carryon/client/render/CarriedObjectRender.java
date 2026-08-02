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
import net.minecraft.client.renderer.entity.EntityRenderer;
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
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.Constants;
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
					drawBlock(player, matrix, light, nodeCollector, firstPerson, partialTicks);
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

	private static void drawBlock(Player player, PoseStack matrix, int light, SubmitNodeCollector nodeCollector, boolean firstPerson, float partialTicks)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		ItemStackRenderState renderState = new ItemStackRenderState();

		matrix.pushPose();

		PoseStack renderPose = CarryRenderHelper.setupBlockTransformations(player, matrix, carry, firstPerson);

		ItemStack renderStack = CarryRenderHelper.getRenderItemStack(player).create();
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

        // Use the player's X,Z so extractShadow() probes an already-loaded chunk, but cap Y
        // at a safe in-world height. The player may be far above the world's block range
        // (e.g. test world at Y=6.4 M). extractShadow() calls level.getBlockState() for
        // blocks near the entity's Y; a Y outside the normal section range causes Sodium to
        // allocate a new Chunk Sections UBO slot (section index ~400 000), which invalidates
        // its visibility graph and forces every section to re-render in one frame — collapsing
        // FPS to ~10 and spiking GPU to 100%. Y=64 is valid in every normal dimension
        // (-64 to 320 for overworld); shadow pieces are cleared immediately after extraction.
        entity.setPos(playerpos.x, 64.0, playerpos.z);
        entity.xOld = playerpos.x;
        entity.yOld = 64.0;
        entity.zOld = playerpos.z;
        entity.xRotO = 0.0f;
        entity.yRotO = 0.0f;
        entity.setYHeadRot(0.0f);
        // LivingEntityRenderer.extractRenderState() reads yBodyRotO/yBodyRot via
        // solveBodyRot() and yHeadRotO directly to compute state.bodyRot. A non-zero
        // bodyRot causes setupRotations() to apply rotate(180 - bodyRot) — rotating the
        // entity to its NBT-captured world-facing direction instead of the poseStack-relative
        // forward direction set up by setupEntityTransformations(). That rotation can point
        // the entity away from the camera, making it appear invisible.
        if (entity instanceof LivingEntity le) {
            le.yBodyRot = 0.0f;
            le.yBodyRotO = 0.0f;
            le.yHeadRotO = 0.0f;
        }
        // EntityType.create() constructs the entity without adding it to the world, so
        // no entity ID is ever assigned. LivingEntityRenderer.extractRenderState() calls
        // ItemModelResolver.updateForLiving() which calls entity.getId() — this throws
        // IllegalStateException every frame, preventing any model submission and
        // generating a full JVM stack trace at 60 Hz (severe CPU overhead).
        // Use the player's ID as a stable fake: cows hold no items so the ID is only
        // used as a seed for item-model variation and the exact value doesn't matter.
        entity.setId(player.getId());

        matrix.pushPose();

		CarryRenderHelper.setupEntityTransformations(player, matrix, carry, firstPerson);

        if (entity instanceof LivingEntity)
            ((LivingEntity) entity).hurtTime = 0;

        try {
            EntityRenderState renderState = manager.extractEntity(entity, partialTicks);
            // Prevent blob-shadow geometry for an entity held in the player's hands.
            renderState.shadowPieces.clear();
            // Entity is never on fire while being carried; clearing also avoids accessing
            // camera.orientation in the flame billboard path.
            renderState.displayFireAnimation = false;
            // A name tag floating above the player's hands would look wrong, and
            // submitNameDisplay() fires extra text-geometry work we don't need here.
            renderState.nameTag = null;
            renderState.scoreText = null;
            // Entity is not leashed while being carried.
            renderState.leashStates = null;
            renderState.lightCoords = light;
            // Call renderer.submit() directly, bypassing EntityRenderDispatcher.submit().
            // EntityRenderDispatcher.submit() internally translates the poseStack by
            // (state.x - cameraX), which with camera=(0,0,0) and state.x at the entity's
            // real world coordinate (~60) shifts the entity ~60 units from the poseStack
            // origin — out of the player's hands entirely.
            // Zeroing state.x/y/z to fix that offset caused the shadow system to probe
            // world-origin chunks, triggering expensive shadow map updates (100% GPU, 13 FPS).
            // renderer.submit() applies only entity-local transforms (rotation, scale) and
            // adds the model node — no world-space translation, no shadow map side-effects.
            @SuppressWarnings({"unchecked", "rawtypes"})
            EntityRenderer renderer = manager.getRenderer(entity);
            renderer.submit(renderState, matrix, nodeCollector, new CameraRenderState());
        }
        catch (Exception e)
        {
            Constants.LOG.warn("CarryOn: entity carry render error", e);
        }

        matrix.popPose();
		if(!firstPerson)
			matrix.popPose();
    }
}

