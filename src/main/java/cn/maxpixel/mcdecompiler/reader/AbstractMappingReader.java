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

import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.AbstractClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractMappingReader {
    protected ObjectArrayList<String> lines;
    private ObjectList<? extends AbstractClassMapping> mappings;
    private ObjectList<PairedMapping> packages;

    public AbstractMappingReader(BufferedReader reader) {
        try(BufferedReader ignored = reader) {
            lines = reader.lines().map(s -> {
                if(s.startsWith("#") || s.isEmpty() || s.replaceAll("\\s+", "").isEmpty()) return null;

                int index = s.indexOf('#');
                if(index > 0) return s.substring(0, index);
                else if(index == 0) return null;

                return s;
            }).filter(Objects::nonNull).collect(Collectors.toCollection(ObjectArrayList::new));
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }
    public AbstractMappingReader(Reader rd) {
        this(new BufferedReader(Objects.requireNonNull(rd)));
    }
    public AbstractMappingReader(InputStream is) {
        this(new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8));
    }
    public AbstractMappingReader(String path) throws FileNotFoundException {
        this(new FileReader(Objects.requireNonNull(path)));
    }

    public abstract MappingProcessor getProcessor();

    private void read() {
        MappingProcessor processor = getProcessor();
        mappings = processor.isPaired() ? processor.asPaired().process() : processor.asNamespaced().process();
        packages = processor instanceof PackageMappingProcessor ? ((PackageMappingProcessor) processor).getPackages() : ObjectLists.emptyList();
    }

    public final ObjectList<? extends AbstractClassMapping> getMappings() {
        if(mappings == null) read();
        return mappings;
    }

    public final ObjectList<PairedMapping> getPackages() {
        if(packages == null) read();
        return packages;
    }

    public final AbstractMappingReader reverse() {
        if(getProcessor().isNamespaced()) throw new UnsupportedOperationException();
        if(mappings == null) read();
        MappingRemapper remapper = new MappingRemapper(this);
        mappings.forEach(cm -> cm.asPaired().reverse(remapper));
        getPackages().forEach(PairedMapping::reverse);
        return this;
    }

    public final Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> getMappingsByUnmappedNameMap() {
        if(getProcessor().isNamespaced()) throw new UnsupportedOperationException();
        return getMappings().stream().map(AbstractClassMapping::asPaired).collect(Collectors.toMap(PairedClassMapping::getUnmappedName,
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public final Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> getMappingsByMappedNameMap() {
        if(getProcessor().isNamespaced()) throw new UnsupportedOperationException();
        return getMappings().stream().map(AbstractClassMapping::asPaired).collect(Collectors.toMap(PairedClassMapping::getMappedName,
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public final Object2ObjectOpenHashMap<String, ? extends NamespacedClassMapping> getMappingsByNamespaceMap(String namespace) {
        if(getProcessor().isPaired()) throw new UnsupportedOperationException();
        return getMappings().stream().map(AbstractClassMapping::asNamespaced).collect(Collectors.toMap(m -> m.getName(namespace),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public final Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> getMappingsByNamespaceMap(String namespace, String fromNamespace, String toNamespace) {
        if(getProcessor().isPaired()) throw new UnsupportedOperationException();
        return getMappings().stream().map(AbstractClassMapping::asNamespaced).collect(Collectors.toMap(m -> m.getName(namespace),
                m -> m.getAsPaired(fromNamespace, toNamespace), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public abstract static class MappingProcessor {
        public boolean isPaired() {
            return this instanceof PairedMappingProcessor;
        }
        public boolean isNamespaced() {
            return this instanceof NamespacedMappingProcessor;
        }

        public PairedMappingProcessor asPaired() {
            return (PairedMappingProcessor) this;
        }
        public NamespacedMappingProcessor asNamespaced() {
            return (NamespacedMappingProcessor) this;
        }
    }

    public abstract static class PairedMappingProcessor extends MappingProcessor {
        public abstract ObjectList<? extends PairedClassMapping> process();
        public abstract PairedClassMapping processClass(String line);
        public abstract PairedMethodMapping processMethod(String line);
        public abstract PairedFieldMapping processField(String line);
    }

    public abstract static class NamespacedMappingProcessor extends MappingProcessor {
        public abstract ObjectList<? extends NamespacedClassMapping> process();
        public abstract String[] getNamespaces();
        public abstract NamespacedClassMapping processClass(String line);
        public abstract NamespacedMethodMapping processMethod(String line);
        public abstract NamespacedFieldMapping processField(String line);
    }

    public interface PackageMappingProcessor {
        ObjectList<PairedMapping> getPackages();
        PairedMapping processPackage(String line);
    }
}