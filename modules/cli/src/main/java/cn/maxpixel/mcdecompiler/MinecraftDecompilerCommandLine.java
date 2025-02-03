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

import cn.maxpixel.mcdecompiler.api.MinecraftDecompiler;
import cn.maxpixel.mcdecompiler.api.extension.ExtensionManager;
import cn.maxpixel.mcdecompiler.api.extension.Option;
import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.mcdecompiler.common.app.Directories;
import cn.maxpixel.mcdecompiler.common.app.SideType;
import cn.maxpixel.mcdecompiler.common.util.LambdaUtil;
import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.decompiler.VineflowerDecompiler;
import cn.maxpixel.mcdecompiler.mapping.detector.FormatDetector;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import joptsimple.*;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static java.util.List.of;

public class MinecraftDecompilerCommandLine {
    static {
        System.setProperty("org.openjdk.java.util.stream.tripwire", Boolean.toString(Constants.IS_DEV));
    }
    private static final Object2ObjectOpenHashMap<Option, OptionSpec<?>> OPTION_MAP = new Object2ObjectOpenHashMap<>();
    private static final Logger LOGGER = LogManager.getLogger("CommandLine");

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
        ArgumentAcceptingOptionSpec<String> targetNamespaceO = parser.acceptsAll(of("t", "target-namespace"), "Namespace to " +
                "remap to if you are using namespaced mappings(Tiny, Tsrgv2)").availableIf(mappingPathO).withRequiredArg();
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

        MinecraftDecompiler.OptionBuilder builder;
        if (options.has(inputO)) {
            builder = new MinecraftDecompiler.OptionBuilder(options.valueOf(inputO), options.has(reverseO));
            options.valueOfOptional(versionO).ifPresent(builder::libsUsing);
        } else {
            builder = new MinecraftDecompiler.OptionBuilder(options.valueOf(versionO), options.valueOf(sideTypeO));
        }
        options.valueOfOptional(mappingPathO).ifPresent(LambdaUtil.unwrapConsumer(m -> builder
                .withMapping(orDetect(options.valueOf(mappingFormatO), m).read(new FileInputStream(m)))));
        if (options.has(regenVarNameO)) builder.regenerateVariableNames();
        if (options.has(dontIncludeOthersO)) builder.doNotIncludeOthers();
        options.valueOfOptional(targetNamespaceO).ifPresent(builder::targetNamespace);
        options.valueOfOptional(outputO).ifPresent(builder::output);
        options.valueOfOptional(outputDecompO).ifPresent(builder::outputDecomp);
        builder.addExtraJars(options.valuesOf(extraJarsO));
        builder.addExtraClasses(options.valuesOf(extraClassesO));

        MinecraftDecompiler md = new MinecraftDecompiler(builder.build());
        md.deobfuscate();

        if (options.has(decompileO)) md.decompile(options.valueOf(decompileO), options.valueOf(incrementalDecompilationO));

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