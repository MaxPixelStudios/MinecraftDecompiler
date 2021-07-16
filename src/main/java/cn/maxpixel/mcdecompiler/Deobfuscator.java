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

package cn.maxpixel.mcdecompiler;

import cn.maxpixel.mcdecompiler.asm.ClassProcessor;
import cn.maxpixel.mcdecompiler.asm.ExtraClassesInformation;
import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.reader.*;
import cn.maxpixel.mcdecompiler.util.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.maxpixel.mcdecompiler.decompiler.ForgeFlowerDecompiler.FERNFLOWER_ABSTRACT_PARAMETER_NAMES;

public class Deobfuscator {
    private static final Logger LOGGER = LogManager.getLogger("Deobfuscator");
    private final AbstractMappingReader reader;

    private static Info.MappingType getMappingType0(Stream<String> lines) {
        List<String> list = lines.limit(2).collect(Collectors.toList());
        String s = list.get(1);
        if(s.startsWith("    ")) return Info.MappingType.PROGUARD;
        else if(s.startsWith("\t")) return Info.MappingType.TSRG;
        s = list.get(0);
        if(s.startsWith("PK: ") || s.startsWith("CL: ") || s.startsWith("FD: ") || s.startsWith("MD: ")) return Info.MappingType.SRG;
        else if(s.startsWith("tiny\t2\t0") || s.startsWith("v1")) return Info.MappingType.TINY;
        else if(s.startsWith("tsrg2")) return Info.MappingType.TSRG;
        else return Info.MappingType.CSRG;
    }

    private static Info.MappingType getMappingType(String mappingPath) {
        try(Stream<String> lines = Files.lines(Path.of(mappingPath), StandardCharsets.UTF_8).filter(s -> !s.startsWith("#"))) {
            return getMappingType0(lines);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(LOGGER.throwing(e));
        }
    }

    private static Info.MappingType getMappingType(BufferedReader reader) {
        try {
            reader.mark(512);
            Info.MappingType result = getMappingType0(reader.lines().filter(s -> !s.startsWith("#")));
            reader.reset();
            return result;
        } catch (IOException e) {
            throw Utils.wrapInRuntime(LOGGER.throwing(e));
        }
    }

    public Deobfuscator(String version, Info.SideType side) {
        this.reader = new ProguardMappingReader(version, side);
    }

    public Deobfuscator(String mappingPath) throws FileNotFoundException {
        this.reader = switch(getMappingType(Objects.requireNonNull(mappingPath, "mappingPath cannot be null"))) {
            case PROGUARD -> new ProguardMappingReader(mappingPath);
            case SRG -> new SrgMappingReader(mappingPath);
            case TSRG -> new TsrgMappingReader(mappingPath);
            case CSRG -> new CsrgMappingReader(mappingPath);
            case TINY -> new TinyMappingReader(mappingPath);
        };
    }

    public Deobfuscator(String mappingPath, Info.MappingType type) throws FileNotFoundException {
        this.reader = switch(Objects.requireNonNull(type, "type cannot be null")) {
            case PROGUARD -> new ProguardMappingReader(mappingPath);
            case SRG -> new SrgMappingReader(mappingPath);
            case TSRG -> new TsrgMappingReader(mappingPath);
            case CSRG -> new CsrgMappingReader(mappingPath);
            case TINY -> new TinyMappingReader(mappingPath);
        };
    }

    public Deobfuscator(InputStream stream) {
        this(new InputStreamReader(stream));
    }

    public Deobfuscator(InputStream stream, Info.MappingType type) {
        this.reader = switch(Objects.requireNonNull(type, "type cannot be null")) {
            case PROGUARD -> new ProguardMappingReader(stream);
            case SRG -> new SrgMappingReader(stream);
            case TSRG -> new TsrgMappingReader(stream);
            case CSRG -> new CsrgMappingReader(stream);
            case TINY -> new TinyMappingReader(stream);
        };
    }

    public Deobfuscator(Reader reader) {
        this(new BufferedReader(reader));
    }

    public Deobfuscator(Reader reader, Info.MappingType type) {
        this.reader = switch(Objects.requireNonNull(type, "type cannot be null")) {
            case PROGUARD -> new ProguardMappingReader(reader);
            case SRG -> new SrgMappingReader(reader);
            case TSRG -> new TsrgMappingReader(reader);
            case CSRG -> new CsrgMappingReader(reader);
            case TINY -> new TinyMappingReader(reader);
        };
    }

    public Deobfuscator(BufferedReader reader) {
        this.reader = switch(getMappingType(Objects.requireNonNull(reader, "reader cannot be null"))) {
            case PROGUARD -> new ProguardMappingReader(reader);
            case SRG -> new SrgMappingReader(reader);
            case TSRG -> new TsrgMappingReader(reader);
            case CSRG -> new CsrgMappingReader(reader);
            case TINY -> new TinyMappingReader(reader);
        };
    }

