package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.TsrgV2MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.TsrgV2MappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum TsrgV2MappingFormat implements MappingFormat.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "tsrg-v2";
    }

    @Override
    public @NotNull TsrgV2MappingProcessor getProcessor() {
        return TsrgV2MappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull TsrgV2MappingGenerator getGenerator() {
        return TsrgV2MappingGenerator.INSTANCE;
    }
}