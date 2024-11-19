package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.TinyV2MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.TinyV2MappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum TinyV2MappingFormat implements MappingFormat.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "tiny-v2";
    }

    @Override
    public char getCommentChar() {
        return '\0';
    }

    @Override
    public @NotNull TinyV2MappingProcessor getProcessor() {
        return TinyV2MappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull TinyV2MappingGenerator getGenerator() {
        return TinyV2MappingGenerator.INSTANCE;
    }
}