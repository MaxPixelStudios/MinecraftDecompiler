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

package cn.maxpixel.mcdecompiler.mapping.parchment;

import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.generator.MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

import static cn.maxpixel.mcdecompiler.mapping.parchment.ParchmentMappingFormat.*;

public enum ParchmentMappingGenerator implements MappingGenerator.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, @Nullable ClassifiedMappingRemapper remapper) {
        StringListWriter slw = new StringListWriter();// No specialized version of returning String because it's not worth it
        try (JsonWriter writer = new JsonWriter(slw)) {
            writer.setIndent("  ");// 2 spaces
            writer.beginObject()
                    .name(KEY_VERSION)
                    .value(FormatVersion.CURRENT.toString());
            if (!mappings.packages.isEmpty()) writePackages(mappings, writer);
            if (!mappings.classes.isEmpty()) writeClasses(mappings, writer);
            writer.endObject();
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
        return slw.list;
    }

    private static void writeClasses(ClassifiedMapping<PairedMapping> mappings, JsonWriter writer) throws IOException {
        writer.name(KEY_CLASSES).beginArray();
        for (@NotNull ClassMapping<@NotNull PairedMapping> cm : mappings.classes) {
            writer.beginObject()
                    .name(KEY_NAME)
                    .value(cm.mapping.unmappedName);
            writeDoc(cm.mapping, writer);
            var fields = cm.getFields();
            if (!fields.isEmpty()) {
                writer.name(KEY_FIELDS).beginArray();
                for (@NotNull PairedMapping field : fields) {
                    writer.beginObject()
                            .name(KEY_NAME)
                            .value(field.unmappedName)
                            .name(KEY_DESCRIPTOR)
                            .value(field.getComponent(Descriptor.Unmapped.class).descriptor);
                    writeDoc(field, writer);
                    writer.endObject();
                }
                writer.endArray();
            }
            var methods = cm.getMethods();
            if (!methods.isEmpty()) {
                writer.name(KEY_METHODS).beginArray();
                for (@NotNull PairedMapping method : methods) {
                    writer.beginObject()
                            .name(KEY_NAME)
                            .value(method.unmappedName)
                            .name(KEY_DESCRIPTOR)
                            .value(method.getComponent(Descriptor.Unmapped.class).descriptor);
                    writeDoc(method, writer);
                    writeParams(writer, method);
                    writer.endObject();
                }
                writer.endArray();
            }
            writer.endObject();
        }
        writer.endArray();
    }

    private static void writeParams(JsonWriter writer, @NotNull PairedMapping method) throws IOException {
        var params = method.getComponent(LocalVariableTable.Paired.class);
        if (params != null && !params.isEmpty()) {
            writer.name(KEY_PARAMETERS).beginArray();
            for (var indexes = params.getLocalVariableIndexes().iterator(); indexes.hasNext(); ) {
                int i = indexes.nextInt();
                PairedMapping param = params.getLocalVariable(i);
                writer.beginObject()
                        .name(KEY_INDEX)
                        .jsonValue(Integer.toString(i))
                        .name(KEY_NAME)
                        .value(param.unmappedName);
                Documented doc = param.getComponent(Documented.class);
                if (doc != null && !doc.contents.isEmpty()) {
                    writer.name(KEY_JAVADOC).value(doc.getContentString());
                }
                writer.endObject();
            }
            writer.endArray();
        }
    }

    private static void writeDoc(PairedMapping m, JsonWriter writer) throws IOException {
        Documented doc = m.getComponent(Documented.class);
        if (doc != null && !doc.contents.isEmpty()) {
            writer.name(KEY_JAVADOC)
                    .beginArray();
            for (String c : doc.contents) writer.value(c);
            writer.endArray();
        }
    }

    private static void writePackages(ClassifiedMapping<PairedMapping> mappings, JsonWriter writer) throws IOException {
        writer.name(KEY_PACKAGES).beginArray();
        for (@NotNull PairedMapping pkg : mappings.packages) {
            writer.beginObject()
                    .name(KEY_NAME)
                    .value(pkg.unmappedName);
            writeDoc(pkg, writer);
            writer.endObject();
        }
        writer.endArray();
    }

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return ParchmentMappingFormat.INSTANCE;
    }

    private static class StringListWriter extends Writer {
        private final ObjectArrayList<String> list = new ObjectArrayList<>();
        private final StringBuilder sb = new StringBuilder();

        @Override
        public void write(int c) {
            if (c == '\n') {// JsonWriter only writes newline here
                list.add(sb.toString());
                sb.setLength(0);
            } else sb.append((char) c);
        }

        @Override
        public void write(char @NotNull [] cbuf) {
            sb.append(cbuf);
        }

        @Override
        public void write(@NotNull String str) {
            sb.append(str);
        }

        @Override
        public void write(@NotNull String str, int off, int len) {
            sb.append(str, off, off + len);
        }

        @Override
        public void write(char @NotNull [] cbuf, int off, int len) {
            sb.append(cbuf, off, len);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            list.add(sb.toString());
        }
    }
}
