package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.PdmeMappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.PdmeMappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum PdmeMappingFormat implements MappingFormat.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "pdme";
    }

    @Override
    public @NotNull PdmeMappingProcessor getProcessor() {
        return PdmeMappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull PdmeMappingGenerator getGenerator() {
        return PdmeMappingGenerator.INSTANCE;
    }
}