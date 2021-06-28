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

package cn.maxpixel.mcdecompiler;

import cn.maxpixel.mcdecompiler.decompiler.*;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.JarUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import cn.maxpixel.mcdecompiler.util.VersionManifest;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class MinecraftDecompiler {
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .proxy(ProxySelector.of((InetSocketAddress) MinecraftDecompilerCommandLine.INTERNAL_PROXY.address()))
            .executor(ForkJoinPool.commonPool())
            .connectTimeout(Duration.ofSeconds(10L))
            .build();

    private static final Logger LOGGER = LogManager.getLogger();
    private final Deobfuscator deobfuscator;
    private String version;
    private Info.SideType type;

    {
        Path tempPath = Properties.get(Properties.Key.TEMP_DIR);
        FileUtil.deleteDirectoryIfExists(tempPath);
        try {
            Files.createDirectories(tempPath);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtil.deleteDirectoryIfExists(Properties.get(Properties.Key.TEMP_DIR))));
    }

    public MinecraftDecompiler(String version, Info.SideType type, Deobfuscator deobfuscator) {
        this.version = Objects.requireNonNull(version, "version cannot be null!");
        this.type = Objects.requireNonNull(type, "type cannot be null!");
        downloadJar(version, type);
        this.deobfuscator = deobfuscator;
    }

    public MinecraftDecompiler(String version, Info.SideType type) {
        this(version, type, new Deobfuscator(version, type));
    }

    public MinecraftDecompiler(String version, Info.SideType type, String mappingPath) throws FileNotFoundException {
        this(version, type, new Deobfuscator(mappingPath));
    }

    public MinecraftDecompiler(String version, Deobfuscator deobfuscator) {
        this.version = version;
        this.deobfuscator = deobfuscator;
    }

    public MinecraftDecompiler(String version, String mappingPath) throws FileNotFoundException {
        this(version, new Deobfuscator(mappingPath));
    }

    public MinecraftDecompiler(Deobfuscator deobfuscator) {
        this.deobfuscator = deobfuscator;
    }

    public MinecraftDecompiler(String mappingPath) throws FileNotFoundException {
        this(new Deobfuscator(mappingPath));
    }

    private void downloadJar(String version, Info.SideType type) {
        Path p = Properties.getDownloadedMcJarPath(version, type);
        if(Files.notExists(p)) {
            try {
                HttpRequest request = HttpRequest.newBuilder(
                        URI.create(VersionManifest.get(version)
                                .get("downloads")
                                .getAsJsonObject()
                                .get(type.toString())
                                .getAsJsonObject()
                                .get("url")
                                .getAsString()
                        ))
                        .build();
                LOGGER.info("Downloading jar...");
                HttpClient.newHttpClient()
                        .send(request, HttpResponse.BodyHandlers.ofFile(FileUtil.ensureFileExist(p), WRITE, TRUNCATE_EXISTING));
            } catch(IOException | InterruptedException e) {
                LOGGER.fatal("Error downloading Minecraft jar");
                throw Utils.wrapInRuntime(LOGGER.throwing(e));
            }
        }
    }

    public void deobfuscate() {
        if(version == null && type == null) {
            deobfuscate(Properties.get(Properties.Key.INPUT_JAR), Properties.getOutputDeobfuscatedJarPath());
        } else {
            Objects.requireNonNull(version, "version cannot be null!");
            Objects.requireNonNull(type, "type cannot be null!");
            deobfuscate(Properties.getDownloadedMcJarPath(version, type),
                    Properties.getOutputDeobfuscatedJarPath(version, type));
        }
    }
    public void deobfuscate(Path input, Path output) {
        try {
            deobfuscator.deobfuscate(input, output);
        } catch (IOException e) {
            LOGGER.fatal("Error deobfuscating");
        }
    }

    public void decompile(Info.DecompilerType decompilerType) {
        Path decompileDir = Properties.getOutputDecompiledDirectory(version, type).toAbsolutePath().normalize();
        Path outputDeobfuscatedJarPath = Properties.getOutputDeobfuscatedJarPath(version, type).toAbsolutePath().normalize();
        if(Files.notExists(outputDeobfuscatedJarPath))
            throw new IllegalArgumentException("Please deobfuscate first or run decompile(DecompilerType, Path, Path) method");
        decompile(decompilerType, outputDeobfuscatedJarPath, decompileDir);
    }

    public void decompile(Info.DecompilerType decompilerType, Path inputJar, Path outputDir) {
        try(FileSystem jarFs = JarUtil.getJarFileSystemProvider().newFileSystem(inputJar, Object2ObjectMaps.emptyMap())) {
            FileUtil.deleteDirectoryIfExists(outputDir);
            Files.createDirectories(outputDir);
            IDecompiler decompiler = Decompilers.get(decompilerType);
            Path libDownloadPath = Properties.getDownloadedLibPath().toAbsolutePath().normalize();
            FileUtil.ensureDirectoryExist(libDownloadPath);
            if(decompiler instanceof IExternalResourcesDecompiler)
                ((IExternalResourcesDecompiler) decompiler).extractTo(Properties.get(Properties.Key.TEMP_DIR).toAbsolutePath().normalize());
            if(decompiler instanceof ILibRecommendedDecompiler && version != null)
                ((ILibRecommendedDecompiler) decompiler).downloadLib(libDownloadPath, version);
            LOGGER.info("Decompiling using \"{}\"", decompilerType);
            switch(decompiler.getSourceType()) {
                case DIRECTORY:
                    Path decompileClasses = Properties.getTempDecompileClassesPath().toAbsolutePath().normalize();
                    FileUtil.copyDirectory(jarFs.getPath("/net"), decompileClasses);
                    try(Stream<Path> mjDirs = Files.list(jarFs.getPath("/com", "mojang")).filter(p ->
                            !(p.endsWith("authlib") || p.endsWith("bridge") || p.endsWith("brigadier") || p.endsWith("datafixers") ||
                                    p.endsWith("serialization") || p.endsWith("util")))) {
                        mjDirs.forEach(p -> FileUtil.copyDirectory(p, decompileClasses));
                    }
                    decompiler.decompile(decompileClasses, outputDir);
                    break;
                case FILE:
                    decompiler.decompile(inputJar, outputDir);
                    break;
            }
        } catch (IOException e) {
            LOGGER.fatal("Error when decompiling", e);
        }
    }

    public void decompileCustomized(String customizedDecompilerName) {
        Path decompileDir = Properties.getOutputDecompiledDirectory(version, type).toAbsolutePath().normalize();
        Path outputDeobfuscatedJarPath = Properties.getOutputDeobfuscatedJarPath(version, type).toAbsolutePath().normalize();
        if(Files.notExists(outputDeobfuscatedJarPath))
            throw new IllegalArgumentException("Please deobfuscate first or run decompile(DecompilerType, Path, Path) method");
        decompileCustomized(customizedDecompilerName, outputDeobfuscatedJarPath, decompileDir);
    }

    public void decompileCustomized(String customizedDecompilerName, Path inputJar, Path outputDir) {
        try(FileSystem jarFs = JarUtil.getJarFileSystemProvider().newFileSystem(inputJar, Object2ObjectMaps.emptyMap())) {
            FileUtil.deleteDirectoryIfExists(outputDir);
            Files.createDirectories(outputDir);
            ICustomDecompiler decompiler = Decompilers.getCustom(customizedDecompilerName);
            Path libDownloadPath = Properties.getDownloadedLibPath().toAbsolutePath().normalize();
            FileUtil.ensureDirectoryExist(libDownloadPath);
            if(decompiler instanceof IExternalResourcesDecompiler)
                ((IExternalResourcesDecompiler) decompiler).extractTo(Properties.get(Properties.Key.TEMP_DIR).toAbsolutePath().normalize());
            if(decompiler instanceof ILibRecommendedDecompiler && version != null)
                ((ILibRecommendedDecompiler) decompiler).downloadLib(libDownloadPath, version);
            LOGGER.info("Decompiling using customized decompiler \"{}\"", customizedDecompilerName);
            switch(decompiler.getSourceType()) {
                case DIRECTORY:
                    Path decompileClasses = Properties.getTempDecompileClassesPath().toAbsolutePath().normalize();
                    FileUtil.copyDirectory(jarFs.getPath("/net"), decompileClasses);
                    try(Stream<Path> mjDirs = Files.list(jarFs.getPath("/com", "mojang")).filter(p ->
                            !(p.endsWith("authlib") || p.endsWith("bridge") || p.endsWith("brigadier") || p.endsWith("datafixers") ||
                                    p.endsWith("serialization") || p.endsWith("util")))) {
                        Path decompiledMj = decompileClasses.resolve("com").resolve("mojang");
                        mjDirs.forEach(p -> FileUtil.copyDirectory(p, decompiledMj));
                    }
                    decompiler.decompile(decompileClasses, outputDir);
                    break;
                case FILE:
                    decompiler.decompile(inputJar, outputDir);
                    break;
            }
        } catch (IOException e) {
            LOGGER.fatal("Error when decompiling", e);
        }
    }
}