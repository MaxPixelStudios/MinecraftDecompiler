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
import cn.maxpixel.mcdecompiler.reader.TsrgMappingReader;
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

public class TsrgDeobfuscator extends AbstractDeobfuscator {
    public TsrgDeobfuscator(String mappingPath) {
        super(mappingPath);
    }
    @Override
    public TsrgDeobfuscator deobfuscate(Path source, Path target) {
        try(TsrgMappingReader mappingReader = new TsrgMappingReader(mappingPath)) {
            LOGGER.info("Deobfuscating...");
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
                        String s = NamingUtil.asNativeName(mapping.getMappedName());
                        Path output = mappedClasses.resolve(s + ".class");
                        FileUtil.ensureDirectoryExist(output.getParent());
                        Files.write(output, writer.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
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