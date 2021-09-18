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

import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.UnmappedDescriptoredPairedMethodMapping;
import it.unimi.dsi.fastutil.objects.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Function;

public class CsrgMappingReader extends AbstractMappingReader {
    public CsrgMappingReader(BufferedReader reader) {
        super(reader);
    }

    public CsrgMappingReader(Reader rd) {
        super(rd);
    }

    public CsrgMappingReader(InputStream is) {
        super(is);
    }

    public CsrgMappingReader(String path) throws FileNotFoundException {
        super(path);
    }

    private final CsrgMappingProcessor PROCESSOR = new CsrgMappingProcessor();

    @Override
    public CsrgMappingProcessor getProcessor() {
        return PROCESSOR;
    }

    private class CsrgMappingProcessor implements PairedMappingProcessor, PackageMappingProcessor {
        private final ObjectArrayList<PairedMapping> packages = new ObjectArrayList<>();
        private ObjectList<PairedClassMapping> mappingsCache;
        @Override
        public ObjectList<PairedClassMapping> process() {
            if(mappingsCache != null && !mappingsCache.isEmpty()) return mappingsCache;
            Object2ObjectOpenHashMap<String, PairedClassMapping> mappings = new Object2ObjectOpenHashMap<>(); // k: unmapped name
            lines.parallelStream().map(String::trim).forEach(s -> {
                String[] sa = s.split(" ");
                switch(sa.length) {
                    case 2: // Class / Package
                        if(sa[0].endsWith("/")) synchronized(packages) {
                            packages.add(processPackage(sa));
                        } else {
                            PairedClassMapping classMapping = processClass(sa);
                            synchronized(mappings) {
                                mappings.merge(classMapping.getUnmappedName(), classMapping, (o, n) -> {
                                    n.addField(o.getFieldMap().values());
                                    n.addMethod(o.getMethods());
                                    return n;
                                });
                            }
                        }
                        break;
                    case 3: // Field
                        PairedFieldMapping fieldMapping = processField(sa);
                        synchronized(mappings) {
                            mappings.computeIfAbsent(fieldMapping.getOwner().getUnmappedName(),
                                    (Function<String, PairedClassMapping>) PairedClassMapping::new).addField(fieldMapping);
                        }
                        break;
                    case 4: // Method
                        UnmappedDescriptoredPairedMethodMapping methodMapping = processMethod(sa);
                        synchronized(mappings) {
                            mappings.computeIfAbsent(methodMapping.getOwner().getUnmappedName(),
                                    (Function<String, PairedClassMapping>) PairedClassMapping::new).addMethod(methodMapping);
                        }
                        break;
                    default: throw new IllegalArgumentException("Is this a CSRG mapping file?");
                }
            });
            mappingsCache = new ObjectImmutableList<>(mappings.values());
            return mappingsCache;
        }

        @Override
        public PairedClassMapping processClass(String line) {
            return processClass(line.split(" "));
        }

        private PairedClassMapping processClass(String[] line) {
            return new PairedClassMapping(line[0], line[1]);
        }

        @Override
        public UnmappedDescriptoredPairedMethodMapping processMethod(String line) {
            return processMethod(line.split(" "));
        }

        private UnmappedDescriptoredPairedMethodMapping processMethod(String[] line) {
            return new UnmappedDescriptoredPairedMethodMapping(line[1], line[3], line[2])
                    .setOwner(new PairedClassMapping(line[0]));
        }

        @Override
        public PairedFieldMapping processField(String line) {
            return processField(line.split(" "));
        }

        private PairedFieldMapping processField(String[] line) {
            return new PairedFieldMapping(line[1], line[2]).setOwner(new PairedClassMapping(line[0]));
        }

        @Override
        public ObjectList<PairedMapping> getPackages() {
            return ObjectLists.unmodifiable(packages);
        }

        @Override
        public PairedMapping processPackage(String line) {
            return processPackage(line.split(" "));
        }

        private PairedMapping processPackage(String[] line) {
            return new PairedMapping(line[0].substring(0, line[0].length() - 1), line[1].substring(0, line[1].length() - 1));
        }
    }
}