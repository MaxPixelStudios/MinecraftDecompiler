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

import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final FileSystemProvider JAR_FSP;

    public static FileSystem createZipFs(Path zipPath) throws IOException {
        return createZipFs(zipPath, false);
    }

    public static FileSystem createZipFs(Path zipPath, boolean create) throws IOException {
        LOGGER.trace("Creating JarFileSystem for \"{}\"", zipPath);
        return JAR_FSP.newFileSystem(zipPath, Map.of("create", create));
    }

    public static Manifest getManifest(Class<?> c) {
        try {
            URL location = c.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                try (URLClassLoader loader = new URLClassLoader(new URL[] {location}, null)) {
                    URL url = loader.findResource(JarFile.MANIFEST_NAME);
                    if (url != null) {
                        return new Manifest(url.openStream());
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static String getProperty(Manifest man, String name, String prop) {
        if (Constants.IS_DEV || man == null)
            return Objects.requireNonNull(System.getProperty(prop), "System property \"" + prop + "\" is not set");
        return Objects.requireNonNull(man.getMainAttributes().getValue(name), "Guess you forgot to add the " +
                name + " property to the manifest!");
    }

    static {
        FileSystemProvider provider = null;
        for (FileSystemProvider p: FileSystemProvider.installedProviders()) {
            if (p.getScheme().equalsIgnoreCase("jar")) {
                provider = p;
                break;
            }
        }
        if (provider == null) throw new IllegalStateException("jar/zip file system provider does not exist");
        JAR_FSP = provider;
    }
}