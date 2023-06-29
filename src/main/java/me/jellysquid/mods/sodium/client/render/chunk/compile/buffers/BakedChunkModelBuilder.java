package me.jellysquid.mods.sodium.client.render.chunk.compile.buffers;

import me.jellysquid.mods.sodium.client.model.IndexBufferBuilder;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import net.minecraft.client.texture.Sprite;

public class BakedChunkModelBuilder implements ChunkModelBuilder {
    private final ChunkVertexBufferBuilder vertexBuffer;
    private final IndexBufferBuilder[] indexBuffers;

    private final ChunkRenderData.Builder renderData;
    private final int id;

    public BakedChunkModelBuilder(ChunkVertexBufferBuilder vertexBuffer, IndexBufferBuilder[] indexBuffers,
                                  ChunkRenderData.Builder renderData,
                                  int chunkId) {
        this.indexBuffers = indexBuffers;
        this.vertexBuffer = vertexBuffer;

        this.renderData = renderData;
        this.id = chunkId;
    }

    @Override
    public ChunkVertexBufferBuilder getVertexBuffer() {
        return this.vertexBuffer;
    }

    @Override
    public IndexBufferBuilder getIndexBuffer(ModelQuadFacing facing) {
        return this.indexBuffers[facing.ordinal()];
    }

    @Override
    public void addSprite(Sprite sprite) {
        this.renderData.addSprite(sprite);
    }

    @Override
    public int getChunkId() {
        return this.id;
    }
}
