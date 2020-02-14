/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2020  XiaoPangxie732
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Scanner;

public class DeobfuscatorCommandLine {
	public static final String USAGE = "java -jar MinecraftDecompiler.jar <version(1.14 or above)> <c or s>";
	private static Logger LOGGER = LogManager.getLogger();
	public static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1080));
	public static void main(String[] args) {
		System.setProperty("log4j2.skipJansi", "false");
		String version = "";
		Info.MappingType type = Info.MappingType.CLIENT;
		if(args.length == 0) {
			System.out.println(USAGE);
			try(Scanner sc = new Scanner(System.in)) {
				System.out.println("Type a version(1.14.4 or above)");
				version = sc.next();
				System.out.println("Type a side: (c)lient, (s)erver");
				String t = sc.next();
				if(t.equalsIgnoreCase("client") || t.equalsIgnoreCase("c")) {
					type = Info.MappingType.CLIENT;
				} else if(t.equalsIgnoreCase("server") || t.equalsIgnoreCase("s")) {
					type = Info.MappingType.SERVER;
				}
			}
		} else {
			version = args[0];
			if(args[1].equalsIgnoreCase("client") || args[1].equalsIgnoreCase("c")) {
				type = Info.MappingType.CLIENT;
			} else if(args[1].equalsIgnoreCase("server") || args[1].equalsIgnoreCase("s")) {
				type = Info.MappingType.SERVER;
			}
		}
		Deobfuscator deobfuscator = new Deobfuscator(version, type);
		deobfuscator.deobfuscate();
	}
}