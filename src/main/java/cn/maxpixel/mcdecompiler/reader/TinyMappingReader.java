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

import cn.maxpixel.mcdecompiler.mapping.components.Documented;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.UnmappedDescriptoredNamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.tiny.TinyClassMapping;
import cn.maxpixel.mcdecompiler.mapping.tiny.TinyFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.tiny.TinyMethodMapping;
import it.unimi.dsi.fastutil.objects.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TinyMappingReader extends AbstractMappingReader {
    public final int version;

    {
        String s = lines.get(0);
        version = s.startsWith("tiny\t2\t0\t") ? 2 : s.startsWith("v1\t") ? 1 : -1;
    }

    public TinyMappingReader(BufferedReader reader) {
        super(reader);
    }

    public TinyMappingReader(Reader rd) {
        super(rd);
    }

    public TinyMappingReader(InputStream is) {
        super(is);
    }

    public TinyMappingReader(String path) throws FileNotFoundException, NullPointerException {
        super(path);
    }

    private final TinyV1MappingProcessor V1_PROCESSOR = new TinyV1MappingProcessor();
    private final TinyV2MappingProcessor V2_PROCESSOR = new TinyV2MappingProcessor();

    @Override
    public NamespacedMappingProcessor getProcessor() {
        return switch(version) {
            case 1 -> V1_PROCESSOR;
            case 2 -> V2_PROCESSOR;
            default -> throw new UnsupportedOperationException("Unknown tiny mapping version");
        };
    }

    private class TinyV1MappingProcessor implements NamespacedMappingProcessor {
        private String[] namespaces;
        private ObjectList<NamespacedClassMapping> mappingsCache;
        @Override
        public ObjectList<NamespacedClassMapping> process() {
            if(mappingsCache != null && !mappingsCache.isEmpty()) return mappingsCache;
            Object2ObjectOpenHashMap<String, NamespacedClassMapping> mappings = new Object2ObjectOpenHashMap<>(); // k: the first namespace, usually unmapped name
            namespaces = lines.remove(0).substring(3).split("\t");
            String k = namespaces[0];
            lines.parallelStream().forEach(s -> {
                if(s.startsWith("CLASS")) {
                    NamespacedClassMapping classMapping = processClass(s);
                    synchronized(mappings) {
                        mappings.merge(classMapping.getName(k), classMapping, (o, n) -> {
                            n.addField(o.getFields());
                            n.addMethod(o.getMethods());
                            return n;
                        });
                    }
                } else if(s.startsWith("FIELD")) {
                    NamespacedFieldMapping fieldMapping = processField(s);
                    synchronized(mappings) {
                        mappings.computeIfAbsent(fieldMapping.getOwner().getName(k), key -> new NamespacedClassMapping())
                                .addField(fieldMapping);
                    }
                } else if(s.startsWith("METHOD")) {
                    NamespacedMethodMapping methodMapping = processMethod(s);
                    synchronized(mappings) {
                        mappings.computeIfAbsent(methodMapping.getOwner().getName(k), key -> new NamespacedClassMapping())
                                .addMethod(methodMapping);
                    }
                } else throw new IllegalArgumentException("Is this a Tiny v1 mapping file?");
            });
            mappingsCache = new ObjectImmutableList<>(mappings.values());
            return mappingsCache;
        }

        @Override
        public String[] getNamespaces() {
            if(mappingsCache == null || mappingsCache.isEmpty()) process();
            return Arrays.copyOf(namespaces, namespaces.length);
        }

        @Override
        public NamespacedClassMapping processClass(String line) {
            return new NamespacedClassMapping(namespaces, line.substring(7).split("\t"));
        }

        @Override
        public NamespacedMethodMapping processMethod(String line) {
            String[] split = line.split("\t");
            return new NamespacedMethodMapping(namespaces, split, 3, split[2])
                    .setOwner(new NamespacedClassMapping(namespaces[0], split[1]));
        }

        @Override
        public UnmappedDescriptoredNamespacedFieldMapping processField(String line) {
            String[] split = line.split("\t");
            return new UnmappedDescriptoredNamespacedFieldMapping(namespaces, split, 3, split[2])
                    .setOwner(new NamespacedClassMapping(namespaces[0], split[1]));
        }
    }

    private class TinyV2MappingProcessor implements NamespacedMappingProcessor {
        private String[] namespaces;
        private final ObjectArrayList<TinyClassMapping> mappings = new ObjectArrayList<>(5000);
        @Override
        public ObjectList<TinyClassMapping> process() {
            if(mappings.isEmpty()) {
                AtomicReference<TinyClassMapping> currClass = new AtomicReference<>();
                AtomicReference<Documented> currSub = new AtomicReference<>();
                AtomicInteger currParam = new AtomicInteger();
                namespaces = lines.remove(0).substring(9).split("\t");
                lines.forEach(s -> {
                    if(s.charAt(0) == 'c') {
                        if(currClass.get() != null) mappings.add(currClass.getAndSet(processClass(s)));
                        else currClass.set(processClass(s));
                    } else if(s.charAt(0) == '\t') {
                        TinyClassMapping curr = currClass.get();
                        switch(s.charAt(1)) {
                            case 'c':
                                curr.setDoc(s.substring(3));
                                break;
                            case 'm':
                                TinyMethodMapping methodMapping = processMethod(s);
                                curr.addMethod(methodMapping);
                                currSub.set(methodMapping);
                                break;
                            case 'f':
                                TinyFieldMapping fieldMapping = processField(s);
                                curr.addField(fieldMapping);
                                currSub.set(fieldMapping);
                                break;
                            case '\t':
                                switch(s.charAt(2)) {
                                    case 'p':
                                        String[] split = s.substring(4).split("\t");
                                        currParam.set(Integer.parseInt(split[0]));
                                        ((TinyMethodMapping) currSub.get()).setLocalVariableName(currParam.get(), namespaces, split, 1);
                                        break;
                                    case 'c':
                                        currSub.get().setDoc(s.substring(4));
                                        break;
                                    case '\t':
                                        if(s.charAt(3) == 'c') {
                                            ((TinyMethodMapping) currSub.get()).setLocalVariableDoc(currParam.get(), s.substring(5));
                                            break;
                                        }
                                    default: error();
                                }
                                break;
                            default: error();
                        }
                    } else error();
                });
                mappings.add(currClass.get()); // Add the last mapping stored in the AtomicReference
            }
            return ObjectLists.unmodifiable(mappings);
        }

        private void error() {
            throw new IllegalArgumentException("Is this a Tiny v2 mapping file?");
        }

        @Override
        public String[] getNamespaces() {
            if(mappings.isEmpty()) process();
            return Arrays.copyOf(namespaces, namespaces.length);
        }

        @Override
        public TinyClassMapping processClass(String line) {
            return new TinyClassMapping(namespaces, line.substring(2).split("\t"));
        }

        @Override
        public TinyMethodMapping processMethod(String line) {
            String[] split = line.split("\t");
            return new TinyMethodMapping(namespaces, split, 3, split[2]);
        }

        @Override
        public TinyFieldMapping processField(String line) {
            String[] split = line.split("\t");
            return new TinyFieldMapping(namespaces, split, 3, split[2]);
        }
    }
}