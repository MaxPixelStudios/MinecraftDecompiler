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
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;

public class TsrgMappingReader extends AbstractMappingReader {
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

    private final TsrgMappingProcessor PROCESSOR = new TsrgMappingProcessor();
    @Override
    public TsrgMappingProcessor getProcessor() {
        return PROCESSOR;
    }

    private class TsrgMappingProcessor extends PairedMappingProcessor implements PackageMappingProcessor {
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
                        switch(sa.length) {
                            case 2:
                                currClass.get().addField(processField(sa));
                                break;
                            case 3:
                                currClass.get().addMethod(processMethod(sa));
                                break;
                            default: throw new IllegalArgumentException("Is this a TSRG mapping file?");
                        }
                    }
                });
                if(currClass.get() != null) mappings.add(currClass.get()); // Add last mapping stored in the AtomicReference
            }
            return ObjectLists.unmodifiable(mappings);
        }

        @Override
        public PairedClassMapping processClass(String line) {
            String[] strings = line.split(" ");
            return new PairedClassMapping(NamingUtil.asJavaName(strings[0]), NamingUtil.asJavaName(strings[1]));
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
            return new PairedMapping(strings[0].substring(0, strings[0].length() - 1), strings[1]);
        }
    }
}