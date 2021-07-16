/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
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

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.Properties;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.proguard.ProguardFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.proguard.ProguardMethodMapping;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import cn.maxpixel.mcdecompiler.util.VersionManifest;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static cn.maxpixel.mcdecompiler.MinecraftDecompiler.HTTP_CLIENT;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class ProguardMappingReader extends AbstractMappingReader {
    public ProguardMappingReader(BufferedReader reader) {
        super(reader);
    }

    public ProguardMappingReader(Reader rd) {
        super(rd);
    }

    public ProguardMappingReader(InputStream is) {
        super(is);
    }

    public ProguardMappingReader(String path) throws FileNotFoundException {
        super(path);
    }

    public ProguardMappingReader(String version, Info.SideType type) {
        super(downloadMapping(version, type));
    }

    private static BufferedReader downloadMapping(String version, Info.SideType type) {
        JsonObject versionDownloads = VersionManifest.get(version).get("downloads").getAsJsonObject();
        if(!versionDownloads.has(type + "_mappings"))
            throw new IllegalArgumentException("Version \"" + version + "\" doesn't contain Proguard mappings. Please use 1.14.4 or above");
        Path p = Properties.getDownloadedProguardMappingPath(version, type);
        if(Files.notExists(p)) {
            try {
                LOGGER.info("Downloading mapping...");
                HttpRequest request = HttpRequest
                        .newBuilder(URI.create(versionDownloads.get(type + "_mappings").getAsJsonObject().get("url").getAsString()))
                        .build();
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(FileUtil.ensureFileExist(p), WRITE, TRUNCATE_EXISTING));
            } catch (IOException | InterruptedException e) {
                LOGGER.fatal("Error downloading Proguard mapping file");
                throw Utils.wrapInRuntime(LOGGER.throwing(e));
            }
        }
        try {
            return Files.newBufferedReader(p);
        } catch (IOException e) {
            LOGGER.fatal("Error creating BufferedReader for Proguard mapping file");
            throw Utils.wrapInRuntime(LOGGER.throwing(e));
        }
    }

    private final ProguardMappingProcessor PROCESSOR = new ProguardMappingProcessor();

    @Override
    public ProguardMappingProcessor getProcessor() {
        return PROCESSOR;
    }

    private class ProguardMappingProcessor implements PairedMappingProcessor {
        private final ObjectArrayList<PairedClassMapping> mappings = new ObjectArrayList<>(5000);
        @Override
        public ObjectList<PairedClassMapping> process() {
            if(mappings.isEmpty()) {
                AtomicReference<PairedClassMapping> currClass = new AtomicReference<>();
                lines.forEach(s -> {
                    if(!s.startsWith("    ")) {
                        if(currClass.get() != null) {
                            mappings.add(currClass.getAndSet(processClass(s)));
                        } else currClass.set(processClass(s));
                    } else {
                        if(s.contains("(") && s.contains(")")) currClass.get().addMethod(processMethod(s.trim()));
                        else currClass.get().addField(processField(s.trim()));
                    }
                });
                mappings.add(currClass.get()); // Add the last mapping stored in the AtomicReference
            }
            return ObjectLists.unmodifiable(mappings);
        }

        @Override
        public PairedClassMapping processClass(String line) {
            String[] split = line.split(" -> |:");
            return new PairedClassMapping(NamingUtil.asNativeName(split[1]), NamingUtil.asNativeName(split[0]));
        }

        @Override
        public ProguardMethodMapping processMethod(String line) {
            String[] method = line.split(":| |\\(|\\) -> ");
            if(method.length == 6) {
                StringBuilder descriptor = new StringBuilder();
                descriptor.append('(');
                for(String arg : method[4].split(",")) descriptor.append(NamingUtil.asDescriptor(arg));
                descriptor.append(')');
                descriptor.append(NamingUtil.asDescriptor(method[2]));
                return new ProguardMethodMapping(method[5], method[3], descriptor.toString(),
                        Integer.parseInt(method[0]), Integer.parseInt(method[1]));
            } else if(method.length == 4) {
                StringBuilder descriptor = new StringBuilder();
                descriptor.append('(');
                for(String arg : method[2].split(",")) descriptor.append(NamingUtil.asDescriptor(arg));
                descriptor.append(')');
                descriptor.append(NamingUtil.asDescriptor(method[0]));
                return new ProguardMethodMapping(method[3], method[1], descriptor.toString());
            } else throw new IllegalArgumentException("Is this a Proguard mapping file?");
        }

        @Override
        public ProguardFieldMapping processField(String line) {
            String[] strings = line.split(" -> | ");
            return new ProguardFieldMapping(strings[2], strings[1], NamingUtil.asDescriptor(strings[0]));
        }
    }
}