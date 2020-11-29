/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2020  MaxPixelStudios
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
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public SrgMappingReader(String path) throws FileNotFoundException, NullPointerException {
        super(path);
    }
    private static final SrgMappingProcessor PROCESSOR = new SrgMappingProcessor();
    @Override
    protected AbstractNonPackageMappingProcessor getProcessor() {
        return PROCESSOR;
    }
    private static class SrgMappingProcessor extends AbstractMappingProcessor {
        private final ObjectArrayList<PackageMapping> packages = new ObjectArrayList<>();
        @Override
        public List<ClassMapping> process(Stream<String> lines) {
            final Object2ObjectOpenHashMap<String, ClassMapping> mappings = new Object2ObjectOpenHashMap<>();
            lines.filter(s -> s.startsWith("CL:") || s.startsWith("PK:")).forEach(s -> {
                s = s.trim();
                switch(s.substring(0, 3)) {
                    case "CL:":
                        ClassMapping classMapping = processClass(s);
                        mappings.put(classMapping.getUnmappedName(), classMapping);
                        break;
                    case "PK:":
                        packages.add(processPackage(s));
                        break;
                    default:
                        throw new RuntimeException("Shouldn't run here");
                }
            });
            lines.filter(s -> !(s.startsWith("CL:") || s.startsWith("PK:"))).forEach(s -> {
                s = s.trim();
                switch(s.substring(0, 3)) {
                    case "FD:":
                        BaseFieldMapping fieldMapping = processField(s);
                        String obfClassName = fieldMapping.getUnmappedName().substring(0, fieldMapping.getUnmappedName().lastIndexOf('/'));
                        ClassMapping cm = mappings.get(obfClassName);
                        if(cm == null) {
                            cm = new ClassMapping(obfClassName,
                                    fieldMapping.getMappedName().substring(0, fieldMapping.getMappedName().lastIndexOf('/'))// original class name
                            );
                            mappings.put(obfClassName, cm);
                        }
                        cm.addField(new BaseFieldMapping(fieldMapping.getUnmappedName().substring(fieldMapping.getUnmappedName().lastIndexOf('/') + 1),
                                fieldMapping.getMappedName().substring(fieldMapping.getMappedName().lastIndexOf('/') + 1)));
                        break;
                    case "MD:":
                        BaseMethodMapping methodMapping = processMethod(s);
                        obfClassName = methodMapping.getUnmappedName().substring(0, methodMapping.getUnmappedName().lastIndexOf('/'));
                        cm = mappings.get(obfClassName);
                        if(cm == null) {
                            cm = new ClassMapping(obfClassName,
                                    methodMapping.getMappedName().substring(0, methodMapping.getMappedName().lastIndexOf('/'))// original class name
                            );
                            mappings.put(obfClassName, cm);
                        }
                        cm.addMethod(new BaseMethodMapping(methodMapping.getUnmappedName().
                                substring(methodMapping.getUnmappedName().lastIndexOf('/') + 1),
                                methodMapping.getMappedName().substring(methodMapping.getMappedName().lastIndexOf('/') + 1),
                                methodMapping.getObfuscatedDescriptor(), methodMapping.getOriginalDescriptor()));
                        break;
                }
            });
            return mappings.values().parallelStream().collect(Collectors.toCollection(ObjectArrayList::new));
        }
        @Override
        protected ClassMapping processClass(String line) {
            String[] strings = line.split(" ");
            return new ClassMapping(NamingUtil.asJavaName(strings[1]), NamingUtil.asJavaName(strings[2]));
        }
        @Override
        protected BaseMethodMapping processMethod(String line) {
            String[] strings = line.split(" ");
            return new BaseMethodMapping(strings[1], strings[3], strings[2], strings[4]);
        }
        @Override
        protected BaseFieldMapping processField(String line) {
            String[] strings = line.split(" ");
            return new BaseFieldMapping(NamingUtil.asJavaName(strings[1]), NamingUtil.asJavaName(strings[2]));
        }
        @Override
        protected List<PackageMapping> getPackages() {
            return packages;
        }
        @Override
        protected PackageMapping processPackage(String line) {
            String[] strings = line.split(" ");
            return new PackageMapping(NamingUtil.asJavaName(strings[1]), NamingUtil.asJavaName(strings[2]));
        }
    }
}