package cn.maxpixel.mcdecompiler.mapping.parchment;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.Objects;

public enum ParchmentMappingFormat implements MappingFormat.Classified<PairedMapping> {
    INSTANCE;
    public static final String KEY_NAME = "name";
    public static final String KEY_JAVADOC = "javadoc";
    public static final String KEY_DESCRIPTOR = "descriptor";
    public static final String KEY_VERSION = "version";
    public static final String KEY_PACKAGES = "packages";
    public static final String KEY_CLASSES = "classes";
    public static final String KEY_FIELDS = "fields";
    public static final String KEY_METHODS = "methods";
    public static final String KEY_INDEX = "index";
    public static final String KEY_PARAMETERS = "parameters";

    @Override
    public @NotNull String getName() {
        return ParchmentExtension.NAME;
    }

    @Override
    public char getCommentChar() {
        return '\0';
    }

    @Override
    public @NotNull ParchmentMappingProcessor getProcessor() {
        return ParchmentMappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull ParchmentMappingGenerator getGenerator() {
        return ParchmentMappingGenerator.INSTANCE;
    }

    @Override
    public @NotNull ClassifiedMapping<PairedMapping> read(@NotNull BufferedReader reader) {
        return getProcessor().process(Objects.requireNonNull(reader));
    }
}