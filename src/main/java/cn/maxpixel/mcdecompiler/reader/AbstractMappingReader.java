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
import cn.maxpixel.mcdecompiler.mapping.AbstractMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;
import cn.maxpixel.mcdecompiler.util.IOUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractMappingReader {
    protected static final Logger LOGGER = LogManager.getLogger("Mapping Reader");
    protected ObjectArrayList<String> lines;
    private ObjectList<? extends AbstractClassMapping> mappings;
    private ObjectList<? extends AbstractMapping> packages;

    public AbstractMappingReader(BufferedReader reader) {
        try(reader) {
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
        this(IOUtil.asBufferedReader(rd));
    }

    public AbstractMappingReader(InputStream is) {
        this(new InputStreamReader(is, StandardCharsets.UTF_8));
    }
    
    public AbstractMappingReader(String path) throws FileNotFoundException {
        this(new FileInputStream(path));
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

    public final ObjectList<? extends AbstractMapping> getPackages() {
        if(packages == null) read();
        return packages;
    }

    public final AbstractMappingReader reverse() {
        if(getProcessor().isNamespaced()) throw new UnsupportedOperationException();
        if(mappings == null) read();
        MappingRemapper remapper = new MappingRemapper(this);
        mappings.forEach(cm -> cm.asPaired().reverse(remapper));
        packages.forEach(mapping -> mapping.asPairedMapping().reverse());
        return this;
    }

    public final AbstractMappingReader reverse(String targetNamespace) {
        if(getProcessor().isPaired()) throw new UnsupportedOperationException();
        if(mappings == null) read();
        MappingRemapper remapper = new MappingRemapper(this);
        String unmNamespace = getProcessor().asNamespaced().getNamespaces()[0];

        ObjectArrayList<PairedClassMapping> newMappings = new ObjectArrayList<>();
        mappings.forEach(cm -> {
            PairedClassMapping reversed = cm.asNamespaced().getAsPaired(unmNamespace, targetNamespace);
            reversed.reverse(remapper);
            newMappings.add(reversed);
        });
        this.mappings = ObjectLists.unmodifiable(newMappings);

        ObjectArrayList<PairedMapping> newPackages = new ObjectArrayList<>();
        packages.forEach(mapping -> {
            NamespacedMapping nm = mapping.asNamespacedMapping();
            newPackages.add(new PairedMapping(nm.getName(targetNamespace), nm.getName(unmNamespace)));
        });
        this.packages = ObjectLists.unmodifiable(newPackages);
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

    public final Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> getMappingsByNamespaceMap(String keyNamespace, String targetNamespace) {
        if(getProcessor().isPaired()) throw new UnsupportedOperationException();
        return getMappings().stream().map(AbstractClassMapping::asNamespaced).collect(Collectors.toMap(m -> m.getName(keyNamespace),
                m -> m.getAsPaired(getProcessor().asNamespaced().getNamespaces()[0], targetNamespace),
                Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public interface MappingProcessor {
        default boolean isPaired() {
            return this instanceof PairedMappingProcessor;
        }

        default boolean isNamespaced() {
            return this instanceof NamespacedMappingProcessor;
        }

        default PairedMappingProcessor asPaired() {
            return (PairedMappingProcessor) this;
        }

        default NamespacedMappingProcessor asNamespaced() {
            return (NamespacedMappingProcessor) this;
        }
    }

    public interface PairedMappingProcessor extends MappingProcessor {
        ObjectList<? extends PairedClassMapping> process();

        PairedClassMapping processClass(String line);

        PairedMethodMapping processMethod(String line);

        PairedFieldMapping processField(String line);
    }

    public interface NamespacedMappingProcessor extends MappingProcessor {
        ObjectList<? extends NamespacedClassMapping> process();

        String[] getNamespaces();

        NamespacedClassMapping processClass(String line);

        NamespacedMethodMapping processMethod(String line);

        NamespacedFieldMapping processField(String line);
    }

    public interface PackageMappingProcessor {
        ObjectList<? extends AbstractMapping> getPackages();

        AbstractMapping processPackage(String line);
    }
}