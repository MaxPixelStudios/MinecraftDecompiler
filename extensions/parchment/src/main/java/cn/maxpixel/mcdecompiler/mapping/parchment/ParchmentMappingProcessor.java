package cn.maxpixel.mcdecompiler.mapping.parchment;

import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.processor.MappingProcessor;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;

import static cn.maxpixel.mcdecompiler.mapping.parchment.ParchmentMappingFormat.*;

public enum ParchmentMappingProcessor implements MappingProcessor.Classified<PairedMapping>, Cloneable {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return ParchmentMappingFormat.INSTANCE;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(ObjectList<String> content) {
        return process(new StringListReader(content));
    }

    public ClassifiedMapping<PairedMapping> process(Reader rd) {
        try (JsonReader reader = new JsonReader(rd)) {
            reader.beginObject();

            ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>();
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
            return mappings;
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
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
            Descriptor mDesc = null;
            Documented mDoc = null;
            LocalVariableTable.Paired lvt = null;
            while (reader.peek() == JsonToken.NAME) switch (reader.nextName()) {
                case KEY_NAME -> method = new PairedMapping(reader.nextString());
                case KEY_DESCRIPTOR -> mDesc = new Descriptor(reader.nextString());
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
            Descriptor fDesc = null;
            Documented fDoc = null;
            while (reader.peek() == JsonToken.NAME) switch (reader.nextName()) {
                case KEY_NAME -> field = new PairedMapping(reader.nextString());
                case KEY_DESCRIPTOR -> fDesc = new Descriptor(reader.nextString());
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

    private static class StringListReader extends Reader {
        private final ObjectList<String> l;
        private int listIndex = 0;
        private final int listSize;
        private String s;
        private int stringIndex = 0;
        private int stringSize = 0;
        private boolean closed;

        public StringListReader(ObjectList<String> l) {
            this.l = l;
            if ((this.listSize = l.size()) > 0) {
                this.s = l.get(0);
                this.stringSize = s.length();
            }
        }

        @Override
        public int read(char @NotNull [] cbuf, int off, int len) throws IOException {
            if (closed) throw new IOException("Reader closed");
            if (listIndex >= listSize) return -1;
            int read = 0;
            while (listIndex < listSize && len > 0) {
                int remaining = stringSize - stringIndex;
                if (remaining >= len) {
                    s.getChars(stringIndex, stringIndex + len, cbuf, off);
                    stringIndex += len;
                    read += len;
                    return read;
                } else {// remaining <= len - 1
                    s.getChars(stringIndex, stringSize, cbuf, off);
                    cbuf[off + remaining] = '\n';
                    read += remaining + 1;
                    off += remaining + 1;
                    len -= remaining + 1;
                    updateString();
                }
            }
            return read;
        }

        private void updateString() {
            if (++listIndex < listSize) {
                s = l.get(listIndex);
                stringIndex = 0;
                stringSize = s.length();
            }
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
