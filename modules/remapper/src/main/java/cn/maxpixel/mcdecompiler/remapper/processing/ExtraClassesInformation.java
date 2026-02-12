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

package cn.maxpixel.mcdecompiler.remapper.processing;

import cn.maxpixel.mcdecompiler.api.util.IOUtil;
import cn.maxpixel.mcdecompiler.remapper.Deobfuscator;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.*;
import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ExtraClassesInformation implements Consumer<Path> {// TODO: extensions
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BiFunction<ExtraClassesInformation, ClassReader, ClassVisitor> NULL_FUNC = (e, c) -> null;
    public final Object2ObjectOpenHashMap<String, ObjectArrayList<String>> superClassMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, Object2IntOpenHashMap<String>> accessMap = new Object2ObjectOpenHashMap<>();
    private final BiFunction<ExtraClassesInformation, ClassReader, ClassVisitor> extraVisitorFunc;

    public ExtraClassesInformation() {
        this(NULL_FUNC);
    }

    public ExtraClassesInformation(Stream<Path> classes) {
        this(classes, false);
    }

    public ExtraClassesInformation(Stream<Path> classes, boolean close) {
        this(classes, close, NULL_FUNC);
    }

    public ExtraClassesInformation(BiFunction<ExtraClassesInformation, ClassReader, ClassVisitor> extraVisitorFunc) {
        this.extraVisitorFunc = extraVisitorFunc;
    }

    public ExtraClassesInformation(Stream<Path> classes, BiFunction<ExtraClassesInformation, ClassReader, ClassVisitor> extraVisitorFunc) {
        this(classes, false, extraVisitorFunc);
    }

    public ExtraClassesInformation(Stream<Path> classes, boolean close, BiFunction<ExtraClassesInformation, ClassReader, ClassVisitor> extraVisitorFunc) {
        this.extraVisitorFunc = Objects.requireNonNull(extraVisitorFunc);
        if (close) try(classes) {
            classes.forEach(this);
        } else classes.forEach(this);
    }

    @Override
    public void accept(Path classFilePath) {
        try {
            ClassReader reader = new ClassReader(IOUtil.readAllBytes(classFilePath));
            reader.accept(new ClassVisitor(Deobfuscator.ASM_VERSION, extraVisitorFunc.apply(this, reader)) {
                private String className;
                private boolean recordAccess;
                private Object2IntOpenHashMap<String> map;

                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    className = name;
                    boolean needToRecord = (access & (Opcodes.ACC_INTERFACE | Opcodes.ACC_RECORD)) == 0;
                    boolean notEnum = (access & Opcodes.ACC_ENUM) == 0;
                    recordAccess = needToRecord && notEnum;
                    if (recordAccess) map = new Object2IntOpenHashMap<>();
                    int itfLen = interfaces.length;
                    if (needToRecord && !superName.startsWith("java/")) {
                        ObjectArrayList<String> list = new ObjectArrayList<>(itfLen + 1);
                        list.add(superName);
                        if (itfLen > 0) for(String itf : interfaces) {
                            if (itf.startsWith("java/")) continue;
                            list.add(itf);
                        }
                        synchronized (superClassMap) {
                            superClassMap.put(className, list);
                        }
                    } else if (itfLen > 0) {
                        ObjectArrayList<String> list = new ObjectArrayList<>(itfLen);
                        for (String itf : interfaces) {
                            if (itf.startsWith("java/")) continue;
                            list.add(itf);
                        }
                        synchronized (superClassMap) {
                            superClassMap.put(className, list);
                        }
                    }
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    if (recordAccess && (access & Opcodes.ACC_PUBLIC) == 0) map.put(name, access);
                    return super.visitField(access, name, descriptor, signature, value);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if (recordAccess && (access & Opcodes.ACC_PUBLIC) == 0) map.put(name.concat(descriptor), access);
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }

                @Override
                public void visitEnd() {
                    if (recordAccess && !map.isEmpty()) {
                        map.defaultReturnValue(Opcodes.ACC_PUBLIC);
                        synchronized (accessMap) {
                            accessMap.put(className, map);
                        }
                    }
                    super.visitEnd();
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException e) {
            LOGGER.warn("Error when collecting extra classes information", e);
        }
    }

    public ObjectList<String> getSuperNames(String name) {
        return superClassMap.get(name);
    }

    public int getAccessFlags(String className, String combinedMemberName) {
        Object2IntMap<String> map = accessMap.get(className);
        if (map == null) return Opcodes.ACC_PUBLIC;
        return map.getInt(combinedMemberName);
    }
}