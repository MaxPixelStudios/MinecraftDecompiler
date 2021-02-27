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
import cn.maxpixel.mcdecompiler.mapping.proguard.ProguardFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.proguard.ProguardMethodMapping;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ProguardMappingReader extends AbstractMappingReader {
    public ProguardMappingReader(BufferedReader reader) {
        super(reader);
    }
    public ProguardMappingReader(Reader rd) {
        super(rd);
    }
    public ProguardMappingReader(InputStream is) {
        super(is);
    }
    public ProguardMappingReader(String path) throws FileNotFoundException, NullPointerException {
        super(path);
    }

    private final ProguardMappingProcessor PROCESSOR = new ProguardMappingProcessor();
    @Override
    protected ProguardMappingProcessor getProcessor() {
        return PROCESSOR;
    }

    private static class ProguardMappingProcessor extends AbstractNonPackageMappingProcessor {
        private final ObjectArrayList<ClassMapping> mappings = new ObjectArrayList<>(5000);
        @Override
        ObjectList<ClassMapping> process(Stream<String> lines) {
            if(!mappings.isEmpty()) return mappings;
            AtomicReference<ClassMapping> currClass = new AtomicReference<>();
            lines.forEach(s -> {
                if(!s.startsWith("    ")) {
                    if(currClass.get() != null) {
                        mappings.add(currClass.getAndSet(processClass(s)));
                    } else currClass.set(processClass(s));
                } else {
                    if(s.contains("(") && s.contains(")")) currClass.get().addMethod(processMethod(s.trim()));
                    else currClass.get().addField(processField(s.trim()));
                }
            });
            if(currClass.get() != null) mappings.add(currClass.get()); // Add last mapping stored in the AtomicReference
            return mappings;
        }

        @Override
        protected ClassMapping processClass(String line) {
            String[] split = line.split("( -> )|:");
            return new ClassMapping(split[1], split[0]);
        }

        @Override
        protected ProguardMethodMapping processMethod(String line) {
            ProguardMethodMapping methodMapping = new ProguardMethodMapping();

            String[] linenums = line.split(":");
            String[] method;
            if(linenums.length == 3){
                method = linenums[2].split("( -> )| ");
                methodMapping.setLineNumberS(Integer.parseInt(linenums[0]));
                methodMapping.setLineNumberE(Integer.parseInt(linenums[1]));
            } else method = linenums[0].split("( -> )| ");
            methodMapping.setUnmappedName(method[2]);

            String[] original_args = method[1].split("\\("); // [0] is original name, [1] is args
            original_args[1] = original_args[1].substring(0, original_args[1].length() -1);
            methodMapping.setMappedName(original_args[0]);

            StringBuilder descriptor = new StringBuilder().append('(');
            for(String argTypeJN : original_args[1].split(",")) descriptor.append(NamingUtil.asDescriptor(argTypeJN)); // JN: Java Name
            descriptor.append(')').append(NamingUtil.asDescriptor(method[0]));
            methodMapping.setMappedDescriptor(descriptor.toString());

            return methodMapping;
        }

        @Override
        protected ProguardFieldMapping processField(String line) {
            String[] strings = line.split("( -> )| ");
            return new ProguardFieldMapping(strings[2], strings[1], NamingUtil.asDescriptor(strings[0]));
        }
    }
}