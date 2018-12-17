package tschipp.carryon.render;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformations.Type;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import tschipp.carryon.RegistryHandler;
import tschipp.carryon.items.ItemTile;

public class BlockRendererLayer extends class_3887<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private static MinecraftClient client = MinecraftClient.getInstance();

    public BlockRendererLayer(class_3883<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> var1) {
        super(var1);
     }

    @Override
    public void method_4199(AbstractClientPlayerEntity player, float var2, float var3, float partialTicks, float var5,
            float var6, float var7, float var8) {
        ItemStack stack = player.getMainHandStack();
        if (!stack.isEmpty() && stack.getItem() == RegistryHandler.TILE_ITEM) {
            ItemStack renderStack = ItemTile.getItemStack(stack);
            // BakedModel model =
            // client.getItemRenderer().getModelMap().getModel(renderStack);

            GlStateManager.pushMatrix();
            GlStateManager.rotated(180, 1, 0, 0);
            GlStateManager.rotated(180, 0, 1, 0);
            GlStateManager.scaled(0.6, 0.6, 0.6);
            GlStateManager.translated(0, -0.75, -0.65);

            if(player.isSneaking())
            {
                GlStateManager.translated(0, -0.15, -0.15);
            }

            if(isChest(ItemTile.getBlock(stack)))
            {
                GlStateManager.rotated(180, 0, 1, 0);
            }

            client.getItemRenderer().renderItemWithTransformation(renderStack, Type.ORIGIN);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean method_4200() {
        return false;
    }

    public static boolean isChest(Block block)
	{
		return block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
    }
    
    public static void renderFirstPerson(LivingEntity entity, ItemStack stack, float partialTicks)
    {
        ItemStack renderStack = ItemTile.getItemStack(stack);

        GlStateManager.pushMatrix();
        GlStateManager.scaled(2.5, 2.5, 2.5);
        GlStateManager.translated(0, -0.6, -1);
        GlStateManager.enableBlend();

        if(isChest(ItemTile.getBlock(stack)))
        {
            GlStateManager.rotated(180, 0, 1, 0);
        }

        setLightCoords();
        client.getItemRenderer().renderItemWithTransformation(renderStack, Type.ORIGIN);
        GlStateManager.popMatrix();
    }

    private static void setLightCoords() {
        AbstractClientPlayerEntity player = client.player;
        int var2 = client.world.getLightmapIndex(new BlockPos(player.x, player.y + (double)player.getEyeHeight(), player.z), 0);
        float var3 = (float)(var2 & '\uffff');
        float var4 = (float)(var2 >> 16);
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, var3, var4);
     }

}