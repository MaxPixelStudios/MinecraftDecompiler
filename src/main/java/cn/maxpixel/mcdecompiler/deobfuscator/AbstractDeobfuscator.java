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
import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.asm.SuperClassMapping;
import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.reader.AbstractMappingReader;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.JarUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public abstract class AbstractDeobfuscator {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected String mappingPath;
    AbstractDeobfuscator() {}
    protected AbstractDeobfuscator(String mappingPath) {
        this.mappingPath = Objects.requireNonNull(mappingPath, "Provided mappingPath cannot be null");
    }
    public abstract AbstractDeobfuscator deobfuscate(Path source, Path target);
    protected final void sharedDeobfuscate(Path source, Path target, AbstractMappingReader mappingReader) throws Exception {
        LOGGER.info("Deobfuscating...");
        FileUtil.requireExist(source);
        FileUtil.ensureFileExist(target);
        Path unmappedClasses = Properties.getTempUnmappedClassesPath();
        Path mappedClasses = Properties.getTempMappedClassesPath();
        FileUtil.ensureDirectoryExist(unmappedClasses);
        JarUtil.unzipJar(source, unmappedClasses);
        LOGGER.info("Remapping...");
        FileUtil.ensureDirectoryExist(mappedClasses);
        CompletableFuture<String> taskCopyThenReturnMain = CompletableFuture.supplyAsync(() -> copyOthers(unmappedClasses, mappedClasses));
        SuperClassMapping superClassMapping = new SuperClassMapping();
        listMcClassFiles(unmappedClasses, path -> {
            try(InputStream inputStream = Files.newInputStream(path)) {
                ClassReader reader = new ClassReader(inputStream);
                reader.accept(superClassMapping, ClassReader.SKIP_DEBUG);
            } catch(IOException e) {
                LOGGER.error("Error when creating super class mapping", e);
            }
        });
        MappingRemapper mappingRemapper = new MappingRemapper(mappingReader, superClassMapping);
        Map<String, ClassMapping> mappings = mappingReader.getMappingsByUnmappedNameMap();
        listMcClassFiles(unmappedClasses, path -> {
            try(InputStream inputStream = Files.newInputStream(path)) {
                ClassReader reader = new ClassReader(inputStream);
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                reader.accept(new ClassRemapper(writer, mappingRemapper), ClassReader.SKIP_DEBUG);
                ClassMapping mapping = mappings.get(NamingUtil.asJavaName(unmappedClasses.relativize(path).toString()));
                if(mapping != null) {
                    Path output = mappedClasses.resolve(NamingUtil.asNativeName(mapping.getMappedName()) + ".class");
                    FileUtil.ensureDirectoryExist(output.getParent());
                    Files.write(output, writer.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
            } catch(Exception e) {
                LOGGER.error("Error when remapping classes", e);
            }
        });
        JarUtil.zipJar(taskCopyThenReturnMain.get(), target, mappedClasses);
    }
    protected final String copyOthers(Path from, Path to) {
        try(Stream<Path> paths = Files.list(from).parallel().filter(p -> !(p.toString().endsWith(".class") || p.endsWith("net")));
            InputStream is = Files.newInputStream(from.resolve("META-INF").resolve("MANIFEST.MF"))) {
            paths.forEach(childPath -> {
                if(Files.isDirectory(childPath)) {
                    if(childPath.endsWith("com")) {
                        try(Stream<Path> s = Files.walk(childPath, 2).skip(2L); // Skip com/ and com/mojang at least
                            Stream<Path> s1 = Files.list(childPath).filter(p -> !p.endsWith("mojang"))) {
                            // Unmapped client only has com.mojang.blaze3d package
                            // If unmapped jar doesn't have this package, it will be treated as unmapped server
                            // In unmapped server jar, packages in com.mojang are its libraries, so we directly copy them
                            if(s.anyMatch(p -> p.endsWith("blaze3d")))
                                s1.forEach(p -> FileUtil.copyDirectory(p, to.resolve("com")));
                            else FileUtil.copyDirectory(childPath, to);
                        } catch (IOException e) {
                            throw Utils.wrapInRuntime(e);
                        }
                    } else if(childPath.endsWith("META-INF")) {
                        try(Stream<Path> s = Files.list(childPath).filter(p -> !(p.endsWith("MANIFEST.MF") || p.endsWith("MOJANGCS.RSA") ||
                                p.endsWith("MOJANGCS.SF")))) {
                            // Due to FileUtil.copy* limitations, we need to be sure that META-INF is exist as directory
                            Path metaInf = to.resolve("META-INF");
                            FileUtil.ensureDirectoryExist(metaInf);
                            s.forEach(p -> FileUtil.copy(p, metaInf));
                        } catch (IOException e) {
                            throw Utils.wrapInRuntime(e);
                        }
                    } else FileUtil.copyDirectory(childPath, to);
                } else FileUtil.copyFile(childPath, to);
            });

            Manifest man = new Manifest(is);
            return man.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        } catch (IOException e) {
            LOGGER.error("Error when opening the directory or reading jar manifest", e);
            throw Utils.wrapInRuntime(e);
        }
    }
    protected final void listMcClassFiles(Path baseDir, Consumer<Path> fileConsumer) {
        try(Stream<Path> baseClasses = Files.list(baseDir).filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".class")).parallel();
            Stream<Path> minecraftClasses = Files.walk(baseDir.resolve("net").resolve("minecraft")).filter(Files::isRegularFile).parallel()) {
            baseClasses.forEach(fileConsumer);
            minecraftClasses.forEach(fileConsumer);
            Path mojang = baseDir.resolve("com").resolve("mojang");
            if(Files.exists(mojang)) {
                try(Stream<Path> mojangClasses = Files.walk(mojang).filter(Files::isRegularFile).parallel()) {
                    mojangClasses.forEach(fileConsumer);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error when listing all Minecraft class files", e);
        }
    }
}