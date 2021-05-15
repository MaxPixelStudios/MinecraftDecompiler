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

package cn.maxpixel.mcdecompiler.deobfuscator;

import cn.maxpixel.mcdecompiler.asm.LVTRenamer;
import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.reader.TinyMappingReader;

import java.nio.file.Path;

public class TinyDeobfuscator extends AbstractDeobfuscator {
    public TinyDeobfuscator(String mappingPath) {
        super(mappingPath);
    }
    @Override
    public TinyDeobfuscator deobfuscate(Path source, Path target, boolean includeOthers, boolean reverse) {
        if(reverse) throw new UnsupportedOperationException();
        try {
            TinyMappingReader mappingReader = new TinyMappingReader(mappingPath);
            String[] namespaces = mappingReader.getProcessor().getNamespaces();
            sharedDeobfuscate(source, target, mappingReader, includeOthers, false, parent -> mappingReader.version == 2 ?
                    new LVTRenamer(parent, mappingReader.getMappingsByNamespaceMap(namespaces[0]), namespaces[0], namespaces[namespaces.length - 1]) : parent,
                    (reader, superClassMapping) -> new MappingRemapper(reader, superClassMapping, namespaces[0], namespaces[namespaces.length - 1]));
        } catch (Exception e) {
            LOGGER.error("Error when deobfuscating", e);
        }
        return this;
    }

    public TinyDeobfuscator deobfuscate(Path source, Path target, boolean includeOthers, String fromNamespace, String toNamespace) {
        try {
            TinyMappingReader mappingReader = new TinyMappingReader(mappingPath);
            sharedDeobfuscate(source, target, mappingReader, includeOthers, false, parent -> mappingReader.version == 2 ?
                    new LVTRenamer(parent, mappingReader.getMappingsByNamespaceMap(fromNamespace), fromNamespace, toNamespace)
                    : parent, (reader, superClassMapping) -> new MappingRemapper(reader, superClassMapping, fromNamespace, toNamespace));
        } catch (Exception e) {
            LOGGER.error("Error when deobfuscating", e);
        }
        return this;
    }
}