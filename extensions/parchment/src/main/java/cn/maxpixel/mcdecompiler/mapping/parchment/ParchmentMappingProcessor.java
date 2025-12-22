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

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.processor.MappingProcessor;
import cn.maxpixel.mcdecompiler.mapping.util.InputCollection;
import cn.maxpixel.mcdecompiler.utils.Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static cn.maxpixel.mcdecompiler.mapping.parchment.ParchmentMappingFormat.*;

public enum ParchmentMappingProcessor implements MappingProcessor.Classified<PairedMapping>, Cloneable {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return ParchmentMappingFormat.INSTANCE;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(InputCollection contents) {
        ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>();
        for (var content : contents) {
            try (JsonReader reader = new JsonReader(content.reader())) {
                reader.beginObject();

                while (reader.peek() == JsonToken.NAME) switch (reader.nextName()) {
                    case KEY_VERSION -> {
                        FormatVersion version = FormatVersion.from(reader.nextString());
                        if (!FormatVersion.CURRENT.compatibleWith(version)) {
                            throw new UnsupportedOperationException("Version " + version + " is incompatible with " +
                                    "current version " + FormatVersion.CURRENT);
                        }
                    }
                    case KEY_PACKAGES -> handlePackages(reader, mappings);
                    case KEY_CLASSES -> handleClasses(reader, mappings);
                    default -> reader.skipValue();
                }
                reader.endObject();
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
        }
        return mappings;
    }

    private static @NotNull Documented handleDocs(JsonReader reader) throws IOException {
        reader.beginArray();
        Documented doc = new Documented();
        while (reader.peek() != JsonToken.END_ARRAY) doc.getContents().add(reader.nextString());
        reader.endArray();
        return doc;
    }

    private static void handleClasses(JsonReader reader, ClassifiedMapping<PairedMapping> mappings) throws IOException {
        reader.beginArray();
        while (reader.peek() != JsonToken.END_ARRAY) {
            reader.beginObject();

            ClassMapping<PairedMapping> cm = new ClassMapping<>(new PairedMapping(""));
            Documented doc = null;
            while (reader.peek() == JsonToken.NAME) switch (reader.nextName()) {
                case KEY_NAME -> cm.mapping = new PairedMapping(reader.nextString());
                case KEY_JAVADOC -> doc = handleDocs(reader);
                case KEY_FIELDS -> handleFields(reader, cm);
                case KEY_METHODS -> handleMethods(reader, cm);
                default -> reader.skipValue();
            }
            reader.endObject();
            if (!cm.mapping.unmappedName.isEmpty()) {
                if (doc != null) cm.mapping.addComponent(doc);
                mappings.classes.add(cm);
            }
        }
        reader.endArray();
    }

    private static LocalVariableTable.@NotNull Paired handleParameters(JsonReader reader) throws IOException {
        reader.beginArray();
        LocalVariableTable.Paired lvt = new LocalVariableTable.Paired();
        while (reader.peek() != JsonToken.END_ARRAY) {
            reader.beginObject();

            PairedMapping param = null;
            int index = -1;
            Documented pDoc = null;
            while (reader.peek() == JsonToken.NAME) switch (reader.nextName()) {
                case KEY_INDEX -> index = reader.nextInt();
                case KEY_NAME -> param = new PairedMapping(reader.nextString());
                case KEY_JAVADOC -> {
                    pDoc = new Documented();
                    pDoc.setContentString(reader.nextString());
                }
                default -> reader.skipValue();
            }
            reader.endObject();
            if (param != null && index >= 0) {
                if (pDoc != null) param.addComponent(pDoc);
                lvt.setLocalVariable(index, param);
            }
        }
        reader.endArray();
        return lvt;
    }

    private static void handleMethods(JsonReader reader, ClassMapping<PairedMapping> cm) throws IOException {
        reader.beginArray();
        while (reader.peek() != JsonToken.END_ARRAY) {
            reader.beginObject();

            PairedMapping method = null;
            Descriptor.Unmapped mDesc = null;
            Documented mDoc = null;
            LocalVariableTable.Paired lvt = null;
            while (reader.peek() == JsonToken.NAME) switch (reader.nextName()) {
                case KEY_NAME -> method = new PairedMapping(reader.nextString());
                case KEY_DESCRIPTOR -> mDesc = new Descriptor.Unmapped(reader.nextString());
                case KEY_JAVADOC -> mDoc = handleDocs(reader);
                case KEY_PARAMETERS -> lvt = handleParameters(reader);
                default -> reader.skipValue();
            }
            reader.endObject();
            if (method != null) {
                if (mDesc != null) method.addComponent(mDesc);
                if (mDoc != null) method.addComponent(mDoc);
                if (lvt != null) method.addComponent(lvt);
                cm.addMethod(method);
            }
        }
        reader.endArray();
    }

    private static void handleFields(JsonReader reader, ClassMapping<PairedMapping> cm) throws IOException {
        reader.beginArray();
        while (reader.peek() != JsonToken.END_ARRAY) {
            reader.beginObject();

            PairedMapping field = null;
            Descriptor.Unmapped fDesc = null;
            Documented fDoc = null;
            while (reader.peek() == JsonToken.NAME) switch (reader.nextName()) {
                case KEY_NAME -> field = new PairedMapping(reader.nextString());
                case KEY_DESCRIPTOR -> fDesc = new Descriptor.Unmapped(reader.nextString());
                case KEY_JAVADOC -> fDoc = handleDocs(reader);
                default -> reader.skipValue();
            }
            reader.endObject();
            if (field != null) {
                if (fDesc != null) field.addComponent(fDesc);
                if (fDoc != null) field.addComponent(fDoc);
                cm.addField(field);
            }
        }
        reader.endArray();
    }

    private static void handlePackages(JsonReader reader, ClassifiedMapping<PairedMapping> mappings) throws IOException {
        reader.beginArray();
        while (reader.peek() != JsonToken.END_ARRAY) {
            reader.beginObject();

            PairedMapping pkg = null;
            Documented doc = null;
            while (reader.peek() == JsonToken.NAME) switch (reader.nextName()) {
                case KEY_NAME -> pkg = new PairedMapping(reader.nextString());
                case KEY_JAVADOC -> doc = handleDocs(reader);
                default -> reader.skipValue();
            }
            reader.endObject();
            if (pkg != null) {
                if (doc != null) pkg.addComponent(doc);
                mappings.packages.add(pkg);
            }
        }
        reader.endArray();
    }
}
