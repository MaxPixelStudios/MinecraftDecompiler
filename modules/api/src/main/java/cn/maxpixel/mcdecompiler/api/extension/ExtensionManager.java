package cn.maxpixel.mcdecompiler.api.extension;

import cn.maxpixel.mcdecompiler.common.app.util.DataMap;
import cn.maxpixel.mcdecompiler.remapper.processing.ClassProcessor;
import cn.maxpixel.mcdecompiler.remapper.processing.Process;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public final class ExtensionManager {
    public static final OptionRegistry OPTION_REGISTRY = new OptionRegistry();

    private static final ServiceLoader<Extension> EXTENSIONS = ServiceLoader.load(Extension.class);

    static {
        OPTION_REGISTRY.registerOptions(EXTENSIONS);
    }

    public static void init() {}

    /**
     * Call the extensions to let them receive their options.
     * This would be automatically called on the first use of {@link cn.maxpixel.mcdecompiler.api.MinecraftDecompiler},
     * but it can be called multiple times manually as needed.
     */
    public static void receiveOptions() {
        OPTION_REGISTRY.receiveOptions(EXTENSIONS);
    }

    static void loadProcesses() {
        for (Extension extension : EXTENSIONS) {
            for (ObjectObjectImmutablePair<Process.Run, Supplier<Process>> process : extension.getProcesses()) {
                ClassProcessor.addProcess(process.left(), process.right());
            }
        }
    }

    public static void setup() {
        SetupHelper.setup();
    }

    public static void onPreprocess(FileSystem fs, Path tempDir, DataMap dataMap) throws IOException {
        for (Extension extension : EXTENSIONS) {
            extension.onPreprocess(fs, tempDir, dataMap);
        }
    }
}

class SetupHelper {
    static {
        ExtensionManager.receiveOptions();
        ExtensionManager.loadProcesses();
    }

    public static void setup() {}
}