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

import cn.maxpixel.mcdecompiler.mapping.AbstractClassMapping;
import cn.maxpixel.mcdecompiler.mapping.AbstractMapping;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class CsrgMappingWriter extends AbstractMappingWriter {
    @Override
    public void writeMappings(ObjectList<? extends AbstractClassMapping> mappings) {
        mappings.parallelStream().map(PairedClassMapping.class::cast).forEach(pcm -> {
            synchronized(buf) {
                buf.add(getGenerator().generateClass(pcm));
            }
            pcm.getFields().parallelStream().map(getGenerator()::generateField).forEach(field -> {
                synchronized(buf) {
                    buf.add(field);
                }
            });
            pcm.getMethods().parallelStream().map(getGenerator()::generateMethod).forEach(method -> {
                synchronized(buf) {
                    buf.add(method);
                }
            });
        });
    }

    private static final CsrgMappingGenerator GENERATOR = new CsrgMappingGenerator();
    @Override
    public CsrgMappingGenerator getGenerator() {
        return GENERATOR;
    }

    private static class CsrgMappingGenerator implements PairedMappingGenerator, PackageMappingGenerator {
        @Override
        public String generateClass(PairedClassMapping mapping) {
            return mapping.getUnmappedName() + ' ' + mapping.getMappedName();
        }

        @Override
        public String generateMethod(PairedMethodMapping mapping) {
            if(!(mapping instanceof Descriptor desc) || mapping instanceof Descriptor.Mapped) throw new UnsupportedOperationException();
            return mapping.getOwner().getUnmappedName() + ' ' + mapping.getUnmappedName() + ' ' +
                    desc.getUnmappedDescriptor() + ' ' + mapping.getMappedName();
        }

        @Override
        public String generateField(PairedFieldMapping mapping) {
            return mapping.getOwner().getUnmappedName() + ' ' + mapping.getUnmappedName() + ' ' + mapping.getMappedName();
        }

        @Override
        public String generatePackage(AbstractMapping mapping) {
            if(mapping instanceof PairedMapping paired && paired.getClass() == PairedMapping.class) {
                return paired.getUnmappedName() + '/' + ' ' + paired.getMappedName();
            } else throw new UnsupportedOperationException();
        }
    }
}