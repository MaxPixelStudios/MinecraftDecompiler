package cn.maxpixel.mcdecompiler.decompiler.thread;

import cn.maxpixel.mcdecompiler.util.Logging;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

public class VineFlowerDecompileThread extends Thread {
    private static final Logger LOGGER = Logging.getLogger("VineFlower");

    private final File[] sources;
    private final File[] libraries;
    private final File target;

    public VineFlowerDecompileThread(File[] sources, File[] libraries, File target) {
        super("VineFlower-Decompile");
        this.sources = sources;
        this.libraries = libraries;
        this.target = target;
    }

    @Override
    public void run() {
        Map<String, Object> options = Map.of(
//                "log", "TRACE",
                "asc", "1",
                "bsm", "1"
        );
        ConsoleDecompiler decompiler = new AccessibleConsoleDecompiler(target, options, LOGGER);
        for(File source : sources) decompiler.addSource(source);
        for(File library : libraries) decompiler.addLibrary(library);
        decompiler.decompileContext();
    }
}