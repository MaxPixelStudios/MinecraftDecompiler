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

import cn.maxpixel.mcdecompiler.common.app.Constants;
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

public class ForgeFlowerDecompiler implements IExternalResourcesDecompiler, ILibRecommendedDecompiler {
    private static final String VERSION = Decompilers.getProperty("ForgeFlower-Version", "forgeflower.version");
    private static final URI RESOURCE = URI.create("https://maven.minecraftforge.net/net/minecraftforge/forgeflower/" + VERSION + "/forgeflower-" + VERSION + ".jar");
    private static final URI RESOURCE_HASH = URI.create("https://maven.minecraftforge.net/net/minecraftforge/forgeflower/" + VERSION + "/forgeflower-" + VERSION + ".jar.sha1");
    public static final String NAME = "forgeflower";
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
    public void extractTo(Path extractPath) throws IOException {
        this.decompilerJarPath = extractPath.resolve("decompiler.jar");
        Files.copy(DownloadingUtil.getRemoteResource(DecompilerType.FORGEFLOWER.getDownloadedDecompilerPath(), RESOURCE, RESOURCE_HASH),
                decompilerJarPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void decompile(@NotNull Path source, @NotNull Path target) throws IOException {
        checkArgs(source, target);
        try (ExternalJarClassLoader cl = new ExternalJarClassLoader(new URL[] {decompilerJarPath.toUri().toURL()})) {
            File[] sources;
            Path abstractMethodParameterNames = Directories.TEMP_DIR.resolve(Constants.FERNFLOWER_ABSTRACT_PARAMETER_NAMES);
            if (Files.exists(abstractMethodParameterNames))
                sources = new File[] {source.toFile(), abstractMethodParameterNames.toAbsolutePath().normalize().toFile()};
            else sources = new File[] {source.toFile()};
            Thread thread = (Thread) cl.loadClass("cn.maxpixel.mcdecompiler.decompiler.thread.ForgeFlowerDecompileThread")
                    .getConstructor(File[].class, File[].class, File.class)
                    .newInstance(sources, libs, target.toFile());
            thread.start();
            while (thread.isAlive()) Thread.onSpinWait();
        } catch(ReflectiveOperationException e) {
            LogManager.getLogger().fatal("Failed to load ForgeFlower", e);
            throw MiscUtils.wrapInRuntime(e);
        }
    }

    @Override
    public void receiveLibs(@NotNull ObjectSet<Path> libs) {
        this.libs = libs.stream().map(Path::toFile).toArray(File[]::new);
    }
}