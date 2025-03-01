package me.jellysquid.mods.sodium.client.render.vertex.transform;

import me.jellysquid.mods.sodium.client.render.vertex.VertexBufferWriter;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import me.jellysquid.mods.sodium.common.util.UnsafeUtil;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class VertexTransform {
    private static final Quaternion sodium$rotate_180 = Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f);
    private static final Quaternion sodium$rotate_neg90 = Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0f);
    /**
     * Transforms the texture UVs for each vertex from their absolute coordinates into the sprite area specified
     * by the parameters.
     *
     * @param ptr    The buffer of vertices to transform
     * @param count  The number of vertices to transform
     * @param format The format of the vertices
     * @param minU   The minimum X-coordinate of the sprite bounds
     * @param minV   The minimum Y-coordinate of the sprite bounds
     * @param maxU   The maximum X-coordinate of the sprite bounds
     * @param maxV   The maximum Y-coordinate of the sprite bounds
     */
    public static void transformSprite(long ptr, int count, VertexFormatDescription format,
                                       float minU, float minV, float maxU, float maxV) {
        long stride = format.stride;
        long offsetUV = format.getOffset(CommonVertexElement.TEXTURE);

        // The width/height of the sprite
        float w = maxU - minU;
        float h = maxV - minV;

        for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
            // The coordinate relative to the sprite bounds
            float u = MemoryUtil.memGetFloat(ptr + offsetUV + 0);
            float v = MemoryUtil.memGetFloat(ptr + offsetUV + 4);

            // The coordinate absolute to the sprite sheet
            float ut = minU + (w * u);
            float vt = minV + (h * v);

            MemoryUtil.memPutFloat(ptr + offsetUV + 0, ut);
            MemoryUtil.memPutFloat(ptr + offsetUV + 4, vt);

            ptr += stride;
        }
    }

    /**
     * Transforms the color element of each vertex to use the specified value.
     *
     * @param ptr    The buffer of vertices to transform
     * @param count  The number of vertices to transform
     * @param format The format of the vertices
     * @param color  The packed color to use for transforming the vertices
     */
    public static void transformColor(long ptr, int count, VertexFormatDescription format,
                                      int color) {
        var offsetColor = format.getOffset(CommonVertexElement.COLOR);

        for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
            MemoryUtil.memPutInt(ptr + offsetColor, color);
            ptr += format.stride;
        }
    }

    /**
     * Transforms the overlay UVs element of each vertex to create a perspective-mapped effect.
     *
     * @param ptr    The buffer of vertices to transform
     * @param count  The number of vertices to transform
     * @param format The format of the vertices
     * @param inverseNormalMatrix The inverted normal matrix
     * @param inverseTextureMatrix The inverted texture matrix
     */
    public static void transformOverlay(long ptr, int count, VertexFormatDescription format,
                                        Matrix3f inverseNormalMatrix, Matrix4f inverseTextureMatrix) {
        var offsetPosition = format.getOffset(CommonVertexElement.POSITION);
        var offsetColor = format.getOffset(CommonVertexElement.COLOR);
        var offsetNormal = format.getOffset(CommonVertexElement.NORMAL);
        var offsetTexture = format.getOffset(CommonVertexElement.TEXTURE);

        int color = ColorABGR.pack(1.0f, 1.0f, 1.0f, 1.0f);

        var normal = new Vec3f();
        var position = new Vector4f();

        for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
            position.setX(MemoryUtil.memGetFloat(ptr + offsetPosition + 0));
            position.setY(MemoryUtil.memGetFloat(ptr + offsetPosition + 4));
            position.setZ(MemoryUtil.memGetFloat(ptr + offsetPosition + 8));
            position.setW(1.0f);

            int packedNormal = MemoryUtil.memGetInt(ptr + offsetNormal);
            normal.setX(Norm3b.unpackX(packedNormal));
            normal.setY(Norm3b.unpackY(packedNormal));
            normal.setZ(Norm3b.unpackZ(packedNormal));

            normal.transform(inverseNormalMatrix);
            Direction direction = Direction.getFacing(normal.getX(), normal.getY(), normal.getZ());

            position.transform(inverseTextureMatrix);
            position.rotate(sodium$rotate_180);
            position.rotate(sodium$rotate_neg90);
            position.rotate(direction.getRotationQuaternion());

            float textureU = -position.getX();
            float textureV = -position.getY();

            MemoryUtil.memPutInt(ptr + offsetColor, color);

            MemoryUtil.memPutFloat(ptr + offsetTexture + 0, textureU);
            MemoryUtil.memPutFloat(ptr + offsetTexture + 4, textureV);

            ptr += format.stride;
        }
    }

    /**
     * Creates a copy of the source data and pushes it into the specified {@param consumer}. This is useful for when
     * you need to use to re-use the source data after the call, and do not want the {@param consumer} to modify
     * the original data.
     *
     * @param consumer The vertex consumer to push the vertices into
     * @param stack    The memory stack which can be used to create temporary allocations
     * @param ptr      The pointer to read vertices from
     * @param count    The number of vertices to push
     * @param format   The format of the vertices
     */
    public static void copyPush(VertexConsumer consumer, MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
        var length = count * format.stride;
        var copy = stack.nmalloc(length);

        UnsafeUtil.copyMemory(ptr, copy, length);

        VertexBufferWriter.of(consumer)
                .push(stack, copy, count, format);
    }
}
