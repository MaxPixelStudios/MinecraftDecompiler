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
import cn.maxpixel.mcdecompiler.mapping.util.ContentList;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A processor which processes strings to mappings.
 *
 * @implNote You should implement {@link Unique} or {@link Classified} instead of this class unless you are creating a new type of mapping
 * @param <T> Mapping type
 * @param <C> Collection type
 */
public interface MappingProcessor<T extends Mapping, C extends MappingCollection<T>> {
    Predicate<String> NOT_BLANK = Predicate.not(String::isBlank);

    MappingFormat<T, C> getFormat();

    default String stripComments(String s) {
        int index = s.indexOf('#');
        if (index >= 0) return s.substring(0, index);
        return s;
    }

    default Stream<String> preprocess(Stream<String> s) {
        return s.filter(NOT_BLANK).map(String::stripTrailing);
    }

    default String getFirstLine(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();
        while (stripComments(firstLine).isBlank()) firstLine = reader.readLine();
        return firstLine;
    }

    /**
     * Processes contents and, if supported, merge them into a single mapping collection.
     *
     * @param contents Content tree to process
     * @throws IOException When IO errors occur
     * @return a mapping collection
     * @implNote If your mapping does not support merging, you can call {@link ContentList#getAsSingle()} to convert
     *      the content tree to a single content, which will throw {@link UnsupportedOperationException} when multiple
     *      contents are given
     */
    C process(ContentList contents) throws IOException;

    interface Unique<T extends Mapping> extends MappingProcessor<T, UniqueMapping<T>> {
    }

    interface Classified<T extends Mapping> extends MappingProcessor<T, ClassifiedMapping<T>> {
    }
}