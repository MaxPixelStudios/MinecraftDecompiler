/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2025 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.common.app.util;

import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.MessageDigest;

public class AppUtils {
    public static StringBuilder createHashString(MessageDigest md) {
        StringBuilder out = new StringBuilder();
        for (byte b : md.digest()) {
            String hex = Integer.toHexString(Byte.toUnsignedInt(b));
            if (hex.length() < 2) out.append('0');
            out.append(hex);
        }
        return out;
    }

    public static String file2Native(@NotNull String fileName) {
        return fileName.replace('\\', '/').replace(".class", "");
    }

    public static void waitFor(Process pro) {
        Logger logger = LogManager.getLogger("Process PID: " + pro.pid());
        try (BufferedReader in = pro.inputReader();
             BufferedReader err = pro.errorReader()) {
            Thread inT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = in.readLine()) != null) logger.debug("{}", ins);// avoid unexpected interpolation
                } catch (Throwable e) {
                    logger.warn("Exception thrown", e);
                }
            }, "Process-PID-" + pro.pid() + "-STDOUT-Reader");
            Thread errT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = err.readLine()) != null) logger.warn("{}", ins);
                } catch (Throwable e) {
                    logger.warn("Exception thrown", e);
                }
            }, "Process-PID-" + pro.pid() + "-STDERR-Reader");
            inT.setDaemon(true);
            errT.setDaemon(true);
            inT.start();
            errT.start();
            pro.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Exception thrown", e);
        }
    }
}
