package me.jellysquid.mods.sodium.client.util.math;

import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

@SuppressWarnings("ConstantConditions")
public class MatrixUtil {
    public static Matrix4fExtended getExtendedMatrix(Matrix4f matrix) {
        return (Matrix4fExtended) (Object) matrix;
    }

    public static Matrix3fExtended getExtendedMatrix(Matrix3f matrix) {
        return (Matrix3fExtended) (Object) matrix;
    }
}
