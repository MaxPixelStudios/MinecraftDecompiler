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
import cn.maxpixel.mcdecompiler.mapping.srg.SrgMethodMapping;
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
    protected SrgMappingProcessor getProcessor() {
        return PROCESSOR;
    }
    private static class SrgMappingProcessor extends AbstractMappingProcessor {
        private final ObjectArrayList<PackageMapping> packages = new ObjectArrayList<>();
        @Override
        public List<ClassMapping> process(Stream<String> lines) {
            Object2ObjectOpenHashMap<String, ClassMapping> mappings = new Object2ObjectOpenHashMap<>(); // k: unmapped name
            lines.map(String::trim).forEach(s -> {
                switch(s.substring(0, 3)) {
                    case "CL:":
                        ClassMapping classMapping = processClass(s);
                        mappings.putIfAbsent(classMapping.getUnmappedName(), classMapping);
                        break;
                    case "PK:":
                        packages.add(processPackage(s));
                        break;
                    case "FD:":
                        BaseFieldMapping fieldMapping = processField(s);
                        String unmClassName = fieldMapping.getOwner().getUnmappedName();
                        ClassMapping cm = mappings.computeIfAbsent(unmClassName, k -> new ClassMapping(unmClassName, fieldMapping.getOwner().getMappedName()));
                        cm.addField(fieldMapping.setOwner(cm));
                        break;
                    case "MD:":
                        SrgMethodMapping methodMapping = processMethod(s);
                        unmClassName = methodMapping.getOwner().getUnmappedName();
                        cm = mappings.computeIfAbsent(unmClassName, k -> new ClassMapping(unmClassName, methodMapping.getOwner().getMappedName()));
                        cm.addMethod(methodMapping.setOwner(cm));
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
        private String getClassName(String s) {
            return NamingUtil.asJavaName(s.substring(0, s.lastIndexOf('/')));
        }
        private String getName(String s) {
            return s.substring(s.lastIndexOf('/') + 1);
        }
        @Override
        protected SrgMethodMapping processMethod(String line) {
            String[] strings = line.split(" ");
            return new SrgMethodMapping(getName(strings[1]), getName(strings[3]), strings[2], strings[4])
                    .setOwner(new ClassMapping(getClassName(strings[1]), getClassName(strings[3])));
        }
        @Override
        protected BaseFieldMapping processField(String line) {
            String[] strings = line.split(" ");
            return new BaseFieldMapping(getName(strings[1]), getName(strings[2]))
                    .setOwner(new ClassMapping(getClassName(strings[1]), getClassName(strings[2])));
        }
        @Override
        protected List<PackageMapping> getPackages() {
            return packages;
        }
        @Override
        protected PackageMapping processPackage(String line) {
            String[] strings = line.split(" ");
            return new PackageMapping(strings[1], strings[2]);
        }
    }
}