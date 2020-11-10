/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2020  MaxPixelStudios
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
import io.github.lxgaming.classloader.ClassLoaderUtils;
import joptsimple.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

public class DeobfuscatorCommandLine {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Proxy INTERNAL_PROXY = Boolean.parseBoolean(System.getProperty("mcd.internalProxy", "false")) ?
			new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1080)) : //Just for internal testing.
			Proxy.NO_PROXY;
	public static void main(String[] args) {
		String version;
		Info.SideType sideType;
		Info.MappingType mappingType;

		OptionParser parser = new OptionParser();
		ArgumentAcceptingOptionSpec<Info.MappingType> mappingTypeO = parser.accepts("mapping", "Select a mapping to deobfuscate. " +
				"Values are: srg, proguard, csrg, tsrg, tiny").withRequiredArg()
				.ofType(Info.MappingType.class).defaultsTo(Info.MappingType.PROGUARD).withValuesConvertedBy(new ValueConverter<Info.MappingType>() {
					@Override
					public Info.MappingType convert(String value) {
						return Info.MappingType.valueOf(value.toUpperCase());
					}
					@Override
					public Class<? extends Info.MappingType> valueType() {
						return Info.MappingType.class;
					}
					@Override
					public String valuePattern() { return null; }
				});
		ArgumentAcceptingOptionSpec<String> versionO = parser.acceptsAll(Arrays.asList("v", "ver", "version"), "Select a version to deobfuscate/decompile. " +
				"Required when inputJar or mapping.").requiredIf(mappingTypeO).withRequiredArg();
		ArgumentAcceptingOptionSpec<Info.SideType> sideTypeO = parser.acceptsAll(Arrays.asList("s", "side"),
				"Select a side to deobfuscate/decompile. Values are client and server. Required when \"version\" and \"mapping\" option is set.")
				.requiredIf(versionO).withRequiredArg().ofType(Info.SideType.class);
		ArgumentAcceptingOptionSpec<String> tempDirO = parser.accepts("tempDir", "Select a temp directory for saving decompressed and remapped files").
				withRequiredArg();
		ArgumentAcceptingOptionSpec<File> mappingPathO = parser.accepts("mapFile", "Which mapping file needs to use.").
				requiredIf(mappingTypeO).withRequiredArg().ofType(File.class);
		ArgumentAcceptingOptionSpec<String> outDeobfO = parser.accepts("outDeobf", "Output file of deobfuscated jar").withRequiredArg();
		ArgumentAcceptingOptionSpec<Info.DecompilerType> decompileO = parser.accepts("decompile", "Decompile deobfuscated jar. " +
				"Values are \"FERNFLOWER\", \"OFFICIAL_FERNFLOWER\", \"FORGEFLOWER\", \"CFR\" and \"USER_DEFINED\". Defaults to \"FERNFLOWER\". Do NOT pass any " +
				"arg to this option when you are using \"customDecompilerName\" option").withOptionalArg().ofType(Info.DecompilerType.class)
				.defaultsTo(Info.DecompilerType.FERNFLOWER);
		ArgumentAcceptingOptionSpec<URL> customDecompilerJarsO = parser.accepts("customDecompilerJars", "The jars of classes contain implementations of ICustomizedDecompiler that can be loaded by SPI. " +
				"Without this option, you need to add them to classpath").withRequiredArg().withValuesSeparatedBy(';')
				.withValuesConvertedBy(new ValueConverter<URL>() {
					@Override
					public URL convert(String value) {
						try {
							return Paths.get(value).toAbsolutePath().normalize().toUri().toURL();
						} catch (MalformedURLException e) {
							LambdaUtil.rethrowAsRuntime(e);
							return null;
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
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		if(options.has(customDecompilerJarsO))
			options.valuesOf(customDecompilerJarsO).forEach(LambdaUtil.handleThrowable(ClassLoaderUtils::appendToClassPath, LambdaUtil::rethrowAsRuntime));

		version = options.valueOf(versionO);
		sideType = options.valueOf(sideTypeO);
		mappingType = options.valueOf(mappingTypeO);
		if(options.has(tempDirO) || options.has(mappingPathO)) {
			InfoProviders.set(new CustomizeInfo() {
				@Override
				public String getTempPath() {
					return options.valueOfOptional(tempDirO).orElse(super.getTempPath());
				}
				@Override
				public File getMappingPath() {
					if(mappingType == MappingType.PROGUARD) throw new IllegalArgumentException("Custom the Proguard mapping file is not allowed");
					return options.valueOfOptional(mappingPathO).orElseThrow(() ->
							new IllegalArgumentException("--mapFile arg is required when you deobfuscate with SRG/CSRG/TSRG mapping"));
				}
				@Override
				public String getDeobfuscateJarPath(String version, SideType type) {
					return options.valueOfOptional(outDeobfO).orElse(super.getDeobfuscateJarPath(version, type));
				}
			});
		}

		Deobfuscator deobfuscator = new Deobfuscator(version, sideType, mappingType);
		deobfuscator.deobfuscate();

		if(options.has(decompileO)) {
			deobfuscator.decompile(options.valueOf(decompileO));
		}
	}

	static {
		System.setProperty("log4j2.skipJansi", "false");
	}
}