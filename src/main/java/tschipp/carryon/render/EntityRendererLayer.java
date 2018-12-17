package tschipp.carryon.render;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import tschipp.carryon.RegistryHandler;
import tschipp.carryon.items.ItemEntity;

public class EntityRendererLayer extends class_3887<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private static MinecraftClient client = MinecraftClient.getInstance();

    public EntityRendererLayer(class_3883<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> var1) {
        super(var1);
     }

    @Override
    public void method_4199(AbstractClientPlayerEntity player, float var2, float var3, float partialTicks, float var5,
            float var6, float var7, float var8) {
        ItemStack stack = player.getMainHandStack();
        if (!stack.isEmpty() && stack.getItem() == RegistryHandler.ENTITY_ITEM) {
            Entity renderEntity = ItemEntity.getEntity(stack, player.world);

            double c0 = player.prevX + (player.x - player.prevX) * partialTicks;
            double c1 = player.prevY + (player.y - player.prevY) * partialTicks;
            double c2 = player.prevZ + (player.z - player.prevZ) * partialTicks;

            renderEntity.setPosition(c0, c1, c2);

            EntityRenderDispatcher renderer = client.getEntityRenderManager();

            renderEntity.yaw = 0f;
            renderEntity.pitch = 0f;
            float height = renderEntity.height;
            float width = renderEntity.width;
            float multiplier = height * width;

            GlStateManager.pushMatrix();
            GlStateManager.scaled(1, -1, 1);
            GlStateManager.scaled((10 - multiplier) * 0.08, (10 - multiplier) * 0.08, (10 - multiplier) * 0.08);
            GlStateManager.rotated(180, 0, 1, 0);
            GlStateManager.translated(0.0, height / 2 + -(height / 2) - 1.2, width - 0.1 < 0.7 ? width - 0.1 + (0.7 - (width - 0.1)) : width - 0.1);
            GlStateManager.color3f(1f, 1f, 1f);
            GlStateManager.enableAlphaTest();

            if (player.isSneaking()) {
                GlStateManager.translated(0, -0.1, 0);
            }

            renderer.method_3948(false);
            renderer.method_3954(renderEntity, 0, 0, 0, partialTicks, 0, true);
            renderer.method_3948(true);          

            GlStateManager.scaled(1, 1, 1);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean method_4200() {
        return false;
    }

    public static boolean isChest(Block block) {
        return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
    }

    public static void renderFirstPerson(LivingEntity player, ItemStack stack, float partialTicks)
    {
        if (!stack.isEmpty() && stack.getItem() == RegistryHandler.ENTITY_ITEM) {
            Entity renderEntity = ItemEntity.getEntity(stack, player.world);
         
            double c0 = player.prevX + (player.x - player.prevX) * partialTicks;
			double c1 = player.prevY + (player.y - player.prevY) * partialTicks;
			double c2 = player.prevZ + (player.z - player.prevZ) * partialTicks;

            renderEntity.setPosition(c0, c1, c2);

            EntityRenderDispatcher renderer = client.getEntityRenderManager();

            renderEntity.yaw = 0f;
            renderEntity.pitch = 0f;
            float height = renderEntity.height;
            float width = renderEntity.width;

            GlStateManager.pushMatrix();
            GlStateManager.scaled(.8, .8, .8);
            GlStateManager.rotated(180, 0, 1, 0);
            GlStateManager.translated(0.0, -height - .1, width + 0.1);
            GlStateManager.enableAlphaTest();

            setLightCoords();
            renderer.method_3948(false);
            renderer.method_3954(renderEntity, 0, 0, 0, partialTicks, 0, true);
            renderer.method_3948(true);   

            GlStateManager.disableAlphaTest();
            GlStateManager.scaled(1, 1, 1);
            GlStateManager.popMatrix();
        }
    }

    private static void setLightCoords() {
        AbstractClientPlayerEntity player = client.player;
        int var2 = client.world.getLightmapIndex(new BlockPos(player.x, player.y + (double)player.getEyeHeight(), player.z), 0);
        float var3 = (float)(var2 & '\uffff');
        float var4 = (float)(var2 >> 16);
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, var3, var4);
     }

}