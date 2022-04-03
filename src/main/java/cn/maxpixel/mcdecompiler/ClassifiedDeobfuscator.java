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

import cn.maxpixel.mcdecompiler.asm.*;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingTypes;
import cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader;
import cn.maxpixel.mcdecompiler.util.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static cn.maxpixel.mcdecompiler.decompiler.ForgeFlowerDecompiler.FERNFLOWER_ABSTRACT_PARAMETER_NAMES;

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
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappings;
    private final ClassifiedMappingRemapper mappingRemapper;
    private final DeobfuscateOptions options;

    private final String targetNamespace;

    public ClassifiedDeobfuscator(String version, Info.SideType side) {
        this(new ClassifiedMappingReader<>(MappingTypes.PROGUARD, DownloadUtil.downloadMapping(version, side)));
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

    public ClassifiedDeobfuscator deobfuscate(Path source, Path target) throws IOException {
        LOGGER.info("Deobfuscating...");
        Files.deleteIfExists(target);
        FileUtil.ensureDirectoryExist(target.getParent());
        try(FileSystem fs = JarUtil.createZipFs(FileUtil.requireExist(source));
            FileSystem targetFs = JarUtil.createZipFs(target);
            Stream<Path> paths = FileUtil.iterateFiles(fs.getPath("/"))) {
            ExtraClassesInformation info = new ExtraClassesInformation(FileUtil.iterateFiles(fs.getPath("/"))
                    .filter(p -> mappings.containsKey(NamingUtil.asNativeName0(p.toString().substring(1)))), true);
            options.extraJars().forEach(jar -> {
                try(FileSystem jarFs = JarUtil.createZipFs(jar)) {
                    FileUtil.iterateFiles(jarFs.getPath("")).filter(p -> p.toString().endsWith(".class")).forEach(info);
                } catch(IOException e) {
                    LOGGER.log(Level.WARNING, "Error reading extra jar: {0}", new Object[] {jar, e});
                }
            });
            mappingRemapper.setExtraClassesInformation(info);
            if(options.rvn()) LocalVariableTableRenamer.startRecord();
            paths.forEach(path -> {
                 try {
                    String classKeyName = NamingUtil.asNativeName0(path.toString().substring(1));
                    if(mappings.containsKey(classKeyName)) {
                        ClassMapping<? extends Mapping> cm = mappings.get(classKeyName);
                        ClassWriter writer = new ClassWriter(0);
                        ClassProcessor processor = new ClassProcessor(parent -> {
                            ClassVisitor cv;
                            if(cm.mapping instanceof NameGetter.Namespaced ngn) {
                                ngn.setMappedNamespace(targetNamespace);
                                cv = new LocalVariableTableRenamer(parent, options.rvn(),
                                        (ClassMapping<NamespacedMapping>) cm, mappingRemapper);
                            } else cv = new LocalVariableTableRenamer(parent, options.rvn());
                            return new RuntimeParameterAnnotationFixer(new ClassRemapper(cv, mappingRemapper));
                        }, writer);
                        new ClassReader(IOUtil.readAllBytes(path)).accept(processor.getVisitor(), 0);
                        try(OutputStream os = Files.newOutputStream(FileUtil.ensureFileExist(targetFs
                                .getPath(cm.mapping.getMappedName().concat(".class"))))) {
                            os.write(writer.toByteArray());
                        }
                    } else if(options.includeOthers()) {
                        String outputPath = path.toString();
                        String upper = outputPath.toUpperCase();
                        if(upper.endsWith(".SF") || upper.endsWith(".RSA")) return;
                        try(InputStream inputStream = Files.newInputStream(path);
                            OutputStream os = Files.newOutputStream(FileUtil.ensureFileExist(targetFs.getPath(outputPath)))) {
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
            if(options.rvn()) LocalVariableTableRenamer.endRecord(Properties.TEMP_DIR.resolve(FERNFLOWER_ABSTRACT_PARAMETER_NAMES));
        } catch(IOException e) {
            LOGGER.log(Level.WARNING, "Error when deobfuscating", e);
        }
        return this;
    }

    interface DeobfuscateOptions {
        boolean includeOthers();

        boolean rvn();

        boolean reverse();

        default ObjectList<Path> extraJars() {
            return ObjectLists.emptyList();
        }
    }
}