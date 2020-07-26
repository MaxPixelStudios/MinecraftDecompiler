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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ProguardMappingReader extends MappingReader {
	public ProguardMappingReader(BufferedReader reader) {
		super(reader);
	}
	public ProguardMappingReader(Reader rd) {
		super(rd);
	}
	public ProguardMappingReader(InputStream is) {
		super(is);
	}
	public ProguardMappingReader(String path) throws FileNotFoundException, NullPointerException {
		super(path);
	}

	@Override
	protected ProguardMappingProcessor getProcessor() {
		return new ProguardMappingProcessor();
	}

	private static class ProguardMappingProcessor extends NonPackageMappingProcessor {
		@Override
		public List<ClassMapping> process(Stream<String> lines) {
			ObjectArrayList<ClassMapping> mappings = new ObjectArrayList<>(5000);
			AtomicReference<ClassMapping> currClass = new AtomicReference<>();
			lines.forEach(s -> {
				if(!s.startsWith(" ")) {
					if(currClass.get() != null) {
						mappings.add(currClass.getAndSet(processClass(s.substring(0, s.indexOf(':') + 1))));
					} else currClass.set(processClass(s.substring(0, s.indexOf(':') + 1)));
				} else {
					if(s.contains("(") && s.contains(")")) currClass.get().addMethod(processMethod(s.trim()));
					else currClass.get().addField(processField(s.trim()));
				}
			});
			if(currClass.get() != null) mappings.add(currClass.get());
			return mappings;
		}

		@Override
		protected ClassMapping processClass(String line) {
			String[] split = line.split("( -> )|:");
			return new ClassMapping(split[1], split[0]);
		}

		@Override
		protected MethodMapping processMethod(String line) {
			MethodMapping mapping = new MethodMapping();
			String[] linenums = line.trim().split(":");
			String[] method;
			if(linenums.length == 3){
				mapping.setLinenumber(Integer.parseInt(linenums[0]), Integer.parseInt(linenums[1]));
				method = linenums[2].split(" ");
			} else method = linenums[0].split(" ");
			mapping.setReturnVal(method[0]);
			mapping.setObfuscatedName(method[3]);
			String ori_arg = method[1];
			if(ori_arg.contains("()")) mapping.setOriginalName(ori_arg.substring(0, ori_arg.indexOf('(')));
			else {
				String[] ori_args = ori_arg.split("([(]|[)])+");
				mapping.setOriginalName(ori_args[0]);
				mapping.setArgTypes(ori_args[1].split(","));
			}
			return mapping;
		}

		@Override
		protected FieldMapping processField(String line) {
			String[] strings = line.trim().split("( -> )| ");
			return new FieldMapping(strings[2], strings[1], strings[0]);
		}
	}
}