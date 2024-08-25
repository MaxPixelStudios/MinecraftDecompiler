package cn.maxpixel.mcdecompiler.mapping.parchment;

import cn.maxpixel.mcdecompiler.api.extension.Extension;
import cn.maxpixel.mcdecompiler.api.extension.OptionRegistry;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import org.jetbrains.annotations.NotNull;

public class ParchmentExtension implements Extension {
    public static final String NAME = "parchment";

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onRegistering(OptionRegistry.Registrar optionRegistrar) {
        MappingFormats.registerMappingFormat(ParchmentMappingFormat.INSTANCE);
    }
}