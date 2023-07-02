package me.jellysquid.mods.sodium.mixin.features.buffer_builder.intrinsics;

import me.jellysquid.mods.sodium.client.render.RenderGlobal;
import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.formats.ModelVertex;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import me.jellysquid.mods.sodium.client.util.math.MatrixUtil;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    private static final int SHADOW_COLOR = ColorABGR.pack(1.0f, 1.0f, 1.0f);
    private static final Vec3f SHADOW_NORMAL = new Vec3f(0.0f, 1.0f, 0.0f);

    /**
     * @author JellySquid
     * @reason Use intrinsics
     */
    @Overwrite
    private static void drawShadowVertex(MatrixStack.Entry entry, VertexConsumer vertices, float alpha, float x, float y, float z, float u, float v) {
        drawOptimizedShadowVertex(entry, vertices, alpha, x, y, z, u, v);
    }

    private static void drawOptimizedShadowVertex(MatrixStack.Entry entry, VertexConsumer vertices, float alpha, float x, float y, float z, float u, float v) {
        var writer = VertexBufferWriter.of(vertices);

        var matNormal = MatrixUtil.getExtendedMatrix(entry.getNormalMatrix());
        var matPosition = MatrixUtil.getExtendedMatrix(entry.getPositionMatrix());

        float nx = SHADOW_NORMAL.getX();
        float ny = SHADOW_NORMAL.getY();
        float nz = SHADOW_NORMAL.getZ();

        // The transformed normal vector
        float nxt = matNormal.transformVecX(nx, ny, nz);
        float nyt = matNormal.transformVecY(nx, ny, nz);
        float nzt = matNormal.transformVecZ(nx, ny, nz);

        int norm = Norm3b.pack(nxt, nyt, nzt);

        try (MemoryStack stack = RenderGlobal.VERTEX_DATA.push()) {
            long buffer = stack.nmalloc(1 * ModelVertex.STRIDE);

            // The transformed position vector
            float xt = matPosition.transformVecX(x, y, z);
            float yt = matPosition.transformVecY(x, y, z);
            float zt = matPosition.transformVecZ(x, y, z);

            ModelVertex.write(buffer, xt, yt, zt, ColorABGR.withAlpha(SHADOW_COLOR, alpha), u, v, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, norm);

            writer.push(stack, buffer, 1, ModelVertex.FORMAT);
        }
    }
}
