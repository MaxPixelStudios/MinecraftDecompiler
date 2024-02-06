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
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class CFRDecompiler implements IExternalResourcesDecompiler, ILibRecommendedDecompiler {
    private static final String VERSION = Properties.getProperty("CFR-Version", "cfr.version");
    private static final URI RESOURCE = URI.create("https://repo1.maven.org/maven2/org/benf/cfr/" + VERSION + "/cfr-" + VERSION + ".jar");
    private static final URI RESOURCE_HASH = URI.create("https://repo1.maven.org/maven2/org/benf/cfr/" + VERSION + "/cfr-" + VERSION + ".jar.sha1");
    private ObjectSet<String> libs = ObjectSets.emptySet();
    private Path decompilerJarPath;
    public static final String NAME = "cfr";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.FILE;
    }

    @Override
    public void decompile(@NotNull Path source, @NotNull Path target) throws IOException {
        checkArgs(source, target);
        try (ExternalJarClassLoader cl = new ExternalJarClassLoader(new URL[] {decompilerJarPath.toUri().toURL()})) {
            Thread thread = (Thread) cl.loadClass("cn.maxpixel.mcdecompiler.decompiler.thread.CFRDecompileThread")
                    .getConstructor(String.class, String.class, String.class)
                    .newInstance(source.toString(), target.toString(), String.join(Info.PATH_SEPARATOR, libs));
            thread.start();
            while(thread.isAlive()) Thread.onSpinWait();
        } catch(ReflectiveOperationException e) {
            Logging.getLogger().log(Level.SEVERE, "Failed to load CFR", e);
            throw Utils.wrapInRuntime(e);
        }
    }

    @Override
    public void extractTo(Path extractPath) throws IOException {
        this.decompilerJarPath = extractPath.resolve("decompiler.jar");
        Files.copy(DownloadingUtil.getRemoteResource(Properties.getDownloadedDecompilerPath(Info.DecompilerType.CFR), RESOURCE, RESOURCE_HASH),
                decompilerJarPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void receiveLibs(@NotNull ObjectSet<Path> libs) {
        this.libs = libs.stream().map(p -> p.toAbsolutePath().normalize().toString())
                .collect(ObjectOpenHashSet.toSetWithExpectedSize(libs.size()));
    }
}