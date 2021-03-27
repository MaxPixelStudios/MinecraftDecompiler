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

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

public class JarUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final FileSystemProvider JAR_FSP;

    public static FileSystemProvider getJarFileSystemProvider() {
        return JAR_FSP;
    }

    public static FileSystem createZipFs(Path zipPath) throws IOException {
        LOGGER.debug("Creating JarFileSystem for \"{}\"", zipPath);
        return JAR_FSP.newFileSystem(zipPath, Object2ObjectMaps.singleton("create", "true"));
    }

    static {
        FileSystemProvider provider = null;
        for (FileSystemProvider p: FileSystemProvider.installedProviders()) {
            if(p.getScheme().equalsIgnoreCase("jar")) {
                provider = p;
                break;
            }
        }
        if(provider == null) throw new IllegalStateException("jar/zip file system provider does not exist");
        JAR_FSP = provider;
    }
}