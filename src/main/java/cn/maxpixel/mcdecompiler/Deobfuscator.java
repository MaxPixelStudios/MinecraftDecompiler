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

import cn.maxpixel.mcdecompiler.asm.*;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.reader.*;
import cn.maxpixel.mcdecompiler.util.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Deobfuscator {
    private static final Logger LOGGER = LogManager.getLogger("Deobfuscator");
    private final AbstractMappingReader reader;

    private Info.MappingType getMappingType(String mappingPath) {
        try(Stream<String> lines = Files.lines(Path.of(mappingPath), StandardCharsets.UTF_8).filter(s -> !s.startsWith("#")).limit(2)) {
            List<String> list = lines.collect(Collectors.toList());
            String s = list.get(1);
            if(s.startsWith("    ")) return Info.MappingType.PROGUARD;
            else if(s.startsWith("\t")) return Info.MappingType.TSRG;
            s = list.get(0);
            if(s.startsWith("PK: ") || s.startsWith("CL: ") || s.startsWith("FD: ") || s.startsWith("MD: ")) return Info.MappingType.SRG;
            else if(s.startsWith("tiny\t2\t0") || s.startsWith("v1")) return Info.MappingType.TINY;
            else if(s.startsWith("tsrg2")) return Info.MappingType.TSRG;
            else return Info.MappingType.CSRG;
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
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

    public Deobfuscator(InputStream stream, Info.MappingType type) {
        this.reader = switch(Objects.requireNonNull(type, "type cannot be null")) {
            case PROGUARD -> new ProguardMappingReader(stream);
            case SRG -> new SrgMappingReader(stream);
            case TSRG -> new TsrgMappingReader(stream);
            case CSRG -> new CsrgMappingReader(stream);
            case TINY -> new TinyMappingReader(stream);
        };
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
        return deobfuscate(source, target, true);
    }

    public Deobfuscator deobfuscate(Path source, Path target, boolean includeOthers) throws IOException {
        if(reader.getProcessor().isNamespaced()) deobfuscate(source, target, includeOthers, false);
        return deobfuscate(source, target, includeOthers, Properties.get(Properties.Key.REVERSE));
    }

    public Deobfuscator deobfuscate(Path source, Path target, boolean includeOthers, boolean reverse) throws IOException {
        if(reader.getProcessor().isPaired()) return deobfuscate(source, target, includeOthers, reverse, null, null);
        String[] namespaces = reader.getProcessor().asNamespaced().getNamespaces();
        return deobfuscate(source, target, includeOthers, reverse, namespaces[0], namespaces[namespaces.length - 1].equals("id") ?
                namespaces[namespaces.length - 2] : namespaces[namespaces.length - 1]);
    }

    public Deobfuscator deobfuscate(Path source, Path target, boolean includeOthers, String fromNamespace, String toNamespace) throws IOException {
        if(reader.getProcessor().isPaired()) throw new UnsupportedOperationException();
        return deobfuscate(source, target, includeOthers, false, fromNamespace, toNamespace);
    }

    public Deobfuscator deobfuscate(Path source, Path target, boolean includeOthers, boolean reverse, String fromNamespace, String toNamespace) throws IOException {
        LOGGER.info("Deobfuscating...");
        FileUtil.requireExist(source);
        Files.deleteIfExists(target);
        if(reverse) reader.reverse();
        Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> mappings = reader.getProcessor().isPaired() ?
                reader.getMappingsByUnmappedNameMap() : reader.getMappingsByNamespaceMap(fromNamespace, toNamespace, toNamespace);
        FileUtil.ensureDirectoryExist(target.getParent());
        try(FileSystem fs = JarUtil.getJarFileSystemProvider().newFileSystem(source, Object2ObjectMaps.emptyMap());
            FileSystem targetFs = JarUtil.getJarFileSystemProvider().newFileSystem(target, Object2ObjectMaps.singleton("create", "true"));
            Stream<Path> paths = Files.walk(fs.getPath("/")).filter(Files::isRegularFile).parallel()) {
            ExtraClassesInformation info = new ExtraClassesInformation(Files.walk(fs.getPath("/"))
                    .filter(p -> Files.isRegularFile(p) && mappings.containsKey(NamingUtil.asNativeName0(p.toString().substring(1))))
                    .parallel(), true, IOUtil::readZipFileBytes);
            MappingRemapper mappingRemapper = reader.getProcessor().isPaired() ? new MappingRemapper(reader, info) :
                    new MappingRemapper(reader, info, fromNamespace, toNamespace);
            boolean rvn = Properties.get(Properties.Key.REGEN_VAR_NAME);
            if(rvn) JADNameGenerator.startRecord();
            Optional<Object2ObjectOpenHashMap<String, ? extends NamespacedClassMapping>> optional = reader.getProcessor().isNamespaced() ?
                    Optional.of(reader.getMappingsByNamespaceMap(fromNamespace)) : Optional.empty();
            paths.forEach(path -> {
                try(InputStream inputStream = Files.newInputStream(path)) {
                    String classKeyName = NamingUtil.asNativeName0(path.toString().substring(1));
                    if(mappings.containsKey(classKeyName)) {
                        ClassReader reader = new ClassReader(inputStream);
                        ClassWriter writer = new ClassWriter(reader, 0);
                        RuntimeInvisibleParameterAnnotationsAttributeFixer fixer = new RuntimeInvisibleParameterAnnotationsAttributeFixer();
                        ClassVisitor visitor = new ClassRemapper(rvn ? new JADNameGenerator(fixer) : fixer, mappingRemapper);
                        reader.accept(optional.<ClassVisitor>map(map -> new LVTRenamer(visitor, map, fromNamespace, toNamespace)).orElse(visitor), 0);
                        fixer.accept(writer);
                        Path output = targetFs.getPath(mappings.get(classKeyName).getMappedName() + ".class");
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
            if(rvn) JADNameGenerator.endRecord(Properties.get(Properties.Key.TEMP_DIR).resolve("fernflower_abstract_parameter_names.txt"));
        } catch (IOException e) {
            LOGGER.error("Error when deobfuscating", e);
        }
        return this;
    }
}