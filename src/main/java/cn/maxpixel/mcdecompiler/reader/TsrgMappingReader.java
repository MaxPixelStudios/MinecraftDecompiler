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

import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.UnmappedDescriptoredNamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.UnmappedDescriptoredPairedMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.tsrg.TsrgMethodMapping;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class TsrgMappingReader extends AbstractMappingReader {
    public final int version = lines.get(0).startsWith("tsrg2") ? 2 : 1;

    public TsrgMappingReader(BufferedReader reader) {
        super(reader);
    }

    public TsrgMappingReader(Reader rd) {
        super(rd);
    }

    public TsrgMappingReader(InputStream is) {
        super(is);
    }

    public TsrgMappingReader(String path) throws FileNotFoundException, NullPointerException {
        super(path);
    }

    private final TsrgV1MappingProcessor V1_PROCESSOR = new TsrgV1MappingProcessor();
    private final TsrgV2MappingProcessor V2_PROCESSOR = new TsrgV2MappingProcessor();

    @Override
    public MappingProcessor getProcessor() {
        return switch(version) {
            case 1 -> V1_PROCESSOR;
            case 2 -> V2_PROCESSOR;
            default -> throw new UnsupportedOperationException("Unknown tsrg mapping version");
        };
    }

    private class TsrgV1MappingProcessor implements PairedMappingProcessor, PackageMappingProcessor {
        private final ObjectArrayList<PairedMapping> packages = new ObjectArrayList<>();
        private final ObjectArrayList<PairedClassMapping> mappings = new ObjectArrayList<>(5000);
        @Override
        public ObjectList<PairedClassMapping> process() {
            if(mappings.isEmpty()) {
                AtomicReference<PairedClassMapping> currClass = new AtomicReference<>();
                lines.forEach(s -> {
                    if(s.charAt(0) != '\t') {
                        if(currClass.get() != null) mappings.add(currClass.getAndSet(processClass(s)));
                        else currClass.set(processClass(s));
                    } else {
                        String[] sa = s.substring(1).split(" ");
                        switch (sa.length) {
                            case 2 -> currClass.get().addField(processField(sa));
                            case 3 -> currClass.get().addMethod(processMethod(sa));
                            default -> throw new IllegalArgumentException("Is this a TSRG mapping file?");
                        }
                    }
                });
                if(currClass.get() != null) mappings.add(currClass.get()); // Add the last mapping stored in the AtomicReference
            }
            return ObjectLists.unmodifiable(mappings);
        }

        @Override
        public PairedClassMapping processClass(String line) {
            String[] strings = line.split(" ");
            return new PairedClassMapping(strings[0], strings[1]);
        }

        private UnmappedDescriptoredPairedMethodMapping processMethod(String[] line) {
            return new UnmappedDescriptoredPairedMethodMapping(line[0], line[2], line[1]);
        }

        @Override
        public UnmappedDescriptoredPairedMethodMapping processMethod(String line) {
            return processMethod(line.trim().split(" "));
        }

        private PairedFieldMapping processField(String[] line) {
            return new PairedFieldMapping(line[0], line[1]);
        }

        @Override
        public PairedFieldMapping processField(String line) {
            return processField(line.trim().split(" "));
        }

        @Override
        public ObjectList<PairedMapping> getPackages() {
            return packages;
        }

        @Override
        public PairedMapping processPackage(String line) {
            String[] strings = line.split(" ");
            return new PairedMapping(strings[0].substring(0, strings[0].length() - 1), strings[1].substring(0, strings[1].length() - 1));
        }
    }

    private class TsrgV2MappingProcessor implements NamespacedMappingProcessor, PackageMappingProcessor {
        private String[] namespaces;
        private final ObjectArrayList<NamespacedMapping> packages = new ObjectArrayList<>();
        private final ObjectArrayList<NamespacedClassMapping> mappings = new ObjectArrayList<>(5000);
        @Override
        public ObjectList<NamespacedClassMapping> process() {
            if(mappings.isEmpty()) {
                AtomicReference<NamespacedClassMapping> currClass = new AtomicReference<>();
                AtomicReference<TsrgMethodMapping> currMethod = new AtomicReference<>();
                namespaces = lines.remove(0).substring(6).split(" ");
                lines.forEach(s -> {
                    if(s.charAt(0) != '\t') {
                        if(s.charAt(s.length() - 1) == '/') packages.add(processPackage(s));
                        else if(currClass.get() != null) mappings.add(currClass.getAndSet(processClass(s)));
                        else currClass.set(processClass(s));
                    } else {
                        String[] sa = s.substring(1).split(" ");
                        NamespacedClassMapping curr = currClass.get();
                        switch(sa.length - namespaces.length) {
                            case 0:
                                curr.addField(processField(sa));
                                break;
                            case 1:
                                if(s.charAt(1) == '\t') {
                                    currMethod.get().setLocalVariableName(Integer.parseInt(sa[0].substring(1)), namespaces, sa, 1);
                                } else if(sa[1].charAt(0) == '(') {
                                    TsrgMethodMapping methodMapping = processMethod(sa);
                                    curr.addMethod(methodMapping);
                                    currMethod.set(methodMapping);
                                } else curr.addField(processField(sa));
                                break;
                            default:
                                if(sa[0].equals("\tstatic")) currMethod.get().isStatic = true;
                                else error();
                        }
                    }
                });
                mappings.add(currClass.get()); // Add the last mapping stored in the AtomicReference
            }
            return ObjectLists.unmodifiable(mappings);
        }

        private <T> T error() {
            throw new IllegalArgumentException("Is this a Tsrg v2 mapping file?");
        }

        @Override
        public String[] getNamespaces() {
            if(mappings.isEmpty()) process();
            return Arrays.copyOf(namespaces, namespaces.length);
        }

        @Override
        public NamespacedClassMapping processClass(String line) {
            return new NamespacedClassMapping(namespaces, line.split(" "));
        }

        @Override
        public TsrgMethodMapping processMethod(String line) {
            return processMethod(line.substring(1).split(" "));
        }

        private TsrgMethodMapping processMethod(String[] line) {
            TsrgMethodMapping mapping = new TsrgMethodMapping(namespaces[0], line[0], line[1]);
            for(int i = 1; i < namespaces.length; i++) mapping.setName(namespaces[i], line[i + 1]);
            return mapping;
        }

        @Override
        public NamespacedFieldMapping processField(String line) {
            return processField(line.substring(1).split(" "));
        }

        private NamespacedFieldMapping processField(String[] line) {
            if(line.length == namespaces.length) return new NamespacedFieldMapping(namespaces, line);
            else if(line.length == namespaces.length + 1) {
                UnmappedDescriptoredNamespacedFieldMapping mapping = new UnmappedDescriptoredNamespacedFieldMapping(namespaces[0], line[0], line[1]);
                for(int i = 1; i < namespaces.length; i++) mapping.setName(namespaces[i], line[i + 1]);
                return mapping;
            } else return error();
        }

        @Override
        public ObjectList<NamespacedMapping> getPackages() {
            return packages;
        }

        @Override
        public NamespacedMapping processPackage(String line) {
            String[] split = line.split(" ");
            for(int i = 0; i < split.length; i++) {
                split[i] = split[i].substring(0, split[i].length() - 1);
            }
            return new NamespacedMapping(namespaces, split);
        }
    }
}