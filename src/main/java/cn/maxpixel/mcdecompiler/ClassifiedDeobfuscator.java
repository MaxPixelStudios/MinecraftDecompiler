/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

import cn.maxpixel.mcdecompiler.asm.ClassProcessor;
import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.asm.ExtraClassesInformation;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingTypes;
import cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader;
import cn.maxpixel.mcdecompiler.util.*;
import it.unimi.dsi.fastutil.objects.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ClassifiedDeobfuscator {
    private static final Logger LOGGER = Logging.getLogger("ClassifiedDeobfuscator");
    private static final DeobfuscateOptions DEFAULT_OPTIONS = new DeobfuscateOptions() {
        @Override
        public boolean includeOthers() {
            return true;
        }

        @Override
        public boolean rvn() {
            return false;
        }

        @Override
        public boolean reverse() {
            return false;
        }
    };

    static {
        ClassProcessor.fetchOptions();
    }

    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappings;
    private final ClassifiedMappingRemapper mappingRemapper;
    private final DeobfuscateOptions options;

    private final String targetNamespace;

    public ClassifiedDeobfuscator(String version, Info.SideType side) {
        this(version, side, DEFAULT_OPTIONS);
    }

    public ClassifiedDeobfuscator(String version, Info.SideType side, DeobfuscateOptions options) {
        this(new ClassifiedMappingReader<>(MappingTypes.PROGUARD, DownloadUtil.downloadMapping(version, side)), options);
    }

    public ClassifiedDeobfuscator(ClassifiedMappingReader<PairedMapping> reader) {
        this(reader, DEFAULT_OPTIONS);
    }

    public ClassifiedDeobfuscator(ClassifiedMappingReader<PairedMapping> reader, DeobfuscateOptions options) {
        this.options = Objects.requireNonNull(options);
        this.targetNamespace = null;
        if(options.reverse()) ClassifiedMappingReader.reverse(Objects.requireNonNull(reader));
        this.mappings = ClassMapping.genMappingsByUnmappedNameMap(reader.mappings);
        this.mappingRemapper = new ClassifiedMappingRemapper(reader.mappings);
    }

    public ClassifiedDeobfuscator(ClassifiedMappingReader<NamespacedMapping> reader, String targetNamespace) {
        this(reader, targetNamespace, DEFAULT_OPTIONS);
    }

    public ClassifiedDeobfuscator(ClassifiedMappingReader<NamespacedMapping> reader, String targetNamespace, DeobfuscateOptions options) {
        this.options = Objects.requireNonNull(options);
        String sourceNamespace = NamingUtil.findSourceNamespace(Objects.requireNonNull(reader).mappings);
        this.targetNamespace = Objects.requireNonNull(targetNamespace);
        if(options.reverse()) ClassifiedMappingReader.swap(reader, sourceNamespace, targetNamespace);
        this.mappings = ClassMapping.genMappingsByNamespaceMap(reader.mappings, sourceNamespace);
        this.mappingRemapper = new ClassifiedMappingRemapper(reader.mappings, sourceNamespace, targetNamespace);
    }

    final ObjectOpenHashSet<String> toDecompile = new ObjectOpenHashSet<>();

    public ClassifiedDeobfuscator deobfuscate(Path source, Path target) throws IOException {
        LOGGER.info("Deobfuscating...");
        Files.deleteIfExists(target);
        Files.createDirectories(target.getParent());
        try(FileSystem fs = JarUtil.createZipFs(FileUtil.requireExist(source));
            FileSystem targetFs = JarUtil.createZipFs(target);
            Stream<Path> paths = FileUtil.iterateFiles(fs.getPath(""))) {
            ObjectSet<String> extraClasses = options.extraClasses();
            boolean extraClassesNotEmpty = !extraClasses.isEmpty();
            ExtraClassesInformation info = new ExtraClassesInformation(options.refMap(), FileUtil.iterateFiles(fs.getPath(""))
                    .filter(p -> {
                        String k = NamingUtil.asNativeName0(p.toString());
                        return mappings.containsKey(k) || (extraClassesNotEmpty && extraClasses.stream().anyMatch(k::startsWith));
                    }), true);
            options.extraJars().forEach(jar -> {
                try(FileSystem jarFs = JarUtil.createZipFs(jar)) {
                    FileUtil.iterateFiles(jarFs.getPath("")).filter(p -> p.toString().endsWith(".class")).forEach(info);
                } catch(IOException e) {
                    LOGGER.log(Level.WARNING, "Error reading extra jar: {0}", new Object[] {jar, e});
                }
            });
            mappingRemapper.setExtraClassesInformation(info);
            ClassProcessor.beforeRunning(options, targetNamespace, mappingRemapper);
            toDecompile.clear();
            paths.forEach(path -> {
                try {
                    String pathString = path.toString();
                    String classKeyName = NamingUtil.asNativeName0(pathString);
                    if(mappings.containsKey(classKeyName) ||
                            (extraClassesNotEmpty && extraClasses.stream().anyMatch(classKeyName::startsWith))) {
                        ClassReader reader = new ClassReader(IOUtil.readAllBytes(path));
                        if (!ExtraClassesInformation.noRefmapClasses.contains(reader.getClassName())) {
                            ClassWriter writer = new ClassWriter(0);
                            ClassMapping<? extends Mapping> cm = mappings.get(classKeyName);
                            reader.accept(ClassProcessor.getVisitor(writer, options, reader, cm, targetNamespace, mappingRemapper), 0);
                            String mapped = cm != null ? cm.mapping.getMappedName().concat(".class") : pathString;
                            synchronized (toDecompile) {
                                toDecompile.add(mapped);
                            }
                            try (OutputStream os = Files.newOutputStream(FileUtil.ensureFileExist(targetFs.getPath(mapped)))) {
                                os.write(writer.toByteArray());
                            }
                        }
                    } else if(options.includeOthers()) {
                        if(pathString.endsWith(".SF") || pathString.endsWith(".RSA")) return;
                        try(InputStream inputStream = Files.newInputStream(path);
                            OutputStream os = Files.newOutputStream(FileUtil.ensureFileExist(targetFs.getPath(pathString)))) {
                            if(path.endsWith("META-INF/MANIFEST.MF")) {
                                Manifest man = new Manifest(inputStream);
                                man.getEntries().clear();
                                man.write(os);
                            } else inputStream.transferTo(os);
                        }
                    }
                } catch(Exception e) {
                    LOGGER.log(Level.WARNING, "Error when remapping classes or coping files", e);
                }
            });
            ClassProcessor.afterRunning(options, targetNamespace, mappingRemapper);
        } catch(IOException e) {
            LOGGER.log(Level.WARNING, "Error when deobfuscating", e);
        }
        return this;
    }

    public interface DeobfuscateOptions {
        boolean includeOthers();

        boolean rvn();

        boolean reverse();

        default ObjectSet<Path> extraJars() {
            return ObjectSets.emptySet();
        }

        default ObjectSet<String> extraClasses() {
            return ObjectSets.emptySet();
        }

        default Map<String, Map<String, String>> refMap() {
            return Object2ObjectMaps.emptyMap();
        }
    }
}