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

package cn.maxpixel.mcdecompiler.mapping.processor;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * A processor which processes strings to mappings.
 *
 * @implNote You should implement {@link Unique} or {@link Classified} instead of this class unless you are creating a new type of mapping
 * @param <T> Mapping type
 * @param <C> Collection type
 */
public interface MappingProcessor<T extends Mapping, C extends MappingCollection<T>> {
    MappingFormat<T, C> getFormat();

    /**
     * Processes contents(probably of one file) to a mapping collection.
     *
     * @param content contents to process
     * @return processed mapping collection
     */
    C process(ObjectList<String> content);

    /**
     * Processes contents(probably of multiple files) and merge them into a single mapping collection.
     *
     * @param contents contents to process
     * @return processed mapping collection
     */
    C process(ObjectList<String>... contents);// TODO: better ways of merging mapping collections?
    // TODO: Maybe use Map<String, List<String>>

    interface Unique<T extends Mapping> extends MappingProcessor<T, UniqueMapping<T>> {
        @Override
        default UniqueMapping<T> process(ObjectList<String>... contents) {
            UniqueMapping<T> result = new UniqueMapping<>();
            for (ObjectList<String> content : contents) result.add(process(content));
            return result;
        }
    }

    interface Classified<T extends Mapping> extends MappingProcessor<T, ClassifiedMapping<T>> {
        @Override
        default ClassifiedMapping<T> process(ObjectList<String>... contents) {
            ClassifiedMapping<T> result = new ClassifiedMapping<>();
            for (ObjectList<String> content : contents) result.add(process(content));
            return result;
        }
    }
}