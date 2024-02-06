/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.decompiler.thread;

import cn.maxpixel.mcdecompiler.util.Logging;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.util.TextUtil;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FernFlowerDecompileThread extends Thread {
    private static final Logger LOGGER = Logging.getLogger("FernFlower");

    private final File source;
    private final File target;
    private final File[] libs;

    public FernFlowerDecompileThread(File source, File target, File[] libs) {
        super("FernFlower-Decompile");
        this.source = source;
        this.target = target;
        this.libs = libs;
    }

    @Override
    public void run() {
        Map<String, Object> options = Map.of(
//                "log", "TRACE",
                "dgs", "1",
                "asc", "1",
                "rsy", "1",
                "bsm", "1"
        );
        ConsoleDecompiler decompiler = new AccessibleConsoleDecompiler(target, options, LOGGER);
        decompiler.addSource(source);
        for (File lib : libs) decompiler.addLibrary(lib);
        decompiler.decompileContext();
    }
}

class AccessibleConsoleDecompiler extends ConsoleDecompiler {
    public AccessibleConsoleDecompiler(File destination, Map<String, Object> options, Logger logger) {
        super(destination, options, new ThreadedLogger(logger));
    }
}

class ThreadedLogger extends IFernflowerLogger {
    private static final EnumMap<Severity, Level> LEVEL_MAP = new EnumMap<>(Severity.class);
    static {
        LEVEL_MAP.put(Severity.TRACE, Level.FINEST);
        LEVEL_MAP.put(Severity.INFO, Level.FINE);
        LEVEL_MAP.put(Severity.WARN, Level.WARNING);
        LEVEL_MAP.put(Severity.ERROR, Level.WARNING);
    }

    private final Logger logger;
    private final ThreadLocal<AtomicInteger> indent = ThreadLocal.withInitial(AtomicInteger::new);

    public ThreadedLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void writeMessage(String message, Severity severity) {
        Level l = LEVEL_MAP.get(severity);
        if (accepts(severity) && logger.isLoggable(l)) {
            logger.log(l, "{0}: {1}{2}{3}", new Object[] {Thread.currentThread().getName(), severity.prefix,
                    TextUtil.getIndentString(indent.get().get()), message});
        }
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable t) {
        Level l = LEVEL_MAP.get(severity);
        if (accepts(severity) && logger.isLoggable(l)) {
            writeMessage(message, severity);
            logger.log(l, null, t);
        }
    }

    public void startProcessingClass(String className) {
        if (accepts(Severity.INFO)) {
            writeMessage("PreProcessing class " + className, Severity.INFO);
            indent.get().incrementAndGet();
        }
    }

    public void endProcessingClass() {
        if (accepts(Severity.INFO)) {
            indent.get().decrementAndGet();
            writeMessage("... done", Severity.INFO);
        }
    }

    @Override
    public void startReadingClass(String className) {
        if (accepts(Severity.INFO)) {
            writeMessage("Decompiling class " + className, Severity.INFO);
            indent.get().incrementAndGet();
        }
    }

    @Override
    public void endReadingClass() {
        if (accepts(Severity.INFO)) {
            indent.get().decrementAndGet();
            writeMessage("... done", Severity.INFO);
        }
    }

    @Override
    public void startClass(String className) {
        if (accepts(Severity.INFO)) {
            writeMessage("Processing class " + className, Severity.TRACE);
            indent.get().decrementAndGet();
        }
    }

    @Override
    public void endClass() {
        if (accepts(Severity.INFO)) {
            indent.get().decrementAndGet();
            writeMessage("... proceeded", Severity.TRACE);
        }
    }

    @Override
    public void startMethod(String methodName) {
        if (accepts(Severity.INFO)) {
            writeMessage("Processing method " + methodName, Severity.TRACE);
            indent.get().decrementAndGet();
        }
    }

    @Override
    public void endMethod() {
        if (accepts(Severity.INFO)) {
            indent.get().decrementAndGet();
            writeMessage("... proceeded", Severity.TRACE);
        }
    }

    @Override
    public void startWriteClass(String className) {
        if (accepts(Severity.INFO)) {
            writeMessage("Writing class " + className, Severity.TRACE);
            indent.get().decrementAndGet();
        }
    }

    @Override
    public void endWriteClass() {
        if (accepts(Severity.INFO)) {
            indent.get().decrementAndGet();
            writeMessage("... written", Severity.TRACE);
        }
    }
}