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

package cn.maxpixel.mcdecompiler;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;

public class Properties {
    public static Path TEMP_DIR = Path.of("temp");
    public static Path DOWNLOAD_DIR = Path.of("downloads");

    public static Path getDownloadedDecompilerPath(@NotNull Info.DecompilerType type) {
        if(type == Info.DecompilerType.USER_DEFINED) throw new UnsupportedOperationException();
        return DOWNLOAD_DIR.resolve("decompiler").resolve(Objects.requireNonNull(type) + ".jar");
    }

    public static String getProperty(String name, String prop) {
        if(Info.IS_DEV || Info.MANIFEST == null)
            return Objects.requireNonNull(System.getProperty(prop), "System property \"" + prop + "\" is not set");
        return Objects.requireNonNull(Info.MANIFEST.getMainAttributes().getValue(name),
                "Guess you forgot to add the " + name + " property to the manifest!");
    }
}