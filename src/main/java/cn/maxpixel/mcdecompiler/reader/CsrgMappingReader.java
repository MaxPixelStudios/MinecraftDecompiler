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
import cn.maxpixel.mcdecompiler.mapping.base.DescriptoredBaseMethodMapping;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public CsrgMappingReader(String path) throws FileNotFoundException, NullPointerException {
        super(path);
    }
    private final CsrgMappingProcessor PROCESSOR = new CsrgMappingProcessor();
    @Override
    protected CsrgMappingProcessor getProcessor() {
        return PROCESSOR;
    }
    private static class CsrgMappingProcessor extends AbstractMappingProcessor {
        private final ObjectArrayList<PackageMapping> packages = new ObjectArrayList<>();
        private ObjectList<ClassMapping> mappingsCache;
        @Override
        ObjectList<ClassMapping> process(Stream<String> lines) {
            if(mappingsCache != null && !mappingsCache.isEmpty()) return mappingsCache;
            Object2ObjectOpenHashMap<String, ClassMapping> mappings = new Object2ObjectOpenHashMap<>(); // k: unmapped name
            lines.map(String::trim).forEach(s -> {
                String[] sa = s.split(" ");
                switch(sa.length) {
                    case 2: // Class / Package
                        if(sa[0].endsWith("/")) packages.add(processPackage(s));
                        else {
                            ClassMapping classMapping = processClass(s);
                            mappings.merge(classMapping.getUnmappedName(), classMapping, (o, n) -> {
                                n.addField(o.getFieldMap().values());
                                n.addMethod(o.getMethods());
                                return n;
                            });
                        }
                        break;
                    case 3: // Field
                        BaseFieldMapping fieldMapping = processField(s);
                        ClassMapping cm = mappings.computeIfAbsent(fieldMapping.getOwner().getUnmappedName(), ClassMapping::new);
                        cm.addField(fieldMapping.setOwner(cm));
                        break;
                    case 4: // Method
                        DescriptoredBaseMethodMapping methodMapping = processMethod(s);
                        cm = mappings.computeIfAbsent(methodMapping.getOwner().getUnmappedName(), ClassMapping::new);
                        cm.addMethod(methodMapping.setOwner(cm));
                        break;
                    default: throw new IllegalArgumentException("Is this a CSRG mapping file?");
                }
            });
            mappingsCache = mappings.values().parallelStream().collect(Collectors.toCollection(ObjectArrayList::new));
            return mappingsCache;
        }
        @Override
        protected ClassMapping processClass(String line) {
            String[] strings = line.split(" ");
            return new ClassMapping(NamingUtil.asJavaName(strings[0]), NamingUtil.asJavaName(strings[1]));
        }
        @Override
        protected DescriptoredBaseMethodMapping processMethod(String line) {
            String[] strings = line.split(" ");
            return new DescriptoredBaseMethodMapping(strings[1], strings[3], strings[2]).setOwner(new ClassMapping(NamingUtil.asJavaName(strings[0])));
        }
        @Override
        protected BaseFieldMapping processField(String line) {
            String[] strings = line.split(" ");
            return new BaseFieldMapping(strings[1], strings[2]).setOwner(new ClassMapping(NamingUtil.asJavaName(strings[0])));
        }
        @Override
        public ObjectList<PackageMapping> getPackages() {
            return packages;
        }
        @Override
        protected PackageMapping processPackage(String line) {
            String[] strings = line.split(" ");
            return new PackageMapping(strings[0].substring(0, strings[0].length() - 1), strings[1]);
        }
    }
}