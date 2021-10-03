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

package cn.maxpixel.mcdecompiler.writer;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.components.LineNumber;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ProguardMappingWriter extends AbstractMappingWriter {
    public ProguardMappingWriter() {
        super();
    }

    public ProguardMappingWriter(ClassifiedMappingRemapper remapper) {
        super(remapper);
    }

    private final ProguardMappingGenerator GENERATOR = new ProguardMappingGenerator();

    @Override
    protected ProguardMappingGenerator getGenerator() {
        return GENERATOR;
    }

    @Override
    protected boolean needLock() {
        return true;
    }

    private class ProguardMappingGenerator implements PairedMappingGenerator {
        @Override
        public String generateClass(PairedClassMapping mapping) {
            return NamingUtil.asJavaName(mapping.getMappedName()) + " -> " + NamingUtil.asJavaName(mapping.getUnmappedName()) + ':';
        }

        @Override
        public String generateMethod(PairedMethodMapping mapping) {
            if(notDescImpl(mapping)) throw new UnsupportedOperationException();
            String mappedDesc;
            if(mapping instanceof Descriptor.Mapped desc) mappedDesc = desc.getMappedDescriptor();
            else if(remapper != null) mappedDesc = remapper.getMappedDescByUnmappedDesc(mapping.asDescriptor().getUnmappedDescriptor());
            else throw new UnsupportedOperationException();
            StringBuilder builder = new StringBuilder("    ");
            if(mapping instanceof LineNumber ln)
                builder.append(ln.getStartLineNumber()).append(':').append(ln.getEndLineNumber()).append(':');
            return builder.append(Type.getType(mappedDesc.substring(mappedDesc.lastIndexOf(')') + 1)).getClassName())
                    .append(' ').append(mapping.getMappedName()).append('(')
                    .append(Arrays.stream(Type.getArgumentTypes(mappedDesc)).map(Type::getClassName).collect(Collectors.joining(",")))
                    .append(") -> ").append(mapping.getUnmappedName()).toString();
        }

        @Override
        public String generateField(PairedFieldMapping mapping) {
            if(notDescImpl(mapping)) throw new UnsupportedOperationException();
            String mappedDesc;
            if(mapping instanceof Descriptor.Mapped desc) mappedDesc = desc.getMappedDescriptor();
            else if(remapper != null) mappedDesc = remapper.mapToMapped(Type.getType(((Descriptor) mapping).getUnmappedDescriptor()));
            else throw new UnsupportedOperationException();
            return "    " + Type.getType(mappedDesc).getClassName() + ' ' + mapping.getMappedName() + " -> " + mapping.getUnmappedName();
        }
    }
}