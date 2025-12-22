/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.remapper.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.OutputCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A generator which generates mappings to strings.<br>
 *
 * @implNote You should implement {@link Unique} or {@link Classified} instead of this class unless you are creating a new type of mapping
 * @param <T> Mapping type
 * @param <C> Collection type
 */
public interface MappingGenerator<T extends Mapping, C extends MappingCollection<T>> {
    MappingFormat<T, C> getFormat();

    default ObjectList<String> generate(C mappings) {
        return generate(mappings, null);
    }

    @ApiStatus.OverrideOnly
    ObjectList<String> generate(C mappings, @Nullable MappingRemapper remapper);

    default void generateAndWrite(C mappings, OutputCollection out, @Nullable MappingRemapper remapper) throws IOException {
        try (var unnamed = out.getUnnamedOutput()) {
            unnamed.write(String.join("\n", generate(mappings, remapper)));
        }
    }

    interface Unique<T extends Mapping> extends MappingGenerator<T, UniqueMapping<T>> {
    }

    interface Classified<T extends Mapping> extends MappingGenerator<T, ClassifiedMapping<T>> {
    }
}