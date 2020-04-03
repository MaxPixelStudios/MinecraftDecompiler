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

package cn.maxpixel.mcdecompiler.deobfuscator;

import cn.maxpixel.mcdecompiler.*;
import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.reader.ProguardMappingReader;
import cn.maxpixel.mcdecompiler.remapper.ProguardMappingRemapper;
import cn.maxpixel.mcdecompiler.asm.SuperClassMapping;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.JarUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import cn.xiaopangxie732.easynetwork.http.HttpConnection;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ProguardDeobfuscator extends AbstractDeobfuscator {
	public ProguardDeobfuscator(String version, Info.SideType type) {
		super(version, type);
		downloadMapping();
		downloadJar();
	}
	private void downloadMapping() {
		checkVersion();
		if(!version_json.get("downloads").getAsJsonObject().has(type.toString() + "_mappings"))
			throw new RuntimeException("This version doesn't have mappings. Please use 1.14.4 or above");
		File f = new File(InfoProviders.get().getProguardMappingDownloadPath(version, type));
		f.getParentFile().mkdirs();
		if(!f.exists()) {
			LOGGER.info("downloading mapping...");
			try(FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
				channel.transferFrom(HttpConnection.newGetConnection(
						version_json.get("downloads").getAsJsonObject().get(type.toString() + "_mappings").getAsJsonObject().get("url").getAsString(),
						DeobfuscatorCommandLine.PROXY).getInChannel(), 0, Long.MAX_VALUE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public ProguardDeobfuscator deobfuscate() {
		try {
			LOGGER.info("deobfuscating...");
			File deobfuscateJar = new File(InfoProviders.get().getDeobfuscateJarPath(version, type));
			deobfuscateJar.getParentFile().mkdirs();
			deobfuscateJar.createNewFile();
			File originalClasses = new File(InfoProviders.get().getTempOriginalClassesPath(version, type));
			originalClasses.mkdirs();
			JarUtil.decompressJar(InfoProviders.get().getMcJarPath(version, type), originalClasses);
			LOGGER.info("remapping...");
			try(ProguardMappingReader mappingReader = new ProguardMappingReader(InfoProviders.get().getProguardMappingDownloadPath(version, type))) {
				SuperClassMapping superClassMapping = new SuperClassMapping();
				listMcClassFiles(originalClasses, path -> {
					try(InputStream inputStream = Files.newInputStream(path.toPath())) {
						ClassReader reader = new ClassReader(inputStream);
						reader.accept(superClassMapping, ClassReader.SKIP_DEBUG);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				ProguardMappingRemapper remapper = new ProguardMappingRemapper(mappingReader.getMappingsMapByObfuscatedName(), mappingReader.getMappingsMapByOriginalName(), superClassMapping);
				Map<String, ClassMapping> mappings = mappingReader.getMappingsMapByObfuscatedName();
				Files.createDirectories(Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type)));
				listMcClassFiles(originalClasses, path -> {
					try(InputStream inputStream = Files.newInputStream(path.toPath())) {
						ClassReader reader = new ClassReader(inputStream);
						ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
						reader.accept(new ClassRemapper(writer, remapper), ClassReader.SKIP_DEBUG);
						String mappingKey;
						if(path.getPath().contains("minecraft" + Info.FILE_SEPARATOR)) {
							mappingKey = NamingUtil.asJavaName("net/minecraft" + path.getPath().substring(path.getPath().
									indexOf("minecraft" + Info.FILE_SEPARATOR) + 9));
						} else if(path.getPath().contains("mojang" + Info.FILE_SEPARATOR)) {
							mappingKey = NamingUtil.asJavaName("com/mojang" + path.getPath().substring(path.getPath().
									indexOf("mojang" + Info.FILE_SEPARATOR) + 6));
						} else {
							mappingKey = NamingUtil.asJavaName(path.getName());
						}
						ClassMapping mapping = mappings.get(mappingKey);
						if(mapping != null) {
							String s = NamingUtil.asNativeName(mapping.getOriginalName());
							Files.createDirectories(Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), s.substring(0, s.lastIndexOf('/'))));
							Files.write(Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), s + ".class"), writer.toByteArray(),
									StandardOpenOption.CREATE, StandardOpenOption.WRITE);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
			copyOthers(originalClasses);
			JarUtil.compressJar(type == Info.SideType.CLIENT ? "net.minecraft.client.main.Main" : "net.minecraft.server.MinecraftServer",
					deobfuscateJar, new File(InfoProviders.get().getTempRemappedClassesPath(version, type)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	private void copyOthers(File baseDir) {
		for(File childFile : Objects.requireNonNull(baseDir.listFiles())) {
			if(childFile.isFile() && !childFile.getPath().endsWith(".class")) {
				FileUtil.copyFile(childFile.toPath(), Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), childFile.getName()));
			} else if(childFile.isDirectory() && !childFile.getAbsolutePath().contains("net")
					&& !childFile.getAbsolutePath().contains("blaze3d") && !childFile.getAbsolutePath().contains("realmsclient")) {
				FileUtil.copyDirectory(childFile.toPath(), Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type)));
			}
		}
		File manifest = Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), "META-INF", "MANIFEST.MF").toFile();
		if(manifest.exists()) manifest.delete();
		File mojangRSA = Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), "META-INF", "MOJANGCS.RSA").toFile();
		if(mojangRSA.exists()) mojangRSA.delete();
		File mojangSF = Paths.get(InfoProviders.get().getTempRemappedClassesPath(version, type), "META-INF", "MOJANGCS.SF").toFile();
		if(mojangSF.exists()) mojangSF.delete();
	}
	private void listMcClassFiles(File baseDir, Consumer<File> fileConsumer) {
		for(File childFile : Objects.requireNonNull(baseDir.listFiles())) {
			if(childFile.isFile() && childFile.getPath().endsWith(".class")) fileConsumer.accept(childFile);
		}
		for(File minecraft : Objects.requireNonNull(new File(baseDir, "net/minecraft").listFiles())) {
			if (minecraft.isDirectory()) processPackage(minecraft, fileConsumer);
		}
		if(type == Info.SideType.CLIENT) {
			for(File mojang : Objects.requireNonNull(new File(baseDir, "com/mojang").listFiles())) {
				if (mojang.isDirectory()) processPackage(mojang, fileConsumer);
			}
		}
	}
	private void processPackage(File dir, Consumer<File> fileConsumer) {
		for(File f : Objects.requireNonNull(dir.listFiles())) {
			if(f.isFile()) {
				fileConsumer.accept(f);
			} else if(f.isDirectory()) {
				processPackage(f, fileConsumer);
			}
		}
	}
}