package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.SrgMappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.SrgMappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum SrgMappingFormat implements MappingFormat.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "srg";
    }

    @Override
    public @NotNull SrgMappingProcessor getProcessor() {
        return SrgMappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull SrgMappingGenerator getGenerator() {
        return SrgMappingGenerator.INSTANCE;
    }
}