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