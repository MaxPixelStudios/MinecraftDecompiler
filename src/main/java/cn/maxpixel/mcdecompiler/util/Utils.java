/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
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

package cn.maxpixel.mcdecompiler.util;

import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {
    public static RuntimeException wrapInRuntime(Throwable e) {
        return new RuntimeException(e);
    }

    public static void waitForProcess(Process pro) {
        Logger logger = LogManager.getLogger("Process PID: " + pro.pid());
        try(BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(pro.getErrorStream()))) {
            Thread inT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = in.readLine()) != null) logger.debug(ins);
                } catch (Throwable e) {
                    logger.catching(e);
                }
            });
            Thread errT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = err.readLine()) != null) logger.error(ins);
                } catch (Throwable e) {
                    logger.catching(e);
                }
            });
            inT.setDaemon(true);
            errT.setDaemon(true);
            inT.start();
            errT.start();
            pro.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.catching(e);
        }
    }

    public static <T> T onKeyDuplicate(T t, T u) {
        throw new IllegalArgumentException("Key \"" + t + "\" and \"" + u + "\" duplicated!");
    }

    public static boolean nameAndDescEquals(PairedMethodMapping left, PairedMethodMapping right) {
        if(left.getClass() != right.getClass() || !((left instanceof Descriptor) || (left instanceof Descriptor.Mapped)))
            throw new UnsupportedOperationException();
        boolean b = left.getUnmappedName().equals(right.getUnmappedName()) && left.getMappedName().equals(right.getMappedName());
        if(left.isDescriptor()) b &= left.asDescriptor().getUnmappedDescriptor().equals(right.asDescriptor().getUnmappedDescriptor());
        if(left.isMappedDescriptor()) b &= left.asMappedDescriptor().getMappedDescriptor().equals(right.asMappedDescriptor().getMappedDescriptor());
        return b;
    }
}