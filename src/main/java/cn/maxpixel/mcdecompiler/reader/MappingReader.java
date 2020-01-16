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

package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.FieldMapping;
import cn.maxpixel.mcdecompiler.mapping.MethodMapping;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public abstract class MappingReader implements AutoCloseable {
	protected BufferedReader reader;

	protected MappingReader(BufferedReader reader) {
		this.reader = reader;
	}
	protected MappingReader(Reader rd) {
		this(new BufferedReader(rd));
	}
	protected MappingReader(InputStream is) {
		this(new InputStreamReader(is));
	}
	protected MappingReader(String path) throws FileNotFoundException, NullPointerException {
		this(new FileReader(Objects.requireNonNull(path)));
	}

	public abstract ArrayList<ClassMapping> getMappings();
	public abstract HashMap<String, ClassMapping> getMappingsMap();
	@Override
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader = null;
		}
	}
	protected abstract static class MappingProcessor {
		protected abstract ClassMapping processClass(String line);
		protected abstract MethodMapping processMethod(String line);
		protected abstract FieldMapping processField(String line);
		protected static boolean startsWithNumber(String text) {
			char[] numbers = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
			char start = text.trim().charAt(0);
			for (char number : numbers) {
				if(start == number) return true;
			}
			return false;
		}
	}
}