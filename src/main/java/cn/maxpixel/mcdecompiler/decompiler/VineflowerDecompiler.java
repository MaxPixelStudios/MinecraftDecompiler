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

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.Properties;
import cn.maxpixel.mcdecompiler.decompiler.thread.ExternalJarClassLoader;
import cn.maxpixel.mcdecompiler.util.DownloadingUtil;
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

import static cn.maxpixel.mcdecompiler.decompiler.ForgeFlowerDecompiler.FERNFLOWER_ABSTRACT_PARAMETER_NAMES;

public class VineflowerDecompiler implements IExternalResourcesDecompiler, ILibRecommendedDecompiler {
    private static final String VERSION = Properties.getProperty("VineFlower-Version", "vineflower.version");
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
            Path abstractMethodParameterNames = Properties.TEMP_DIR.resolve(FERNFLOWER_ABSTRACT_PARAMETER_NAMES);
            if (Files.exists(abstractMethodParameterNames))
                sources = new File[] {source.toFile(), abstractMethodParameterNames.toAbsolutePath().normalize().toFile()};
            else sources = new File[] {source.toFile()};
            Thread thread = (Thread) cl.loadClass("cn.maxpixel.mcdecompiler.decompiler.thread.VineFlowerDecompileThread")
                    .getConstructor(File[].class, File[].class, File.class)
                    .newInstance(sources, libs, targetDir.toFile());
            thread.start();
            while (thread.isAlive()) Thread.onSpinWait();
        } catch(ReflectiveOperationException e) {
            Logging.getLogger().log(Level.SEVERE, "Failed to load VineFlower", e);
            throw Utils.wrapInRuntime(e);
        }
    }

    @Override
    public void extractTo(Path extractPath) throws IOException {
        this.decompilerJarPath = extractPath.resolve("decompiler.jar");
        Files.copy(DownloadingUtil.getRemoteResource(Properties.getDownloadedDecompilerPath(Info.DecompilerType.VINEFLOWER), RESOURCE, RESOURCE_HASH),
                decompilerJarPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void receiveLibs(@NotNull ObjectSet<Path> libs) {
        this.libs = libs.stream().map(Path::toFile).toArray(File[]::new);
    }
}