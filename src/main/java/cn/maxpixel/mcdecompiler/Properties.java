/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Properties {
    public static class Key<V> {
        private final String name;
        private Key(String name) {
            this.name = Objects.requireNonNull(name);
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            @SuppressWarnings("rawtypes") Key key = (Key) o;
            return name.equals(key.name);
        }
        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        public static final Key<Path> TEMP_DIR = new Key<>("tempDir");
        public static final Key<Path> DOWNLOAD_DIR = new Key<>("downloadDir");
        public static final Key<Path> INPUT_JAR = new Key<>("inputJar");
        public static final Key<String> MAPPING_PATH = new Key<>("mappingPath");

        public static final Key<Path> OUTPUT_DIR = new Key<>("outputDir");
        public static final Key<String> OUTPUT_DEOBFUSCATED_NAME = new Key<>("outputDeobfuscatedName");
        public static final Key<String> OUTPUT_DECOMPILED_NAME = new Key<>("outputDecompiledName");
    }
    private static final Object2ObjectOpenHashMap<Key<?>, Object> PROPERTIES_MAP = new Object2ObjectOpenHashMap<>();
    @SuppressWarnings("unchecked")
    public static <V> V put(Key<V> key, V value) {
        return (V) PROPERTIES_MAP.put(key, value);
    }
    @SuppressWarnings("unchecked")
    public static <V> V get(Key<V> key) {
        return (V) PROPERTIES_MAP.get(key);
    }
    static {
        // Default values
        put(Key.TEMP_DIR, Paths.get("temp"));
        put(Key.DOWNLOAD_DIR, Paths.get("downloads"));
        put(Key.OUTPUT_DIR, Paths.get("output"));
        put(Key.OUTPUT_DEOBFUSCATED_NAME, "deobfuscated");
        put(Key.OUTPUT_DECOMPILED_NAME, "decompiled");
    }

    // Methods have to do with Key.TEMP_DIR
    public static Path getTempUnmappedClassesPath() {
        return get(Key.TEMP_DIR).resolve("unmappedClasses");
    }
    public static Path getTempMappedClassesPath() {
        return get(Key.TEMP_DIR).resolve("mappedClasses");
    }
    public static Path getTempDecompileClassesPath() {
        return get(Key.TEMP_DIR).resolve("decompileClasses");
    }
    public static Path getTempDecompilerPath() {
        return get(Key.TEMP_DIR).resolve("decompiler.jar");
    }

    // Methods have to do with Key.DOWNLOAD_DIR
    public static Path getDownloadedLibPath() {
        return get(Key.DOWNLOAD_DIR).resolve("libs");
    }

    // Methods have to do with Key.OUTPUT_*
    public static Path getOutputDecompiledDirectory() {
        return get(Key.OUTPUT_DIR).resolve(get(Key.OUTPUT_DECOMPILED_NAME));
    }
    public static Path getOutputDeobfuscatedJarPath() {
        return get(Key.OUTPUT_DIR).resolve(get(Key.OUTPUT_DEOBFUSCATED_NAME) + ".jar");
    }




    // Proguard only -- start
    public static Path getDownloadedProguardMappingPath(String version, Info.SideType type) {
        return get(Key.DOWNLOAD_DIR).resolve(version).resolve(type + "_mappings.txt");
    }
    public static Path getDownloadedMcJarPath(String version, Info.SideType type) {
        return get(Key.DOWNLOAD_DIR).resolve(version).resolve(type + ".jar");
    }
    public static Path getOutputDecompiledDirectory(String version, Info.SideType type) {
        if(version == null || type == null) return getOutputDecompiledDirectory();
        return get(Key.OUTPUT_DIR).resolve(version + "_" + type + "_decompiled");
    }
    public static Path getOutputDeobfuscatedJarPath(String version, Info.SideType type) {
        if(version == null || type == null) return getOutputDeobfuscatedJarPath();
        return get(Key.OUTPUT_DIR).resolve(version + "_" + type + "_deobfuscated.jar");
    }
    // Proguard only -- end
}