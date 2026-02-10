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

package cn.maxpixel.mcdecompiler;

import cn.maxpixel.mcdecompiler.api.Constants;
import cn.maxpixel.mcdecompiler.api.Directories;
import cn.maxpixel.mcdecompiler.api.MinecraftDecompiler;
import cn.maxpixel.mcdecompiler.api.SideType;
import cn.maxpixel.mcdecompiler.api.extension.ExtensionManager;
import cn.maxpixel.mcdecompiler.api.extension.Option;
import cn.maxpixel.mcdecompiler.api.util.DownloadingUtil;
import cn.maxpixel.mcdecompiler.api.util.FileUtil;
import cn.maxpixel.mcdecompiler.api.util.VersionManifest;
import cn.maxpixel.mcdecompiler.decompiler.DecompilationOptions;
import cn.maxpixel.mcdecompiler.decompiler.DecompilerAction;
import cn.maxpixel.mcdecompiler.decompiler.VineflowerDecompiler;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.detector.FormatDetector;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.remapper.ClassifiedDeobfuscator;
import cn.maxpixel.mcdecompiler.remapper.DeobfuscationOptions;
import cn.maxpixel.mcdecompiler.utils.LambdaUtil;
import cn.maxpixel.mcdecompiler.utils.Utils;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.*;
import joptsimple.*;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import static java.util.List.of;

public class MinecraftDecompilerCommandLine {
    static {
        System.setProperty("org.openjdk.java.util.stream.tripwire", Boolean.toString(Constants.IS_DEV));
    }
    private static final Object2ObjectOpenHashMap<Option, OptionSpec<?>> OPTION_MAP = new Object2ObjectOpenHashMap<>();
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
    private final ObjectOpenHashSet<Path> bundledLibs = new ObjectOpenHashSet<>();
    private final Path inputJar;
    private final boolean reverse;

    private String namespaceTarget;

    private boolean skipWhenAbsent;

    private String decompiler;
    private Path incrementalDecompilation;

    public MinecraftDecompilerCommandLine(String version, SideType type) {
        this.version = Objects.requireNonNull(version, "version cannot be null!");
        this.type = Objects.requireNonNull(type, "type cannot be null!");
        this.inputJar = FileUtil.extractBundle(DownloadingUtil.downloadJarSync(version, type),
                Directories.TEMP_DIR.resolve("bundleExtract"), this.bundledLibs);
        this.reverse = false;
        this.outputJar = Path.of("output", version + "_" + type + "_deobfuscated.jar").toAbsolutePath().normalize();
        this.outputDecompDir = Path.of("output", version + "_" + type + "_decompiled").toAbsolutePath().normalize();
    }

    public MinecraftDecompilerCommandLine(Path inputJar) {
        this(inputJar, false);
    }

    public MinecraftDecompilerCommandLine(Path inputJar, boolean reverse) {
        this.inputJar = FileUtil.extractBundle(inputJar, Directories.TEMP_DIR.resolve("bundleExtract"), this.bundledLibs);
        this.reverse = reverse;
        String outputName = inputJar.getFileName().toString();
        outputName = outputName.substring(0, outputName.lastIndexOf('.'));
        this.outputJar = Path.of("output", outputName + "_deobfuscated.jar").toAbsolutePath().normalize();
        this.outputDecompDir = Path.of("output", outputName + "_decompiled").toAbsolutePath().normalize();
    }

    public MinecraftDecompilerCommandLine libsUsing(String version) {
        if (this.version != null) throw new IllegalArgumentException("Version already defined, do not define it twice");
        this.version = Objects.requireNonNull(version, "version cannot be null!");
        return this;
    }

    public MinecraftDecompilerCommandLine withMapping(MappingCollection<?> mappingCollection) {
        this.mappingCollection = Objects.requireNonNull(mappingCollection, "mappingCollection cannot be null");
        return this;
    }

    public MinecraftDecompilerCommandLine output(Path outputJar) {
        this.outputJar = Objects.requireNonNull(outputJar, "outputJar cannot be null").toAbsolutePath().normalize();
        return this;
    }

