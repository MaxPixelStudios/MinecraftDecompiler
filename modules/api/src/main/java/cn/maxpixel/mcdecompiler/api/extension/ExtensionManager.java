package cn.maxpixel.mcdecompiler.api.extension;

import java.util.ServiceLoader;

public final class ExtensionManager {
    public static final OptionRegistry OPTION_REGISTRY = new OptionRegistry();

    private static final ServiceLoader<Extension> EXTENSIONS = ServiceLoader.load(Extension.class);

    static {
        OPTION_REGISTRY.registerOptions(EXTENSIONS);
    }

    public static void init() {}

    static void receiveOptions0() {
        OPTION_REGISTRY.receiveOptions(EXTENSIONS);
    }

    public static void receiveOptions() {
        OptionReceivingHelper.receive();
    }
}

class OptionReceivingHelper {
    static {
        ExtensionManager.receiveOptions0();
    }

    public static void receive() {}
}