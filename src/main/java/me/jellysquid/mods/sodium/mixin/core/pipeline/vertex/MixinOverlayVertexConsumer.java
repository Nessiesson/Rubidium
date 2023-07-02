package me.jellysquid.mods.sodium.mixin.core.pipeline.vertex;

import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.render.vertex.transform.VertexTransform;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OverlayVertexConsumer.class)
public class MixinOverlayVertexConsumer implements VertexBufferWriter {
    @Shadow
    @Final
    private VertexConsumer vertexConsumer;

    @Shadow
    @Final
    private Matrix3f normalMatrix;

    @Shadow
    @Final
    private Matrix4f textureMatrix;

    @Override
    public void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
        VertexTransform.transformOverlay(ptr, count, format,
                this.normalMatrix, this.textureMatrix);

        VertexBufferWriter.of(this.vertexConsumer)
                .push(stack, ptr, count, format);
    }
}
