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

package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.PackageMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseMethodMapping;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractMappingReader implements AutoCloseable {
    protected BufferedReader reader;
    private final List<ClassMapping> mappings;
    private final List<PackageMapping> packages;

    protected AbstractMappingReader(BufferedReader reader) {
        this.reader = reader;
        AbstractNonPackageMappingProcessor processor = getProcessor();
        mappings = processor.process(reader.lines().map(s -> {
            if(s.startsWith("#") || s.isEmpty() || s.replaceAll("\\s+", "").isEmpty()) return null;

            int index = s.indexOf('#');
            if(index > 0) return s.substring(0, index);
            else if(index == 0) return null;

            return s;
        }).filter(Objects::nonNull));
        packages = processor instanceof AbstractMappingProcessor ? ((AbstractMappingProcessor) processor).getPackages() : ObjectLists.emptyList();
    }
    protected AbstractMappingReader(Reader rd) {
        this(new BufferedReader(Objects.requireNonNull(rd)));
    }
    protected AbstractMappingReader(InputStream is) {
        this(new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8));
    }
    protected AbstractMappingReader(String path) throws FileNotFoundException, NullPointerException {
        this(new FileReader(Objects.requireNonNull(path)));
    }

    protected abstract AbstractNonPackageMappingProcessor getProcessor();
    public List<ClassMapping> getMappings() {
        return mappings;
    }
    public List<PackageMapping> getPackages() {
        return packages;
    }
    public Map<String, ClassMapping> getMappingsByUnmappedNameMap() {
        return getMappings().stream().collect(Collectors.toMap(ClassMapping::getUnmappedName, Function.identity(), (cm1, cm2) ->
        {throw new IllegalArgumentException("Key \"" + cm1 + "\" and \"" + cm2 + "\" duplicated!");}, Object2ObjectOpenHashMap::new));
    }
    public Map<String, ClassMapping> getMappingsByMappedNameMap() {
        return getMappings().stream().collect(Collectors.toMap(ClassMapping::getMappedName, Function.identity(), (cm1, cm2) ->
        {throw new IllegalArgumentException("Key \"" + cm1 + "\" and \"" + cm2 + "\" duplicated!");}, Object2ObjectOpenHashMap::new));
    }
    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        } finally {
            reader = null;
        }
    }
    protected abstract static class AbstractMappingProcessor extends AbstractNonPackageMappingProcessor {
        protected abstract List<PackageMapping> getPackages();
        protected abstract PackageMapping processPackage(String line);
    }
    protected abstract static class AbstractNonPackageMappingProcessor {
        public abstract List<ClassMapping> process(Stream<String> lines);
        protected abstract ClassMapping processClass(String line);
        protected abstract BaseMethodMapping processMethod(String line);
        protected abstract BaseFieldMapping processField(String line);
    }
}