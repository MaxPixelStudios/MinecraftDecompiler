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

package cn.maxpixel.mcdecompiler.util;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.mapping.type.MappingTypes;
import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
        throw new IllegalArgumentException("Key \"" + t + "\" and \"" + u + "\" duplicated!");
    }

    // https://github.com/LXGaming/ClassLoaderUtils/blob/master/src/main/java/io/github/lxgaming/classloader/ClassLoaderUtils.java
    @SuppressWarnings("unchecked")
    public static void appendToClassPath(ClassLoader classLoader, List<URL> urlList) throws ReflectiveOperationException {
        Class<?> classLoaderClass = Class.forName("jdk.internal.loader.BuiltinClassLoader");
        Class<?> classPathClass = Class.forName("jdk.internal.loader.URLClassPath");
        if (classLoaderClass.isInstance(classLoader)) {
            Unsafe unsafe = getUnsafe();

            // jdk.internal.loader.BuiltinClassLoader.ucp
            Field ucpField = classLoaderClass.getDeclaredField("ucp");
            long ucpFieldOffset = unsafe.objectFieldOffset(ucpField);
            Object ucpObject = unsafe.getObject(classLoader, ucpFieldOffset);

            // jdk.internal.loader.URLClassPath.path
            Field pathField = classPathClass.getDeclaredField("path");
            long pathFieldOffset = unsafe.objectFieldOffset(pathField);
            ArrayList<URL> path = (ArrayList<URL>) unsafe.getObject(ucpObject, pathFieldOffset);

            // Java 11 - jdk.internal.loader.URLClassPath.unopenedUrls
            Field urlsField = classPathClass.getDeclaredField("unopenedUrls");
            long urlsFieldOffset = unsafe.objectFieldOffset(urlsField);
            Collection<URL> urls = (Collection<URL>) unsafe.getObject(ucpObject, urlsFieldOffset);

            // noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (urls) {
                if (!path.containsAll(urlList)) {
                    urls.addAll(urlList);
                    path.addAll(urlList);
                }
            }

        }
    }

    public static URL[] getClassPath() throws ReflectiveOperationException {
        ClassLoader cl = Utils.class.getClassLoader();
        Class<?> classLoaderClass = Class.forName("jdk.internal.loader.BuiltinClassLoader");
        Unsafe unsafe = getUnsafe();

        // jdk.internal.loader.BuiltinClassLoader.ucp
        Field ucpField = classLoaderClass.getDeclaredField("ucp");
        long ucpFieldOffset = unsafe.objectFieldOffset(ucpField);
        Object ucpObject = unsafe.getObject(cl, ucpFieldOffset);

        // jdk.internal.loader.URLClassPath.path
        Field pathField = ucpField.getType().getDeclaredField("path");
        long pathFieldOffset = unsafe.objectFieldOffset(pathField);
        ArrayList<URL> path = (ArrayList<URL>) unsafe.getObject(ucpObject, pathFieldOffset);
        return path.toArray(new URL[0]);
    }

    private static Unsafe getUnsafe() throws ReflectiveOperationException {
        Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        return (Unsafe) theUnsafeField.get(null);
    }

    public static MappingType<? extends Mapping, ?> tryIdentifyingMappingType(String mappingPath) {
        try(Stream<String> lines = Files.lines(Path.of(mappingPath), StandardCharsets.UTF_8).filter(s -> !s.startsWith("#"))) {
            return tryIdentifyingMappingType(lines);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static MappingType<? extends Mapping, ?> tryIdentifyingMappingType(BufferedReader reader) {
        try {
            reader.mark(512);
            MappingType<? extends Mapping, ?> result = tryIdentifyingMappingType(reader.lines().filter(s -> !s.startsWith("#")));
            reader.reset();
            return result;
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static MappingType<? extends Mapping, ?> tryIdentifyingMappingType(Stream<String> lines) {
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
}