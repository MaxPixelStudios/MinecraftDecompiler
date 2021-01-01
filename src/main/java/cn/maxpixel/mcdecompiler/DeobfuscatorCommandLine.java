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
import java.nio.file.Paths;
import java.util.Arrays;

public class DeobfuscatorCommandLine {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Proxy INTERNAL_PROXY = System.console() == null &&
            Boolean.parseBoolean(System.getProperty("mcd.internalProxy", "false")) ?
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1080)) : //Just for internal testing.
            Proxy.NO_PROXY;
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<String> versionO = parser.acceptsAll(Arrays.asList("v", "ver", "version"), "Select a version to deobfuscate/decompile. " +
                "Only works on Proguard mappings.").withRequiredArg();
        ArgumentAcceptingOptionSpec<Info.SideType> sideTypeO = parser.acceptsAll(Arrays.asList("s", "side"),
                "Select a side to deobfuscate/decompile. Values are client and server. Only works with \"version\" option.")
                .requiredIf(versionO).withRequiredArg().ofType(Info.SideType.class);
        ArgumentAcceptingOptionSpec<Path> tempDirO = parser.accepts("tempDir", "Select a temp directory for saving decompressed and remapped " +
                "files").withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<Path> mappingPathO = parser.accepts("mapFile", "Which mapping file needs to use.")
                .requiredUnless(versionO, sideTypeO).withRequiredArg().withValuesConvertedBy(new PathConverter());
        ArgumentAcceptingOptionSpec<String> outO = parser.accepts("out", "The output directory of deobfuscated jar and decompiled dir").withRequiredArg();
        ArgumentAcceptingOptionSpec<Info.DecompilerType> decompileO = parser.accepts("decompile", "Whether to decompile the deobfuscated jar. " +
                "Values are \"FERNFLOWER\", \"OFFICIAL_FERNFLOWER\", \"FORGEFLOWER\", \"CFR\" and \"USER_DEFINED\". Defaults to \"FERNFLOWER\". Do NOT pass any " +
                "arg to this option when \"customDecompilerName\" option is specified").withOptionalArg().ofType(Info.DecompilerType.class)
                .defaultsTo(Info.DecompilerType.FERNFLOWER);
        ArgumentAcceptingOptionSpec<URL> customDecompilerJarsO = parser.accepts("customDecompilerJars", "The jars of classes contain implementations " +
                "of ICustomizedDecompiler that can be loaded by SPI. Without this option, you need to add them to classpath").withRequiredArg().withValuesSeparatedBy(';')
                .withValuesConvertedBy(new ValueConverter<URL>() {
                    @Override
                    public URL convert(String value) {
                        try {
                            return Paths.get(value).toAbsolutePath().normalize().toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw Utils.wrapInRuntime(e);
                        }
                    }
                    @Override
                    public Class<? extends URL> valueType() { return URL.class; }
                    @Override
                    public String valuePattern() { return null; }
                });
        ArgumentAcceptingOptionSpec<String> customDecompilerO = parser.accepts("customDecompilerName",
                "Use your custom decompiler to decompile, do NOT pass any arg to \"decompile\" option when you use this option").withRequiredArg();
        AbstractOptionSpec<Void> help = parser.acceptsAll(Arrays.asList("h", "help"), "For help").forHelp();

        OptionSet options = parser.parse(args);
        if(options.has(help)) {
            try {
                System.out.println("Minecraft Decompiler version " + DeobfuscatorCommandLine.class.getPackage().getImplementationVersion());
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
            return;
        }
        if(options.has(customDecompilerJarsO))
            options.valuesOf(customDecompilerJarsO).forEach(LambdaUtil.handleThrowable(ClassLoaderUtils::appendToClassPath, LambdaUtil::rethrowAsRuntime));

        String version = options.valueOf(versionO);
        Info.SideType sideType = options.valueOf(sideTypeO);
        InfoProviders.set(new CustomizeInfo() {
            @Override
            public Path getTempPath() {
                return options.valueOfOptional(tempDirO).orElse(super.getTempPath());
            }
            @Override
            public Path getMappingPath() {
                if(options.has(versionO) && options.has(sideTypeO)) {
                    if(options.has(mappingPathO)) throw new IllegalArgumentException("Do NOT specify --mapFile option when --version and --side is specified");
                    return super.getMappingPath();
                }
                return options.valueOfOptional(mappingPathO).orElseThrow(() ->
                        new IllegalArgumentException("--mapFile is required when you deobfuscate with SRG/CSRG/TSRG mapping"));
            }
            @Override
            public String getOutputPath() {
                return options.valueOfOptional(outO).orElse(super.getOutputPath());
            }
        });

        Deobfuscator deobfuscator = new Deobfuscator(version, sideType);
        deobfuscator.deobfuscate();

        if(options.has(decompileO)) {
            deobfuscator.decompile(options.valueOf(decompileO));
        }
    }

    static {
        System.setProperty("log4j2.skipJansi", "false");
        Runtime.getRuntime().addShutdownHook(new Thread(LOGGER::traceExit));
    }
}