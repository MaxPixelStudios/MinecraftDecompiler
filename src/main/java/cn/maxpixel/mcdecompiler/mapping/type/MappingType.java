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

package cn.maxpixel.mcdecompiler.mapping.type;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.reader.MappingProcessor;
import cn.maxpixel.mcdecompiler.writer.MappingGenerator;

/**
 * Represents a type of mapping.<br>
 *
 * @implNote You should implement {@link Unique} or {@link Classified} instead of this class unless you are creating a new kind of mapping</b>
 * @param <M> Mapping type
 * @param <C> Collection type
 */
public interface MappingType<M extends Mapping, C extends MappingCollection<M>> {
    /**
     * Gets the name of this mapping type.
     *
     * @return The name of this mapping type
     */
    String getName();

    /**
     * Gets the comment char of this mapping type.
     *
     * @return The comment char of this mapping type. '\0' if this mapping type does not support comments
     */
    default char getCommentChar() {
        return '#';
    }

    /**
     * Gets the processor of this mapping type.
     *
     * @return The processor of this mapping type
     */
    MappingProcessor<M, C> getProcessor();

    /**
     * Gets the generator of this mapping type.
     *
     * @return The generator of this mapping type
     */
    MappingGenerator<M, C> getGenerator();

    interface Classified<M extends Mapping> extends MappingType<M, ClassifiedMapping<M>> {
        @Override
        MappingProcessor.Classified<M> getProcessor();

        @Override
        MappingGenerator.Classified<M> getGenerator();
    }

    interface Unique<M extends Mapping> extends MappingType<M, UniqueMapping<M>> {
        @Override
        MappingProcessor.Unique<M> getProcessor();

        @Override
        MappingGenerator.Unique<M> getGenerator();
    }
}