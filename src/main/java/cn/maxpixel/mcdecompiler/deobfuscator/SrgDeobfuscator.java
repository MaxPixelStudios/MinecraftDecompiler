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
import cn.maxpixel.mcdecompiler.reader.SrgMappingReader;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.JarUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SrgDeobfuscator extends AbstractDeobfuscator {
    protected SrgDeobfuscator(String mappingPath) {
        super(mappingPath);
    }
    @Override
    public SrgDeobfuscator deobfuscate(Path source, Path target) {
        try(SrgMappingReader mappingReader = new SrgMappingReader(mappingPath)) {
            LOGGER.info("Deobfuscating...");
            FileUtil.ensureFileExist(target);
            Path unmappedClasses = InfoProviders.get().getTempUnmappedClassesPath();
            Path mappedClasses = InfoProviders.get().getTempMappedClassesPath();
            FileUtil.ensureDirectoryExist(unmappedClasses);
            JarUtil.unzipJar(source, unmappedClasses);
            LOGGER.info("Remapping...");
            FileUtil.ensureDirectoryExist(mappedClasses);
            CompletableFuture<String> taskCopyThenReturnMain = CompletableFuture.supplyAsync(() -> copyOthers(unmappedClasses, mappedClasses));
            CompletableFuture<SuperClassMapping> taskSuperClassMapping = CompletableFuture.supplyAsync(() -> {
                SuperClassMapping superClassMapping = new SuperClassMapping();
                listMcClassFiles(unmappedClasses, path -> {
                    try(InputStream inputStream = Files.newInputStream(path)) {
                        ClassReader reader = new ClassReader(inputStream);
                        reader.accept(superClassMapping, ClassReader.SKIP_DEBUG);
                    } catch(IOException e) {
                        LOGGER.error("Error when creating super class mapping", e);
                    }
                });
                return superClassMapping;
            });
            Map<String, ClassMapping> mappings = mappingReader.getMappingsByUnmappedNameMap();
            listMcClassFiles(unmappedClasses, path -> {
                try(InputStream inputStream = Files.newInputStream(path)) {
                    ClassReader reader = new ClassReader(inputStream);
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                    reader.accept(new ClassRemapper(writer, new MappingRemapper(mappingReader, taskSuperClassMapping.get())), ClassReader.SKIP_DEBUG);
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
                        FileUtil.ensureDirectoryExist(mappedClasses.resolve(s.substring(0, s.lastIndexOf('/'))));
                        Files.write(mappedClasses.resolve(s + ".class"), writer.toByteArray(),
                                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    }
                } catch(Exception e) {
                    LOGGER.error("Error when remapping classes", e);
                }
            });
            JarUtil.zipJar(taskCopyThenReturnMain.get(), target, mappedClasses);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}