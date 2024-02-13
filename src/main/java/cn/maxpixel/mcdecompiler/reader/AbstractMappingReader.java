/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.util.IOUtil;
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * The parent class of {@code MappingReader}s
 * @param <M> Mapping form(paired, namespaced, etc.)
 * @param <R> Read result
 * @param <T> Mapping type
 */
public abstract class AbstractMappingReader<M extends Mapping, R extends MappingCollection<M>, T extends MappingType<M, R>> {
    protected static final Logger LOGGER = Logging.getLogger();
    public final T type;

    public AbstractMappingReader(T type) {
        this.type = Objects.requireNonNull(type);
    }

    public final @NotNull R read(@NotNull BufferedReader reader) {
        Objects.requireNonNull(reader);
        try (reader) {
            LOGGER.finer("Reading file");
            ObjectArrayList<String> lines = reader.lines().map(s -> {
                if (s.isBlank()) return null;

                char comment = this.type.getCommentChar();
                if (comment != '\0') {
                    if (s.charAt(0) == comment) return null;
                    int index = s.indexOf(comment);
                    if (index > 0) return s.substring(0, index);
                    else if (index == 0) return null;
                }

                return s;
            }).filter(Objects::nonNull).collect(ObjectArrayList.toList());
            LOGGER.finest("Read file");
            LOGGER.fine("Processing content");
            return type.getProcessor().process(lines);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        } finally {
            LOGGER.finest("Processed content");
        }
    }

    public final @NotNull R read(@NotNull Reader reader) {
        return read(IOUtil.asBufferedReader(reader));
    }

    public final @NotNull R read(@NotNull InputStream is) {
        return read(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    public final @NotNull R read(@NotNull String path) throws FileNotFoundException {
        return read(new FileInputStream(path));
    }

    public final @NotNull R read(@NotNull BufferedReader @NotNull ... readers) {
        LOGGER.finer("Reading files");
        @SuppressWarnings("unchecked")
        ObjectArrayList<String>[] contents = Utils.mapArray(readers, ObjectArrayList[]::new, reader -> {
            try (reader) {
                return reader.lines().map(s -> {
                    if (s.isBlank()) return null;

                    char comment = this.type.getCommentChar();
                    if (comment != '\0') {
                        if (s.charAt(0) == comment) return null;
                        int index = s.indexOf(comment);
                        if (index > 0) return s.substring(0, index);
                        else if (index == 0) return null;
                    }

                    return s;
                }).filter(Objects::nonNull).collect(ObjectArrayList.toList());
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
        });
        LOGGER.finest("Read files");
        LOGGER.fine("Processing contents");
        try {
            return type.getProcessor().process(contents);
        } finally {
            LOGGER.finest("Processed contents");
        }
    }

    public final @NotNull R read(@NotNull Reader @NotNull ... reader) {
        return read(Utils.mapArray(reader, BufferedReader[]::new, IOUtil::asBufferedReader));
    }

    public final @NotNull R read(@NotNull InputStream @NotNull ... is) {
        return read(Utils.mapArray(is, InputStreamReader[]::new, i -> new InputStreamReader(i, StandardCharsets.UTF_8)));
    }

    public final @NotNull R read(@NotNull String @NotNull ... path) throws FileNotFoundException {
        return read(Utils.mapArray(path, FileInputStream[]::new, FileInputStream::new));
    }
}