package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.TsrgV1MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.TsrgV1MappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum TsrgV1MappingFormat implements MappingFormat.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "tsrg-v1";
    }

    @Override
    public @NotNull TsrgV1MappingProcessor getProcessor() {
        return TsrgV1MappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull TsrgV1MappingGenerator getGenerator() {
        return TsrgV1MappingGenerator.INSTANCE;
    }
}