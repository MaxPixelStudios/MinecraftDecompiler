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

package cn.maxpixel.mcdecompiler.decompiler;

import cn.maxpixel.mcdecompiler.common.app.DecompilerType;
import cn.maxpixel.mcdecompiler.common.app.Directories;
import cn.maxpixel.mcdecompiler.common.app.util.DownloadingUtil;
import cn.maxpixel.mcdecompiler.common.app.util.MiscUtils;
import cn.maxpixel.mcdecompiler.decompiler.thread.ExternalJarClassLoader;
import cn.maxpixel.rewh.logging.LogManager;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static cn.maxpixel.mcdecompiler.common.app.Constants.FERNFLOWER_ABSTRACT_PARAMETER_NAMES;

public class VineflowerDecompiler implements IExternalResourcesDecompiler, ILibRecommendedDecompiler {
    private static final String VERSION = Decompilers.getProperty("VineFlower-Version", "vineflower.version");
    private static final URI RESOURCE = URI.create("https://repo1.maven.org/maven2/org/vineflower/vineflower/" + VERSION + "/vineflower-" + VERSION + ".jar");
    private static final URI RESOURCE_HASH = URI.create("https://repo1.maven.org/maven2/org/vineflower/vineflower/" + VERSION + "/vineflower-" + VERSION + ".jar.sha1");
    public static final String NAME = "vineflower";
    private File[] libs = new File[0];
    private Path decompilerJarPath;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.DIRECTORY;
    }

    @Override
    public void decompile(@NotNull Path source, @NotNull Path targetDir) throws IOException {
        checkArgs(source, targetDir);
        try (ExternalJarClassLoader cl = new ExternalJarClassLoader(new URL[] {decompilerJarPath.toUri().toURL()})) {
            File[] sources;
            Path abstractMethodParameterNames = Directories.TEMP_DIR.resolve(FERNFLOWER_ABSTRACT_PARAMETER_NAMES);
            if (Files.exists(abstractMethodParameterNames))
                sources = new File[] {source.toFile(), abstractMethodParameterNames.toAbsolutePath().normalize().toFile()};
            else sources = new File[] {source.toFile()};
            Thread thread = (Thread) cl.loadClass("cn.maxpixel.mcdecompiler.decompiler.thread.VineFlowerDecompileThread")
                    .getConstructor(File[].class, File[].class, File.class)
                    .newInstance(sources, libs, targetDir.toFile());
            thread.start();
            while (thread.isAlive()) Thread.onSpinWait();
        } catch(ReflectiveOperationException e) {
            LogManager.getLogger().fatal("Failed to load VineFlower", e);
            throw MiscUtils.wrapInRuntime(e);
        }
    }

    @Override
    public void extractTo(Path extractPath) throws IOException {
        this.decompilerJarPath = extractPath.resolve("decompiler.jar");
        Files.copy(DownloadingUtil.getRemoteResource(DecompilerType.VINEFLOWER.getDownloadedDecompilerPath(), RESOURCE, RESOURCE_HASH),
                decompilerJarPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void receiveLibs(@NotNull ObjectSet<Path> libs) {
        this.libs = libs.stream().map(Path::toFile).toArray(File[]::new);
    }
}