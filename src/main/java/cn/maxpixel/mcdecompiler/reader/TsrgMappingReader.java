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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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
    private static final TsrgMappingProcessor PROCESSOR = new TsrgMappingProcessor();
    @Override
    protected TsrgMappingProcessor getProcessor() {
        return PROCESSOR;
    }
    private static class TsrgMappingProcessor extends AbstractMappingProcessor {
        private final ObjectArrayList<PackageMapping> packages = new ObjectArrayList<>();
        @Override
        public ObjectList<ClassMapping> process(Stream<String> lines) {
            ObjectArrayList<ClassMapping> mappings = new ObjectArrayList<>(5000);
            AtomicReference<ClassMapping> currClass = new AtomicReference<>();
            lines.forEach(s -> {
                if(!s.startsWith("\t")) {
                    if(currClass.get() != null) mappings.add(currClass.getAndSet(processClass(s)));
                    else currClass.set(processClass(s));
                } else {
                    ClassMapping curr = currClass.get();
                    int len = s.split(" ").length;
                    if(len == 3) curr.addMethod(processMethod(s.trim()).setOwner(curr));
                    else if(len == 2) curr.addField(processField(s.trim()).setOwner(curr));
                    else throw new IllegalArgumentException("Is this a TSRG mapping file?");
                }
            });
            if(currClass.get() != null) mappings.add(currClass.get()); // Add last mapping stored in the AtomicReference
            return mappings;
        }
        @Override
        protected ClassMapping processClass(String line) {
            String[] strings = line.split(" ");
            return new ClassMapping(NamingUtil.asJavaName(strings[0]), NamingUtil.asJavaName(strings[1]));
        }
        @Override
        protected DescriptoredBaseMethodMapping processMethod(String line) {
            String[] strings = line.split(" ");
            return new DescriptoredBaseMethodMapping(strings[0], strings[2], strings[1]);
        }
        @Override
        protected BaseFieldMapping processField(String line) {
            String[] strings = line.split(" ");
            return new BaseFieldMapping(strings[0], strings[1]);
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