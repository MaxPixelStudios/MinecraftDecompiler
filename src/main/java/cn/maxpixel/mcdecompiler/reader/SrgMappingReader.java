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

import cn.maxpixel.mcdecompiler.mapping.paired.DescriptoredPairedMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMapping;
import it.unimi.dsi.fastutil.objects.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;

public class SrgMappingReader extends AbstractMappingReader {
    public SrgMappingReader(BufferedReader reader) {
        super(reader);
    }

    public SrgMappingReader(Reader rd) {
        super(rd);
    }

    public SrgMappingReader(InputStream is) {
        super(is);
    }

    public SrgMappingReader(String path) throws FileNotFoundException {
        super(path);
    }

    private final SrgMappingProcessor PROCESSOR = new SrgMappingProcessor();

    @Override
    public SrgMappingProcessor getProcessor() {
        return PROCESSOR;
    }

    private class SrgMappingProcessor implements PairedMappingProcessor, PackageMappingProcessor {
        private final ObjectArrayList<PairedMapping> packages = new ObjectArrayList<>();
        private ObjectList<PairedClassMapping> mappingCache;
        @Override
        public ObjectList<PairedClassMapping> process() {
            if(mappingCache != null && !mappingCache.isEmpty()) return mappingCache;
            Object2ObjectOpenHashMap<String, PairedClassMapping> mappings = new Object2ObjectOpenHashMap<>(); // k: unmapped name
            lines.parallelStream().map(String::trim).forEach(s -> {
                if(s.startsWith("CL:")) {
                    PairedClassMapping classMapping = processClass(s);
                    synchronized(mappings) {
                        mappings.putIfAbsent(classMapping.getUnmappedName(), classMapping);
                    }
                } else if(s.startsWith("FD:")) {
                    PairedFieldMapping fieldMapping = processField(s);
                    String unmClassName = fieldMapping.getOwner().getUnmappedName();
                    synchronized(mappings) {
                        mappings.computeIfAbsent(unmClassName, k -> new PairedClassMapping(unmClassName, fieldMapping.getOwner().getMappedName()))
                                .addField(fieldMapping);
                    }
                } else if(s.startsWith("MD:")) {
                    DescriptoredPairedMethodMapping methodMapping = processMethod(s);
                    String unmClassName = methodMapping.getOwner().getUnmappedName();
                    synchronized(mappings) {
                        mappings.computeIfAbsent(unmClassName, k -> new PairedClassMapping(unmClassName, methodMapping.getOwner().getMappedName()))
                                .addMethod(methodMapping);
                    }
                } else if(s.startsWith("PK:")) {
                    synchronized(packages) {
                        packages.add(processPackage(s));
                    }
                } else throw new IllegalArgumentException("Is this a SRG mapping file?");
            });
            mappingCache = new ObjectImmutableList<>(mappings.values());
            return mappingCache;
        }

        @Override
        public PairedClassMapping processClass(String line) {
            String[] strings = line.split(" ");
            return new PairedClassMapping(strings[1], strings[2]);
        }

        private String getClassName(String s) {
            return s.substring(0, s.lastIndexOf('/'));
        }

        private String getName(String s) {
            return s.substring(s.lastIndexOf('/') + 1);
        }

        @Override
        public DescriptoredPairedMethodMapping processMethod(String line) {
            String[] strings = line.split(" ");
            return new DescriptoredPairedMethodMapping(getName(strings[1]), getName(strings[3]), strings[2], strings[4])
                    .setOwner(new PairedClassMapping(getClassName(strings[1]), getClassName(strings[3])));
        }

        @Override
        public PairedFieldMapping processField(String line) {
            String[] strings = line.split(" ");
            return new PairedFieldMapping(getName(strings[1]), getName(strings[2]))
                    .setOwner(new PairedClassMapping(getClassName(strings[1]), getClassName(strings[2])));
        }

        @Override
        public ObjectList<PairedMapping> getPackages() {
            return ObjectLists.unmodifiable(packages);
        }

        @Override
        public PairedMapping processPackage(String line) {
            String[] strings = line.split(" ");
            return new PairedMapping(strings[1], strings[2]);
        }
    }
}