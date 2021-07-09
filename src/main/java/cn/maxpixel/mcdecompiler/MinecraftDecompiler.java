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

import cn.maxpixel.mcdecompiler.decompiler.Decompilers;
import cn.maxpixel.mcdecompiler.decompiler.IDecompiler;
import cn.maxpixel.mcdecompiler.decompiler.IExternalResourcesDecompiler;
import cn.maxpixel.mcdecompiler.decompiler.ILibRecommendedDecompiler;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.JarUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import cn.maxpixel.mcdecompiler.util.VersionManifest;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
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
    private static final Logger LOGGER = LogManager.getLogger();
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .proxy(ProxySelector.of((InetSocketAddress) MinecraftDecompilerCommandLine.INTERNAL_PROXY.address()))
            .executor(ForkJoinPool.commonPool())
            .connectTimeout(Duration.ofSeconds(10L))
            .build();

    private final Options options;
    private final Deobfuscator deobfuscator;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtil.deleteDirectoryIfExists(Properties.get(Properties.Key.TEMP_DIR))));
    }

    public MinecraftDecompiler(Options options) {
        Path tempPath = Properties.get(Properties.Key.TEMP_DIR);
        FileUtil.deleteDirectoryIfExists(tempPath);
        try {
            Files.createDirectories(tempPath);
        } catch (IOException e) {
            LOGGER.fatal("Error creating temp directory");
            throw Utils.wrapInRuntime(LOGGER.throwing(e));
        }
        this.options = options;
        this.deobfuscator = options.buildDeobfuscator();
        if(options.shouldDownloadJar()) downloadJar(options.version(), options.type());
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
                        )).build();
                LOGGER.info("Downloading jar...");
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(FileUtil.ensureFileExist(p), WRITE, TRUNCATE_EXISTING));
            } catch(IOException | InterruptedException e) {
                LOGGER.fatal("Error downloading Minecraft jar");
                throw Utils.wrapInRuntime(LOGGER.throwing(e));
            }
        }
    }

    public void deobfuscate() {
        if(options.shouldDownloadJar()) {
            deobfuscate(Properties.getOutputDeobfuscatedJarPath(options.version(), options.type()));
        } else {
            deobfuscate(Properties.getOutputDeobfuscatedJarPath());
        }
    }

    public void deobfuscate(Path output) {
        try {
            Path input = options.shouldDownloadJar() ? Properties.getDownloadedMcJarPath(options.version(), options.type()) :
                    options.inputJar();
            deobfuscator.deobfuscate(input, output, options.targetNamespace(), options);
        } catch (IOException e) {
            LOGGER.fatal("Error deobfuscating", e);
        }
    }

    public void decompile(Info.DecompilerType decompilerType) {
        Path decompileDir = Properties.getOutputDecompiledDirectory(options.version(), options.type()).toAbsolutePath().normalize();
        Path outputDeobfuscatedJarPath = Properties.getOutputDeobfuscatedJarPath(options.version(), options.type()).toAbsolutePath().normalize();
        if(Files.notExists(outputDeobfuscatedJarPath))
            throw new IllegalArgumentException("Please deobfuscate first or run decompile(DecompilerType, Path, Path) method");
        decompile(decompilerType, outputDeobfuscatedJarPath, decompileDir);
    }

    public void decompile(Info.DecompilerType decompilerType, Path inputJar, Path outputDir) {
        LOGGER.info("Decompiling using \"{}\"", decompilerType);
        decompile0(Decompilers.get(decompilerType), inputJar, outputDir);
    }

    public void decompileCustomized(String customizedDecompilerName) {
        Path decompileDir = Properties.getOutputDecompiledDirectory(options.version(), options.type()).toAbsolutePath().normalize();
        Path outputDeobfuscatedJarPath = Properties.getOutputDeobfuscatedJarPath(options.version(), options.type()).toAbsolutePath().normalize();
        if(Files.notExists(outputDeobfuscatedJarPath))
            throw new IllegalArgumentException("Please deobfuscate first or run decompile(DecompilerType, Path, Path) method");
        decompileCustomized(customizedDecompilerName, outputDeobfuscatedJarPath, decompileDir);
    }

    public void decompileCustomized(String customizedDecompilerName, Path inputJar, Path outputDir) {
        LOGGER.info("Decompiling using customized decompiler \"{}\"", customizedDecompilerName);
        decompile0(Decompilers.getCustom(customizedDecompilerName), inputJar, outputDir);
    }

    private void decompile0(IDecompiler decompiler, Path inputJar, Path outputDir) {
        try(FileSystem jarFs = JarUtil.getJarFileSystemProvider().newFileSystem(inputJar, Object2ObjectMaps.emptyMap())) {
            FileUtil.deleteDirectoryIfExists(outputDir);
            Files.createDirectories(outputDir);
            Path libDownloadPath = Properties.getDownloadedLibPath().toAbsolutePath().normalize();
            FileUtil.ensureDirectoryExist(libDownloadPath);
            if(decompiler instanceof IExternalResourcesDecompiler erd)
                erd.extractTo(Properties.get(Properties.Key.TEMP_DIR).toAbsolutePath().normalize());
            if(decompiler instanceof ILibRecommendedDecompiler lrd && options.version() != null)
                lrd.downloadLib(libDownloadPath, options.version());
            switch (decompiler.getSourceType()) {
                case DIRECTORY -> {
                    Path decompileClasses = Properties.getTempDecompileClassesPath().toAbsolutePath().normalize();
                    FileUtil.copyDirectory(jarFs.getPath("/net"), decompileClasses);
                    try(Stream<Path> mjDirs = Files.list(jarFs.getPath("/com", "mojang")).filter(p ->
                            !(p.endsWith("authlib") || p.endsWith("bridge") || p.endsWith("brigadier") || p.endsWith("datafixers") ||
                                    p.endsWith("serialization") || p.endsWith("util")))) {
                        mjDirs.forEach(p -> FileUtil.copyDirectory(p, decompileClasses));
                    }
                    decompiler.decompile(decompileClasses, outputDir);
                }
                case FILE -> decompiler.decompile(inputJar, outputDir);
            }
        } catch (IOException e) {
            LOGGER.fatal("Error when decompiling", e);
        }
    }

    public static class OptionBuilder {
        private static final Logger LOGGER = LogManager.getLogger("Option Builder");
        private String version;
        private Info.SideType type;
        private boolean includeOthers = true;
        private boolean rvn;
        private BufferedReader inputMappings;

        private Path inputJar;
        private boolean reverse;

        private String targetNamespace;

        public OptionBuilder(String version, Info.SideType type) {
            this.version = Objects.requireNonNull(version, "version cannot be null!");
            this.type = Objects.requireNonNull(type, "type cannot be null!");
        }

        public OptionBuilder(Path inputJar) {
            this(inputJar, false);
        }

        public OptionBuilder(Path inputJar, boolean reverse) {
            this.inputJar = inputJar;
            this.reverse = reverse;
        }

        public OptionBuilder libsUsing(String version) {
            if(this.version != null) throw new IllegalArgumentException("version already defined, do not define it twice");
            this.version = Objects.requireNonNull(version, "version cannot be null!");
            return this;
        }

        public OptionBuilder withMapping(String inputMappings) {
            try {
                return withMapping(Files.newInputStream(Path.of(inputMappings)));
            } catch (IOException e) {
                LOGGER.fatal("Error opening mapping file");
                throw Utils.wrapInRuntime(LOGGER.throwing(e));
            }
        }

        public OptionBuilder withMapping(InputStream inputMappings) {
            return withMapping(new InputStreamReader(inputMappings));
        }

        public OptionBuilder withMapping(Reader inputMappings) {
            return withMapping(inputMappings instanceof BufferedReader buf ? buf : new BufferedReader(inputMappings));
        }

        public OptionBuilder withMapping(BufferedReader inputMappings) {
            this.inputMappings = inputMappings;
            return this;
        }

        public OptionBuilder targetNamespace(String targetNamespace) {
            this.targetNamespace = targetNamespace;
            return this;
        }

        public OptionBuilder doNotIncludeOthers() {
            this.includeOthers = false;
            return this;
        }

        public OptionBuilder regenerateVariableNames() {
            this.rvn = true;
            return this;
        }

        public Options build() {
            return new Options() {
                @Override
                public String version() {
                    return version;
                }

                @Override
                public Info.SideType type() {
                    return type;
                }

                @Override
                public boolean includeOthers() {
                    return includeOthers;
                }

                @Override
                public boolean rvn() {
                    return rvn;
                }

                @Override
                public BufferedReader inputMappings() {
                    return inputMappings;
                }

                @Override
                public Path inputJar() {
                    return inputJar;
                }

                @Override
                public boolean reverse() {
                    return reverse;
                }

                @Override
                public String targetNamespace() {
                    return targetNamespace;
                }
            };
        }
    }

    private interface Options extends Deobfuscator.DeobfuscateOptions {
        String version();

        Info.SideType type();

        private Deobfuscator buildDeobfuscator() {
            return inputMappings() != null ? new Deobfuscator(inputMappings()) : new Deobfuscator(version(), type());
        }

        private boolean shouldDownloadJar() {
            return version() != null || type() != null;
        }

        @Override
        boolean includeOthers();

        @Override
        boolean rvn();

        BufferedReader inputMappings();

        Path inputJar();

        @Override
        boolean reverse();

        String targetNamespace();
    }
}