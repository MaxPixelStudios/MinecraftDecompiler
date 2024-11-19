package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.TinyV1MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.TinyV1MappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum TinyV1MappingFormat implements MappingFormat.Classified<NamespacedMapping> {// TODO: support properties
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "tiny-v1";
    }

    @Override
    public @NotNull TinyV1MappingProcessor getProcessor() {
        return TinyV1MappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull TinyV1MappingGenerator getGenerator() {
        return TinyV1MappingGenerator.INSTANCE;
    }
}