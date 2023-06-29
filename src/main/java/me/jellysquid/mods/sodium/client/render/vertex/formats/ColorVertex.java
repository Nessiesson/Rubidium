package me.jellysquid.mods.sodium.client.render.vertex.formats;

import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatRegistry;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import me.jellysquid.mods.sodium.client.util.math.Matrix4fExtended;
import me.jellysquid.mods.sodium.client.util.math.MatrixUtil;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public final class ColorVertex {
	public static final VertexFormatDescription FORMAT = VertexFormatRegistry.get(VertexFormats.POSITION_COLOR);

	public static final int STRIDE = 16;

	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_COLOR = 12;

	public static void write(long ptr, Matrix4f matrix, float x, float y, float z, int color) {
		Matrix4fExtended ext = MatrixUtil.getExtendedMatrix(matrix);
		float x2 = ext.transformVecX(x, y, z);
		float y2 = ext.transformVecY(x, y, z);
		float z2 = ext.transformVecZ(x, y, z);

		write(ptr, x2, y2, z2, color);
	}

	public static void write(long ptr, float x, float y, float z, int color) {
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 0, x);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 4, y);
		MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 8, z);

		MemoryUtil.memPutInt(ptr + OFFSET_COLOR + 0, color);
	}
}
