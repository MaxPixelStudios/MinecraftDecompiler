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
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Optional;
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
	protected NonPackageMappingProcessor getProcessor() {
		return new SrgMappingProcessor();
	}
	private static class SrgMappingProcessor extends MappingProcessor {
		private Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>();
		@Override
		public List<ClassMapping> process(Stream<String> lines) {
			ObjectArrayList<ClassMapping> mappings = new ObjectArrayList<>();
			lines.filter(s -> s.startsWith("CL:")).forEach(s -> mappings.add(processClass(s)));
			for(int i = 0; i < mappings.size(); ++i)
				map.put(mappings.get(i).getObfuscatedName(), i); //Use obf name
			lines.filter(s -> !(s.startsWith("CL:") || s.startsWith("PK:"))).forEach(s -> {
				if(s.startsWith("FD:")) {
					FieldMapping mapping = processField(s);
					String obfClassName = mapping.getObfuscatedName().substring(0, mapping.getObfuscatedName().lastIndexOf('.'));
					String oriClassName = mapping.getOriginalName().substring(0, mapping.getOriginalName().lastIndexOf('.'));
					int index = map.getInt(obfClassName);
					ClassMapping cm;
					if(index < mappings.size()) {
						cm = mappings.get(index);
						if(cm == null) {
							cm = new ClassMapping(obfClassName, oriClassName);
							mappings.add(cm);
						}
					} else {
						cm = new ClassMapping(obfClassName, oriClassName);
						mappings.add(cm);
					}
					cm.addField(new FieldMapping(mapping.getObfuscatedName().substring(mapping.getObfuscatedName().lastIndexOf('.') + 1),
												mapping.getOriginalName().substring(mapping.getOriginalName().lastIndexOf('.') + 1)));
				} else if(s.startsWith("MD:")) {
					processMethod(s);
				}
			});
			return mappings;
		}
		@Override
		protected ClassMapping processClass(String line) {
			String[] split = line.split(" ");
			return new ClassMapping(NamingUtil.asJavaName(split[1]), NamingUtil.asJavaName(split[2]));
		}
		@Override
		protected MethodMapping processMethod(String line) {
			String[] split = line.split(" ");
			return null;
		}
		@Override
		protected FieldMapping processField(String line) {
			String[] split = line.split(" ");
			return new FieldMapping(NamingUtil.asJavaName(split[1]), NamingUtil.asJavaName(split[2]));
		}
		@Override
		protected List<PackageMapping> processPackage(Stream<String> lines) {
			ObjectArrayList<PackageMapping> mappings = new ObjectArrayList<>();
			lines.filter(s -> s.startsWith("PK:")).forEach(s -> {
				String[] split = s.split(" ");
				mappings.add(new PackageMapping(NamingUtil.asJavaName(split[1]), NamingUtil.asJavaName(split[2])));
			});
			return mappings;
		}
	}
}