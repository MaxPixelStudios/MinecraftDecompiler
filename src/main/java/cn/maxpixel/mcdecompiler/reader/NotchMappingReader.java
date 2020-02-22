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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NotchMappingReader extends MappingReader {
	private NotchMappingProcessor processor = new NotchMappingProcessor();
	private ObjectArrayList<ClassMapping> mappings = new ObjectArrayList<>(4850);
	public NotchMappingReader(BufferedReader reader) {
		super(reader);
		AtomicReference<ClassMapping> currClass = new AtomicReference<>(null);
		reader.lines().forEach(s -> {
			if(!s.startsWith("#") && !s.contains("<clinit>") && !s.contains("package-info") && !s.contains("<init>")) {
				if(!s.startsWith(" ")) {
					if(currClass.get() != null) {
						mappings.add(currClass.getAndSet(processor.processClass(s)));
					} else currClass.set(processor.processClass(s));
				} else {
					if(s.contains("(") && s.contains(")")) currClass.get().addMethod(processor.processMethod(s.trim()));
					else currClass.get().addField(processor.processField(s.trim()));
				}
			}
		});
	}
	public NotchMappingReader(Reader rd) {
		this(new BufferedReader(rd));
	}
	public NotchMappingReader(InputStream is) {
		this(new InputStreamReader(is));
	}
	public NotchMappingReader(String path) throws FileNotFoundException, NullPointerException {
		this(new FileReader(Objects.requireNonNull(path)));
	}

	@Override
	public List<ClassMapping> getMappings() {
		return mappings;
	}

	@Override
	public Map<String, ClassMapping> getMappingsMapByObfuscatedName() {
		return mappings.stream().collect(Collectors.toMap(ClassMapping::getObfuscatedName, Function.identity(),
				(classMapping, classMapping2) -> {throw new IllegalArgumentException("Key duplicated!");}, Object2ObjectOpenHashMap::new));
	}

	@Override
	public Map<String, ClassMapping> getMappingsMapByOriginalName() {
		return mappings.stream().collect(Collectors.toMap(ClassMapping::getOriginalName, Function.identity(),
				(classMapping, classMapping2) -> {throw new IllegalArgumentException("Key duplicated!");}, Object2ObjectOpenHashMap::new));
	}

	private static class NotchMappingProcessor extends MappingProcessor {
		@Override
		public ClassMapping processClass(String line) {
			String[] split = line.split("(\\s->\\s)+|(:)+");
			return new ClassMapping(split[1], split[0]);
		}

		@Override
		public MethodMapping processMethod(String line) {
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
		public FieldMapping processField(String line) {
			String[] strings = line.trim().split("(\\s->\\s)+|(\\s)+");
			return new FieldMapping(strings[2], strings[1], strings[0]);
		}
	}
}