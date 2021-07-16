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

import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.AbstractMapping;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;

public class SrgMappingWriter extends AbstractMappingWriter {
    public SrgMappingWriter() {
        super();
    }

    public SrgMappingWriter(MappingRemapper remapper) {
        super(remapper);
    }

    private final SrgMappingGenerator GENERATOR = new SrgMappingGenerator();

    @Override
    protected SrgMappingGenerator getGenerator() {
        return GENERATOR;
    }

    @Override
    protected boolean needLock() {
        return false;
    }

    private class SrgMappingGenerator implements PairedMappingGenerator, PackageMappingGenerator {
        @Override
        public String generateClass(PairedClassMapping mapping) {
            return "CL: " + mapping.getUnmappedName() + ' ' + mapping.getMappedName();
        }

        @Override
        public String generateMethod(PairedMethodMapping mapping) {
            if(notDescImpl(mapping)) throw new UnsupportedOperationException();
            String unmappedDesc = null;
            String mappedDesc = null;
            if(mapping instanceof Descriptor desc) unmappedDesc = desc.getUnmappedDescriptor();
            if(mapping instanceof Descriptor.Mapped desc) mappedDesc = desc.getMappedDescriptor();
            if(unmappedDesc == null) {
                if(remapper != null) unmappedDesc = remapper.getUnmappedDescByMappedDesc(mappedDesc);
                else throw new UnsupportedOperationException();
            } else if(mappedDesc == null) {
                if(remapper != null) mappedDesc = remapper.getMappedDescByUnmappedDesc(unmappedDesc);
                else throw new UnsupportedOperationException();
            }
            return "MD: " + mapping.getOwner().getUnmappedName() + '/' + mapping.getUnmappedName() + ' ' + unmappedDesc +
                    ' ' + mapping.getOwner().getMappedName() + '/' + mapping.getMappedName() + ' ' + mappedDesc;
        }

        @Override
        public String generateField(PairedFieldMapping mapping) {
            return "FD: " + mapping.getOwner().getUnmappedName() + '/' + mapping.getUnmappedName() + ' ' +
                    mapping.getOwner().getMappedName() + '/' + mapping.getMappedName();
        }

        @Override
        public String generatePackage(AbstractMapping mapping) {
            if(mapping instanceof PairedMapping paired && paired.getClass() == PairedMapping.class) {
                return "PK: " + paired.getUnmappedName() + ' ' + paired.getMappedName();
            } else throw new UnsupportedOperationException();
        }
    }
}