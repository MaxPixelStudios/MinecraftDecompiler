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

package cn.maxpixel.mcdecompiler.remapper.processing;

import cn.maxpixel.mcdecompiler.remapper.DeobfuscationOptions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.IOException;
import java.util.function.Function;

public interface Process {
    enum Run {
        /**
         * Run before the class is remapped
         */
        BEFORE,
        /**
         * Run after the class is remapped
         */
        AFTER
    }

    String getName();// may have uses in the future, or may be removed

    default void beforeRunning(DeobfuscationOptions options, ClassFileRemapper mappingRemapper) throws IOException {
    }

    default void afterRunning(DeobfuscationOptions options, ClassFileRemapper mappingRemapper) throws IOException {
    }

    Function<ClassVisitor, ClassVisitor> getVisitor(DeobfuscationOptions options, ClassReader reader, ClassFileRemapper cfr);
}