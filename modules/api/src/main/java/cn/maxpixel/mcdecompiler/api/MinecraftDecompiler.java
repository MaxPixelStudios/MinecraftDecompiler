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
import cn.maxpixel.mcdecompiler.api.util.*;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.remapper.ClassifiedDeobfuscator;
import cn.maxpixel.mcdecompiler.remapper.DeobfuscationOptions;
import cn.maxpixel.mcdecompiler.utils.LambdaUtil;
import cn.maxpixel.mcdecompiler.utils.Utils;
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
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import static it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair.of;

/**
 * The core class of the application.
 * <p>
 * This class accepts actions and execute them. Starting the execution, the class will receive an input,
 * which can either be a jar or a directory, and pass it to actions. The actions will be executed in the
 * order when they were added. For each action, it will receive input from the output of the previous action,
 * or the input of this class if it is the first action. Its output will be passed as input to the next action.
 * You can request an intermediate output if you need the output of the action. An intermediate output consists of
 * the output of the action and the others in the state when the action is just finished.
 */
public class MinecraftDecompiler {
    private static final Logger LOGGER = LogManager.getLogger();
    static {
        ExtensionManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtil.deleteIfExists(Directories.TEMP_DIR)));
    }

    private final ObjectArrayList<ObjectObjectImmutablePair<Action, Optional<Path>>> actions = new ObjectArrayList<>();

    public void add(Action action) {
        add(action, false);
    }

    public void add(int i, Action action) {
        add(i, action, false);
    }

    public void add(Action action, boolean createIntermediateOutput) {
        actions.add(of(Objects.requireNonNull(action, "Action cannot be null"),
                createIntermediateOutput ? Optional.empty() : null));
    }

    public void add(int i, Action action, boolean createIntermediateOutput) {
        actions.add(i, of(Objects.requireNonNull(action, "Action cannot be null"),
                createIntermediateOutput ? Optional.empty() : null));
    }

    public void add(Action action, Path intermediateOutput) {
        actions.add(of(Objects.requireNonNull(action, "Action cannot be null"),
                Optional.of(intermediateOutput)));
    }

    public void add(int i, Action action, Path intermediateOutput) {
        actions.add(i, of(Objects.requireNonNull(action, "Action cannot be null"),
                Optional.of(intermediateOutput)));
    }

    public void execute(Path input) throws Exception {
        Path actionInput = input.toAbsolutePath().normalize();
        Path others = Directories.TEMP_DIR.resolve("others.jar").toAbsolutePath().normalize();
        for (int i = 0; i < actions.size(); i++) {
            var pair = actions.get(i);
            var action = pair.left();
            Path output = Directories.TEMP_DIR.resolve(action.getDefaultOutputName()).toAbsolutePath().normalize();
            try {
                action.executeRaw(actionInput, others, output);
            } catch (Exception e) {
                LOGGER.fatal("Error when executing action \"{}\"", action.getName(), e);
                for (int j = i; j < actions.size(); j++) try {
                    actions.get(j).left().close();
                } catch (Exception ex) {
                    LOGGER.warn("Error when cleaning up action \"{}\"", action.getName(), ex);
                    e.addSuppressed(ex);
                }
                throw e;
            }
            try {
                action.close();
            } catch (Exception e) {
                LOGGER.warn("Error when cleaning up action \"{}\"", action.getName(), e);
            }
            if (pair.right() != null) {
                Path intermediateOutput = pair.right().orElseGet(() -> {
                    String inName = input.getFileName().toString();
                    return Path.of(inName.substring(0, inName.lastIndexOf('.')) + '_' +
                            action.getDefaultOutputName());
                });
                try {
                    FileUtil.deleteIfExists(intermediateOutput);
                    if (Files.isDirectory(output)) FileUtil.copyDirectory(output, intermediateOutput);
                    else try (FileSystem ofs = JarUtil.createZipFs(output)) {
                        FileUtil.copyDirectory(ofs.getPath(""), intermediateOutput);
                    }
                    try (FileSystem ofs = JarUtil.createZipFs(others);
                         DirectoryStream<Path> ds = Files.newDirectoryStream(ofs.getPath(""))) {
                        for (var entry : ds) {
                            FileUtil.copyDirectory(entry, intermediateOutput);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error when generating intermediate output for action \"{}\" at \"{}\"",
                            action.getName(), intermediateOutput.toAbsolutePath().normalize(), e);
                }
            }
            actionInput = output;
        }
    }

    public static final class OptionBuilder {
        private static final Logger LOGGER = LogManager.getLogger();
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

        private String namespaceTarget;

        private boolean skipWhenAbsent;

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
                        Path libraries = metaInf.resolve("libraries");
                        lines.forEach(line -> {
                            Path lib = libraries.resolve(line.split("\t")[2]);
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

        public OptionBuilder namespaceTarget(@Nullable String namespaceTarget) {
            this.namespaceTarget = namespaceTarget;
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

        public OptionBuilder skipRemappingWhenMappingsAreAbsent() {
            this.skipWhenAbsent = true;
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
                public String namespaceTarget() {
                    return namespaceTarget;
                }

                @Override
                public Optional<ObjectSet<Path>> bundledLibs() {
                    return bundledLibs;
                }

                @Override
                public boolean skipWhenAbsent() {
                    return skipWhenAbsent;
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
                        return new ClassifiedDeobfuscator((ClassifiedMapping<NamespacedMapping>) mappings, namespaceTarget(), deobfuscation());
                    } else return new ClassifiedDeobfuscator((ClassifiedMapping<PairedMapping>) mappings, deobfuscation());
                } else throw new UnsupportedOperationException("Unsupported yet"); // TODO
            }
            if (skipWhenAbsent() && !containsMappings(version(), type())) return null;
            return new ClassifiedDeobfuscator(version(), type(), deobfuscation());
        }

        private static boolean containsMappings(String version, SideType type) {
            return VersionManifest.getSync(version).getAsJsonObject("downloads")
                    .has(type + "_mappings");
        }

        DataMap dataMap();

        DeobfuscationOptions deobfuscation();

        MappingCollection<?> mappings();

        Path inputJar();

        Path outputJar();

        Path outputDecompDir();

        String namespaceTarget();

        Optional<ObjectSet<Path>> bundledLibs();

        boolean skipWhenAbsent();
    }
}