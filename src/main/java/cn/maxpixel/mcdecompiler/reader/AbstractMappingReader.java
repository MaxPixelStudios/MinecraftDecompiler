/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.util.IOUtil;
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class AbstractMappingReader<M extends Mapping, R, T extends MappingType<M, R>> {
    protected static final Logger LOGGER = Logging.getLogger("Mapping Reader");
    public final R mappings;
    public final ObjectList<M> packages;

    public AbstractMappingReader(T type, BufferedReader reader) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(reader);
        try(reader) {
            LOGGER.finer("Reading file");
            ObjectArrayList<String> lines = reader.lines().map(s -> {
                if(s.startsWith("#") || s.isBlank()) return null;

                int index = s.indexOf('#');
                if(index > 0) return s.substring(0, index);
                else if(index == 0) return null;

                return s;
            }).filter(Objects::nonNull).collect(ObjectArrayList.toList());
            LOGGER.finest("Read file");
            LOGGER.fine("Processing content");
            Pair<R, ObjectList<M>> result = type.getProcessor().process(lines);
            LOGGER.finest("Processed content");
            mappings = result.left();
            packages = type.supportPackage() ? result.right() : ObjectLists.emptyList();
        } catch(IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public AbstractMappingReader(T type, Reader rd) {
        this(type, IOUtil.asBufferedReader(rd));
    }

    public AbstractMappingReader(T type, InputStream is) {
        this(type, new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    public AbstractMappingReader(T type, String path) throws FileNotFoundException {
        this(type, new FileInputStream(path));
    }

    public AbstractMappingReader(T type, BufferedReader... readers) {
        LOGGER.finer("Reading files");
        @SuppressWarnings("unchecked")
        ObjectArrayList<String>[] contents = Utils.mapArray(readers, ObjectArrayList[]::new, reader -> {
            try(reader) {
                return reader.lines().map(s -> {
                    if(s.startsWith("#") || s.isBlank()) return null;

                    int index = s.indexOf('#');
                    if(index > 0) return s.substring(0, index);
                    else if(index == 0) return null;

                    return s;
                }).filter(Objects::nonNull).collect(ObjectArrayList.toList());
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
        });
        LOGGER.finest("Read files");
        LOGGER.fine("Processing contents");
        Pair<R, ObjectList<M>> result = type.getProcessor().process(contents);
        LOGGER.finest("Processed contents");
        mappings = result.left();
        packages = type.supportPackage() ? result.right() : ObjectLists.emptyList();
    }

    public AbstractMappingReader(T type, Reader... rd) {
        this(type, Utils.mapArray(rd, BufferedReader[]::new, IOUtil::asBufferedReader));
    }

    public AbstractMappingReader(T type, InputStream... is) {
        this(type, Utils.mapArray(is, InputStreamReader[]::new, i -> new InputStreamReader(i, StandardCharsets.UTF_8)));
    }

    public AbstractMappingReader(T type, String... path) throws FileNotFoundException {
        this(type, Utils.mapArray(path, FileInputStream[]::new, FileInputStream::new));
    }
}