    public Deobfuscator(BufferedReader reader, Info.MappingType type) {
        this.reader = switch(Objects.requireNonNull(type, "type cannot be null")) {
            case PROGUARD -> new ProguardMappingReader(reader);
            case SRG -> new SrgMappingReader(reader);
            case TSRG -> new TsrgMappingReader(reader);
            case CSRG -> new CsrgMappingReader(reader);
            case TINY -> new TinyMappingReader(reader);
        };
    }

    public Deobfuscator deobfuscate(Path source, Path target) throws IOException {
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

    public Deobfuscator deobfuscate(Path source, Path target, DeobfuscateOptions options) throws IOException {
        if(reader.getProcessor().isPaired()) return deobfuscate(source, target, null, options);
        String[] namespaces = reader.getProcessor().asNamespaced().getNamespaces();
        return deobfuscate(source, target, namespaces[namespaces.length - 1].equals("id") ?
                namespaces[namespaces.length - 2] : namespaces[namespaces.length - 1], options);
    }

    public Deobfuscator deobfuscate(Path source, Path target, String targetNamespace) throws IOException {
        if(reader.getProcessor().isPaired()) throw new UnsupportedOperationException();
        return deobfuscate(source, target, targetNamespace, new DeobfuscateOptions() {
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

    public Deobfuscator deobfuscate(Path source, Path target, String targetNamespace, DeobfuscateOptions options) throws IOException {
        LOGGER.info("Deobfuscating...");
        FileUtil.requireExist(source);
        Files.deleteIfExists(target);
        // Move here because after reversing, namespaced mappings will be converted to paired mappings
        Object2ObjectOpenHashMap<String, ? extends NamespacedClassMapping> namespaced = reader.getProcessor().isNamespaced() ?
                reader.getMappingsByNamespaceMap(reader.getProcessor().asNamespaced().getNamespaces()[0]) : null;
        if(options.reverse()) {
            if(reader.getProcessor().isNamespaced()) reader.reverse(targetNamespace);
            else reader.reverse();
        }
        Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> mappings = reader.getProcessor().isPaired() ?
                reader.getMappingsByUnmappedNameMap() :
                reader.getMappingsByNamespaceMap(reader.getProcessor().asNamespaced().getNamespaces()[0], targetNamespace);
        FileUtil.ensureDirectoryExist(target.getParent());
        try(FileSystem fs = JarUtil.createZipFs(source);
            FileSystem targetFs = JarUtil.createZipFs(target);
            Stream<Path> paths = FileUtil.iterateFiles(fs.getPath("/"))) {
            ExtraClassesInformation info = new ExtraClassesInformation(FileUtil.iterateFiles(fs.getPath("/"))
                    .filter(p -> mappings.containsKey(NamingUtil.asNativeName0(p.toString().substring(1)))), true);
            MappingRemapper mappingRemapper = reader.getProcessor().isPaired() ? new MappingRemapper(reader, info) :
                    new MappingRemapper(reader, info, targetNamespace);
            if(options.rvn()) ClassProcessor.startRecord();
            paths.forEach(path -> {
                 try {
                    String classKeyName = NamingUtil.asNativeName0(path.toString().substring(1));
                    if(mappings.containsKey(classKeyName)) {
                        ClassReader reader = new ClassReader(IOUtil.readAllBytes(path));
                        ClassWriter writer = new ClassWriter(reader, 0);
                        ClassProcessor processor = new ClassProcessor(options.rvn(), this.reader, namespaced, targetNamespace);
                        reader.accept(new ClassRemapper(processor, mappingRemapper), 0);
                        processor.accept(writer);
                        try(OutputStream os = Files.newOutputStream(FileUtil.ensureFileExist(
                                targetFs.getPath(mappings.get(classKeyName).getMappedName().concat(".class"))))) {
                            os.write(writer.toByteArray());
                        }
                    } else if(options.includeOthers()) {
                        String outputPath = path.toString();
                        if(outputPath.endsWith(".SF") || outputPath.endsWith(".RSA")) return;
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
                    LOGGER.error("Error when remapping classes or coping files", e);
                }
            });
            if(options.rvn()) ClassProcessor.endRecord(Properties.TEMP_DIR.resolve(FERNFLOWER_ABSTRACT_PARAMETER_NAMES));
        } catch (IOException e) {
            LOGGER.error("Error when deobfuscating", e);
        }
        return this;
    }

    public interface DeobfuscateOptions {
        boolean includeOthers();

        boolean rvn();

        boolean reverse();
    }
}