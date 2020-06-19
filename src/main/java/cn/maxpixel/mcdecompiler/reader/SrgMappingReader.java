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

package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.FieldMapping;
import cn.maxpixel.mcdecompiler.mapping.MethodMapping;
import cn.maxpixel.mcdecompiler.mapping.PackageMapping;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class SrgMappingReader extends MappingReader {
	public SrgMappingReader(BufferedReader reader) {
		super(reader);
	}
	public SrgMappingReader(Reader rd) {
		super(rd);
	}
	public SrgMappingReader(InputStream is) {
		super(is);
	}
	public SrgMappingReader(String path) throws FileNotFoundException, NullPointerException {
		super(path);
	}
	@Override
	protected MappingProcessor getProcessor() {
		return new SrgMappingProcessor();
	}
	private static class SrgMappingProcessor extends MappingProcessor {
		@Override
		public List<ClassMapping> process(Stream<String> stream) {
			ObjectArrayList<ClassMapping> mappings = new ObjectArrayList<>(5000);
			AtomicReference<ClassMapping> currClass = new AtomicReference<>();
			return null;
		}
		@Override
		protected ClassMapping processClass(String line) {
			return null;
		}
		@Override
		protected List<PackageMapping> processPackage(Stream<String> stream) {
			return null;
		}
		@Override
		protected MethodMapping processMethod(String line) {
			return null;
		}
		@Override
		protected FieldMapping processField(String line) {
			return null;
		}
	}
}