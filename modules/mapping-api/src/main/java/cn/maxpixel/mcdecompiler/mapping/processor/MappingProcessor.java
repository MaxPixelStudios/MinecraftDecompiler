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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;

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
    C process(List<String> content);

    /**
     * Processes contents(probably of multiple files) and merge them into a single mapping collection.
     *
     * @param contents contents to process
     * @return processed mapping collection
     */
    default C process(List<String>... contents) {
        Object2ObjectOpenHashMap<String, List<String>> map = new Object2ObjectOpenHashMap<>();
        for (int i = 0; i < contents.length; i++) map.put(String.valueOf(i), contents[i]);
        return process(map);
    }

    /**
     * Processes contents(probably of multiple files) and merge them into a single mapping collection.
     *
     * @param contents map of contents to process, with the key usually being the file name or relative path and
     *                 the value being the content of the file
     * @return processed mapping collection
     */
    C process(Map<String, List<String>> contents);// TODO: better ways of merging mapping collections?

    interface Unique<T extends Mapping> extends MappingProcessor<T, UniqueMapping<T>> {
        @Override
        default UniqueMapping<T> process(Map<String, List<String>> contents) {
            UniqueMapping<T> result = new UniqueMapping<>();
            for (List<String> content : contents.values()) result.add(process(content));
            return result;
        }
    }

    interface Classified<T extends Mapping> extends MappingProcessor<T, ClassifiedMapping<T>> {
        @Override
        default ClassifiedMapping<T> process(Map<String, List<String>> contents) {
            ClassifiedMapping<T> result = new ClassifiedMapping<>();
            for (List<String> content : contents.values()) result.add(process(content));
            return result;
        }
    }
}