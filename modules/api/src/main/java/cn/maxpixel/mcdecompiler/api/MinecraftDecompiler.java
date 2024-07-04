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

package cn.maxpixel.mcdecompiler.api;

import cn.maxpixel.mcdecompiler.api.extension.ExtensionManager;
import cn.maxpixel.mcdecompiler.common.app.Directories;
import cn.maxpixel.mcdecompiler.common.app.SideType;
import cn.maxpixel.mcdecompiler.common.app.util.DataMap;
import cn.maxpixel.mcdecompiler.common.app.util.DownloadingUtil;
import cn.maxpixel.mcdecompiler.common.app.util.FileUtil;
import cn.maxpixel.mcdecompiler.common.app.util.JarUtil;
import cn.maxpixel.mcdecompiler.common.util.IOUtil;
import cn.maxpixel.mcdecompiler.common.util.LambdaUtil;
import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.decompiler.Decompilers;
import cn.maxpixel.mcdecompiler.decompiler.IDecompiler;
import cn.maxpixel.mcdecompiler.decompiler.IExternalResourcesDecompiler;
import cn.maxpixel.mcdecompiler.decompiler.ILibRecommendedDecompiler;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.remapper.ClassifiedDeobfuscator;
import cn.maxpixel.mcdecompiler.remapper.DeobfuscationOptions;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class MinecraftDecompiler {// This class is not designed to be reusable
    private static final Logger LOGGER = LogManager.getLogger();
    static {
        ExtensionManager.init();
    }

    private final Options options;
    private final ClassifiedDeobfuscator deobfuscator;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtil.deleteIfExists(Directories.TEMP_DIR)));
    }

    public MinecraftDecompiler(Options options) {
        this.options = options;
        this.deobfuscator = options.buildDeobfuscator();
    }

    public void deobfuscate() {
        try {
            deobfuscator.deobfuscate(options.inputJar(), options.outputJar());
        } catch (IOException e) {
            LOGGER.fatal("Error deobfuscating", e);
            throw Utils.wrapInRuntime(e);
        } finally {
            deobfuscator.release();// clean up the memory for decompilation
        }
    }

    public void decompile(String decompilerName) {
        decompile(decompilerName, null);
    }

    public void decompile(String decompilerName, @Nullable Path incrementalJar) {
        var decompiler = Decompilers.get(decompilerName);
        if (decompiler == null) throw new IllegalArgumentException("Decompiler \"" + decompilerName + "\" does not exist");
        if (Files.notExists(options.outputJar())) deobfuscate();
        if (deobfuscator.toDecompile.isEmpty()) {
            LOGGER.info("Nothing to decompile, skipping decompilation");
            return;
        }
        LOGGER.info("Decompiling using \"{}\"", decompiler.name());
        var inputJar = options.outputJar();
        var outputDir = options.outputDecompDir();
        try (FileSystem jarFs = JarUtil.createZipFs(inputJar)) {
            if (incrementalJar == null) FileUtil.deleteIfExists(outputDir);
            Files.createDirectories(outputDir);
            Path libDownloadPath = Files.createDirectories(Directories.DOWNLOAD_DIR.resolve("libs").toAbsolutePath().normalize());
            if (decompiler instanceof IExternalResourcesDecompiler erd)
                erd.extractTo(Directories.TEMP_DIR.toAbsolutePath().normalize());
            if (decompiler instanceof ILibRecommendedDecompiler lrd) {
                ObjectSet<Path> libs = options.bundledLibs().<ObjectSet<Path>>map(ObjectOpenHashSet::new).orElseGet(() ->
                        DownloadingUtil.downloadLibraries(options.version(), libDownloadPath));
                if (incrementalJar != null && decompiler.getSourceType() == IDecompiler.SourceType.DIRECTORY) {
                    try (FileSystem incrementalFs = JarUtil.createZipFs(incrementalJar)) {
                        var toDecompile = deobfuscator.toDecompile;
                        ObjectOpenHashSet<String> possibleInnerClasses = new ObjectOpenHashSet<>();
                        ObjectOpenHashSet<String> maybeRemoved = new ObjectOpenHashSet<>();
                        FileUtil.iterateFiles(incrementalFs.getPath("")).forEach(p -> {
                            String path = p.toString();
                            if (path.endsWith(".class")) {
                                String fileName = p.getFileName().toString();
                                if (toDecompile.contains(path)) {
                                    try {
                                        MessageDigest md = MessageDigest.getInstance("SHA-1");
                                        md.update(IOUtil.readAllBytes(p));
                                        StringBuilder hashA = Utils.createHashString(md);

                                        md.update(IOUtil.readAllBytes(jarFs.getPath(path)));
                                        StringBuilder hashB = Utils.createHashString(md);
                                        if (hashA.compareTo(hashB) == 0) {
                                            maybeRemoved.add(path);
                                        } else if (fileName.lastIndexOf('$') > 0) {
                                            possibleInnerClasses.add(fileName.substring(0, fileName.indexOf('$')));
                                        }
                                    } catch (IOException | NoSuchAlgorithmException e) {
                                        throw Utils.wrapInRuntime(e);
                                    }
                                } else { // deleted classes(delete java files here)
                                    FileUtil.deleteIfExists(outputDir.resolve(path.replace(".class", ".java")));
                                }
                            }
                        });
                        for (String entry : maybeRemoved) {
                            int i = entry.indexOf('$');
                            String key = i >= 0 ? entry.substring(0, i) : entry.substring(0, entry.length() - 6);// Remove ".class"
                            if (!possibleInnerClasses.contains(key) && (i < 0 || maybeRemoved.contains(key + ".class"))) {
                                toDecompile.remove(entry);
                            }
                        }
                    }
                    libs.add(inputJar);
                }
                if (!libs.isEmpty()) lrd.receiveLibs(libs);
            }
            switch (decompiler.getSourceType()) {
                case DIRECTORY -> {
                    Path decompileClasses = Directories.TEMP_DIR.resolve("decompileClasses").toAbsolutePath().normalize();
                    deobfuscator.toDecompile.parallelStream().forEach(path -> FileUtil.copyFile(jarFs.getPath(path), decompileClasses.resolve(path)));
                    decompiler.decompile(decompileClasses, outputDir);
                }
                case FILE -> decompiler.decompile(inputJar, outputDir);
            }
        } catch (IOException e) {
            LOGGER.fatal("Error when decompiling", e);
        }
    }

    public static final class OptionBuilder {
        private static final Logger LOGGER = LogManager.getLogger("Option Builder");
        private String version;
        private SideType type;
        private boolean includeOthers = true;
        private boolean rvn;
        private MappingCollection<?> mappingCollection;
        private Path outputJar;
        private Path outputDecompDir;
        private final ObjectSet<Path> extraJars = new ObjectOpenHashSet<>();
        private final ObjectSet<String> extraClasses = new ObjectOpenHashSet<>();
        private Optional<ObjectSet<Path>> bundledLibs = Optional.empty();
        private Map<String, Map<String, String>> refMap = Object2ObjectMaps.emptyMap();// TODO: move this to datamap
        private final DataMap dataMap = new DataMap();

        private Path inputJar;
        private boolean reverse;

        private String targetNamespace;

        public OptionBuilder(String version, SideType type) {
            this.version = Objects.requireNonNull(version, "version cannot be null!");
            this.type = Objects.requireNonNull(type, "type cannot be null!");
            preprocess(DownloadingUtil.downloadJarSync(version, type));
            this.outputJar = Path.of("output", version + "_" + type + "_deobfuscated.jar").toAbsolutePath().normalize();
            this.outputDecompDir = Path.of("output", version + "_" + type + "_decompiled").toAbsolutePath().normalize();
        }

        public OptionBuilder(Path inputJar) {
            this(inputJar, false);
        }

        public OptionBuilder(Path inputJar, boolean reverse) {
            preprocess(inputJar);
            this.reverse = reverse;
            String outputName = inputJar.getFileName().toString();
            outputName = outputName.substring(0, outputName.lastIndexOf('.'));
            this.outputJar = Path.of("output", outputName + "_deobfuscated.jar").toAbsolutePath().normalize();
            this.outputDecompDir = Path.of("output", outputName + "_decompiled").toAbsolutePath().normalize();
        }

        private void preprocess(Path inputJar) {
            ExtensionManager.setup();
            FileUtil.deleteIfExists(Directories.TEMP_DIR);
            try (FileSystem jarFs = JarUtil.createZipFs(FileUtil.requireExist(inputJar))) {
                Files.createDirectories(Directories.TEMP_DIR);
                Path metaInf = jarFs.getPath("META-INF");
                if (Files.exists(jarFs.getPath("/net/minecraft/bundler/Main.class"))) {
                    Path extractDir = Files.createDirectories(Directories.TEMP_DIR.resolve("bundleExtract"));
                    List<String> jar = Files.readAllLines(metaInf.resolve("versions.list"));
                    if (jar.size() == 1) {
                        Path versionPath = metaInf.resolve("versions").resolve(jar.get(0).split("\t")[2]);
                        FileUtil.copyFile(versionPath, extractDir);
                        this.inputJar = extractDir.resolve(versionPath.getFileName().toString());
                    } else throw new IllegalArgumentException("Why multiple versions in a bundle?");
                    ObjectOpenHashSet<Path> libs = new ObjectOpenHashSet<>();
                    try (Stream<String> lines = Files.lines(metaInf.resolve("libraries.list"))) {
                        lines.forEach(line -> {
                            Path lib = metaInf.resolve("libraries").resolve(line.split("\t")[2]);
                            FileUtil.copyFile(lib, extractDir);
                            libs.add(extractDir.resolve(lib.getFileName().toString()));
                        });
                    }
                    this.bundledLibs = Optional.of(ObjectSets.unmodifiable(libs));
                } else this.inputJar = inputJar;
                Path versionJson = jarFs.getPath("/version.json");
                if (version == null && Files.exists(versionJson)) {
                    try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(versionJson),
                            StandardCharsets.UTF_8)) {
                        this.version = JsonParser.parseReader(isr).getAsJsonObject().get("id").getAsString();
                    }
                }
                try (InputStream is = Files.newInputStream(metaInf.resolve("MANIFEST.MF"))) {
                    this.refMap = Optional.of(new Manifest(is))
                            .map(man -> man.getMainAttributes().getValue("MixinConfigs"))
                            .map(jarFs::getPath)
                            .filter(Files::exists)
                            .flatMap(path -> Optional.of(path)
                                    .map(LambdaUtil.unwrap(Files::newInputStream))
                                    .map(inputStream -> new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                                    .flatMap(isr -> {
                                        try (isr) {
                                            return Optional.of(JsonParser.parseReader(isr).getAsJsonObject());
                                        } catch (IOException e) {
                                            return Optional.empty();
                                        }
                                    }).map(obj -> jarFs.getPath(obj.get("refmap").getAsString()))
                                    .filter(Files::exists)
                                    .map(LambdaUtil.unwrap(Files::newInputStream))
                                    .map(inputStream -> new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                                    .flatMap(isr -> {
                                        try (isr) {
                                            return Optional.of(JsonParser.parseReader(isr).getAsJsonObject());
                                        } catch (IOException e) {
                                            return Optional.empty();
                                        }
                                    }).map(obj -> obj.getAsJsonObject("mappings"))
                                    .map(mappings -> {
                                        Object2ObjectMap<String, Map<String, String>> refMap = new Object2ObjectOpenHashMap<>();
                                        refMap.defaultReturnValue(Object2ObjectMaps.emptyMap());
                                        mappings.keySet().forEach(key -> {
                                            JsonObject value = mappings.getAsJsonObject(key);
                                            Map<String, String> mapping = new Object2ObjectOpenHashMap<>();
                                            value.keySet().forEach(k -> mapping.put(k, value.get(k).getAsString()));
                                            refMap.put(key, mapping);
                                        });
                                        return refMap;
                                    })
                            ).orElse(Object2ObjectMaps.emptyMap());
                }
                ExtensionManager.onPreprocess(jarFs, Directories.TEMP_DIR, dataMap);
            } catch (IOException e) {
                LOGGER.fatal("Error preprocessing jar file {}", inputJar, e);
                throw Utils.wrapInRuntime(e);
            }
        }

        public OptionBuilder libsUsing(String version) {
            if (this.version != null) throw new IllegalArgumentException("Version already defined, do not define it twice");
            this.version = Objects.requireNonNull(version, "version cannot be null!");
            return this;
        }

        public OptionBuilder withMapping(MappingCollection<?> mappingCollection) {
            this.mappingCollection = Objects.requireNonNull(mappingCollection, "mappingCollection cannot be null");
            return this;
        }

        public OptionBuilder output(Path outputJar) {
            this.outputJar = Objects.requireNonNull(outputJar, "outputJar cannot be null").toAbsolutePath().normalize();
            return this;
        }

        public OptionBuilder outputDecomp(Path outputDecompDir) {
            this.outputDecompDir = Objects.requireNonNull(outputDecompDir, "outputDecompDir cannot be null").toAbsolutePath().normalize();
            return this;
        }

        public OptionBuilder targetNamespace(@Nullable String targetNamespace) {
            this.targetNamespace = targetNamespace;
            return this;
        }

        public OptionBuilder doNotIncludeOthers() {
            this.includeOthers = false;
            return this;
        }

        public OptionBuilder regenerateVariableNames() {
            this.rvn = true;
            return this;
        }

        public OptionBuilder addExtraJar(Path jar) {
            this.extraJars.add(jar);
            return this;
        }

        public OptionBuilder addExtraJars(Collection<Path> jars) {
            this.extraJars.addAll(jars);
            return this;
        }

        public OptionBuilder addExtraJars(ObjectList<Path> jars) {
            this.extraJars.addAll(jars);
            return this;
        }

        public OptionBuilder addExtraClass(String cls) {
            this.extraClasses.add(cls);
            return this;
        }

        public OptionBuilder addExtraClasses(Collection<String> classes) {
            this.extraClasses.addAll(classes);
            return this;
        }

        public OptionBuilder addExtraClasses(ObjectList<String> classes) {
            this.extraClasses.addAll(classes);
            return this;
        }

        public Options build() {
            if(this.outputJar.getParent().equals(this.outputDecompDir))
                throw new IllegalArgumentException("The parent directory of outputJar cannot be the same as outputDecomp");
            return new Options() {
                private final DeobfuscationOptions deobfuscation = new DeobfuscationOptions(includeOthers, rvn, reverse,
                        ObjectSets.unmodifiable(extraJars), ObjectSets.unmodifiable(extraClasses), refMap);

                @Override
                public String version() {
                    return version;
                }

                @Override
                public SideType type() {
                    return type;
                }

                @Override
                public DataMap dataMap() {
                    return dataMap;
                }

                @Override
                public DeobfuscationOptions deobfuscation() {
                    return deobfuscation;
                }

                @Override
                public MappingCollection<?> mappings() {
                    return mappingCollection;
                }

                @Override
                public Path inputJar() {
                    return inputJar;
                }

                @Override
                public Path outputJar() {
                    return outputJar;
                }

                @Override
                public Path outputDecompDir() {
                    return outputDecompDir;
                }

                @Override
                public String targetNamespace() {
                    return targetNamespace;
                }

                @Override
                public Optional<ObjectSet<Path>> bundledLibs() {
                    return bundledLibs;
                }
            };
        }
    }

    public interface Options {
        String version();

        SideType type();

        @SuppressWarnings("unchecked")
        private ClassifiedDeobfuscator buildDeobfuscator() {
            if (mappings() != null) {
                if (mappings() instanceof ClassifiedMapping<?> mappings) {
                    if (mappings.hasTrait(NamespacedTrait.class)) {
                        return new ClassifiedDeobfuscator((ClassifiedMapping<NamespacedMapping>) mappings, targetNamespace(), deobfuscation());
                    } else return new ClassifiedDeobfuscator((ClassifiedMapping<PairedMapping>) mappings, deobfuscation());
                } else throw new UnsupportedOperationException("Unsupported yet"); // TODO
            }
            return new ClassifiedDeobfuscator(version(), type(), deobfuscation());
        }

        DataMap dataMap();

        DeobfuscationOptions deobfuscation();

        MappingCollection<?> mappings();

        Path inputJar();

        Path outputJar();

        Path outputDecompDir();

        String targetNamespace();

        Optional<ObjectSet<Path>> bundledLibs();
    }
}