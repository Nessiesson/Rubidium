package me.jellysquid.mods.sodium.mixin.features.matrix_stack;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(MatrixStack.class)
public abstract class MixinMatrixStack {
    @Shadow
    @Final
    private Deque<MatrixStack.Entry> stack;

    private final Deque<MatrixStack.Entry> cache = new ArrayDeque<>();


    /**
     * @author JellySquid
     * @reason Re-use entries when possible
     */
    @Overwrite
    public void push() {
        var prev = this.stack.getLast();

        MatrixStack.Entry entry;

        if (!this.cache.isEmpty()) {
            entry = this.cache.removeLast();
            entry.getPositionMatrix()
                    .load(prev.getPositionMatrix());
            entry.getNormalMatrix()
                    .load(prev.getNormalMatrix());
        } else {
            entry = new MatrixStack.Entry(new Matrix4f(prev.getPositionMatrix()), new Matrix3f(prev.getNormalMatrix()));
        }

        this.stack.addLast(entry);
    }

    /**
     * @author JellySquid
     * @reason Re-use entries when possible
     */
    @Overwrite
    public void pop() {
        this.cache.addLast(this.stack.removeLast());
    }
}
