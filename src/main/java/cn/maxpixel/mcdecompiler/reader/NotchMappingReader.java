package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.FieldMapping;
import cn.maxpixel.mcdecompiler.mapping.MethodMapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class NotchMappingReader extends MappingReader {
	private  NotchMappingProcessor processor = new NotchMappingProcessor();
	public NotchMappingReader(BufferedReader reader) {
		super(reader);
	}
	public NotchMappingReader(Reader rd) {
		super(rd);
	}
	public NotchMappingReader(InputStream is) {
		super(is);
	}
	public NotchMappingReader(String path) throws FileNotFoundException {
		super(path);
	}

	@Override
	public ArrayList<ClassMapping> getMappings() {
		ArrayList<ClassMapping> mappings = new ArrayList<>();
		AtomicReference<ClassMapping> currClass = new AtomicReference<>(null);
		reader.lines().forEach(s -> {
			if(!s.startsWith("#") && s.contains("<clinit>") && s.contains("<init>")) {
				if(!s.startsWith(" ")) {
					if(currClass.get() != null) mappings.add(currClass.get());
					currClass.set(processor.processClass(s));
				} else {
					if(NotchMappingProcessor.startsWithNumber(s)) currClass.get().addMethod(processor.processMethod(s));
					else currClass.get().addField(processor.processField(s));
				}
			}
		});
		return mappings;
	}

	private class NotchMappingProcessor extends MappingProcessor {
		@Override
		public ClassMapping processClass(String line) {
			String[] names = line.split("(?:[^\\s\":./()]+\\.)*[^\\s\":./()]+");
			return new ClassMapping(names[0], names[2]);
		}

		@Override
		public MethodMapping processMethod(String line) {
			MethodMapping methodMapping = new MethodMapping();
			String[] strings = line.trim().split(":");
			methodMapping.setLinenumber(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
			String[] methodSplit = strings[2].split(" ");
			methodMapping.setReturnVal(methodSplit[0]);
			String[] nameSplit = methodSplit[1].split("\\(");
			methodMapping.setOriginalName(nameSplit[0]);
			methodMapping.setObfuscatedName(methodSplit[3]);
			methodMapping.setArgTypes(nameSplit[1].split("\\)")[0].split(", "));
			return methodMapping;
		}

		@Override
		public FieldMapping processField(String line) {
			String[] strings = line.trim().split(" ");
			return new FieldMapping(strings[1], strings[3], strings[0]);
		}
	}
}