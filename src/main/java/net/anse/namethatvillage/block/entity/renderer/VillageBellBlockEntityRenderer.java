package net.anse.namethatvillage.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.anse.namethatvillage.block.VillageBellBlock;
import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BellModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public class VillageBellBlockEntityRenderer implements BlockEntityRenderer<VillageBellBlockEntity>
{
    private final BellModel bellModel;

    public VillageBellBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.bellModel = new BellModel(context.bakeLayer(ModelLayers.BELL));
    }

    @Override
    public void render(VillageBellBlockEntity bell, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {

        poseStack.pushPose();
        poseStack.scale(1.0F, 1.0F, 1.0F);

        VertexConsumer vertexconsumer = bufferSource.getBuffer(this.bellModel.renderType(ResourceLocation.fromNamespaceAndPath("namethatvillage", "textures/entity/bell/bell_body.png")));


        float angle = bell.getShakeTime() + partialTick;
        float f1 = 0.0F;
        float f2 = 0.0F;

        if (bell.isShaking()) {
            // Calculamos la cantidad de sacudida (f3), que es similar a cómo lo hace la vanilla
            float shake = (float) Math.sin((double) angle / Math.PI) / (4.0F + angle / 3.0F) * 60.0F;
            // Dependiendo de la dirección de la campana, aplicamos la rotación en los ejes X o Z
            if (bell.getClickDirection() == Direction.NORTH) {
                f1 = -shake;  // Rotación en el eje X (hacia atrás)
            } else if (bell.getClickDirection() == Direction.SOUTH) {
                f1 = shake;   // Rotación en el eje X (hacia adelante)
            } else if (bell.getClickDirection() == Direction.EAST) {
                f2 = -shake;  // Rotación en el eje Z (hacia la izquierda)
            } else if (bell.getClickDirection() == Direction.WEST) {
                f2 = shake;   // Rotación en el eje Z (hacia la derecha)
            }
        }

        // Trasladamos el origen de la rotación al centro de la campana
        poseStack.translate(0.5D, 0.75D, 0.5D);
        // Aplicamos la rotación en los ejes correspondientes
        poseStack.mulPose(Axis.XP.rotationDegrees(f1));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f2));
        // Volvemos al origen original del bloque
        poseStack.translate(-0.5D, -0.75D, -0.5D);

        this.bellModel.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay);
        poseStack.popPose();

    }
}
