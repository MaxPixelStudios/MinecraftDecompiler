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
import cn.maxpixel.mcdecompiler.mapping.util.ContentList;
import cn.maxpixel.mcdecompiler.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
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
            return getProcessor().process(new ContentList.ContentStream(reader));
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    default @NotNull C read(@NotNull InputStream is) {
        return read(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    default @NotNull C read(@NotNull ContentList contents) {
        try {
            return getProcessor().process(contents);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    default void write(@NotNull C collection, @NotNull OutputStream os) throws IOException {
        if (getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use write(OutputStream...) instead.");
        os.write(String.join("\n", getGenerator().generate(collection)).getBytes(StandardCharsets.UTF_8));
    }

    default void write(@NotNull C collection, @NotNull Writer writer) throws IOException {
        if (getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use write(Writer...) instead.");
        writer.write(String.join("\n", getGenerator().generate(collection)));
    }

    default void write(@NotNull C collection, @NotNull WritableByteChannel ch) throws IOException {
        if (getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use write(WritableByteChannel...) instead.");
        ch.write(ByteBuffer.wrap(String.join("\n", getGenerator().generate(collection)).getBytes(StandardCharsets.UTF_8)));
    }

    default void write(@NotNull C collection, @NotNull OutputStream... os) throws IOException {
        if (!getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use write(OutputStream) instead.");
        ObjectList<String>[] output = getGenerator().generateMulti(collection);
        if (output.length != os.length) throw new UnsupportedOperationException();
        for (int i = 0; i < output.length; i++) {
            os[i].write(String.join("\n", output[i]).getBytes(StandardCharsets.UTF_8));
        }
    }

    default void write(@NotNull C collection, @NotNull Writer... writer) throws IOException {
        if (!getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use write(Writer) instead.");
        ObjectList<String>[] output = getGenerator().generateMulti(collection);
        if (output.length != writer.length) throw new UnsupportedOperationException();
        for (int i = 0; i < output.length; i++) {
            writer[i].write(String.join("\n", output[i]));
        }
    }

    default void write(@NotNull C collection, @NotNull WritableByteChannel... ch) throws IOException {
        if (!getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use write(WritableByteChannel) instead.");
        ObjectList<String>[] output = getGenerator().generateMulti(collection);
        if (output.length != ch.length) throw new UnsupportedOperationException();
        for (int i = 0; i < output.length; i++) {
            ch[i].write(ByteBuffer.wrap(String.join("\n", output[i]).getBytes(StandardCharsets.UTF_8)));
        }
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