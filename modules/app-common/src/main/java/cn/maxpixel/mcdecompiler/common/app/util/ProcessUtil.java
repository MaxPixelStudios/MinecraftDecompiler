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

package cn.maxpixel.mcdecompiler.common.app.util;

import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtil {
    public static void waitFor(Process pro) {
        Logger logger = LogManager.getLogger("Process PID: " + pro.pid());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(pro.getErrorStream()))) {
            Thread inT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = in.readLine()) != null) logger.debug("{}", ins);// avoid unexpected interpolation
                } catch (Throwable e) {
                    logger.warn("Exception thrown", e);
                }
            });
            Thread errT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = err.readLine()) != null) logger.warn("{}", ins);
                } catch (Throwable e) {
                    logger.warn("Exception thrown", e);
                }
            });
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
