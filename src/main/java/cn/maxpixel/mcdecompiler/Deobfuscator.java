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
import cn.maxpixel.mcdecompiler.deobfuscator.*;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.JarUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Deobfuscator {
    private static final Logger LOGGER = LogManager.getLogger();
    private final AbstractDeobfuscator deobfuscator;
    private String version;
    private Info.SideType type;
    {
        Path tempPath = Properties.get(Properties.Key.TEMP_DIR);
        FileUtil.deleteDirectory(tempPath);
        try {
            Files.createDirectories(tempPath);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtil.deleteDirectory(tempPath)));
    }
    public Deobfuscator(String version, Info.SideType type) {
        this.version = Objects.requireNonNull(version, "version cannot be null!");
        this.type = Objects.requireNonNull(type, "type cannot be null!");
        this.deobfuscator = new ProguardDeobfuscator(version, type);
    }
    public Deobfuscator(String version, String mappingPath) {
        this.version = version;
        switch(getMappingType(Objects.requireNonNull(mappingPath, "mappingPath cannot be null!"))) {
            case PROGUARD:
                this.deobfuscator = new ProguardDeobfuscator(mappingPath);
                break;
            case SRG:
                this.deobfuscator = new SrgDeobfuscator(mappingPath);
                break;
            case CSRG:
                this.deobfuscator = new CsrgDeobfuscator(mappingPath);
                break;
            case TSRG:
                this.deobfuscator = new TsrgDeobfuscator(mappingPath);
                break;
            case TINY:
                this.deobfuscator = new TinyDeobfuscator(mappingPath);
            default:
                throw new IllegalArgumentException("This type of mapping is currently unsupported");
        }
    }
    public Deobfuscator(AbstractDeobfuscator deobfuscator) {
        this.deobfuscator = deobfuscator;
    }
    private Info.MappingType getMappingType(String mappingPath) {
        try(Stream<String> lines = Files.lines(Path.of(mappingPath), StandardCharsets.UTF_8).filter(s -> !s.startsWith("#")).limit(2)) {
            List<String> list = lines.collect(Collectors.toList());
            String s = list.get(1);
            if(s.startsWith("    ")) return Info.MappingType.PROGUARD;
            else if(s.startsWith("\t")) return Info.MappingType.TSRG;
            else if(s.startsWith("tiny\t2\t0") || s.startsWith("v1")) return Info.MappingType.TINY;
            s = list.get(0);
            if(s.startsWith("PK: ") || s.startsWith("CL: ") || s.startsWith("FD: ") || s.startsWith("MD: ")) return Info.MappingType.SRG;
            else if(s.startsWith("tsrg2")) return Info.MappingType.TSRG;
            else return Info.MappingType.CSRG;
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
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
        deobfuscator.deobfuscate(input, output);
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
            FileUtil.deleteDirectory(outputDir);
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
            FileUtil.deleteDirectory(outputDir);
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