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

package cn.maxpixel.mcdecompiler.decompiler;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public record DecompilationOptions(String decompilerName, @Nullable String version, @Nullable ObjectSet<Path> bundledLibs,
                                   @Nullable Path incrementalInput, @Nullable Path incrementalOutput) {
    public static final DecompilationOptions DEFAULT = new DecompilationOptions();

    public DecompilationOptions() {
        this("vineflower");
    }

    public DecompilationOptions(String decompilerName) {
        this(decompilerName, null, null, null, null);
    }

    public DecompilationOptions(String decompilerName, String version) {
        this(decompilerName, version, null, null, null);
    }

    public DecompilationOptions(String decompilerName, ObjectSet<Path> bundledLibs) {
        this(decompilerName, null, bundledLibs, null, null);
    }

    public DecompilationOptions(String decompilerName, String version, ObjectSet<Path> bundledLibs) {
        this(decompilerName, version, bundledLibs, null, null);
    }
}