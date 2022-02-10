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

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.Properties;
import cn.maxpixel.mcdecompiler.decompiler.thread.ExternalJarClassLoader;
import cn.maxpixel.mcdecompiler.util.DownloadUtil;
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class ForgeFlowerDecompiler extends AbstractLibRecommendedDecompiler implements IExternalResourcesDecompiler {
    private static final URI RESOURCE = URI.create("https://maven.minecraftforge.net/net/minecraftforge/forgeflower/1.5.498.23/forgeflower-1.5.498.23.jar");
    private static final URI RESOURCE_HASH = URI.create("https://maven.minecraftforge.net/net/minecraftforge/forgeflower/1.5.498.23/forgeflower-1.5.498.23.jar.sha1");
    public static final String FERNFLOWER_ABSTRACT_PARAMETER_NAMES = "fernflower_abstract_parameter_names.txt";
    private Path decompilerJarPath;
    private ExternalJarClassLoader cl;

    ForgeFlowerDecompiler() {}

    @Override
    public SourceType getSourceType() {
        return SourceType.DIRECTORY;
    }

    @Override
    public void extractTo(Path extractPath) throws IOException {
        this.decompilerJarPath = extractPath.resolve("decompiler.jar");
        Files.copy(DownloadUtil.getRemoteResource(Properties.getDownloadedDecompilerPath(Info.DecompilerType.FORGEFLOWER), RESOURCE, RESOURCE_HASH),
                decompilerJarPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void decompile(Path source, Path target) throws IOException {
        checkArgs(source, target);
        try {
            if(cl == null) cl = new ExternalJarClassLoader(new URL[] {decompilerJarPath.toUri().toURL()}, getClass().getClassLoader());
            File[] sources;
            Path abstractMethodParameterNames = Properties.TEMP_DIR.resolve(FERNFLOWER_ABSTRACT_PARAMETER_NAMES);
            if(Files.exists(abstractMethodParameterNames))
                sources = new File[] {source.toFile(), abstractMethodParameterNames.toAbsolutePath().normalize().toFile()};
            else sources = new File[] {source.toFile()};
            Thread thread = (Thread) cl.loadClass("cn.maxpixel.mcdecompiler.decompiler.thread.ForgeFlowerDecompileThread")
                    .getConstructor(File[].class, File[].class, File.class)
                    .newInstance(sources, listLibs().stream().map(File::new).toArray(File[]::new), target.toFile());
            thread.start();
            while(thread.isAlive()) Thread.onSpinWait();
        } catch(ReflectiveOperationException e) {
            Logging.getLogger().log(Level.SEVERE, "Failed to load ForgeFlower", e);
            throw Utils.wrapInRuntime(e);
        }
    }
}