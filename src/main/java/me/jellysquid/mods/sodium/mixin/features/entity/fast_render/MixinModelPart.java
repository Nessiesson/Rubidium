package me.jellysquid.mods.sodium.mixin.features.entity.fast_render;

import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.VertexConsumerUtils;
import me.jellysquid.mods.sodium.client.render.vertex.formats.ModelVertex;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import me.jellysquid.mods.sodium.client.util.math.Matrix3fExtended;
import me.jellysquid.mods.sodium.client.util.math.Matrix4fExtended;
import me.jellysquid.mods.sodium.client.util.math.MatrixUtil;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelPart.class)
public class MixinModelPart {
    private static final float NORM = 1.0F / 16.0F;

    @Shadow
    @Final
    private List<ModelPart.Cuboid> cuboids;

    /**
     * @author JellySquid
     * @reason Use optimized vertex writer, avoid allocations, use quick matrix transformations
     */
    //@Overwrite
    @Inject(method = "renderCuboids", at = @At("HEAD"), cancellable = true)
    private void renderCuboids(MatrixStack.Entry matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        var writer = VertexConsumerUtils.convertOrLog(vertexConsumer);
        if(writer == null) {
            return;
        }

        ci.cancel();

        Matrix3fExtended normalExt = MatrixUtil.getExtendedMatrix(matrices.getNormalMatrix());
        Matrix4fExtended positionExt = MatrixUtil.getExtendedMatrix(matrices.getPositionMatrix());

        int color = ColorABGR.pack(red, green, blue, alpha);

        for (ModelPart.Cuboid cuboid : this.cuboids) {
            for (ModelPart.Quad quad : ((ModelCuboidAccessor) cuboid).getQuads()) {
                float normX = normalExt.transformVecX(quad.direction);
                float normY = normalExt.transformVecY(quad.direction);
                float normZ = normalExt.transformVecZ(quad.direction);

                int norm = Norm3b.pack(normX, normY, normZ);

                try (MemoryStack stack = VertexBufferWriter.STACK.push()) {
                    long buffer = writer.buffer(stack, 4, ModelVertex.STRIDE, ModelVertex.FORMAT);
                    long ptr = buffer;

                    for (ModelPart.Vertex vertex : quad.vertices) {
                        Vec3f pos = vertex.pos;

                        float x1 = pos.getX() * NORM;
                        float y1 = pos.getY() * NORM;
                        float z1 = pos.getZ() * NORM;

                        float x2 = positionExt.transformVecX(x1, y1, z1);
                        float y2 = positionExt.transformVecY(x1, y1, z1);
                        float z2 = positionExt.transformVecZ(x1, y1, z1);

                        ModelVertex.write(ptr, x2, y2, z2, color, vertex.u, vertex.v, light, overlay, norm);

                        ptr += ModelVertex.STRIDE;
                    }

                    writer.push(buffer, 4, ModelVertex.STRIDE, ModelVertex.FORMAT);
                }
            }
        }
    }
}
