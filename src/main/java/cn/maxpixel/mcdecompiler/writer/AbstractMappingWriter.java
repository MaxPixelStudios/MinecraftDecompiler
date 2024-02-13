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

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class AbstractMappingWriter<M extends Mapping, C extends MappingCollection<M>, T extends MappingType<M, C>> {
    protected static final Logger LOGGER = Logging.getLogger("Mapping Writer");

    public final T type;

    public AbstractMappingWriter(@NotNull T type) {
        this.type = Objects.requireNonNull(type);
    }

    public final void write(@NotNull C collection, @NotNull OutputStream os) throws IOException {
        if (type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(OutputStream...) instead.");
        os.write(String.join("\n", type.getGenerator().generate(collection)).getBytes(StandardCharsets.UTF_8));
    }

    public final void write(@NotNull C collection, @NotNull Writer writer) throws IOException {
        if (type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(Writer...) instead.");
        writer.write(String.join("\n", type.getGenerator().generate(collection)));
    }

    public final void write(@NotNull C collection, @NotNull WritableByteChannel ch) throws IOException {
        if (type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(WritableByteChannel...) instead.");
        ch.write(ByteBuffer.wrap(String.join("\n", type.getGenerator().generate(collection)).getBytes(StandardCharsets.UTF_8)));
    }

    public final void write(@NotNull C collection, @NotNull OutputStream... os) throws IOException {
        if (!type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(OutputStream) instead.");
        ObjectList<String>[] output = type.getGenerator().generateMulti(collection);
        if(output.length != os.length) throw new UnsupportedOperationException();
        for(int i = 0; i < output.length; i++) {
            os[i].write(String.join("\n", output[i]).getBytes(StandardCharsets.UTF_8));
        }
    }

    public final void write(@NotNull C collection, @NotNull Writer... writer) throws IOException {
        if (!type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(Writer) instead.");
        ObjectList<String>[] output = type.getGenerator().generateMulti(collection);
        if(output.length != writer.length) throw new UnsupportedOperationException();
        for(int i = 0; i < output.length; i++) {
            writer[i].write(String.join("\n", output[i]));
        }
    }

    public final void write(@NotNull C collection, @NotNull WritableByteChannel... ch) throws IOException {
        if (!type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(WritableByteChannel) instead.");
        ObjectList<String>[] output = type.getGenerator().generateMulti(collection);
        if (output.length != ch.length) throw new UnsupportedOperationException();
        for(int i = 0; i < output.length; i++) {
            ch[i].write(ByteBuffer.wrap(String.join("\n", output[i]).getBytes(StandardCharsets.UTF_8)));
        }
    }
}