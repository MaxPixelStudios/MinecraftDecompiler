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

import cn.maxpixel.mcdecompiler.common.app.SideType;
import cn.maxpixel.mcdecompiler.common.app.util.DownloadingUtil;
import cn.maxpixel.mcdecompiler.common.app.util.FileUtil;
import cn.maxpixel.mcdecompiler.common.app.util.JarUtil;
import cn.maxpixel.mcdecompiler.common.util.IOUtil;
import cn.maxpixel.mcdecompiler.common.util.NamingUtil;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.remapper.processing.ClassFileRemapper;
import cn.maxpixel.mcdecompiler.remapper.processing.ClassProcessor;
import cn.maxpixel.mcdecompiler.remapper.processing.ExtraClassesInformation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class ClassifiedDeobfuscator extends Deobfuscator {
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappings;
    private final String targetNamespace;

    public ClassifiedDeobfuscator(String version, SideType side) {
        this(version, side, DeobfuscationOptions.DEFAULT);
    }

    public ClassifiedDeobfuscator(String version, SideType side, DeobfuscationOptions options) {
        this(MappingFormats.PROGUARD.read(DownloadingUtil.downloadMappingSync(version, side)), options);
    }

    public ClassifiedDeobfuscator(ClassifiedMapping<PairedMapping> mappings) {
        this(mappings, DeobfuscationOptions.DEFAULT);
    }

    public ClassifiedDeobfuscator(ClassifiedMapping<PairedMapping> mappings, DeobfuscationOptions options) {
        super(options);
        this.targetNamespace = null;
        this.mappings = ClassifiedMappingRemapper.genMappingsByUnmappedNameMap(mappings.classes);
        this.remapper = new ClassFileRemapper(new ClassifiedMappingRemapper(mappings, options.reverse));
    }

    public ClassifiedDeobfuscator(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace) {
        this(mappings, targetNamespace, DeobfuscationOptions.DEFAULT);
    }

    public ClassifiedDeobfuscator(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace, DeobfuscationOptions options) {
        super(options);
        this.targetNamespace = inferTargetNamespace(targetNamespace, mappings);
        this.mappings = ClassifiedMappingRemapper.genMappingsByNamespaceMap(mappings.classes, ClassifiedMapping.getSourceNamespace(mappings));
        this.remapper = new ClassFileRemapper(new ClassifiedMappingRemapper(mappings, targetNamespace, options.reverse));
    }

    private static String inferTargetNamespace(String targetNamespace, @NotNull ClassifiedMapping<NamespacedMapping> mappings) {
        if (targetNamespace != null) return targetNamespace;
        var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
        if (namespaces.size() > 2) throw new IllegalArgumentException("Cannot infer a target namespace. You must manually specify a target namespace.");
        return namespaces.last();
    }

    public final ObjectOpenHashSet<String> toDecompile = new ObjectOpenHashSet<>();

    public ClassifiedDeobfuscator deobfuscate(Path source, Path target) throws IOException {
        LOGGER.info("Deobfuscating...");
        Files.deleteIfExists(target);
        Files.createDirectories(target.getParent());
        try (FileSystem fs = JarUtil.createZipFs(FileUtil.requireExist(source));
            FileSystem targetFs = JarUtil.createZipFs(target, true);
            Stream<Path> paths = FileUtil.iterateFiles(fs.getPath(""))) {
            Set<String> extraClasses = options.extraClasses;
            boolean deobfAll = extraClasses.contains("*") || extraClasses.contains("*all*");
            boolean extraClassesNotEmpty = !extraClasses.isEmpty();
            ExtraClassesInformation info = new ExtraClassesInformation(options.refMap, FileUtil.iterateFiles(fs.getPath(""))
                    .filter(p -> {
                        String k = NamingUtil.file2Native(p.toString());
                        return (deobfAll && p.toString().endsWith(".class")) || mappings.containsKey(k) || (extraClassesNotEmpty && extraClasses.stream().anyMatch(k::startsWith));
                    }), true);
            options.extraJars.forEach(jar -> {
                try (FileSystem jarFs = JarUtil.createZipFs(jar)) {
                    FileUtil.iterateFiles(jarFs.getPath("")).filter(p -> p.toString().endsWith(".class")).forEach(info);
                } catch (IOException e) {
                    LOGGER.warn("Error reading extra jar: {}", jar, e);
                }
            });
            remapper.setExtraClassesInformation(info);
            ClassProcessor.beforeRunning(options, targetNamespace, remapper);
            toDecompile.clear();
            paths.forEach(path -> {
                try {
                    String pathString = path.toString();
                    String classKeyName = NamingUtil.file2Native(pathString);
                    if ((deobfAll && pathString.endsWith(".class")) || mappings.containsKey(classKeyName) ||
                            (extraClassesNotEmpty && extraClasses.stream().anyMatch(classKeyName::startsWith))) {
                        ClassReader reader = new ClassReader(IOUtil.readAllBytes(path));
                        ClassWriter writer = new ClassWriter(0);
                        ClassMapping<? extends Mapping> cm = mappings.get(classKeyName);
                        reader.accept(ClassProcessor.getVisitor(writer, options, reader, cm, targetNamespace, remapper), 0);
                        String mapped = cm != null ? cm.mapping.getMappedName().concat(".class") : pathString;
                        synchronized (toDecompile) {
                            toDecompile.add(mapped);
                        }
                        try (OutputStream os = Files.newOutputStream(FileUtil.ensureFileExist(targetFs.getPath(mapped)))) {
                            os.write(writer.toByteArray());
                        }
                    } else if (options.includeOthers) {
                        if (pathString.endsWith(".SF") || pathString.endsWith(".RSA")) return;
                        try (InputStream inputStream = Files.newInputStream(path);
                            OutputStream os = Files.newOutputStream(FileUtil.ensureFileExist(targetFs.getPath(pathString)))) {
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
            ClassProcessor.afterRunning(options, targetNamespace, remapper);
        }
        return this;
    }
}