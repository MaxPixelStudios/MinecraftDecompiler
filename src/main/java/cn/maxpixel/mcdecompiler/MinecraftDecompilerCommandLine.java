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
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.util.Utils;
import joptsimple.*;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.List.of;

public class MinecraftDecompilerCommandLine {
    static {
        System.setProperty("org.openjdk.java.util.stream.tripwire", "true");
    }
    private static final Logger LOGGER = Logging.getLogger("CommandLine");
    public static final Proxy INTERNAL_PROXY = Info.IS_DEV ?
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1080)) : // Just for internal testing.
            Proxy.NO_PROXY;
    public static void main(String[] args) throws Throwable {
        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<Info.SideType> sideTypeO = parser.acceptsAll(of("s", "side"), "Side to deobfuscate/" +
                "decompile. Values are \"CLIENT\" and \"SERVER\". With this option, you must specify --version option and can't " +
                "specify --input option.").withRequiredArg().ofType(Info.SideType.class);
        ArgumentAcceptingOptionSpec<String> versionO = parser.acceptsAll(of("v", "ver", "version"), "Version to " +
                "deobfuscate/decompile. Only works on Proguard mappings or downloading libraries for the decompiler.")
                .requiredIf(sideTypeO).withRequiredArg();
        OptionSpecBuilder regenVarNameO = parser.acceptsAll(of("r", "rvn", "regenerate-variable-names"), "Regenerate local variable " +
                "names if the input mapping doesn't provide ones");
        OptionSpecBuilder reverseO = parser.accepts("reverse", "Reverse the input mapping, then use the reversed mapping " +
                "to deobfuscate.").availableUnless(sideTypeO);
        OptionSpecBuilder dontIncludeOthersO = parser.accepts("exclude-others", "Drop non-class files of the output jar.");
        ArgumentAcceptingOptionSpec<Path> inputO = parser.acceptsAll(of("i", "input"), "Input jar. With this option, you must " +
                "specify --mappingPath and can't specify --side.").availableUnless(sideTypeO).requiredUnless(sideTypeO).withRequiredArg()
                .withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING));
        ArgumentAcceptingOptionSpec<String> mappingPathO = parser.acceptsAll(of("m", "map", "mapping-path"), "Mapping file that " +
                "is used to deobfuscate.").requiredUnless(sideTypeO).withRequiredArg();
        ArgumentAcceptingOptionSpec<String> targetNamespaceO = parser.accepts("target-namespace", "Namespace to " +
                "remap to if you are using namespaced mappings(Tiny, Tsrgv2)").availableIf(mappingPathO).withRequiredArg();
        ArgumentAcceptingOptionSpec<Path> outputO = parser.acceptsAll(of("o", "output"), "Mapped output file. Including the suffix.")
                .withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<Path> outputDecompO = parser.accepts("decompiled-output", "Decompiled output directory. " +
                "Will be deleted before decompiling if it exists").withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<Info.DecompilerType> decompileO = parser.acceptsAll(of("d", "decompile"), "Decompile the " +
                "deobfuscated jar. Values are \"FERNFLOWER\", \"FORGEFLOWER\", \"CFR\" and \"USER_DEFINED\". Defaults to FORGEFLOWER. If a value " +
                "other than above is used, will use the default decompiler to decompile. Do NOT pass any arg to this option when " +
                "--custom-decompiler is specified.").withOptionalArg().ofType(Info.DecompilerType.class).defaultsTo(Info.DecompilerType.FORGEFLOWER);
        ArgumentAcceptingOptionSpec<URL> customDecompilerJarsO = parser.accepts("custom-decompiler-jars", "Jars that " +
                "contain implementations of ICustomizedDecompiler that can be loaded by SPI. Alternative option is to add them to classpath.")
                .withRequiredArg().withValuesSeparatedBy(';').withValuesConvertedBy(new ValueConverter<>() {
                    @Override
                    public URL convert(String value) {
                        try {
                            return Path.of(value).toAbsolutePath().normalize().toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw Utils.wrapInRuntime(e);
                        }
                    }
                    @Override
                    public Class<? extends URL> valueType() { return URL.class; }
                    @Override
                    public String valuePattern() { return null; }
                });
        ArgumentAcceptingOptionSpec<String> customDecompilerO = parser.accepts("custom-decompiler", "FQCN of your custom decompiler" +
                ", do NOT pass any arg to --decompile when you use this option").withRequiredArg();
        ArgumentAcceptingOptionSpec<Path> tempDirO = parser.accepts("temp", "Temp directory for saving unzipped and remapped " +
                "files.").withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<Path> extraJarsO = parser.accepts("extra-jars", "Extra jars used to get class " +
                "information").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING));
        ArgumentAcceptingOptionSpec<String> extraClassesO = parser.accepts("extra-class", "Extra classes/packages that " +
                "will be deobfuscated. Can be specified multiple times. Use \"/\" instead of \".\" to separate names").withRequiredArg();
        AbstractOptionSpec<Void> help = parser.acceptsAll(of("h", "?", "help"), "For help").forHelp();
        ClassProcessor.registerCommandLineOptions(parser);

        if(args == null || args.length == 0) {
            printHelp(parser);
            return;
        }

        OptionSet options = parser.parse(args);
        if(!options.hasOptions() || options.has(help)) {
            printHelp(parser);
            return;
        }
        if(options.has(customDecompilerO) && options.hasArgument(decompileO)) {
            throw new IllegalArgumentException("Do NOT pass args to --decompile when --custom-decompiler is specified");
        }
        Utils.appendToClassPath(MinecraftDecompilerCommandLine.class.getClassLoader(), options.valuesOf(customDecompilerJarsO));

        options.valueOfOptional(tempDirO).ifPresent(p -> Properties.TEMP_DIR = p);
        if(!options.has(sideTypeO)) {
            if(!options.has(inputO)) throw new IllegalArgumentException("--input is required when --side is unspecified");
            if(!options.has(mappingPathO)) throw new IllegalArgumentException("--mapping-path is required when --side is unspecified");
        }

        MinecraftDecompiler.OptionBuilder builder;
        if(options.has(sideTypeO)) {
            builder = new MinecraftDecompiler.OptionBuilder(options.valueOf(versionO), options.valueOf(sideTypeO));
            options.valueOfOptional(mappingPathO).ifPresent(builder::withMapping);
        } else {
            builder = new MinecraftDecompiler.OptionBuilder(options.valueOf(inputO), options.has(reverseO))
                    .withMapping(options.valueOf(mappingPathO));
            options.valueOfOptional(versionO).ifPresent(builder::libsUsing);
        }
        if(options.has(regenVarNameO)) builder.regenerateVariableNames();
        if(options.has(dontIncludeOthersO)) builder.doNotIncludeOthers();
        options.valueOfOptional(targetNamespaceO).ifPresent(builder::targetNamespace);
        options.valueOfOptional(outputO).ifPresent(builder::output);
        options.valueOfOptional(outputDecompO).ifPresent(builder::outputDecomp);
        builder.addExtraJars(options.valuesOf(extraJarsO));
        builder.addExtraClasses(options.valuesOf(extraClassesO));

        ClassProcessor.acceptCommandLineValues(options);

        MinecraftDecompiler md = new MinecraftDecompiler(builder.build());
        md.deobfuscate();

        if(options.has(decompileO)) {
            if(options.has(customDecompilerO)) md.decompileCustomized(options.valueOf(customDecompilerO));
            else md.decompile(options.valueOf(decompileO));
        }
        LOGGER.log(Level.INFO, "Done. Thanks for using Minecraft Decompiler {0}", MinecraftDecompilerCommandLine.class.getPackage().getImplementationVersion());
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