package me.jellysquid.mods.sodium.mixin.core.pipeline.vertex;

import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
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
    public void push(long ptr, int count, int stride, VertexFormatDescription format) {
        this.writeVerticesSlow(ptr, count, stride, format);
    }

    @Override
    public long buffer(MemoryStack stack, int count, int stride, VertexFormatDescription format) {
        return VertexBufferWriter.of(this.vertexConsumer)
                .buffer(stack, count, stride, format);
    }

    private void writeVerticesSlow(long ptr, int count, int stride, VertexFormatDescription format) {
        var offsetPosition = format.getOffset(VertexFormats.POSITION_ELEMENT);
        var offsetNormal = format.getOffset(VertexFormats.NORMAL_ELEMENT);
        var offsetOverlay = format.getOffset(VertexFormats.OVERLAY_ELEMENT);
        var offsetLight = format.getOffset(VertexFormats.LIGHT_ELEMENT);

        for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
            float positionX = MemoryUtil.memGetFloat(ptr + offsetPosition + 0);
            float positionY = MemoryUtil.memGetFloat(ptr + offsetPosition + 4);
            float positionZ = MemoryUtil.memGetFloat(ptr + offsetPosition + 8);

            int overlay = MemoryUtil.memGetInt(ptr + offsetOverlay);
            int light = MemoryUtil.memGetInt(ptr + offsetLight);
            int normal = MemoryUtil.memGetInt(ptr + offsetNormal);

            float normalX = Norm3b.unpackX(normal);
            float normalY = Norm3b.unpackY(normal);
            float normalZ = Norm3b.unpackZ(normal);

            Vec3f normalCoord = new Vec3f(normalX, normalY, normalZ);
            normalCoord.transform(this.normalMatrix);
            Direction direction = Direction.getFacing(normalCoord.getX(), normalCoord.getY(), normalCoord.getZ());

            Vector4f textureCoord = new Vector4f(positionX, positionY, positionZ, 1.0F);
            textureCoord.transform(this.textureMatrix);
            textureCoord.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
            textureCoord.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0F));
            textureCoord.rotate(direction.getRotationQuaternion());

            float textureU = -textureCoord.getX();
            float textureV = -textureCoord.getY();

            this.vertexConsumer.vertex(positionX, positionY, positionZ)
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .texture(textureU, textureV)
                    .overlay(overlay)
                    .light(light)
                    .normal(normalX, normalY, normalZ)
                    .next();

            ptr += stride;
        }
    }
}
