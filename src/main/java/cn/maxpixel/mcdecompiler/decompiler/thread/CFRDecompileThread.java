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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.OptionsImpl;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

public class CFRDecompileThread extends Thread {
    private final String source;
    private final String target;
    private final String libs;

    public CFRDecompileThread(String source, String target, String libs) {
        super("CFR-Decompile");
        this.source = source;
        this.target = target;
        this.libs = libs;
    }

    @Override
    public void run() {
        Object2ObjectOpenHashMap<String, String> options = new Object2ObjectOpenHashMap<>();
        options.put(OptionsImpl.FORCE_AGGRESSIVE_EXCEPTION_AGG.getName(), "true");
        options.put(OptionsImpl.CLOBBER_FILES.getName(), "true");
        options.put(OptionsImpl.ECLIPSE.getName(), "false");
        options.put(OptionsImpl.EXTRA_CLASS_PATH.getName(), libs);
        options.put(OptionsImpl.OUTPUT_PATH.getName(), target);
        options.put(OptionsImpl.REMOVE_BAD_GENERICS.getName(), "false");
        options.put(OptionsImpl.REMOVE_DEAD_CONDITIONALS.getName(), "false");
        options.put(OptionsImpl.JAR_FILTER.getName(), "^(net\\.minecraft|com\\.mojang\\.(blaze3d|math|realmsclient))\\.*");
        CfrDriver cfr = new CfrDriver.Builder().withOptions(options).build();
        PrintStream sysErr = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            private static final Logger LOGGER = Logging.getLogger("CFR");

            @Override
            public void write(int b) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void write(byte[] b, int off, int len) {
                LOGGER.fine(() -> new String(b, off, len - 1));
            }
        }));
        cfr.analyse(ObjectLists.singleton(source));
        System.setErr(sysErr);
    }
}