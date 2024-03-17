/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.remapper.variable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

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

    @NotNull RenameFunction forMethod(int access, String name, String descriptor, String signature, String[] exceptions);

    default @NotNull RenameAbstractFunction forAbstractMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return RenameAbstractFunction.NOP;
    }

    default boolean omitThis() {
        return false;
    }
}