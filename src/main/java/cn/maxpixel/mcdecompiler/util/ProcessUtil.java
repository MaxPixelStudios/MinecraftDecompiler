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

package cn.maxpixel.mcdecompiler.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	public static void waitForProcess(Process pro) {
		try(BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
		    BufferedReader err = new BufferedReader(new InputStreamReader(pro.getErrorStream()))) {
			Thread inT = new Thread(() -> {
				try {
					String ins;
					while ((ins = in.readLine()) != null) {
						LOGGER.debug(ins);
					}
				} catch (Throwable e) {
					LOGGER.catching(e);
				}
			});
			Thread errT = new Thread(() -> {
				try {
					String ins;
					while ((ins = err.readLine()) != null) {
						LOGGER.error(ins);
					}
				} catch (Throwable e) {
					LOGGER.catching(e);
				}
			});
			inT.start();
			errT.start();
			pro.waitFor();
		} catch (IOException | InterruptedException e) {
			LOGGER.catching(e);
		}
	}
}