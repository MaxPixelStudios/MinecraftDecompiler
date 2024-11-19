package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.CsrgMappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.CsrgMappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum CsrgMappingFormat implements MappingFormat.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "csrg";
    }

    @Override
    public @NotNull CsrgMappingProcessor getProcessor() {
        return CsrgMappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull CsrgMappingGenerator getGenerator() {
        return CsrgMappingGenerator.INSTANCE;
    }
}