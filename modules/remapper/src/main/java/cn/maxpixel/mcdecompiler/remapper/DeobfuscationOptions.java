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

package cn.maxpixel.mcdecompiler.remapper;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class DeobfuscationOptions {
    public static final DeobfuscationOptions DEFAULT = new DeobfuscationOptions();

    public final boolean includeOthers;
    public final boolean rvn;
    public final boolean reverse;
    public final Set<Path> extraJars;
    public final Set<String> extraClasses;
    public final Map<String, Map<String, String>> refMap;

    public DeobfuscationOptions() {
        this(true, false, false);
    }

    public DeobfuscationOptions(boolean includeOthers, boolean rvn, boolean reverse) {
        this(includeOthers, rvn, reverse, ObjectSets.emptySet(), ObjectSets.emptySet(), Object2ObjectMaps.emptyMap());
    }

    public DeobfuscationOptions(boolean includeOthers, boolean rvn, boolean reverse, Set<Path> extraJars,
                                Set<String> extraClasses, Map<String, Map<String, String>> refMap) {
        this.includeOthers = includeOthers;
        this.rvn = rvn;
        this.reverse = reverse;
        this.extraJars = extraJars;
        this.extraClasses = extraClasses;
        this.refMap = refMap;
    }
}