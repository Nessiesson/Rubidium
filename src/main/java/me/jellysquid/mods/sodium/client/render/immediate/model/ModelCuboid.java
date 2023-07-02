package me.jellysquid.mods.sodium.client.render.immediate.model;

import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.client.util.math.Matrix3fExtended;
import me.jellysquid.mods.sodium.client.util.math.Matrix4fExtended;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

public class ModelCuboid {
    public final Quad[] quads;

    private final Vec3f[] vertices;
    private final Vec3f[] shared;

    public ModelCuboid(int u, int v, float x1, float y1, float z1, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight) {
        float x2 = x1 + sizeX;
        float y2 = y1 + sizeY;
        float z2 = z1 + sizeZ;

        x1 -= extraX;
        y1 -= extraY;
        z1 -= extraZ;

        x2 += extraX;
        y2 += extraY;
        z2 += extraZ;

        if (mirror) {
            float i = x2;
            x2 = x1;
            x1 = i;
        }

        Vec3f[] vertices = new Vec3f[8];
        vertices[0] = new Vec3f(x1, y1, z1);
        vertices[1] = new Vec3f(x2, y1, z1);
        vertices[2] = new Vec3f(x2, y2, z1);
        vertices[3] = new Vec3f(x1, y2, z1);
        vertices[4] = new Vec3f(x1, y1, z2);
        vertices[5] = new Vec3f(x2, y1, z2);
        vertices[6] = new Vec3f(x2, y2, z2);
        vertices[7] = new Vec3f(x1, y2, z2);

        Vec3f[] shared = new Vec3f[8];

        for (int i = 0; i < 8; i++) {
            vertices[i].scale(1.0f/16.0f);
            shared[i] = new Vec3f(Float.NaN, Float.NaN, Float.NaN);
        }

        float u0 = (float) u;
        float u1 = (float) u + sizeZ;
        float u2 = (float) u + sizeZ + sizeX;
        float u3 = (float) u + sizeZ + sizeX + sizeX;
        float u4 = (float) u + sizeZ + sizeX + sizeZ;
        float u5 = (float) u + sizeZ + sizeX + sizeZ + sizeX;

        float v0 = (float) v;
        float v1 = (float) v + sizeZ;
        float v2 = (float) v + sizeZ + sizeY;

        var sides = new Quad[6];
        sides[2] = new Quad(new Vec3f[] { shared[5], shared[4], shared[0], shared[1] }, u1, v0, u2, v1, textureWidth, textureHeight, mirror, Direction.DOWN);
        sides[3] = new Quad(new Vec3f[] { shared[2], shared[3], shared[7], shared[6] }, u2, v1, u3, v0, textureWidth, textureHeight, mirror, Direction.UP);
        sides[1] = new Quad(new Vec3f[] { shared[0], shared[4], shared[7], shared[3] }, u0, v1, u1, v2, textureWidth, textureHeight, mirror, Direction.WEST);
        sides[4] = new Quad(new Vec3f[] { shared[1], shared[0], shared[3], shared[2] }, u1, v1, u2, v2, textureWidth, textureHeight, mirror, Direction.NORTH);
        sides[0] = new Quad(new Vec3f[] { shared[5], shared[1], shared[2], shared[6] }, u2, v1, u4, v2, textureWidth, textureHeight, mirror, Direction.EAST);
        sides[5] = new Quad(new Vec3f[] { shared[4], shared[5], shared[6], shared[7] }, u4, v1, u5, v2, textureWidth, textureHeight, mirror, Direction.SOUTH);

        this.quads = sides;

        this.vertices = vertices;
        this.shared = shared;
    }

    public void updateVertices(Matrix4fExtended mat) {
        for (int i = 0; i < 8; i++) {
            var src = this.vertices[i];
            var dst = this.shared[i];


            dst.setX(mat.transformVecX(src.getX(), src.getY(), src.getZ()));
            dst.setY(mat.transformVecY(src.getX(), src.getY(), src.getZ()));
            dst.setZ(mat.transformVecZ(src.getX(), src.getY(), src.getZ()));
        }
    }

    public static class Quad {
        public final Vec3f[] positions;
        public final Vector2f[] textures;


        public final Vec3f direction;

        public Quad(Vec3f[] positions, float u1, float v1, float u2, float v2, float textureWidth, float textureHeight, boolean flip, Direction direction) {
            var textures = new Vector2f[4];
            textures[0] = new Vector2f(u2 / textureWidth, v1 / textureHeight);
            textures[1] = new Vector2f(u1 / textureWidth, v1 / textureHeight);
            textures[2] = new Vector2f(u1 / textureWidth, v2 / textureHeight);
            textures[3] = new Vector2f(u2 / textureWidth, v2 / textureHeight);

            if (flip) {
                int len = positions.length;

                for (int i = 0; i < len / 2; ++i) {
                    var pos = positions[i];
                    positions[i] = positions[len - 1 - i];
                    positions[len - 1 - i] = pos;

                    var tex = textures[i];
                    textures[i] = textures[len - 1 - i];
                    textures[len - 1 - i] = tex;
                }
            }

            this.positions = positions;
            this.textures = textures;

            this.direction = direction.getUnitVector();

            if (flip) {
                this.direction.multiplyComponentwise(-1.0F, 1.0F, 1.0F);
            }
        }

        public int getNormal(Matrix3fExtended mat) {
            Vec3f dir = this.direction;

            float normX = mat.transformVecX(dir);
            float normY = mat.transformVecY(dir);
            float normZ = mat.transformVecZ(dir);

            return Norm3b.pack(normX, normY, normZ);
        }
    }
}
