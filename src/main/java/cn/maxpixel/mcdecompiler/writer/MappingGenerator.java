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

package cn.maxpixel.mcdecompiler.writer;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * A generator which generates mappings to strings.<br>
 *
 * @implNote You should implement {@link Unique} or {@link Classified} instead of this class unless you are creating a new type of mapping
 * @param <T> Mapping type
 * @param <C> Collection type
 */
public interface MappingGenerator<T extends Mapping, C extends MappingCollection<T>> {
    MappingType<T, C> getType();

    ObjectList<String> generate(C mappings);

    // Just in case that maybe some types of mapping need to generate multiple files

    default boolean requireMultiFiles() {
        return false;
    }

    default ObjectList<String>[] generateMulti(C mappings) {
        throw new UnsupportedOperationException();
    }

    interface Unique<T extends Mapping> extends MappingGenerator<T, UniqueMapping<T>> {
    }

    interface Classified<T extends Mapping> extends MappingGenerator<T, ClassifiedMapping<T>> {
        @Override
        default ObjectList<String> generate(ClassifiedMapping<T> mappings) {
            return generate(mappings, null);
        }

        ObjectList<String> generate(ClassifiedMapping<T> mappings, ClassifiedMappingRemapper remapper);

        @Override
        default ObjectList<String>[] generateMulti(ClassifiedMapping<T> mappings) {
            return generateMulti(mappings, null);
        }

        default ObjectList<String>[] generateMulti(ClassifiedMapping<T> mappings, ClassifiedMappingRemapper remapper) {
            throw new UnsupportedOperationException();
        }
    }
}