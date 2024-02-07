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

package cn.maxpixel.mcdecompiler.util;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.mapping.type.MappingTypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Utils {
    public static RuntimeException wrapInRuntime(Throwable e) {
        return new RuntimeException(e);
    }

    public static <I, O, E extends Throwable> O[] mapArray(I[] input, IntFunction<O[]> outputGenerator,
                                                           LambdaUtil.Function_WithThrowable<I, O, E> func) throws E {
        Objects.requireNonNull(input);
        Objects.requireNonNull(outputGenerator);
        Objects.requireNonNull(func);
        O[] output = outputGenerator.apply(input.length);
        for(int i = 0; i < input.length; i++) {
            output[i] = Objects.requireNonNull(func.apply(Objects.requireNonNull(input[i])));
        }
        return output;
    }

    public static void waitForProcess(Process pro) {
        Logger logger = Logging.getLogger("Process PID: " + pro.pid());
        try(BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(pro.getErrorStream()))) {
            Thread inT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = in.readLine()) != null) logger.fine(ins);
                } catch (Throwable e) {
                    logger.throwing("Utils", "waitForProcess", e);
                }
            });
            Thread errT = new Thread(() -> {
                try {
                    String ins;
                    while ((ins = err.readLine()) != null) logger.warning(ins);
                } catch (Throwable e) {
                    logger.throwing("Utils", "waitForProcess", e);
                }
            });
            inT.setDaemon(true);
            errT.setDaemon(true);
            inT.start();
            errT.start();
            pro.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.throwing("Utils", "waitForProcess", e);
        }
    }

    public static <T> T onKeyDuplicate(T t, T u) {
        throw new IllegalArgumentException("Key duplicated for \"" + t + "\" and \"" + u + "\"");
    }

    public static MappingType.Classified<? extends Mapping> tryIdentifyingMappingType(String mappingPath) {
        try(Stream<String> lines = Files.lines(Path.of(mappingPath), StandardCharsets.UTF_8).filter(s -> !s.startsWith("#"))) {
            return tryIdentifyingMappingType(lines);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static MappingType.Classified<? extends Mapping> tryIdentifyingMappingType(BufferedReader reader) {
        try {
            reader.mark(512);
            MappingType.Classified<? extends Mapping> result = tryIdentifyingMappingType(reader.lines().filter(s -> !s.startsWith("#")));
            reader.reset();
            return result;
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static MappingType.Classified<? extends Mapping> tryIdentifyingMappingType(Stream<String> lines) {
        List<String> list = lines.limit(2).toList();
        String s = list.get(0);
        if(s.startsWith("PK: ") || s.startsWith("CL: ") || s.startsWith("FD: ") || s.startsWith("MD: ")) return MappingTypes.SRG;
        else if(s.startsWith("v1")) return MappingTypes.TINY_V1;
        else if(s.startsWith("tiny\t2\t0")) return MappingTypes.TINY_V2;
        else if(s.startsWith("tsrg2")) return MappingTypes.TSRG_V2;
        s = list.get(1);
        if(s.startsWith("    ")) return MappingTypes.PROGUARD;
        else if(s.startsWith("\t")) return MappingTypes.TSRG_V1;
        else return MappingTypes.CSRG;
    }

    public static StringBuilder createHashString(MessageDigest md) {
        StringBuilder out = new StringBuilder();
        for(byte b : md.digest()) {
            String hex = Integer.toHexString(Byte.toUnsignedInt(b));
            if(hex.length() < 2) out.append('0');
            out.append(hex);
        }
        return out;
    }
}