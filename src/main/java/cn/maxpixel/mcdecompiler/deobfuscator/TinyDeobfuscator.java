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

import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.asm.TinyV2LVTRenamer;
import cn.maxpixel.mcdecompiler.mapping.TinyClassMapping;
import cn.maxpixel.mcdecompiler.reader.TinyMappingReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.ClassVisitor;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TinyDeobfuscator extends AbstractDeobfuscator {
    public TinyDeobfuscator(String mappingPath) {
        super(mappingPath);
    }
    @Override
    public TinyDeobfuscator deobfuscate(Path source, Path target, boolean includeOthers, boolean reverse) {
        if(reverse) throw new UnsupportedOperationException();
        try(TinyMappingReader mappingReader = new TinyMappingReader(mappingPath)) {
            sharedDeobfuscate(source, target, mappingReader, includeOthers, false, (rvn, writer, mappingRemapper) -> {
                ClassVisitor shared = RemapperConstructor.shared(rvn, writer, mappingRemapper);
                return mappingReader.getVersion() == 2 ? new TinyV2LVTRenamer(shared, (Object2ObjectOpenHashMap<String, TinyClassMapping>) mappingReader.getMappingsByMappedNameMap()) : shared;
            });
        } catch (Exception e) {
            LOGGER.error("Error when deobfuscating", e);
        }
        return this;
    }

    public TinyDeobfuscator deobfuscate(Path source, Path target, boolean includeOthers, String fromNamespace, String toNamespace) {
        try(TinyMappingReader mappingReader = new TinyMappingReader(mappingPath)) {
            sharedDeobfuscate(source, target, mappingReader, includeOthers, false, (rvn, writer, mappingRemapper) -> {
                ClassVisitor shared = RemapperConstructor.shared(rvn, writer, mappingRemapper);
                return mappingReader.getVersion() == 2 ? new TinyV2LVTRenamer(shared, ((List<TinyClassMapping>) mappingReader.getMappings())
                        .stream().collect(Collectors.toMap(cm -> cm.getName(fromNamespace), Function.identity(), (cm1, cm2) -> {throw new IllegalArgumentException("Key \"" + cm1 + "\" and \"" + cm2 + "\" duplicated!");},
                                Object2ObjectOpenHashMap::new))) : shared;
            }, (reader, superClassMapping) -> new MappingRemapper(reader, superClassMapping, fromNamespace, toNamespace));
        } catch (Exception e) {
            LOGGER.error("Error when deobfuscating", e);
        }
        return this;
    }
}