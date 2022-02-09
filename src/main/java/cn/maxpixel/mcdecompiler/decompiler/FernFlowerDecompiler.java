/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.decompiler;

import cn.maxpixel.mcdecompiler.util.Logging;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

// Do not extend AbstractLibRecommendedDecompiler because this decompiler cannot read some of the libraries successfully
// TODO: Make FernFlowerDecompiler read all libraries successfully
public class FernFlowerDecompiler/* extends AbstractLibRecommendedDecompiler */ implements IDecompiler {
    FernFlowerDecompiler() {}

    @Override
    public SourceType getSourceType() {
        return SourceType.DIRECTORY;
    }

    @Override
    public void decompile(Path source, Path target) throws IOException {
        checkArgs(source, target);
        Map<String, Object> options = Map.of(
                "log", "TRACE",
                "dgs", "1",
                "asc", "1",
                "rsy", "1"
        );
        ConsoleDecompiler decompiler = new AccessibleConsoleDecompiler(target.toFile(), options,
                new PrintStreamLogger(new PrintStream(new OutputStream() {
                    private static final Logger LOGGER = Logging.getLogger("FernFlower");

                    @Override
                    public void write(int b) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void write(byte[] b, int off, int len) {
                        LOGGER.fine(new String(b, off, len).stripTrailing());
                    }
                })));
        decompiler.addSource(source.toFile());
//		ObjectList<String> libs = listLibs();
//		for(int index = 0; index < libs.size(); index++) decompiler.addLibrary(new File(libs.get(index)));
        decompiler.decompileContext();
    }

    private static class AccessibleConsoleDecompiler extends ConsoleDecompiler {
        public AccessibleConsoleDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger) {
            super(destination, options, logger);
        }
    }
}