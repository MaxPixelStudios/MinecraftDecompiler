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

package cn.maxpixel.mcdecompiler;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.reader.NotchMappingReader;
import cn.xiaopangxie732.easynetwork.coder.ByteDecoder;
import cn.xiaopangxie732.easynetwork.http.HttpConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Deobfuscator {
	private static final Logger LOGGER = LogManager.getLogger();
	private String version;
	private Info.MappingType type;
	private static JsonArray versions;
	private JsonObject version_json;
	private String MAIN_CLASS;
	static {
		versions = JsonParser.parseString(ByteDecoder.decodeToString(HttpConnection
				.newGetConnection("https://launchermeta.mojang.com/mc/game/version_manifest.json", DeobfuscatorCommandLine.PROXY)))
				.getAsJsonObject().get("versions").getAsJsonArray();
	}
	public Deobfuscator(String version, Info.MappingType type) {
		this.version = Objects.requireNonNull(version);
		this.type = Objects.requireNonNull(type);
		downloadMapping();
		downloadJar();
	}
	private Deobfuscator downloadMapping() {
		checkVersion();
		File f = new File(Info.getMappingPath(version, type));
		f.getParentFile().mkdirs();
		if(!f.exists()) {
			try(FileOutputStream fout = new FileOutputStream(f)) {
				f.createNewFile();
				fout.write(HttpConnection.newGetConnection(
						version_json.get("downloads").getAsJsonObject().get(type.toString() + "_mappings").getAsJsonObject().get("url").getAsString(),
						DeobfuscatorCommandLine.PROXY));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	private Deobfuscator downloadJar() {
		File f = new File(Info.getMcJarPath(version, type));
		f.getParentFile().mkdirs();
		if(!f.exists()) {
			try(FileOutputStream fout = new FileOutputStream(f)) {
				f.createNewFile();
				fout.write(HttpConnection.newGetConnection(
						version_json.get("downloads").getAsJsonObject().get(type.toString()).getAsJsonObject().get("url").getAsString(),
						DeobfuscatorCommandLine.PROXY));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	public Deobfuscator deobfuscate() {
		try {
			LOGGER.info("deobfuscating...");
			File f = new File(Info.getDeobfuscateJarPath(version, type));
			f.getParentFile().mkdirs();
			f.createNewFile();
			File temp = new File(Info.TEMP_PATH);
			temp.mkdirs();
			decompress(new JarFile(Info.getMcJarPath(version, type)), new File(temp, version + "/" + type.toString() + "/originalClasses"));
			LOGGER.info("remapping...");
			try(NotchMappingReader mappingReader = new NotchMappingReader(Info.getMappingPath(version, type))) {
				SuperClassMapping superClassMapping = new SuperClassMapping();
				ASMRemapper remapper = new ASMRemapper(mappingReader.getMappingsMapByObfuscatedName(), mappingReader.getMappingsMapByOriginalName(), superClassMapping);
				Map<String, ClassMapping> mappings = mappingReader.getMappingsMapByObfuscatedName();
				Files.createDirectories(Paths.get("temp", version, type.toString(), "deobfuscatedClasses"));
				$$listFiles$$(Paths.get("temp", version, type.toString(), "originalClasses").toFile(), path -> {
					try(InputStream inputStream = Files.newInputStream(path.toPath())) {
						ClassReader reader = new ClassReader(inputStream);
						reader.accept(superClassMapping, ClassReader.SKIP_DEBUG);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				$$listFiles$$(Paths.get("temp", version, type.toString(), "originalClasses").toFile(), path -> {
					try(InputStream inputStream = Files.newInputStream(path.toPath())) {
						ClassReader reader = new ClassReader(inputStream);
						ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
						reader.accept(new ClassRemapper(writer, remapper), ClassReader.SKIP_DEBUG);
						ClassMapping mapping = mappings.get((path.getPath().contains("net\\minecraft\\") ? NamingUtil.asJavaName(path.getPath().replace("temp\\" + version + "\\" + type + "\\originalClasses\\", "")) : path.getName()).replace(".class", ""));
						if(mapping != null) {
							String s = NamingUtil.asNativeName(mapping.getOriginalName());
							Files.createDirectories(Paths.get("temp", version, type.toString(), "deobfuscatedClasses",
									s.substring(0, s.lastIndexOf('/'))));
							Files.write(Paths.get("temp", version, type.toString(), "deobfuscatedClasses",
									s + ".class"), writer.toByteArray(),
									StandardOpenOption.CREATE, StandardOpenOption.WRITE);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
			compress(f, new File(temp, version + "/" + type + "/" + "deobfuscatedClasses"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	private void runProcess(String command) {
		try {
			Process pro = Runtime.getRuntime().exec(new String[] {"cmd", "/C", command});
			try(BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			    BufferedReader err = new BufferedReader(new InputStreamReader(pro.getErrorStream()))) {
				Thread inT = new Thread(() -> {
					try {
						String ins;
						while ((ins = in.readLine()) != null) {
							LOGGER.debug(ins);
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
				});
				Thread errT = new Thread(() -> {
					try {
						String ins;
						while ((ins = err.readLine()) != null) {
							LOGGER.error(ins);
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
				});
				inT.start();
				errT.start();
				pro.waitFor();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println(command);
		}
	}
	private void $$listFiles$$(File baseDir, Consumer<File> fileConsumer) {
		for(File childFile : Objects.requireNonNull(baseDir.listFiles())) {
			if(childFile.isFile())
				if(childFile.getPath().endsWith(".class")) fileConsumer.accept(childFile);
				else runProcess("copy \"" + childFile.getAbsolutePath() + "\" /B \"" + Paths.get(new File(Info.TEMP_PATH).getAbsolutePath(),
						version, type.toString(), "deobfuscatedClasses", childFile.getName()).toAbsolutePath() + "\"");
			else if(childFile.isDirectory() && !childFile.getAbsolutePath().contains("net"))
				runProcess("xcopy \"" + childFile.getAbsolutePath() + "\" \"" + Paths.get("temp", version,
						type.toString(), "deobfuscatedClasses", childFile.getName()).toFile().getAbsolutePath() + "\" /E /I /H /Y");
		}
		for(File minecraft : Objects.requireNonNull(new File(baseDir, "net/minecraft").listFiles())) {
			if (minecraft.isDirectory()) $$$a$$$(minecraft, fileConsumer);
		}
		File manifest = Paths.get("temp", version,
				type.toString(), "deobfuscatedClasses/META-INF/MANIFEST.MF").toFile();
		if(manifest.exists()) manifest.delete();
		File mojangRSA = Paths.get("temp", version,
				type.toString(), "deobfuscatedClasses/META-INF/MOJANGCS.RSA").toFile();
		if(mojangRSA.exists()) mojangRSA.delete();
		File mojangSF = Paths.get("temp", version,
				type.toString(), "deobfuscatedClasses/META-INF/MOJANGCS.SF").toFile();
		if(mojangSF.exists()) mojangSF.delete();
	}
	private void $$$a$$$(File dir, Consumer<File> fileConsumer) {
		for(File f : Objects.requireNonNull(dir.listFiles())) {
			if(f.isFile()) fileConsumer.accept(f);
			else if(f.isDirectory()) $$$a$$$(f, fileConsumer);
		}
	}
	private void compress(File f, File from) {
		try {
			Manifest manifest = new Manifest();
			switch (type) {
				case CLIENT:
					manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
					manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN_CLASS);
					break;
				case SERVER:
					manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
					manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "net.minecraft.server.MinecraftServer");
					break;
			}
			JarOutputStream out = new JarOutputStream(new FileOutputStream(f), manifest);
			try {
				if(from.exists()) processDirectory(from, out);
			} finally {
				out.finish();
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void processDirectory(File directory, JarOutputStream stream) throws IOException {
		if(directory.listFiles() != null) {
			for (File children : directory.listFiles()) {
				if (children.isDirectory()) processDirectory(children, stream);
				else if (!children.getAbsolutePath().contains("MANIFEST.MF") && !children.getAbsolutePath().contains("MOJANGCS")) {
					stream.putNextEntry(new ZipEntry(children.getPath().replace("temp\\" + version + "\\" + type + "\\deobfuscatedClasses\\", "")
																		.replace('\\', '/')));
					stream.write(Files.readAllBytes(children.toPath()));
					stream.closeEntry();
					stream.flush();
				}
			}
		}
	}
	private void decompress(JarFile jar, File target) {
		LOGGER.info("decompressing");
		jar.stream().forEach(entry -> {
			if(entry.isDirectory()) {
				new File(target, entry.getName()).mkdirs();
			} else {
				try {
					File f = new File(target, entry.getName());
					if(!f.exists()) {
						f.getParentFile().mkdirs();
						f.createNewFile();
						try(InputStream is = jar.getInputStream(entry);
						    FileOutputStream out = new FileOutputStream(f)) {
							for (int i = is.read(); i != -1; i = is.read()) out.write(i);
						}
					}
				} catch (IOException ex) {
					LOGGER.error("A exception occurred while decompressing jar file", ex);
				}
			}
		});
		try {
			jar.close();
		} catch (IOException ex) {
			LOGGER.error("A exception occurred while closing jar file", ex);
		}
	}
	private void checkVersion() {
		LOGGER.info("checking version...");
		for (JsonElement element : versions) {
			JsonObject object = element.getAsJsonObject();
			if(object.get("id").getAsString().equalsIgnoreCase(version)) {
				version_json = JsonParser.parseString(ByteDecoder.decodeToString(HttpConnection
						.newGetConnection(object.get("url").getAsString(), DeobfuscatorCommandLine.PROXY))).getAsJsonObject();
				if (version_json.get("downloads").getAsJsonObject().has(type.toString() + "_mappings")) break;
				else throw new RuntimeException("This version doesn't have mappings");
			}
		}
		MAIN_CLASS = version_json.get("mainClass").getAsString();
	}
}