    public MinecraftDecompilerCommandLine outputDecomp(Path outputDecompDir) {
        this.outputDecompDir = Objects.requireNonNull(outputDecompDir, "outputDecompDir cannot be null").toAbsolutePath().normalize();
        return this;
    }

    public MinecraftDecompilerCommandLine namespaceTarget(@Nullable String namespaceTarget) {
        this.namespaceTarget = namespaceTarget;
        return this;
    }

    public MinecraftDecompilerCommandLine doNotIncludeOthers() {
        this.includeOthers = false;
        return this;
    }

    public MinecraftDecompilerCommandLine regenerateVariableNames() {
        this.rvn = true;
        return this;
    }

    public MinecraftDecompilerCommandLine addExtraJar(Path jar) {
        this.extraJars.add(jar);
        return this;
    }

    public MinecraftDecompilerCommandLine addExtraJars(Collection<Path> jars) {
        this.extraJars.addAll(jars);
        return this;
    }

    public MinecraftDecompilerCommandLine addExtraJars(ObjectList<Path> jars) {
        this.extraJars.addAll(jars);
        return this;
    }

    public MinecraftDecompilerCommandLine addExtraClass(String cls) {
        this.extraClasses.add(cls);
        return this;
    }

    public MinecraftDecompilerCommandLine addExtraClasses(Collection<String> classes) {
        this.extraClasses.addAll(classes);
        return this;
    }

    public MinecraftDecompilerCommandLine addExtraClasses(ObjectList<String> classes) {
        this.extraClasses.addAll(classes);
        return this;
    }

    public MinecraftDecompilerCommandLine skipRemappingWhenMappingsAreAbsent() {
        this.skipWhenAbsent = true;
        return this;
    }

    public MinecraftDecompilerCommandLine decompile(String decompiler, Path incrementalDecompilation) {
        this.decompiler = Objects.requireNonNull(decompiler);
        this.incrementalDecompilation = incrementalDecompilation;
        return this;
    }

    @SuppressWarnings("unchecked")
    private ClassifiedDeobfuscator buildDeobfuscator() {
        var deobfuscation = new DeobfuscationOptions(includeOthers, rvn, reverse, extraJars, extraClasses);
        if (mappingCollection != null) {
            if (mappingCollection instanceof ClassifiedMapping<?> mappings) {
                if (mappings.hasTrait(NamespacedTrait.class)) {
                    return new ClassifiedDeobfuscator((ClassifiedMapping<NamespacedMapping>) mappings, namespaceTarget, deobfuscation);
                } else return new ClassifiedDeobfuscator((ClassifiedMapping<PairedMapping>) mappings, deobfuscation);
            } else throw new UnsupportedOperationException("Unsupported yet"); // TODO
        }
        if (!containsMappings(version, type)) {
            if (skipWhenAbsent) {
                LOGGER.info("Skipping deobfuscation as no mappings present for version {}", version);
                return null;
            } else {
                LOGGER.fatal("No mappings present for version {}. Consider directly using a decompiler instead of " +
                        "using this software, providing a mapping manually, or using --skip-when-absent", version);
            }
        }
        return new ClassifiedDeobfuscator(version, type, deobfuscation);
    }

    private static boolean containsMappings(String version, SideType type) {
        return VersionManifest.getSync(version).getAsJsonObject("downloads")
                .has(type + "_mappings");
    }

    private void run() throws Exception {
        if (this.outputJar.startsWith(this.outputDecompDir))
            throw new IllegalArgumentException("The parent directory of outputJar cannot be the same as outputDecomp");
        MinecraftDecompiler mcd = new MinecraftDecompiler();// FIXME: Is this right?
        var deobfuscator = buildDeobfuscator();
        if (deobfuscator != null) mcd.add(deobfuscator, outputJar);
        if (decompiler != null) mcd.add(new DecompilerAction(new DecompilationOptions(decompiler, version, bundledLibs,
                outputJar, incrementalDecompilation)), outputDecompDir);
        mcd.execute(inputJar);
    }

