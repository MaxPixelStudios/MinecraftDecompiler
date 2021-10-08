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
import cn.maxpixel.mcdecompiler.mapping1.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping1.collection.UniqueMapping;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

/**
 * A processor which processes files to mappings.<br>
 * <b>NOTE: You should implements {@link Unique} or {@link Classified} instead of this class</b>
 * @param <T> Mapping type
 * @param <R> Return value
 */
public interface MappingProcessor<T extends Mapping, R> {
    default boolean supportPackage() {
        return false;
    }

    Pair<R, ObjectList<T>> process(ObjectList<String> content);

    Pair<R, ObjectList<T>> process(ObjectList<String>... contents);

    interface Unique<T extends Mapping> extends MappingProcessor<T, UniqueMapping<T>> {
        @Override
        Pair<UniqueMapping<T>, ObjectList<T>> process(ObjectList<String> content);

        @Override
        default Pair<UniqueMapping<T>, ObjectList<T>> process(ObjectList<String>... contents) {
            ObjectObjectImmutablePair<UniqueMapping<T>, ObjectList<T>> pair = new ObjectObjectImmutablePair<>(new UniqueMapping<>(),
                    supportPackage() ? new ObjectArrayList<>() : ObjectLists.emptyList());
            for(ObjectList<String> content : contents) {
                Pair<UniqueMapping<T>, ObjectList<T>> result = process(content);
                pair.left().classes.addAll(result.left().classes);
                pair.left().fields.addAll(result.left().fields);
                pair.left().methods.addAll(result.left().methods);
                if(supportPackage()) pair.right().addAll(result.right());
            }
            return pair;
        }
    }

    interface Classified<T extends Mapping> extends MappingProcessor<T, ObjectList<ClassMapping<T>>> {
        @Override
        Pair<ObjectList<ClassMapping<T>>, ObjectList<T>> process(ObjectList<String> content);

        @Override
        default Pair<ObjectList<ClassMapping<T>>, ObjectList<T>> process(ObjectList<String>... contents) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<T>>, ObjectList<T>> pair = new ObjectObjectImmutablePair<>(
                    new ObjectArrayList<>(), supportPackage() ? new ObjectArrayList<>() : ObjectLists.emptyList());
            for(ObjectList<String> content : contents) {
                Pair<ObjectList<ClassMapping<T>>, ObjectList<T>> result = process(content);
                pair.left().addAll(result.left());
                if(supportPackage()) pair.right().addAll(result.right());
            }
            return pair;
        }
    }
}