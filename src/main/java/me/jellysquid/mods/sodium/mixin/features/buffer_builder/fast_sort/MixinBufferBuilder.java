package me.jellysquid.mods.sodium.mixin.features.buffer_builder.fast_sort;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.util.GeometrySort;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder {
    @Shadow
    private ByteBuffer buffer;

    @Shadow
    private VertexFormat.DrawMode drawMode;

    @Shadow
    private int elementOffset;

    @Shadow
    private float sortingCameraX;

    @Shadow
    private float sortingCameraY;

    @Shadow
    private float sortingCameraZ;

    @Shadow
    @Nullable
    private Vec3f[] sortingPrimitiveCenters;

    @Shadow
    private int vertexCount;

    @Shadow
    private VertexFormat format;

    @Shadow
    private int batchOffset;

    /**
     * @author JellySquid
     * @reason Avoid slow memory accesses
     * TODO
     */
    @Overwrite
    private Vec3f[] buildPrimitiveCenters() {
        int vertexStride = this.format.getVertexSizeByte();
        int primitiveCount = this.vertexCount / 4;

        Vec3f[] centers = new Vec3f[primitiveCount];

        for (int index = 0; index < primitiveCount; ++index) {
            long v1 = MemoryUtil.memAddress(this.buffer, this.batchOffset + (((index * 4) + 0) * vertexStride));
            long v2 = MemoryUtil.memAddress(this.buffer, this.batchOffset + (((index * 4) + 2) * vertexStride));

            float x1 = MemoryUtil.memGetFloat(v1 + 0);
            float y1 = MemoryUtil.memGetFloat(v1 + 4);
            float z1 = MemoryUtil.memGetFloat(v1 + 8);

            float x2 = MemoryUtil.memGetFloat(v2 + 0);
            float y2 = MemoryUtil.memGetFloat(v2 + 4);
            float z2 = MemoryUtil.memGetFloat(v2 + 8);

            centers[index] = new Vec3f((x1 + x2) * 0.5F, (y1 + y2) * 0.5F, (z1 + z2) * 0.5F);
        }

        return centers;
    }

    /**
     * @author JellySquid
     * @reason Use direct memory access, avoid indirection
     */
    @Overwrite
    private void writeSortedIndices(VertexFormat.IndexType indexType) {
        float[] distance = new float[this.sortingPrimitiveCenters.length];
        int[] indices = new int[this.sortingPrimitiveCenters.length];

        this.calculatePrimitiveDistances(distance, indices);

        GeometrySort.mergeSort(indices, distance);

        this.writePrimitiveIndices(indexType, indices);
    }

    private void calculatePrimitiveDistances(float[] distance, int[] indices) {
        int i = 0;

        while (i < this.sortingPrimitiveCenters.length) {
            Vec3f pos = this.sortingPrimitiveCenters[i];

            if (pos == null) {
                throw new NullPointerException("Primitive center is null");
            }

            float x = pos.getX() - this.sortingCameraX;
            float y = pos.getY() - this.sortingCameraY;
            float z = pos.getZ() - this.sortingCameraZ;

            distance[i] = (x * x) + (y * y) + (z * z);
            indices[i] = i++;
        }
    }

    private static final int[] VERTEX_ORDER = new int[] { 0, 1, 2, 2, 3, 0 };

    private void writePrimitiveIndices(VertexFormat.IndexType indexType, int[] indices) {
        long ptr = MemoryUtil.memAddress(this.buffer, this.elementOffset);

        switch (indexType) {
            case BYTE -> {
                for (int index : indices) {
                    int start = index * 4;

                    for (int offset : VERTEX_ORDER) {
                        MemoryUtil.memPutByte(ptr, (byte) (start + offset));
                        ptr += Byte.BYTES;
                    }
                }
            }
            case SHORT -> {
                for (int index : indices) {
                    int start = index * 4;

                    for (int offset : VERTEX_ORDER) {
                        MemoryUtil.memPutShort(ptr, (short) (start + offset));
                        ptr += Short.BYTES;
                    }
                }
            }
            case INT -> {
                for (int index : indices) {
                    int start = index * 4;

                    for (int offset : VERTEX_ORDER) {
                        MemoryUtil.memPutInt(ptr, (start + offset));
                        ptr += Integer.BYTES;
                    }
                }
            }
        }
    }
}