    public static void main(String[] args) throws Throwable {
        if (Constants.IS_DEV) LOGGER.info("MCD Begin");// Used to measure time
        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<SideType> sideTypeO = parser.acceptsAll(of("s", "side"), "Side to deobfuscate/" +
                "decompile. Values are \"CLIENT\" and \"SERVER\". With this option, you must specify --version option.")
                .withRequiredArg().ofType(SideType.class).defaultsTo(SideType.CLIENT);
        ArgumentAcceptingOptionSpec<Path> inputO = parser.acceptsAll(of("i", "input"), "Input jar.")
                .withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING));
        ArgumentAcceptingOptionSpec<String> mappingPathO = parser.acceptsAll(of("m", "map", "mapping-path"), "Mapping file that " +
                "is used to deobfuscate.").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> versionO = parser.acceptsAll(of("v", "ver", "version"), "Version to " +
                "deobfuscate/decompile. Only works on Proguard mappings or downloading libraries for the decompiler.")
                .requiredUnless(inputO, mappingPathO).requiredIf(sideTypeO).withRequiredArg();
        OptionSpecBuilder regenVarNameO = parser.acceptsAll(of("r", "rvn", "regenerate-variable-names"),
                "Regenerate local variable names if the input mapping doesn't provide ones");
        OptionSpecBuilder dontIncludeOthersO = parser.accepts("exclude-others", "Drop non-class files of the output jar.");
        OptionSpecBuilder reverseO = parser.accepts("reverse", "Reverse the input mapping, then use " +
                "the reversed mapping to deobfuscate.").availableIf(inputO);
        ArgumentAcceptingOptionSpec<String> mappingFormatO = parser.acceptsAll(of("M", "mapping-format"),
                "Manually specify the mapping format").availableIf(mappingPathO).withRequiredArg();
        ArgumentAcceptingOptionSpec<String> namespaceTargetO = parser.acceptsAll(of("t", "namespace-target"), "Namespace to " +
                "remap from/to if you are using namespaced mappings(Tiny, Tsrgv2)").availableIf(mappingPathO).withRequiredArg();
        ArgumentAcceptingOptionSpec<Path> outputO = parser.acceptsAll(of("o", "output"), "Mapped output file, including the suffix.")
                .withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<Path> outputDecompO = parser.accepts("decompiled-output", "Decompiled output directory. " +
                "Will be deleted before decompiling if it exists").withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<String> decompileO = parser.acceptsAll(of("d", "decompile"), "Decompile the " +
                "deobfuscated jar. Values are \"fernflower\", \"forgeflower\", \"cfr\", \"vineflower\", and \"user-defined\" " +
                "or the custom decompiler name. Defaults to \"vineflower\". If the decompiler does not exist, the program will crash.")
                .withOptionalArg().defaultsTo(VineflowerDecompiler.NAME);
        ArgumentAcceptingOptionSpec<Path> tempDirO = parser.accepts("temp", "Temp directory for saving unzipped and remapped " +
                "files.").withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<Path> extraJarsO = parser.acceptsAll(of("e", "extra-jars"), "Extra jars used to get class " +
                "information").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING));
        ArgumentAcceptingOptionSpec<String> extraClassesO = parser.acceptsAll(of("c", "extra-class"), "Extra classes/packages that " +
                "will be deobfuscated. Can be specified multiple times. Use \"/\" instead of \".\" to separate names. Use \"*\" or \"*all*\" to " +
                "deobfuscate all").withRequiredArg();
        ArgumentAcceptingOptionSpec<Path> incrementalDecompilationO = parser.accepts("incremental-decompilation","Try to decompile " +
                "incrementally. Specify a jar to compare the difference. Only works with decompilers of source type \"DIRECTORY\"")
                .withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING));
        OptionSpecBuilder skipWhenAbsentO = parser.accepts("skip-when-absent",
                "Skip remapping when mappings are absent");
        AbstractOptionSpec<Void> help = parser.acceptsAll(of("h", "?", "help"), "For help").forHelp();

        for (Option option : ExtensionManager.OPTION_REGISTRY.getOptions()) {
            var spec = parser.acceptsAll(option.options, option.description == null ? "" : option.description);
            if (option instanceof Option.ValueAccepting<?> v) {
                var valueAcceptingSpec = v.requiresArg ? spec.withRequiredArg() : spec.withOptionalArg();
                if (v.converter != null) {
                    valueAcceptingSpec.withValuesConvertedBy(new ValueConverter<>() {
                        @Override
                        public Object convert(String value) {
                            return v.converter.apply(value);
                        }

                        @Override
                        public Class<?> valueType() {
                            return v.type;
                        }

                        @Override
                        public String valuePattern() {
                            return null;
                        }
                    });
                } else valueAcceptingSpec.ofType(v.type);
                if (v.isRequired()) valueAcceptingSpec.required();
                if (v.getDefaultValue() != null)
                    ((ArgumentAcceptingOptionSpec) valueAcceptingSpec).defaultsTo(v.getDefaultValue());
                OPTION_MAP.put(option, valueAcceptingSpec);
            } else {
                OPTION_MAP.put(option, spec);
            }
        }

        if (args == null || args.length == 0) {
            printHelp(parser);
            return;
        }

        OptionSet options = parser.parse(args);
        if (!options.hasOptions() || options.has(help)) {
            printHelp(parser);
            return;
        }

        options.valueOfOptional(tempDirO).ifPresent(p -> Directories.TEMP_DIR = p);

        for (var it = Object2ObjectMaps.fastIterator(OPTION_MAP); it.hasNext(); ) {
            var entry = it.next();
            var o = entry.getKey();
            var spec = entry.getValue();
            if (options.has(spec)) {
                if (options.hasArgument(spec)) {
                    if (!(o instanceof Option.ValueAccepting<?>)) throw new IllegalArgumentException("Should not get here");
                    ExtensionManager.OPTION_REGISTRY.addOption(o.options.get(0), options.valuesOf(spec));
                } else ExtensionManager.OPTION_REGISTRY.addOption(o.options.get(0));
            }
        }

        MinecraftDecompilerCommandLine cli;
        if (options.has(inputO)) {
            cli = new MinecraftDecompilerCommandLine(options.valueOf(inputO), options.has(reverseO));
            options.valueOfOptional(versionO).ifPresent(cli::libsUsing);
        } else {
            cli = new MinecraftDecompilerCommandLine(options.valueOf(versionO), options.valueOf(sideTypeO));
        }
        options.valueOfOptional(mappingPathO).ifPresent(LambdaUtil.unwrapConsumer(m -> cli
                .withMapping(orDetect(options.valueOf(mappingFormatO), m).read(new FileInputStream(m)))));
        if (options.has(regenVarNameO)) cli.regenerateVariableNames();
        if (options.has(dontIncludeOthersO)) cli.doNotIncludeOthers();
        options.valueOfOptional(namespaceTargetO).ifPresent(cli::namespaceTarget);
        options.valueOfOptional(outputO).ifPresent(cli::output);
        options.valueOfOptional(outputDecompO).ifPresent(cli::outputDecomp);
        cli.addExtraJars(options.valuesOf(extraJarsO));
        cli.addExtraClasses(options.valuesOf(extraClassesO));

        if (options.has(skipWhenAbsentO)) cli.skipRemappingWhenMappingsAreAbsent();
        if (options.has(decompileO)) cli.decompile(options.valueOf(decompileO), options.valueOf(incrementalDecompilationO));

        cli.run();

        LOGGER.info("Done. Thanks for using Minecraft Decompiler {}", MinecraftDecompilerCommandLine.class.getPackage().getImplementationVersion());
    }

    private static MappingFormat<?, ?> orDetect(String mappingFormat, String path) {
        if (mappingFormat != null) {
            MappingFormat<?, ?> format = MappingFormats.get(mappingFormat);
            if (format == null) {
                LOGGER.warn("The specified mapping format \"{}\" does not exist. Available formats are: {}. MCD will" +
                        "try to automatically detect the mapping format", mappingFormat, MappingFormats.getFormatNames());
            } else return format;
        }
        return FormatDetector.tryDetecting(Path.of(path));
    }

    private static void printHelp(OptionParser parser) {
        try {
            System.out.println("Minecraft Decompiler version " + MinecraftDecompilerCommandLine.class.getPackage().getImplementationVersion());
            parser.printHelpOn(System.out);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }
}