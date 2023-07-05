package cn.maxpixel.mcdecompiler.asm.variable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

@ApiStatus.Experimental
public interface VariableNameProvider {
    @FunctionalInterface
    interface RenameFunction {
        RenameFunction NOP = (originalName, descriptor, signature, start, end, index) -> null;

        @Nullable String getName(String originalName, String descriptor, String signature, Label start, Label end, int index);
    }

    @FunctionalInterface
    interface RenameAbstractFunction {
        RenameAbstractFunction NOP = (index, type) -> null;

        @Nullable String getName(int index, Type type);
    }

    VariableNameProvider NOP = (access, name, descriptor, signature, exceptions) -> RenameFunction.NOP;

    @NotNull RenameFunction forMethod(int access, String name, String descriptor, String signature, String[] exceptions);

    default @NotNull RenameAbstractFunction forAbstractMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return RenameAbstractFunction.NOP;
    }

    default boolean omitThis() {
        return false;
    }
}