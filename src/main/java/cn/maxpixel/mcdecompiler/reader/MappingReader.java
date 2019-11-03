package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.FieldMapping;
import cn.maxpixel.mcdecompiler.mapping.MethodMapping;

import java.io.*;
import java.util.ArrayList;
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
	protected MappingReader(String path) throws FileNotFoundException {
		this(new FileReader(Objects.requireNonNull(path)));
	}

	public abstract ArrayList<ClassMapping> getMappings();
	@Override
	public void close() throws Exception {
		reader.close();
	}
	protected abstract static class MappingProcessor {
		protected abstract ClassMapping processClass(String line);
		protected abstract MethodMapping processMethod(String line);
		protected abstract FieldMapping processField(String line);
		public static boolean startsWithNumber(String text) {
			char[] numbers = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
			for (char number : numbers) {
				if(text.trim().charAt(0) == number) return true;
			}
			return false;
		}
	}
}