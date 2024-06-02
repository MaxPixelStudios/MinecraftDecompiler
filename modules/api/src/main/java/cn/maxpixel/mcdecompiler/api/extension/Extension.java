package cn.maxpixel.mcdecompiler.api.extension;

import cn.maxpixel.mcdecompiler.common.app.util.DataMap;
import cn.maxpixel.mcdecompiler.remapper.processing.Process;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public interface Extension {
    @NotNull String getName();

    /**
     * Register options, mapping formats, format detectors, decompilers, etc.
     * @param registrar option registrar
     */
    default void onRegistering(OptionRegistry.Registrar registrar) {
    }

    default void onReceivingOptions(OptionRegistry.ValueGetter getter) {
    }

    default void onPreprocess(FileSystem fs, Path tempDir, DataMap dataMap) throws IOException {
    }

    default void onGatheringExtraClassesInformation(ClassReader reader, String className, String superName, int access, String[] interfaces) {
    }

    default @NotNull @Unmodifiable List<@NotNull ObjectObjectImmutablePair<Process.@NotNull Run, @NotNull Supplier<@NotNull Process>>> getProcesses() {
        return List.of();
    }
}