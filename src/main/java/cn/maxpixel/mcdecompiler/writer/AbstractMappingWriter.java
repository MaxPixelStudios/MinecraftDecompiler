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

import cn.maxpixel.mcdecompiler.mapping1.Mapping;
import cn.maxpixel.mcdecompiler.mapping1.type.MappingType;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class AbstractMappingWriter<M extends Mapping, C, T extends MappingType<M, C>> {
    protected static final Logger LOGGER = Logging.getLogger("Mapping Writer");

    protected final T type;

    public AbstractMappingWriter(T type) {
        this.type = Objects.requireNonNull(type);
    }

    public abstract void addMapping(M mapping);

    public abstract void addMappings(Collection<M> mappings);

    public abstract void addMappings(ObjectList<M> mappings);

//    public final void writeTo(OutputStream os) throws IOException {
//        synchronized(buf) {
//            os.write(String.join("\n", buf).getBytes(StandardCharsets.UTF_8));
//            buf.clear();
//        }
//    }
//
//    public final void writeTo(Writer writer) throws IOException {
//        synchronized(buf) {
//            writer.write(String.join("\n", buf));
//            buf.clear();
//        }
//    }
//
//    public final void writeTo(WritableByteChannel os) throws IOException {
//        synchronized(buf) {
//            os.write(ByteBuffer.wrap(String.join("\n", buf).getBytes(StandardCharsets.UTF_8)));
//            buf.clear();
//        }
//    }
}