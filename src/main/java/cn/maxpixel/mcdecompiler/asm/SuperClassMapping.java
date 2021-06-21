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

import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SuperClassMapping implements Consumer<Path> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Utils.Function_WithThrowable<Path, byte[], IOException> readFunc;
    private final Object2ObjectOpenHashMap<String, ObjectArrayList<String>> superClassMap = new Object2ObjectOpenHashMap<>();
    public final Object2ObjectMap<String, ObjectArrayList<String>> MAP = Object2ObjectMaps.unmodifiable(superClassMap);

    public SuperClassMapping() {
        this.readFunc = Files::readAllBytes;
    }

    public SuperClassMapping(Stream<Path> classes) {
        this(classes, false);
    }

    public SuperClassMapping(Stream<Path> classes, boolean close) {
        this(classes, close, Files::readAllBytes);
    }

    public SuperClassMapping(Stream<Path> classes, boolean close, Utils.Function_WithThrowable<Path, byte[], IOException> readFunc) {
        this.readFunc = readFunc;
        if(close) try(classes) {
            classes.forEach(this);
        } else classes.forEach(this);
    }

    @Override
    public void accept(Path classFilePath) {
        try {
            ClassReader reader = new ClassReader(readFunc.apply(classFilePath));
            ObjectArrayList<String> list = new ObjectArrayList<>();
            String superName = reader.getSuperName();
            if(!superName.equals("java/lang/Object")) list.add(superName);
            list.addElements(list.size(), reader.getInterfaces());
            if(!list.isEmpty()) synchronized(superClassMap) {
                superClassMap.put(reader.getClassName(), list);
            }
        } catch(IOException e) {
            LOGGER.error("Error when creating super class mapping", e);
        }
    }
}