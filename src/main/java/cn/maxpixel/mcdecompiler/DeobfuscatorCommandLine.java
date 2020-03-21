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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class DeobfuscatorCommandLine {
	private static Logger LOGGER = LogManager.getLogger();
	public static final Proxy PROXY =
//			new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1080)); //Just for internal testing.
			Proxy.NO_PROXY;
	public static void main(String[] args) {
		System.setProperty("log4j2.skipJansi", "false");
		String version;
		Info.SideType type = null;
		if(args.length == 0) {
			try(Scanner sc = new Scanner(System.in)) {
				System.out.println("Type a version(1.14.4 or above)");
				version = sc.next();
				System.out.println("Type a side: (c)lient, (s)erver");
				String t = sc.next();
				if(t.equalsIgnoreCase("client") || t.equalsIgnoreCase("c")) {
					type = Info.SideType.CLIENT;
				} else if(t.equalsIgnoreCase("server") || t.equalsIgnoreCase("s")) {
					type = Info.SideType.SERVER;
				}
			}
		} else {
			OptionParser parser = new OptionParser();
			OptionSpec<String> versionO = parser.accepts("version", "Select a version to deobfuscate/decompile.").withRequiredArg();
			OptionSpec<Info.SideType> typeO = parser
					.accepts("side", "Select a side to deobfuscate/decompile. Use \"CLIENT\" for client and \"SERVER\" for server").withRequiredArg()
					.ofType(Info.SideType.class).defaultsTo(Info.SideType.CLIENT);
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
			type = options.valueOf(typeO);
		}
		Deobfuscator deobfuscator = new Deobfuscator(version, Objects.requireNonNull(type, "INVALID SIDE TYPE DETECTED"));
		deobfuscator.deobfuscate();
	}
}