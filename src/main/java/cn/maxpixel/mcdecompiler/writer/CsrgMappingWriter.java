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

public class CsrgMappingWriter extends AbstractMappingWriter {
    public CsrgMappingWriter() {
        super();
    }

    public CsrgMappingWriter(MappingRemapper remapper) {
        super(remapper);
    }

    private final CsrgMappingGenerator GENERATOR = new CsrgMappingGenerator();
    @Override
    protected CsrgMappingGenerator getGenerator() {
        return GENERATOR;
    }

    @Override
    protected boolean needLock() {
        return false;
    }

    private class CsrgMappingGenerator implements PairedMappingGenerator, PackageMappingGenerator {
        @Override
        public String generateClass(PairedClassMapping mapping) {
            return mapping.getUnmappedName() + ' ' + mapping.getMappedName();
        }

        @Override
        public String generateMethod(PairedMethodMapping mapping) {
            if(notDescImpl(mapping)) throw new UnsupportedOperationException();
            String unmappedDesc;
            if(mapping instanceof Descriptor desc) unmappedDesc = desc.getUnmappedDescriptor();
            else if(remapper != null) unmappedDesc = remapper.getUnmappedDescByMappedDesc(mapping.asMappedDescriptor().getMappedDescriptor());
            else throw new UnsupportedOperationException();
            return mapping.getOwner().getUnmappedName() + ' ' + mapping.getUnmappedName() + ' ' +
                    unmappedDesc + ' ' + mapping.getMappedName();
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