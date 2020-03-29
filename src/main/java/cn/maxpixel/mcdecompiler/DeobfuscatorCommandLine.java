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

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Scanner;

public class DeobfuscatorCommandLine {
	private static Logger LOGGER = LogManager.getLogger();
	public static final Proxy PROXY =
			new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1080)); //Just for internal testing.
//			Proxy.NO_PROXY;
	public static void main(String[] args) {
		System.setProperty("log4j2.skipJansi", "false");
		String version;
		Info.SideType sideType = null;
		Info.MappingType mappingType = Info.MappingType.PROGUARD;
		if(args.length == 0) {
			try(Scanner sc = new Scanner(System.in)) {
				System.out.println("Type a version(1.14.4 or above)");
				version = sc.next();
				System.out.println("Type a side: (c)lient, (s)erver");
				String t = sc.next();
				if(t.equalsIgnoreCase("client") || t.equalsIgnoreCase("c")) {
					sideType = Info.SideType.CLIENT;
				} else if(t.equalsIgnoreCase("server") || t.equalsIgnoreCase("s")) {
					sideType = Info.SideType.SERVER;
				}
			}
		} else {
			OptionParser parser = new OptionParser();
			OptionSpec<String> versionO = parser.accepts("version", "Select a version to deobfuscate/decompile.").withRequiredArg();
			OptionSpec<Info.SideType> sideTypeO = parser
					.accepts("side", "Select a side to deobfuscate/decompile. Use \"CLIENT\" for client and \"SERVER\" for server").withRequiredArg()
					.ofType(Info.SideType.class).defaultsTo(Info.SideType.CLIENT);
			OptionSpec<Info.MappingType> mappingTypeO = parser.accepts("mapping", "Select a mapping to deobfuscate. " +
					"Use \"SRG\" for srg, \"PROGUARD\" for Proguard, \"CSRG\" for csrg, \"TSRG\" for tsrg").
					withOptionalArg().ofType(Info.MappingType.class).defaultsTo(Info.MappingType.PROGUARD);
			OptionSpec<String> tempDirO = parser.accepts("tempDir", "Select a temp directory for saving decompressed and remapped files").
					withOptionalArg();
			OptionSpec<File> mappingPathO = parser.accepts("mapFile", "Which mapping file needs to use. Use this arg when using mapping SRG/CSRG/TSRG").
					requiredIf(mappingTypeO).withOptionalArg().ofType(File.class);
			OptionSpec<Void> help = parser.acceptsAll(Arrays.asList("h", "help"), "For help").forHelp();

			OptionSet options = parser.parse(args);
			if(options.has(help)) {
				try {
					parser.printHelpOn(System.out);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}

			version = options.valueOf(versionO);
			sideType = options.valueOf(sideTypeO);
			mappingType = options.valueOf(mappingTypeO);
			if(options.has(tempDirO) || options.has(mappingPathO)) {
				InfoProviders.set(new CustomizeInfo() {
					@Override
					public String getTempPath() {
						if(options.has(tempDirO)) return options.valueOf(tempDirO);
						return super.getTempPath();
					}
				});
			}
		}
		Deobfuscator deobfuscator = new Deobfuscator(version, sideType, mappingType);
		deobfuscator.deobfuscate();
	}
}