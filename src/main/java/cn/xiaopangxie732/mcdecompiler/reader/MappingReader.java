package cn.xiaopangxie732.mcdecompiler.reader;

import cn.xiaopangxie732.mcdecompiler.mapping.ClassMapping;
import cn.xiaopangxie732.mcdecompiler.mapping.FieldMapping;
import cn.xiaopangxie732.mcdecompiler.mapping.MethodMapping;

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

	public abstract ArrayList<ClassMapping> deobfuscate();
	@Override
	public void close() throws Exception {
		reader.close();
	}
	protected abstract static class MappingProcessor {
		protected abstract ClassMapping processClass(String line);
		protected abstract MethodMapping processMethod(String line);
		protected abstract FieldMapping processField(String line);
	}
}