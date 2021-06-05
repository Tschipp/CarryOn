package tschipp.carryon.client.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.client.helper.CarryRenderHelper;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemCarryonEntity;
import tschipp.carryon.common.scripting.CarryOnOverride;
import tschipp.carryon.common.scripting.ScriptChecker;

public class RenderEntityEvents
{
	
	public static final Map<String, Entity> nbtEntityMap = new HashMap<>();
	
	public static Entity getEntity(ItemStack carried, World world)
	{
		String nbt = ItemCarryonEntity.getPersistentData(carried).toString();
		if(nbtEntityMap.containsKey(nbt))
		{
			return nbtEntityMap.get(nbt);
		}
		
		Entity entity = ItemCarryonEntity.getEntity(carried, world);
		nbtEntityMap.put(nbt, entity);
		
		return entity;
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		nbtEntityMap.clear();
	}
	
	
	/*
	 * Renders the Entity in First Person
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{
		World world = Minecraft.getInstance().level;
		PlayerEntity player = Minecraft.getInstance().player;
		ItemStack stack = player.getMainHandItem();
		int perspective = CarryRenderHelper.getPerspective();
		float partialticks = event.getPartialTicks();
		MatrixStack matrix = event.getMatrixStack();
		int light = event.getLight();
		IRenderTypeBuffer buffer = event.getBuffers();
		EntityRendererManager manager = Minecraft.getInstance().getEntityRenderDispatcher();
		
		if (!stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity && ItemCarryonEntity.hasEntityData(stack))
		{
			if(ModList.get().isLoaded("realrender") || ModList.get().isLoaded("rfpr"))
				return;
			
			Entity entity = getEntity(stack, world);

			if (entity != null)
			{
				Vector3d playerpos = CarryRenderHelper.getExactPos(player, partialticks);
				
				entity.setPos(playerpos.x, playerpos.y, playerpos.z);
				entity.yRot = 0.0f;
				entity.yRotO = 0.0f;
				entity.setYHeadRot(0.0f);
				
				float height = entity.getBbHeight();
				float width = entity.getBbWidth();
				
				matrix.pushPose();
				matrix.scale(0.8f, 0.8f, 0.8f);
				matrix.mulPose(Vector3f.YP.rotationDegrees(180));
				matrix.translate(0.0, -height - .1, width + 0.1);
				
				RenderSystem.enableAlphaTest();

				if (perspective == 0)
				{
					RenderHelper.turnBackOn();
					manager.setRenderShadow(false);

					CarryOnOverride carryOverride = ScriptChecker.getOverride(player);
					if (carryOverride != null)
					{
						CarryRenderHelper.performOverrideTransformation(matrix, carryOverride);
						
						String entityname = carryOverride.getRenderNameEntity();
						if (entityname != null)
						{
							Entity newEntity = null;

							Optional<EntityType<?>> type = EntityType.byString(entityname);
							if(type.isPresent())
								newEntity = type.get().create(world);
							
							if (newEntity != null)
							{
								CompoundNBT nbttag = carryOverride.getRenderNBT();
								if (nbttag != null)
									newEntity.deserializeNBT(nbttag);
								entity = newEntity;
								entity.setPos(playerpos.x, playerpos.y, playerpos.z);
								entity.yRot = 0.0f;
								entity.yRotO = 0.0f;
								entity.setYHeadRot(0.0f);
							}
						}
					}

					if(entity instanceof LivingEntity)
						((LivingEntity) entity).hurtTime = 0;
										
					manager.render(entity, 0, 0, 0, 0f, 0, matrix, buffer, light);
					manager.setRenderShadow(true);
				}

				RenderSystem.disableAlphaTest();
				matrix.popPose();

				RenderHelper.turnOff();
				RenderSystem.disableRescaleNormal();

				if (perspective == 0)
				{
					event.setCanceled(true);
				}
			}
		}
	}
}
