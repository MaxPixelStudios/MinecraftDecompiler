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
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.logging.Logger;

public abstract class AbstractMappingWriter<M extends Mapping, C, T extends MappingType<M, C>> {
    protected static final Logger LOGGER = Logging.getLogger("Mapping Writer");

    protected final T type;
    private final ObjectList<M> packages;

    public AbstractMappingWriter(@NotNull T type) {
        this.type = type;
        this.packages = type.getGenerator().supportPackage() ? new ObjectArrayList<>() : ObjectLists.emptyList();
    }

    public abstract void addMappings(@NotNull C mappings);

    public final void addPackage(@NotNull M pkg) {
        if(!type.getGenerator().supportPackage()) throw new UnsupportedOperationException();
        packages.add(pkg);
    }

    public final void addPackages(@NotNull Collection<M> packages) {
        if(!type.getGenerator().supportPackage()) throw new UnsupportedOperationException();
        this.packages.addAll(packages);
    }

    public final void addPackages(@NotNull ObjectList<M> packages) {
        if(!type.getGenerator().supportPackage()) throw new UnsupportedOperationException();
        this.packages.addAll(packages);
    }

    protected abstract C getCollection();

    protected abstract void clearCollection();

    private ObjectList<String> generate() {
        ObjectList<String> output = type.getGenerator().generate(getCollection());
        if(type.supportPackage() && !packages.isEmpty()) output.addAll(type.getGenerator().generatePackages(packages));
        clearCollection();
        packages.clear();
        return output;
    }

    private ObjectList<String>[] generateMulti() {
        ObjectList<String>[] output = type.getGenerator().generateMulti(getCollection());
        if(type.supportPackage()) {
            ObjectList<String>[] outputPackages = type.getGenerator().generatePackagesMulti(packages);
            if(output.length != outputPackages.length) throw new UnsupportedOperationException(
                    "The length of the generated mappings array and the generated packages array must be the same.");
            for(int i = 0; i < output.length; i++) {
                output[i].addAll(outputPackages[i]);
            }
        }
        clearCollection();
        packages.clear();
        return output;
    }

    public final void writeTo(@NotNull OutputStream os) throws IOException {
        if(type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(OutputStream...) instead.");
        os.write(String.join("\n", generate()).getBytes(StandardCharsets.UTF_8));
    }

    public final void writeTo(@NotNull Writer writer) throws IOException {
        if(type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(Writer...) instead.");
        writer.write(String.join("\n", generate()));
    }

    public final void writeTo(@NotNull WritableByteChannel ch) throws IOException {
        if(type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(WritableByteChannel...) instead.");
        ch.write(ByteBuffer.wrap(String.join("\n", generate()).getBytes(StandardCharsets.UTF_8)));
    }

    public final void writeTo(@NotNull OutputStream... os) throws IOException {
        if(!type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(OutputStream) instead.");
        ObjectList<String>[] output = generateMulti();
        if(output.length != os.length) throw new UnsupportedOperationException();
        for(int i = 0; i < output.length; i++) {
            os[i].write(String.join("\n", output[i]).getBytes(StandardCharsets.UTF_8));
        }
    }

    public final void writeTo(@NotNull Writer... writer) throws IOException {
        if(!type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(Writer) instead.");
        ObjectList<String>[] output = generateMulti();
        if(output.length != writer.length) throw new UnsupportedOperationException();
        for(int i = 0; i < output.length; i++) {
            writer[i].write(String.join("\n", output[i]));
        }
    }

    public final void writeTo(@NotNull WritableByteChannel... ch) throws IOException {
        if(!type.getGenerator().requireMultiFiles()) throw new UnsupportedOperationException("Use writeTo(WritableByteChannel) instead.");
        ObjectList<String>[] output = generateMulti();
        if(output.length != ch.length) throw new UnsupportedOperationException();
        for(int i = 0; i < output.length; i++) {
            ch[i].write(ByteBuffer.wrap(String.join("\n", output[i]).getBytes(StandardCharsets.UTF_8)));
        }
    }
}