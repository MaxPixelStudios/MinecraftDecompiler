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

package cn.maxpixel.mcdecompiler.remapper;

import cn.maxpixel.mcdecompiler.common.app.util.AppUtils;
import cn.maxpixel.mcdecompiler.common.app.util.FileUtil;
import cn.maxpixel.mcdecompiler.common.app.util.JarUtil;
import cn.maxpixel.mcdecompiler.mapping.remapper.MappingRemapper;
import cn.maxpixel.mcdecompiler.remapper.processing.ClassFileRemapper;
import cn.maxpixel.mcdecompiler.remapper.processing.ClassProcessor;
import cn.maxpixel.mcdecompiler.remapper.processing.ExtraClassesInformation;
import cn.maxpixel.mcdecompiler.remapper.util.IOUtil;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public abstract class Deobfuscator<T extends MappingRemapper> {
    public static final int ASM_VERSION = Opcodes.ASM9;
    protected static final Logger LOGGER = LogManager.getLogger();

    protected final DeobfuscationOptions options;
    protected final ClassProcessor processor;
    protected T remapper;

    protected Deobfuscator(DeobfuscationOptions options) {
        this.options = options;
        this.processor = new ClassProcessor(options);
    }

    public final ObjectOpenHashSet<String> toDecompile = new ObjectOpenHashSet<>();

    public Deobfuscator<T> deobfuscate(Path source, Path target) throws IOException {
        LOGGER.info("Deobfuscating...");
        Files.deleteIfExists(target);
        try (FileSystem fs = JarUtil.createZipFs(FileUtil.requireExist(source));
             FileSystem targetFs = JarUtil.createZipFs(FileUtil.makeParentDirs(target), true);
             Stream<Path> paths = FileUtil.iterateFiles(fs.getPath(""))) {
            Set<String> extraClasses = options.extraClasses;
            boolean deobfAll = extraClasses.contains("*") || extraClasses.contains("*all*");
            boolean extraClassesNotEmpty = !extraClasses.isEmpty();
            ExtraClassesInformation info = new ExtraClassesInformation(options.refMap, FileUtil.iterateFiles(fs.getPath(""))
                    .filter(p -> {
                        String ps = p.toString();
                        String k = AppUtils.file2Native(ps);
                        return (deobfAll && ps.endsWith(".class")) || remapper.hasClassMapping(k) ||
                                (extraClassesNotEmpty && extraClasses.stream().anyMatch(k::startsWith));
                    }), true);
            options.extraJars.forEach(jar -> {
                try (FileSystem jarFs = JarUtil.createZipFs(jar);
                    Stream<Path> s = FileUtil.iterateFiles(jarFs.getPath(""))) {
                    s.filter(p -> p.toString().endsWith(".class")).forEach(info);
                } catch (IOException e) {
                    LOGGER.warn("Error reading extra jar: {}", jar, e);
                }
            });
            ClassFileRemapper cfr = new ClassFileRemapper(remapper, info);
            processor.beforeRunning(cfr);
            toDecompile.clear();
            paths.forEach(path -> {
                try {
                    String pathString = path.toString();
                    String classKeyName = AppUtils.file2Native(pathString);
                    if ((deobfAll && pathString.endsWith(".class")) || remapper.hasClassMapping(classKeyName) ||
                            (extraClassesNotEmpty && extraClasses.stream().anyMatch(classKeyName::startsWith))) {
                        ClassReader reader = new ClassReader(IOUtil.readAllBytes(path));
                        ClassWriter writer = new ClassWriter(0);
                        String s = remapper.mapClass(classKeyName);
                        reader.accept(processor.getVisitor(writer, reader, cfr), 0);
                        String mapped = s != null ? s.concat(".class") : pathString;
                        synchronized (toDecompile) {
                            toDecompile.add(mapped);
                        }
                        try (OutputStream os = Files.newOutputStream(FileUtil.makeParentDirs(targetFs.getPath(mapped)))) {
                            os.write(writer.toByteArray());
                        }
                    } else if (options.includeOthers) {
                        if (pathString.endsWith(".SF") || pathString.endsWith(".RSA")) return;
                        try (InputStream inputStream = Files.newInputStream(path);
                             OutputStream os = Files.newOutputStream(FileUtil.makeParentDirs(targetFs.getPath(pathString)))) {
                            if (path.endsWith("META-INF/MANIFEST.MF")) {
                                Manifest man = new Manifest(inputStream);
                                man.getEntries().clear();
                                man.write(os);
                            } else inputStream.transferTo(os);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error when remapping classes or coping files", e);
                }
            });
            processor.afterRunning(cfr);
        }
        return this;
    }

    /**
     * Release the remapper so that memory can be cleaned up after the deobfuscation.
     */
    public final void releaseRemapper() {
        this.remapper = null;
    }

    public void release() {
        releaseRemapper();
    }
}