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

import cn.maxpixel.mcdecompiler.mapping1.Mapping;
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
import java.util.stream.Collectors;

public abstract class AbstractMappingReader<M extends Mapping, R, P extends MappingProcessor<M, R>> {
    protected static final Logger LOGGER = Logging.getLogger("Mapping Reader");
    public final R mappings;
    public final ObjectList<M> packages;

    public AbstractMappingReader(P processor, BufferedReader reader) {
        Objects.requireNonNull(processor);
        Objects.requireNonNull(reader);
        try(reader) {
            LOGGER.finer("Reading file");
            ObjectArrayList<String> lines = reader.lines().map(s -> {
                if(s.startsWith("#") || s.isEmpty() || s.isBlank()) return null;

                int index = s.indexOf('#');
                if(index > 0) return s.substring(0, index);
                else if(index == 0) return null;

                return s;
            }).filter(Objects::nonNull).collect(Collectors.toCollection(ObjectArrayList::new));
            LOGGER.finest("Read file");
            LOGGER.fine("Processing content");
            Pair<R, ObjectList<M>> result = processor.process(lines);
            LOGGER.finest("Processed content");
            mappings = result.left();
            packages = processor.supportPackage() ? result.right() : ObjectLists.emptyList();
        } catch(IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public AbstractMappingReader(P processor, Reader rd) {
        this(processor, IOUtil.asBufferedReader(rd));
    }

    public AbstractMappingReader(P processor, InputStream is) {
        this(processor, new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    public AbstractMappingReader(P processor, String path) throws FileNotFoundException {
        this(processor, new FileInputStream(path));
    }

    public AbstractMappingReader(P processor, BufferedReader... readers) {
        LOGGER.finer("Reading files");
        ObjectArrayList<String>[] contents = Utils.mapArray(readers, new ObjectArrayList[readers.length], reader -> {
            try(reader) {
                return reader.lines().map(s -> {
                    if(s.startsWith("#") || s.isEmpty() || s.replaceAll("\\s+", "").isEmpty()) return null;

                    int index = s.indexOf('#');
                    if(index > 0) return s.substring(0, index);
                    else if(index == 0) return null;

                    return s;
                }).filter(Objects::nonNull).collect(Collectors.toCollection(ObjectArrayList::new));
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
        });
        LOGGER.finest("Read files");
        LOGGER.fine("Processing contents");
        Pair<R, ObjectList<M>> result = processor.process(contents);
        LOGGER.finest("Processed contents");
        mappings = result.left();
        packages = processor.supportPackage() ? result.right() : ObjectLists.emptyList();
    }

    public AbstractMappingReader(P processor, Reader... rd) {
        this(processor, Utils.mapArray(rd, new BufferedReader[rd.length], IOUtil::asBufferedReader));
    }

    public AbstractMappingReader(P processor, InputStream... is) {
        this(processor, Utils.mapArray(is, new InputStreamReader[is.length], i -> new InputStreamReader(i, StandardCharsets.UTF_8)));
    }

    public AbstractMappingReader(P processor, String... path) throws FileNotFoundException {
        this(processor, Utils.mapArray(path, new FileInputStream[path.length], FileInputStream::new));
    }
}