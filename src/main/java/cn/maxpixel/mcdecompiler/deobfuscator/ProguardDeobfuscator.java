/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2020  MaxPixelStudios
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
import java.util.function.Consumer;

public class ProguardDeobfuscator extends AbstractDeobfuscator {
    private final String version;
    private final Info.SideType type;
    private JsonObject version_json;
    public ProguardDeobfuscator(String version, Info.SideType type) {
        this.version = Objects.requireNonNull(version);
        this.type = Objects.requireNonNull(type);
        downloadMapping();
        downloadJar();
    }
    private void downloadMapping() {
        version_json = VersionManifest.getVersion(version);
        if(!version_json.get("downloads").getAsJsonObject().has(type.toString() + "_mappings"))
            throw new RuntimeException("This version doesn't have mappings. Please use 1.14.4 or above");
        Path p = Paths.get(InfoProviders.get().getProguardMappingDownloadPath(version, type));
        try {
            Files.createDirectories(p.getParent());
        } catch(IOException ignored) {}
        if(Files.notExists(p)) {
            LOGGER.info("downloading mapping...");
            try(FileChannel channel = FileChannel.open(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                channel.transferFrom(NetworkUtil.newBuilder(version_json.get("downloads").getAsJsonObject().get(type.toString() + "_mappings").
                        getAsJsonObject().get("url").getAsString()).connect().asChannel(), 0, Long.MAX_VALUE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void downloadJar() {
        Path p = Paths.get(InfoProviders.get().getMcJarPath(version, type));
        if(Files.notExists(p)) {
            try {
                Files.createDirectories(p.getParent());
            }catch(IOException ignored){}
            LOGGER.info("downloading jar...");
            try(FileChannel channel = FileChannel.open(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                ReadableByteChannel from = NetworkUtil.newBuilder(version_json.get("downloads").getAsJsonObject().get(type.toString()).getAsJsonObject()
                        .get("url").getAsString()).connect().asChannel()) {
                channel.transferFrom(from, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public ProguardDeobfuscator deobfuscate(Path source, Path target) {
        try {
            LOGGER.info("deobfuscating...");
            Files.createDirectories(target.getParent());
            if(Files.notExists(target)) Files.createFile(target);
            Path originalClasses = Paths.get(InfoProviders.get().getTempOriginalClassesPath(version, type));
            Files.createDirectories(originalClasses);
            JarUtil.decompressJar(source, originalClasses);
            LOGGER.info("remapping...");
            try(ProguardMappingReader mappingReader = new ProguardMappingReader(InfoProviders.get().getProguardMappingDownloadPath(version, type))) {
                SuperClassMapping superClassMapping = new SuperClassMapping();
                listMcClassFiles(originalClasses, path -> {
                    try(InputStream inputStream = Files.newInputStream(path)) {
                        ClassReader reader = new ClassReader(inputStream);
                        reader.accept(superClassMapping, ClassReader.SKIP_DEBUG);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                MappingRemapper remapper = new MappingRemapper(mappingReader, superClassMapping);
                Map<String, ClassMapping> mappings = mappingReader.getMappingsByUnmappedNameMap();
                Files.createDirectories(Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type)));
                listMcClassFiles(originalClasses, path -> {
                    try(InputStream inputStream = Files.newInputStream(path)) {
                        ClassReader reader = new ClassReader(inputStream);
                        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                        reader.accept(new ClassRemapper(writer, remapper), ClassReader.SKIP_DEBUG);
                        String mappingKey;
                        if(path.toString().contains("minecraft" + Info.FILE_SEPARATOR)) {
                            mappingKey = NamingUtil.asJavaName("net/minecraft" + path.toString().substring(path.toString().
                                    indexOf("minecraft" + Info.FILE_SEPARATOR) + 9));
                        } else if(path.toString().contains("mojang" + Info.FILE_SEPARATOR)) {
                            mappingKey = NamingUtil.asJavaName("com/mojang" + path.toString().substring(path.toString().
                                    indexOf("mojang" + Info.FILE_SEPARATOR) + 6));
                        } else {
                            mappingKey = NamingUtil.asJavaName(path.getFileName().toString());
                        }
                        ClassMapping mapping = mappings.get(mappingKey);
                        if(mapping != null) {
                            String s = NamingUtil.asNativeName(mapping.getMappedName());
                            Files.createDirectories(Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), s.substring(0, s.lastIndexOf('/'))));
                            Files.write(Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), s + ".class"), writer.toByteArray(),
                                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            copyOthers(originalClasses);
            JarUtil.compressJar(type == Info.SideType.CLIENT ? "net.minecraft.client.main.Main" : "net.minecraft.server.MinecraftServer",
                    target, Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
    private void copyOthers(Path baseDir) throws IOException {
        Files.list(baseDir).forEach(childPath -> {
            if(Files.isRegularFile(childPath) && !childPath.toString().endsWith(".class")) {
                FileUtil.copyFile(childPath, Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), childPath.getFileName().toString()));
            } else if(Files.isDirectory(childPath) && !childPath.toAbsolutePath().toString().contains("net")
                    && !childPath.toAbsolutePath().toString().contains("blaze3d") && !childPath.toAbsolutePath().toString().contains("realmsclient")) {
                try {
                    FileUtil.copyDirectory(childPath, Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Path manifest = Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), "META-INF", "MANIFEST.MF");
        Files.deleteIfExists(manifest);
        Path mojangRSA = Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), "META-INF", "MOJANGCS.RSA");
        Files.deleteIfExists(mojangRSA);
        Path mojangSF = Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), "META-INF", "MOJANGCS.SF");
        Files.deleteIfExists(mojangSF);
    }
    private void listMcClassFiles(Path baseDir, Consumer<Path> fileConsumer) {
        try {
            Files.list(baseDir).forEach(childPath -> {
                if(Files.isRegularFile(childPath) && childPath.getFileName().endsWith(".class")) fileConsumer.accept(childPath);
            });
            Files.walk(baseDir.resolve("net").resolve("minecraft")).filter(Files::isRegularFile).forEach(fileConsumer);
            Path mojang = baseDir.resolve("com").resolve("mojang");
            if(Files.exists(mojang)) Files.walk(mojang).filter(Files::isRegularFile).forEach(fileConsumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}