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

package cn.maxpixel.mcdecompiler.deobfuscator;

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.InfoProviders;
import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.asm.SuperClassMapping;
import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.reader.ProguardMappingReader;
import cn.maxpixel.mcdecompiler.util.*;
import com.google.gson.JsonObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ProguardDeobfuscator extends AbstractDeobfuscator {
    private JsonObject version_json;
    private String version;
    private Info.SideType type;
    public ProguardDeobfuscator(String mappingPath) {
        super(mappingPath);
    }
    public ProguardDeobfuscator(String version, Info.SideType type) {
        this.version = Objects.requireNonNull(version);
        this.type = Objects.requireNonNull(type);
        downloadMapping(version, type);
        downloadJar(version, type);
    }
    private void downloadMapping(String version, Info.SideType type) {
        version_json = VersionManifest.getVersion(version);
        if(!version_json.get("downloads").getAsJsonObject().has(type.toString() + "_mappings"))
            throw new RuntimeException("Version \"" + version + "\" doesn't have Proguard mappings. Please use 1.14.4 or above");
        Path p = Paths.get(InfoProviders.get().getProguardMappingDownloadPath(version, type));
        if(Files.notExists(p)) {
            try {
                Files.createDirectories(p.getParent());
            } catch(IOException ignored) {}
            try(FileChannel channel = FileChannel.open(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                ReadableByteChannel from = NetworkUtil.newBuilder(version_json.get("downloads").getAsJsonObject().get(type.toString() + "_mappings").
                        getAsJsonObject().get("url").getAsString()).connect().asChannel()) {
                LOGGER.info("Downloading mapping...");
                channel.transferFrom(from, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                LOGGER.error("Error when downloading Proguard mapping file", e);
            }
        }
    }
    private void downloadJar(String version, Info.SideType type) {
        Path p = Paths.get(InfoProviders.get().getMcJarPath(version, type));
        if(Files.notExists(p)) {
            try {
                Files.createDirectories(p.getParent());
            }catch(IOException ignored){}
            try(FileChannel channel = FileChannel.open(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                ReadableByteChannel from = NetworkUtil.newBuilder(version_json.get("downloads").getAsJsonObject().get(type.toString()).getAsJsonObject()
                        .get("url").getAsString()).connect().asChannel()) {
                LOGGER.info("Downloading jar...");
                channel.transferFrom(from, 0, Long.MAX_VALUE);
            } catch(IOException e) {
                LOGGER.error("Error when downloading Minecraft jar", e);
            }
        }
    }
    @Override
    public ProguardDeobfuscator deobfuscate(Path source, Path target) {
        try(ProguardMappingReader mappingReader = new ProguardMappingReader(mappingPath == null ?
                InfoProviders.get().getProguardMappingDownloadPath(version, type) : mappingPath)) {
            LOGGER.info("Deobfuscating...");
            FileUtil.ensureFileExist(target);
            Path unmappedClasses = InfoProviders.get().getTempUnmappedClassesPath();
            Path mappedClasses = InfoProviders.get().getTempMappedClassesPath();
            Files.createDirectories(unmappedClasses);
            JarUtil.decompressJar(source, unmappedClasses);
            LOGGER.info("Remapping...");
            Files.createDirectories(mappedClasses);
            CompletableFuture<String> task = CompletableFuture.supplyAsync(() -> copyOthers(unmappedClasses, mappedClasses));
            SuperClassMapping superClassMapping = new SuperClassMapping();
            listMcClassFiles(unmappedClasses, path -> {
                try(InputStream inputStream = Files.newInputStream(path)) {
                    ClassReader reader = new ClassReader(inputStream);
                    reader.accept(superClassMapping, ClassReader.SKIP_DEBUG);
                } catch(IOException e) {
                    LOGGER.error("Error when creating super class mapping", e);
                }
            });
            Map<String, ClassMapping> mappings = mappingReader.getMappingsByUnmappedNameMap();
            listMcClassFiles(unmappedClasses, path -> {
                try(InputStream inputStream = Files.newInputStream(path)) {
                    ClassReader reader = new ClassReader(inputStream);
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                    reader.accept(new ClassRemapper(writer, new MappingRemapper(mappingReader, superClassMapping)), ClassReader.SKIP_DEBUG);
                    String mappingKey;
                    if(path.toString().contains("net" + Info.FILE_SEPARATOR + "minecraft" + Info.FILE_SEPARATOR)) {
                        mappingKey = NamingUtil.asJavaName(path.toString().substring(path.toString().indexOf("net" + Info.FILE_SEPARATOR + "minecraft" +
                                Info.FILE_SEPARATOR)));
                    } else if(path.toString().contains("com" + Info.FILE_SEPARATOR + "mojang" + Info.FILE_SEPARATOR)) {
                        mappingKey = NamingUtil.asJavaName(path.toString().substring(path.toString().indexOf("com" + Info.FILE_SEPARATOR + "mojang" +
                                Info.FILE_SEPARATOR)));
                    } else mappingKey = NamingUtil.asJavaName(path.getFileName().toString());
                    ClassMapping mapping = mappings.get(mappingKey);
                    if(mapping != null) {
                        String s = NamingUtil.asNativeName(mapping.getMappedName());
                        Files.createDirectories(mappedClasses.resolve(s.substring(0, s.lastIndexOf('/'))));
                        Files.write(mappedClasses.resolve(s + ".class"), writer.toByteArray(),
                                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    }
                } catch(IOException e) {
                    LOGGER.error("Error when remapping classes", e);
                }
            });
            JarUtil.compressJar(task.get(), target, mappedClasses);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}