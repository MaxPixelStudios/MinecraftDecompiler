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

import cn.maxpixel.mcdecompiler.Properties;
import cn.maxpixel.mcdecompiler.asm.JADNameGenerator;
import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.asm.SuperClassMapping;
import cn.maxpixel.mcdecompiler.asm.TinyV2LVTRenamer;
import cn.maxpixel.mcdecompiler.mapping.TinyClassMapping;
import cn.maxpixel.mcdecompiler.reader.TinyMappingReader;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.JarUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TinyDeobfuscator extends AbstractDeobfuscator {
    public TinyDeobfuscator(String mappingPath) {
        super(mappingPath);
    }
    @Override
    public TinyDeobfuscator deobfuscate(Path source, Path target, boolean includeOthers) {
        try(TinyMappingReader mappingReader = new TinyMappingReader(mappingPath)) {
            LOGGER.info("Deobfuscating...");
            FileUtil.requireExist(source);
            Files.deleteIfExists(target);
            Object2ObjectOpenHashMap<String, TinyClassMapping> mappings = (Object2ObjectOpenHashMap<String, TinyClassMapping>) mappingReader.getMappingsByUnmappedNameMap();
            SuperClassMapping superClassMapping = new SuperClassMapping();
            try(FileSystem fs = JarUtil.getJarFileSystemProvider().newFileSystem(source, Object2ObjectMaps.emptyMap());
                Stream<Path> classes = Files.walk(fs.getPath("/")).filter(p -> Files.isRegularFile(p) &&
                        mappings.containsKey(NamingUtil.asJavaName(p.toString().substring(1)))).parallel()) {
                classes.forEach(path -> {
                    try {
                        new ClassReader(Files.readAllBytes(path)).accept(superClassMapping,
                                ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    } catch(IOException e) {
                        LOGGER.error("Error when creating super class mapping", e);
                    }
                });
                MappingRemapper mappingRemapper = new MappingRemapper(mappingReader, superClassMapping);
                boolean rvn = Properties.get(Properties.Key.REGEN_VAR_NAME);
                try(FileSystem targetFs = JarUtil.getJarFileSystemProvider().newFileSystem(target, Object2ObjectMaps.singleton("create", "true"));
                    Stream<Path> paths = Files.walk(fs.getPath("/")).skip(1L).filter(Files::isRegularFile).parallel()) {
                    paths.forEach(path -> {
                        try(InputStream inputStream = Files.newInputStream(path)) {
                            String classKeyName = NamingUtil.asJavaName(path.toString().substring(1));
                            if(mappings.containsKey(classKeyName)) {
                                ClassReader reader = new ClassReader(inputStream);
                                ClassWriter writer = new ClassWriter(reader, 0);
                                reader.accept(mappingReader.version == 2 ? new TinyV2LVTRenamer(new ClassRemapper(rvn ? new JADNameGenerator(writer) : writer, mappingRemapper), mappings) : new ClassRemapper(rvn ? new JADNameGenerator(writer) : writer, mappingRemapper), 0);
                                Path output = targetFs.getPath(NamingUtil.asNativeName(mappings.get(classKeyName).getMappedName()) + ".class");
                                FileUtil.ensureDirectoryExist(output.getParent());
                                Files.write(output, writer.toByteArray(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            } else if(includeOthers) {
                                String outputPath = path.toString();
                                if(outputPath.endsWith(".SF") || outputPath.endsWith(".RSA")) return;
                                Path output = targetFs.getPath(outputPath);
                                FileUtil.ensureDirectoryExist(output.getParent());
                                try(OutputStream os = Files.newOutputStream(output, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                                    if(path.endsWith("META-INF/MANIFEST.MF")) {
                                        Manifest man = new Manifest(inputStream);
                                        man.getEntries().clear();
                                        man.write(os);
                                    } else {
                                        byte[] buf = new byte[8192];
                                        for(int len = inputStream.read(buf); len > 0; len = inputStream.read(buf)) {
                                            os.write(buf, 0, len);
                                        }
                                    }
                                }
                            }
                        } catch(Exception e) {
                            LOGGER.error("Error when remapping classes or coping files", e);
                        }
                    });
                }
            } catch (IOException e) {
                LOGGER.error("Error when deobfuscating", e);
            }
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        return this;
    }

    public TinyDeobfuscator deobfuscate(Path source, Path target, boolean includeOthers, String fromNamespace, String toNamespace) {
        try(TinyMappingReader mappingReader = new TinyMappingReader(mappingPath)) {
            LOGGER.info("Deobfuscating...");
            FileUtil.requireExist(source);
            Files.deleteIfExists(target);
            Object2ObjectOpenHashMap<String, TinyClassMapping> mappings = ((List<TinyClassMapping>) mappingReader.getMappings()).stream()
                    .collect(Collectors.toMap(cm -> cm.getName(fromNamespace), Function.identity(), (cm1, cm2) -> {throw new IllegalArgumentException("Key \"" + cm1 + "\" and \"" + cm2 + "\" duplicated!");}, Object2ObjectOpenHashMap::new));
            SuperClassMapping superClassMapping = new SuperClassMapping();
            try(FileSystem fs = JarUtil.getJarFileSystemProvider().newFileSystem(source, Object2ObjectMaps.emptyMap());
                Stream<Path> classes = Files.walk(fs.getPath("/")).filter(p -> Files.isRegularFile(p) &&
                        mappings.containsKey(NamingUtil.asJavaName(p.toString().substring(1)))).parallel()) {
                classes.forEach(path -> {
                    try {
                        new ClassReader(Files.readAllBytes(path)).accept(superClassMapping,
                                ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    } catch(IOException e) {
                        LOGGER.error("Error when creating super class mapping", e);
                    }
                });
                MappingRemapper mappingRemapper = new MappingRemapper(mappingReader, superClassMapping, fromNamespace, toNamespace);
                boolean rvn = Properties.get(Properties.Key.REGEN_VAR_NAME);
                try(FileSystem targetFs = JarUtil.getJarFileSystemProvider().newFileSystem(target, Object2ObjectMaps.singleton("create", "true"));
                    Stream<Path> paths = Files.walk(fs.getPath("/")).skip(1L).filter(Files::isRegularFile).parallel()) {
                    paths.forEach(path -> {
                        try(InputStream inputStream = Files.newInputStream(path)) {
                            String classKeyName = NamingUtil.asJavaName(path.toString().substring(1));
                            if(mappings.containsKey(classKeyName)) {
                                ClassReader reader = new ClassReader(inputStream);
                                ClassWriter writer = new ClassWriter(reader, 0);
                                reader.accept(mappingReader.version == 2 ? new TinyV2LVTRenamer(new ClassRemapper(rvn ? new JADNameGenerator(writer) : writer, mappingRemapper), mappings) : new ClassRemapper(rvn ? new JADNameGenerator(writer) : writer, mappingRemapper), 0);
                                Path output = targetFs.getPath(NamingUtil.asNativeName(mappings.get(classKeyName).getMappedName()) + ".class");
                                FileUtil.ensureDirectoryExist(output.getParent());
                                Files.write(output, writer.toByteArray(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            } else if(includeOthers) {
                                String outputPath = path.toString();
                                if(outputPath.endsWith(".SF") || outputPath.endsWith(".RSA")) return;
                                Path output = targetFs.getPath(outputPath);
                                FileUtil.ensureDirectoryExist(output.getParent());
                                try(OutputStream os = Files.newOutputStream(output, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                                    if(path.endsWith("META-INF/MANIFEST.MF")) {
                                        Manifest man = new Manifest(inputStream);
                                        man.getEntries().clear();
                                        man.write(os);
                                    } else {
                                        byte[] buf = new byte[8192];
                                        for(int len = inputStream.read(buf); len > 0; len = inputStream.read(buf)) {
                                            os.write(buf, 0, len);
                                        }
                                    }
                                }
                            }
                        } catch(Exception e) {
                            LOGGER.error("Error when remapping classes or coping files", e);
                        }
                    });
                }
            } catch (IOException e) {
                LOGGER.error("Error when deobfuscating", e);
            }
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        return this;
    }
}