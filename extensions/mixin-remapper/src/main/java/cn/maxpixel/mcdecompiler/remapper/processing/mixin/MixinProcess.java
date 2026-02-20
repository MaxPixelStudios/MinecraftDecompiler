/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2026 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.remapper.processing.mixin;

import cn.maxpixel.mcdecompiler.remapper.DeobfuscationOptions;
import cn.maxpixel.mcdecompiler.remapper.processing.ClassFileRemapper;
import cn.maxpixel.mcdecompiler.remapper.processing.ExtraClassesInformation;
import cn.maxpixel.mcdecompiler.remapper.processing.Process;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.IOException;
import java.nio.file.Path;

public class MixinProcess implements Process {
    @Override
    public @NotNull String getName() {
        return "mixin";
    }

    @Override
    public void beforeCollectingExtraClassesInformation(Path input) throws IOException {
        Process.super.beforeCollectingExtraClassesInformation(input);
    }

    @Override
    public @Nullable ClassVisitor getExtraClassesInformationVisitor(ExtraClassesInformation eci, ClassReader reader, ClassVisitor parent) {
        return Process.super.getExtraClassesInformationVisitor(eci, reader, parent);
    }

    @Override
    public @NotNull ClassVisitor getVisitor(DeobfuscationOptions options, ClassReader reader, ClassFileRemapper cfr, ClassVisitor parent) {
        return null;
    }
}
