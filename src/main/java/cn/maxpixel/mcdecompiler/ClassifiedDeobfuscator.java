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
    private final ClassifiedMappingReader<? extends Mapping> reader;

    private final boolean isNamespaced;
    private final String sourceNamespace;
    private final String targetNamespace;

    public ClassifiedDeobfuscator(String version, Info.SideType side) {
        this(new ClassifiedMappingReader<>(MappingTypes.PROGUARD, DownloadUtil.downloadMapping(version, side)));
    }

    public ClassifiedDeobfuscator(ClassifiedMappingReader<PairedMapping> reader) {
        this.reader = Objects.requireNonNull(reader);
        this.isNamespaced = false;
        this.sourceNamespace = null;
        this.targetNamespace = null;
    }

    public ClassifiedDeobfuscator(ClassifiedMappingReader<NamespacedMapping> reader, String targetNamespace) {
        this.reader = Objects.requireNonNull(reader);
        this.isNamespaced = true;
        this.sourceNamespace = NamingUtil.findSourceNamespace(reader.mappings);
        this.targetNamespace = Objects.requireNonNull(targetNamespace);
    }

    public ClassifiedDeobfuscator deobfuscate(Path source, Path target) throws IOException {
        return deobfuscate(source, target, new DeobfuscateOptions() {
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
        });
    }

    public ClassifiedDeobfuscator deobfuscate(Path source, Path target, DeobfuscateOptions options) throws IOException {
        LOGGER.info("Deobfuscating...");
        Files.deleteIfExists(target);
        if(options.reverse()) {
            if(isNamespaced) ClassifiedMappingReader.swap((ClassifiedMappingReader<NamespacedMapping>) reader, sourceNamespace, targetNamespace);
            else ClassifiedMappingReader.reverse((ClassifiedMappingReader<PairedMapping>) reader);
        }
        Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappings = isNamespaced ?
                ClassMapping.genMappingsByNamespaceMap(((ClassifiedMappingReader<NamespacedMapping>) reader).mappings, sourceNamespace) :
                ClassMapping.genMappingsByUnmappedNameMap(((ClassifiedMappingReader<PairedMapping>) reader).mappings);
        FileUtil.ensureDirectoryExist(target.getParent());
        try(FileSystem fs = JarUtil.createZipFs(FileUtil.requireExist(source));
            FileSystem targetFs = JarUtil.createZipFs(target);
            Stream<Path> paths = FileUtil.iterateFiles(fs.getPath("/"))) {
            ExtraClassesInformation info = new ExtraClassesInformation(FileUtil.iterateFiles(fs.getPath("/"))
                    .filter(p -> mappings.containsKey(NamingUtil.asNativeName0(p.toString().substring(1)))), true);
            options.extraJars().forEach(jar -> {
                try(FileSystem jarFs = JarUtil.createZipFs(jar)) {
                    FileUtil.iterateFiles(jarFs.getPath("/")).filter(p -> p.toString().endsWith(".class")).forEach(info);
                } catch(IOException e) {
                    LOGGER.log(Level.WARNING, "Error reading extra jar: {0}", new Object[] {jar, e});
                }
            });
            ClassifiedMappingRemapper mappingRemapper = isNamespaced ?
                    new ClassifiedMappingRemapper(((ClassifiedMappingReader<NamespacedMapping>) reader).mappings, info, sourceNamespace, targetNamespace) :
                    new ClassifiedMappingRemapper(((ClassifiedMappingReader<PairedMapping>) reader).mappings, info);
            if(options.rvn()) LocalVariableTableRenamer.startRecord();
            paths.forEach(path -> {
                 try {
                    String classKeyName = NamingUtil.asNativeName0(path.toString().substring(1));
                    if(mappings.containsKey(classKeyName)) {
                        ClassMapping<? extends Mapping> cm = mappings.get(classKeyName);
                        ClassWriter writer = new ClassWriter(0);
                        ClassProcessor processor = new ClassProcessor(parent -> {
                            ClassVisitor cv = isNamespaced ? new LocalVariableTableRenamer(parent, options.rvn(), sourceNamespace,
                                    targetNamespace, (ClassMapping<NamespacedMapping>) cm, mappingRemapper) :
                                    new LocalVariableTableRenamer(parent, options.rvn());
                            return new RuntimeParameterAnnotationFixer(new ClassRemapper(cv, mappingRemapper));
                        }, writer);
                        new ClassReader(IOUtil.readAllBytes(path)).accept(processor.getVisitor(), 0);
                        try(OutputStream os = Files.newOutputStream(FileUtil.ensureFileExist(targetFs.getPath((isNamespaced ?
                                ((NamespacedMapping) cm.mapping).getName(targetNamespace) :
                                ((PairedMapping) cm.mapping).mappedName).concat(".class"))))) {
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