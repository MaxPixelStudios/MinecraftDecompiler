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

package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.MappingProcessor;
import cn.maxpixel.mcdecompiler.mapping.util.InputCollection;
import cn.maxpixel.mcdecompiler.mapping.util.OutputCollection;
import cn.maxpixel.mcdecompiler.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Represents a mapping format.<br>
 *
 * @implNote You should implement {@link Unique} or {@link Classified} instead of this class unless you are creating a new type of mapping
 * @param <M> Mapping type
 * @param <C> Collection type
 */
public interface MappingFormat<M extends Mapping, C extends MappingCollection<M>> {
    /**
     * Gets the name of this mapping format.
     *
     * @return The name of this mapping format
     */
    @NotNull String getName();

    /**
     * Gets the processor of this mapping format.
     *
     * @return The processor of this mapping format
     */
    @NotNull MappingProcessor<M, C> getProcessor();

    /**
     * Gets the generator of this mapping format.
     *
     * @return The generator of this mapping format
     */
    @NotNull MappingGenerator<M, C> getGenerator();

    default @NotNull C read(@NotNull Reader reader) {
        try {
            return getProcessor().process(new InputCollection.Entry(reader));
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    default @NotNull C read(@NotNull InputStream is) {
        return read(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    default @NotNull C read(@NotNull InputCollection contents) {
        try {
            return getProcessor().process(contents);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    default void write(@NotNull C collection, @NotNull OutputStream os) throws IOException {
        write(collection, new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    default void write(@NotNull C collection, @NotNull Writer writer) throws IOException {
        getGenerator().generateAndWrite(collection, OutputCollection.ofUnnamed(writer), null);
    }

    default void write(@NotNull C collection, @NotNull OutputCollection out) throws IOException {
        getGenerator().generateAndWrite(collection, out, null);
    }

    interface Classified<M extends Mapping> extends MappingFormat<M, ClassifiedMapping<M>> {
        @Override
        @NotNull MappingProcessor.Classified<M> getProcessor();

        @Override
        @NotNull MappingGenerator.Classified<M> getGenerator();
    }

    interface Unique<M extends Mapping> extends MappingFormat<M, UniqueMapping<M>> {
        @Override
        @NotNull MappingProcessor.Unique<M> getProcessor();

        @Override
        @NotNull MappingGenerator.Unique<M> getGenerator();
    }
}