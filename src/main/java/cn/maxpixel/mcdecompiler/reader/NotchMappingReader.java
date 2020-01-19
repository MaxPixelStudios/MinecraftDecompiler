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

import cn.maxpixel.mcdecompiler.mapping.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotchMappingReader extends MappingReader {
	private NotchMappingProcessor processor = new NotchMappingProcessor();
	private HashMap<String, ClassMapping> mappings = new HashMap<>(4850);
	public NotchMappingReader(BufferedReader reader) {
		super(reader);
		AtomicReference<ClassMapping> currClass = new AtomicReference<>(null);
		reader.lines().forEach(s -> {
			if(!s.startsWith("#") && !s.contains("<clinit>")) {
				if(!s.startsWith(" ")) {
					if(currClass.get() != null)
						mappings.put(currClass.get().getObfuscatedName(), currClass.getAndSet(processor.processClass(s)));
					else currClass.set(processor.processClass(s));
				} else {
					if(NotchMappingProcessor.startsWithNumber(s)) currClass.get().addMethod(processor.processMethod(s.trim()));
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
	public ArrayList<ClassMapping> getMappings() {
		return new ArrayList<>(mappings.values());
	}

	@Override
	public HashMap<String, ClassMapping> getMappingsMap() {
		return mappings;
	}

	private static class NotchMappingProcessor extends MappingProcessor {
		private static Pattern pattern = Pattern.compile("(?:[^\\s\":./()]+\\.)*[^\\s\":./()]+");
		@Override
		public ClassMapping processClass(String line) {
			ClassMapping mapping = new ClassMapping();
			Matcher m = pattern.matcher(line);
			m.find();
			mapping.setOriginalName(m.group());
			m.find();m.find();
			mapping.setObfuscatedName(m.group());
			return mapping;
		}

		@Override
		public MethodMapping processMethod(String line) {
			MethodMapping mapping = new MethodMapping();
			Matcher m = pattern.matcher(line);
			m.find();
			int l1 = Integer.parseInt(m.group());
			m.find();
			mapping.setLinenumber(l1, Integer.parseInt(m.group()));
			m.find();
			mapping.setReturnVal(m.group());
			m.find();
			mapping.setOriginalName(m.group());
			m.find();
			String s1;
			if(!(s1 = m.group()).equals("->")) {
				mapping.setArgTypes(s1.split(","));
				m.find();
				m.find();
				mapping.setObfuscatedName(m.group());
			} else {
				m.find();
				mapping.setObfuscatedName(m.group());
			}
			return mapping;
		}

		@Override
		public FieldMapping processField(String line) {
			String[] strings = line.trim().split(" ");
			return new FieldMapping(strings[3], strings[1], strings[0]);
		}
	}
}