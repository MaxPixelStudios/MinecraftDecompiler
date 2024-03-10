package cn.maxpixel.mcdecompiler.api.extension;

import org.jetbrains.annotations.NotNull;

public interface Extension {
    @NotNull String getName();

    default void onRegisteringOptions(OptionRegistry.Registrar registrar) {
    }

    default void onReceivingOptions(OptionRegistry.ValueGetter getter) {
    }
}