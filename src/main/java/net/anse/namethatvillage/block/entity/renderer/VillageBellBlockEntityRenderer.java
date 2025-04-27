package net.anse.namethatvillage.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.anse.namethatvillage.block.VillageBellBlock;
import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BellModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;

import static net.minecraft.client.renderer.blockentity.BellRenderer.BELL_RESOURCE_LOCATION;

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
        this.bellModel.setupAnim(bell, partialTick);
        this.bellModel.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
