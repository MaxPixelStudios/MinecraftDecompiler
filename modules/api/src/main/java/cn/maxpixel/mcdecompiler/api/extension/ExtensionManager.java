package cn.maxpixel.mcdecompiler.api.extension;

import cn.maxpixel.mcdecompiler.remapper.processing.ClassProcessor;
import cn.maxpixel.mcdecompiler.remapper.processing.Process;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public final class ExtensionManager {
    public static final OptionRegistry OPTION_REGISTRY = new OptionRegistry();

    private static final ServiceLoader<Extension> EXTENSIONS = ServiceLoader.load(Extension.class);

    static {
        OPTION_REGISTRY.registerOptions(EXTENSIONS);
    }

    public static void init() {}

    static void receiveOptions() {
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
}

class SetupHelper {
    static {
        ExtensionManager.receiveOptions();
        ExtensionManager.loadProcesses();
    }

    public static void setup() {}
}