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

import cn.maxpixel.mcdecompiler.util.LambdaUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import io.github.lxgaming.classloader.ClassLoaderUtils;
import joptsimple.*;
import joptsimple.util.PathConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;

import static java.util.Arrays.asList;

public class MinecraftDecompilerCommandLine {
    private static final Logger LOGGER = LogManager.getLogger("CommandLine");
    public static final Proxy INTERNAL_PROXY = System.console() == null &&
            Boolean.getBoolean("mcd.internalProxy") ?
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1080)) : // Just for internal testing.
            Proxy.NO_PROXY;
    public static void main(String[] args) throws Throwable {
        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<Info.SideType> sideTypeO = parser.acceptsAll(asList("s", "side"), "Side to deobfuscate/" +
                "decompile. Values are \"CLIENT\" and \"SERVER\". With this option, you must specify --version " +
                "option and mustn't specify --input option.").withRequiredArg().ofType(Info.SideType.class);
        ArgumentAcceptingOptionSpec<String> versionO = parser.acceptsAll(asList("v", "ver", "version"),
                "Version to deobfuscate/decompile. Only works on Proguard mappings. With this option, you must specify --side option " +
                "and mustn't specify --input or --mappingPath option.").requiredIf(sideTypeO).withRequiredArg();
        OptionSpecBuilder regenVarNameO = parser.acceptsAll(asList("r", "rvn", "regenVarName"), "Regenerate local variable " +
                "names using JAD style");
        OptionSpecBuilder reverseO = parser.accepts("reverse", "Reverse the input mapping, then use the reversed mapping to " +
                "deobfuscate. Doesn't support Tiny mappings. This option is ignored if you are using Tiny mappings.").availableUnless(sideTypeO);
        ArgumentAcceptingOptionSpec<Path> inputO = parser.acceptsAll(asList("i", "input"), "The input file. With this option, you must " +
                "specify --mappingPath option and musn't specify --version or --side option.").availableUnless(sideTypeO).requiredUnless(sideTypeO)
                .withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<String> mappingPathO = parser.acceptsAll(asList("m", "map", "mappingPath"), "Mapping file use to " +
                "deobfuscate.").requiredUnless(sideTypeO).withRequiredArg();
        ArgumentAcceptingOptionSpec<Path> outDirO = parser.acceptsAll(asList("o", "outDir"), "The output directory of deobfuscated jar " +
                "and decompiled dir.").withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<String> outDeobfNameO = parser.accepts("outDeobfName", "The name of deobfuscated jar in the " +
                "outDir which specified by --outDir option. Do NOT add suffix.").withRequiredArg().defaultsTo("deobfuscated");
        ArgumentAcceptingOptionSpec<String> outDecomNameO = parser.accepts("outDecomName", "The name of decompiled dir in the " +
                "outDir which specified by --outDir option.").withRequiredArg().defaultsTo("decompiled");
        ArgumentAcceptingOptionSpec<Info.DecompilerType> decompileO = parser.acceptsAll(asList("d", "decompile"), "Decompile the " +
                "deobfuscated jar. Values are \"FERNFLOWER\", \"OFFICIAL_FERNFLOWER\", \"FORGEFLOWER\", \"CFR\" and \"USER_DEFINED\". Do NOT pass " +
                "any arg to this option when \"customDecompilerName\" option is specified.").withOptionalArg().ofType(Info.DecompilerType.class)
                .defaultsTo(Info.DecompilerType.FORGEFLOWER);
        ArgumentAcceptingOptionSpec<URL> customDecompilerJarsO = parser.accepts("customDecompilerJars", "Jars that " +
                "contain implementations of ICustomizedDecompiler that can be loaded by SPI. Without this option, you need to add them to classpath.")
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
        ArgumentAcceptingOptionSpec<String> customDecompilerO = parser.accepts("customDecompiler", "Use your custom decompiler " +
                "to decompile, do NOT pass any arg to \"decompile\" option when you use this option").withRequiredArg();
        ArgumentAcceptingOptionSpec<Path> tempDirO = parser.accepts("tempDir", "Temp directory for saving unzipped and remapped " +
                "files.").withRequiredArg().withValuesConvertedBy(new PathConverter());
        AbstractOptionSpec<Void> help = parser.acceptsAll(asList("h", "?", "help"), "For help").forHelp();

        if(args == null) {
            printHelp(parser);
            return;
        }

        OptionSet options = parser.parse(args);
        if(!options.hasOptions() || options.has(help)) {
            printHelp(parser);
            return;
        }
        if(options.has(customDecompilerO) && options.hasArgument(decompileO)) {
            throw new IllegalArgumentException("Do NOT pass args to \"decompile\" option when you use --customDecompiler option");
        }
        options.valuesOf(customDecompilerJarsO).forEach(LambdaUtil.handleThrowable(
                ClassLoaderUtils::appendToClassPath, LambdaUtil::rethrowAsRuntime));
        if(options.has(regenVarNameO)) Properties.put(Properties.Key.REGEN_VAR_NAME, true);
        if(options.has(reverseO)) Properties.put(Properties.Key.REVERSE, true);

        options.valueOfOptional(tempDirO).ifPresent(p -> Properties.put(Properties.Key.TEMP_DIR, p));
        if(!options.has(sideTypeO)) {
            Properties.put(Properties.Key.INPUT_JAR, options.valueOfOptional(inputO).orElseThrow(() -> new IllegalArgumentException(
                    "--input is required when you doesn't specify --side option")));
            Properties.put(Properties.Key.MAPPING_PATH, options.valueOfOptional(mappingPathO).orElseThrow(() -> new IllegalArgumentException(
                    "--mappingPath is required when you doesn't specify --side option")));
        }
        options.valueOfOptional(outDirO).ifPresent(p -> Properties.put(Properties.Key.OUTPUT_DIR, p));
        options.valueOfOptional(outDeobfNameO).ifPresent(s -> Properties.put(Properties.Key.OUTPUT_DEOBFUSCATED_NAME, s));
        options.valueOfOptional(outDecomNameO).ifPresent(s -> Properties.put(Properties.Key.OUTPUT_DECOMPILED_NAME, s));

        MinecraftDecompiler md;
        if(options.has(sideTypeO)) {
            if(options.has(mappingPathO))
                md = new MinecraftDecompiler(options.valueOf(versionO), options.valueOf(sideTypeO), Properties.get(Properties.Key.MAPPING_PATH));
            else md = new MinecraftDecompiler(options.valueOf(versionO), options.valueOf(sideTypeO));
        } else if(options.has(versionO))
            md = new MinecraftDecompiler(options.valueOf(versionO), Properties.get(Properties.Key.MAPPING_PATH));
        else md = new MinecraftDecompiler(Properties.get(Properties.Key.MAPPING_PATH));
        md.deobfuscate();

        if(options.has(decompileO)) {
            if(options.has(customDecompilerO)) md.decompileCustomized(options.valueOf(customDecompilerO));
            else md.decompile(options.valueOf(decompileO));
        }
        LOGGER.info("Done. Thanks for using Minecraft Decompiler {}", MinecraftDecompilerCommandLine.class.getPackage().getImplementationVersion());
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