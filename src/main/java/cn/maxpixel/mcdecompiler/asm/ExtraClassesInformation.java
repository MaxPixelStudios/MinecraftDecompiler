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

package cn.maxpixel.mcdecompiler.asm;

import cn.maxpixel.mcdecompiler.util.IOUtil;
import it.unimi.dsi.fastutil.objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ExtraClassesInformation implements Consumer<Path> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Object2ObjectOpenHashMap<String, ObjectArrayList<String>> superClassMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, Object2IntMap<String>> accessMap = new Object2ObjectOpenHashMap<>();
    public final Object2ObjectMap<String, ObjectArrayList<String>> SUPER_NAMES = Object2ObjectMaps.unmodifiable(superClassMap);
    public final Object2ObjectMap<String, Object2IntMap<String>> ACCESS_MAP = Object2ObjectMaps.unmodifiable(accessMap);

    public ExtraClassesInformation() {}

    public ExtraClassesInformation(Stream<Path> classes) {
        this(classes, false);
    }

    public ExtraClassesInformation(Stream<Path> classes, boolean close) {
        if(close) try(classes) {
            classes.forEach(this);
        } else classes.forEach(this);
    }

    @Override
    public void accept(Path classFilePath) {
        try {
            new ClassReader(IOUtil.readAllBytes(classFilePath)).accept(new ClassVisitor(Opcodes.ASM9) {
                private final Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>();
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    map.put(name, access);
                    ObjectArrayList<String> list = new ObjectArrayList<>();
                    if((access & Opcodes.ACC_INTERFACE) == 0 && !superName.equals("java/lang/Object")) list.add(superName);
                    list.addElements(list.size(), interfaces);
                    if(!list.isEmpty()) synchronized(superClassMap) {
                        superClassMap.put(name, list);
                    }
                    synchronized(accessMap) {
                        accessMap.put(name, Object2IntMaps.unmodifiable(map));
                    }
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    if((access & Opcodes.ACC_PUBLIC) == 0) map.put(name, access);
                    return null;
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if((access & Opcodes.ACC_PUBLIC) == 0) map.put(name.concat(descriptor), access);
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch(IOException e) {
            LOGGER.error("Error when creating super class mapping", e);
        }
    }
}