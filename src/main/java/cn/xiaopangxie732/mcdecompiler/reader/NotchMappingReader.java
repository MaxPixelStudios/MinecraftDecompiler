package cn.xiaopangxie732.mcdecompiler.reader;

import cn.xiaopangxie732.mcdecompiler.mapping.ClassMapping;
import cn.xiaopangxie732.mcdecompiler.mapping.FieldMapping;
import cn.xiaopangxie732.mcdecompiler.mapping.MethodMapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotchMappingReader extends MappingReader {

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
	public ArrayList<ClassMapping> deobfuscate() {
		return null;
	}

	private class NotchMappingProcessor extends MappingProcessor {
		@Override
		protected ClassMapping processClass(String line) {
			String[] names = line.split("(?:[^\\s\":./()]+\\.)*[^\\s\":./()]+");
			return new ClassMapping(names[0], names[2]);
		}

		@Override
		protected MethodMapping processMethod(String line) {
			String[] strings = line.trim().split(":");
			return null;
		}

		@Override
		protected FieldMapping processField(String line) {
			return null;
		}
	}
}