/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.writer;

import cn.maxpixel.mcdecompiler.mapping.AbstractClassMapping;
import cn.maxpixel.mcdecompiler.mapping.AbstractMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

public abstract class AbstractMappingWriter {
    protected static final Logger LOGGER = LogManager.getLogger("Mapping Writer");

    protected final ObjectArrayList<String> buf = new ObjectArrayList<>();

    public void writePackages(ObjectList<? extends AbstractMapping> mappings) {
        if(getGenerator() instanceof PackageMappingGenerator generator) {
            synchronized(buf) {
                buf.ensureCapacity(buf.size() + mappings.size());
            }
            mappings.parallelStream().map(generator::generatePackage).forEach(pkg -> {
                synchronized(buf) {
                    buf.add(pkg);
                }
            });
        } else throw new UnsupportedOperationException();
    }

    public abstract void writeMappings(ObjectList<? extends AbstractClassMapping> mappings);

    public void writeTo(OutputStream os) throws IOException {
        os.write(String.join("\n", buf).getBytes(StandardCharsets.UTF_8));
    }

    public void writeTo(Writer writer) throws IOException {
        writer.write(String.join("\n", buf));
    }

    public void writeTo(WritableByteChannel os) throws IOException {
        os.write(ByteBuffer.wrap(String.join("\n", buf).getBytes(StandardCharsets.UTF_8)));
    }

    public abstract MappingGenerator getGenerator();

    public interface MappingGenerator {
        default boolean isPaired() {
            return this instanceof PairedMappingGenerator;
        }
        default boolean isNamespaced() {
            return this instanceof NamespacedMappingGenerator;
        }

        default PairedMappingGenerator asPaired() {
            return (PairedMappingGenerator) this;
        }
        default NamespacedMappingGenerator asNamespaced() {
            return (NamespacedMappingGenerator) this;
        }
    }

    public interface PairedMappingGenerator extends MappingGenerator {
        String generateClass(PairedClassMapping mapping);
        String generateMethod(PairedMethodMapping mapping);
        String generateField(PairedFieldMapping mapping);
    }

    public interface NamespacedMappingGenerator extends MappingGenerator {
        String generateClass(NamespacedClassMapping mapping);
        String generateMethod(NamespacedMethodMapping mapping);
        String generateField(NamespacedFieldMapping mapping);
    }

    public interface PackageMappingGenerator {
        String generatePackage(AbstractMapping mapping);
    }
}