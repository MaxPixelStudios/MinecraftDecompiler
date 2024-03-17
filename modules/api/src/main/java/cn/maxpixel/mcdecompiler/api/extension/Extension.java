package cn.maxpixel.mcdecompiler.api.extension;

import cn.maxpixel.mcdecompiler.remapper.processing.Process;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.FileSystem;
import java.util.List;
import java.util.function.Supplier;

public interface Extension {
    @NotNull String getName();

    default void onRegisteringOptions(OptionRegistry.Registrar registrar) {
    }

    default void onReceivingOptions(OptionRegistry.ValueGetter getter) {
    }

    default void onPreprocess(FileSystem fs) {// TODO
    }

    default @NotNull @Unmodifiable List<@NotNull ObjectObjectImmutablePair<Process.@NotNull Run, @NotNull Supplier<@NotNull Process>>> getProcesses() {
        return List.of();
    }
}