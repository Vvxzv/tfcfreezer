package com.github.Mev10.client.render;

import com.github.Mev10.common.TfcfreezerHelpers;
import com.github.Mev10.common.block.FourWayFacingDeviceBlock;
import com.github.Mev10.common.blockentities.freezerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class FreezerBlockEntityRenderer implements BlockEntityRenderer<freezerBlockEntity> {
    private static final ResourceLocation TEXTURE = TfcfreezerHelpers.identifier("textures/block/freezer.png");

    private static final float HINGE_X = 15.8F / 16.0F;
    private static final float HINGE_Y = 8.0F / 16.0F;
    private static final float HINGE_Z = 3.0F / 16.0F;
    private static final float MAX_OPEN_ANGLE = -112.5F;

    public FreezerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(freezerBlockEntity freezer, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = freezer.getBlockState();
        Direction facing = state.hasProperty(FourWayFacingDeviceBlock.FACING) ? state.getValue(FourWayFacingDeviceBlock.FACING) : Direction.NORTH;

        float openness = freezer.getOpenness(partialTick);
        openness = 1.0F - openness;
        openness = 1.0F - openness * openness * openness;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-modelRotation(facing)));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        poseStack.translate(HINGE_X, HINGE_Y, HINGE_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(MAX_OPEN_ANGLE * openness));
        poseStack.translate(-HINGE_X, -HINGE_Y, -HINGE_Z);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        renderDoor(consumer, poseStack, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static float modelRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static void renderDoor(VertexConsumer consumer, PoseStack poseStack, int packedLight, int packedOverlay) {
        renderMainDoor(consumer, poseStack, packedLight, packedOverlay);
        renderInnerDoor(consumer, poseStack, packedLight, packedOverlay);
        renderHandle(consumer, poseStack, packedLight, packedOverlay);
    }

    private static void renderMainDoor(VertexConsumer consumer, PoseStack poseStack, int packedLight, int packedOverlay) {
        float minX = 0.0F / 16.0F;
        float minY = 0.0F / 16.0F;
        float minZ = 0.0F / 16.0F;
        float maxX = 16.0F / 16.0F;
        float maxY = 16.0F / 16.0F;
        float maxZ = 2.0F / 16.0F;

        north(consumer, poseStack, minX, minY, minZ, maxX, maxY, 0.0F, 0.0F, 4.0F, 4.0F, packedLight, packedOverlay);
        east(consumer, poseStack, maxX, minY, minZ, maxY, maxZ, 8.0F, 3.25F, 8.5F, 7.25F, packedLight, packedOverlay);
        south(consumer, poseStack, minX, minY, maxZ, maxX, maxY, 0.0F, 4.0F, 4.0F, 8.0F, packedLight, packedOverlay);
        west(consumer, poseStack, minX, minY, minZ, maxY, maxZ, 8.5F, 3.25F, 8.0F, 7.25F, packedLight, packedOverlay);
        up(consumer, poseStack, minX, maxY, minZ, maxX, maxZ, 11.25F, 8.5F, 7.25F, 8.0F, packedLight, packedOverlay);
        down(consumer, poseStack, minX, minY, minZ, maxX, maxZ, 12.0F, 7.25F, 8.0F, 7.75F, packedLight, packedOverlay);
    }

    private static void renderInnerDoor(VertexConsumer consumer, PoseStack poseStack, int packedLight, int packedOverlay) {
        float minX = 0.2F / 16.0F;
        float minY = 0.2F / 16.0F;
        float minZ = 2.0F / 16.0F;
        float maxX = 15.8F / 16.0F;
        float maxY = 15.8F / 16.0F;
        float maxZ = 3.0F / 16.0F;

        east(consumer, poseStack, maxX, minY, minZ, maxY, maxZ, 8.5F, 3.25F, 8.75F, 7.25F, packedLight, packedOverlay);
        south(consumer, poseStack, minX, minY, maxZ, maxX, maxY, 0.0F, 4.0F, 4.0F, 8.0F, packedLight, packedOverlay);
        west(consumer, poseStack, minX, minY, minZ, maxY, maxZ, 8.5F, 3.25F, 8.75F, 7.25F, packedLight, packedOverlay);
        up(consumer, poseStack, minX, maxY, minZ, maxX, maxZ, 8.75F, 7.25F, 8.5F, 3.25F, packedLight, packedOverlay);
        down(consumer, poseStack, minX, minY, minZ, maxX, maxZ, 8.75F, 7.25F, 8.5F, 3.25F, packedLight, packedOverlay);
    }

    private static void renderHandle(VertexConsumer consumer, PoseStack poseStack, int packedLight, int packedOverlay) {
        float minX = 1.0F / 16.0F;
        float minY = 5.0F / 16.0F;
        float minZ = -1.5F / 16.0F;
        float maxX = 3.0F / 16.0F;
        float maxY = 12.0F / 16.0F;
        float maxZ = 0.5F / 16.0F;

        north(consumer, poseStack, minX, minY, minZ, maxX, maxY, 15.5F, 9.75F, 16.0F, 11.5F, packedLight, packedOverlay);
        up(consumer, poseStack, minX, maxY, minZ, maxX, maxZ, 15.5F, 9.75F, 16.0F, 9.25F, packedLight, packedOverlay);
        down(consumer, poseStack, minX, minY, minZ, maxX, maxZ, 15.5F, 9.25F, 16.0F, 9.75F, packedLight, packedOverlay);
    }

    private static void north(VertexConsumer consumer, PoseStack poseStack, float minX, float minY, float z, float maxX, float maxY, float u1, float v1, float u2, float v2, int packedLight, int packedOverlay) {
        vertex(consumer, poseStack, maxX, minY, z, u2, v2, 0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, minX, minY, z, u1, v2, 0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, minX, maxY, z, u1, v1, 0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, maxX, maxY, z, u2, v1, 0.0F, 0.0F, -1.0F, packedLight, packedOverlay);
    }

    private static void south(VertexConsumer consumer, PoseStack poseStack, float minX, float minY, float z, float maxX, float maxY, float u1, float v1, float u2, float v2, int packedLight, int packedOverlay) {
        vertex(consumer, poseStack, minX, minY, z, u2, v2, 0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, maxX, minY, z, u1, v2, 0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, maxX, maxY, z, u1, v1, 0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, minX, maxY, z, u2, v1, 0.0F, 0.0F, 1.0F, packedLight, packedOverlay);
    }

    private static void east(VertexConsumer consumer, PoseStack poseStack, float x, float minY, float minZ, float maxY, float maxZ, float u1, float v1, float u2, float v2, int packedLight, int packedOverlay) {
        vertex(consumer, poseStack, x, minY, maxZ, u2, v2, 1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, x, minY, minZ, u1, v2, 1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, x, maxY, minZ, u1, v1, 1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, x, maxY, maxZ, u2, v1, 1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
    }

    private static void west(VertexConsumer consumer, PoseStack poseStack, float x, float minY, float minZ, float maxY, float maxZ, float u1, float v1, float u2, float v2, int packedLight, int packedOverlay) {
        vertex(consumer, poseStack, x, minY, minZ, u2, v2, -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, x, minY, maxZ, u1, v2, -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, x, maxY, maxZ, u1, v1, -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, x, maxY, minZ, u2, v1, -1.0F, 0.0F, 0.0F, packedLight, packedOverlay);
    }

    private static void up(VertexConsumer consumer, PoseStack poseStack, float minX, float y, float minZ, float maxX, float maxZ, float u1, float v1, float u2, float v2, int packedLight, int packedOverlay) {
        vertex(consumer, poseStack, minX, y, maxZ, u2, v2, 0.0F, 1.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, maxX, y, maxZ, u1, v2, 0.0F, 1.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, maxX, y, minZ, u1, v1, 0.0F, 1.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, minX, y, minZ, u2, v1, 0.0F, 1.0F, 0.0F, packedLight, packedOverlay);
    }

    private static void down(VertexConsumer consumer, PoseStack poseStack, float minX, float y, float minZ, float maxX, float maxZ, float u1, float v1, float u2, float v2, int packedLight, int packedOverlay) {
        vertex(consumer, poseStack, minX, y, minZ, u2, v2, 0.0F, -1.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, maxX, y, minZ, u1, v2, 0.0F, -1.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, maxX, y, maxZ, u1, v1, 0.0F, -1.0F, 0.0F, packedLight, packedOverlay);
        vertex(consumer, poseStack, minX, y, maxZ, u2, v1, 0.0F, -1.0F, 0.0F, packedLight, packedOverlay);
    }

    private static void vertex(VertexConsumer consumer, PoseStack poseStack, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u / 16.0F, v / 16.0F)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }
}
