package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.ProguardMappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.ProguardMappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum ProguardMappingFormat implements MappingFormat.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "proguard";
    }

    @Override
    public @NotNull ProguardMappingProcessor getProcessor() {
        return ProguardMappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull ProguardMappingGenerator getGenerator() {
        return ProguardMappingGenerator.INSTANCE;
    }
}