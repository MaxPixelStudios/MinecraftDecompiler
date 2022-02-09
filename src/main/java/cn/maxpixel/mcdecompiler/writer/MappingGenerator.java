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

package cn.maxpixel.mcdecompiler.writer;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

/**
 * A generator which generates mappings to strings.<br>
 * <b>NOTE: You should implement {@link Unique} or {@link Classified} instead of this class unless you are creating a new type of mapping</b>
 * @param <T> Mapping type
 * @param <C> Collection type
 */
public interface MappingGenerator<T extends Mapping, C> {
    MappingType<T, C> getType();

    default boolean supportPackage() {
        return getType().supportPackage();
    }

    default boolean isNamespaced() {
        return getType().isNamespaced();
    }

    ObjectList<String> generate(C mappings);

    default ObjectList<String> generatePackages(ObjectList<T> packages) {
        if(supportPackage()) throw new UnsupportedOperationException("Why this method isn't been overridden?");
        else Logging.getLogger().warning("This type of mapping doesn't support package mappings. Returning empty list");
        return ObjectLists.emptyList();
    }

    // Just in case that maybe some types of mapping need to generate multiple files

    default boolean requireMultiFiles() {
        return false;
    }

    default ObjectList<String>[] generateMulti(C mappings) {
        throw new UnsupportedOperationException();
    }

    default ObjectList<String>[] generatePackagesMulti(ObjectList<T> packages) {
        throw new UnsupportedOperationException();
    }

    interface Unique<T extends Mapping> extends MappingGenerator<T, UniqueMapping<T>> {
    }

    interface Classified<T extends Mapping> extends MappingGenerator<T, ObjectList<ClassMapping<T>>> {
        @Override
        default ObjectList<String> generate(ObjectList<ClassMapping<T>> mappings) {
            return generate(mappings, null);
        }

        ObjectList<String> generate(ObjectList<ClassMapping<T>> mappings, ClassifiedMappingRemapper remapper);

        @Override
        default ObjectList<String>[] generateMulti(ObjectList<ClassMapping<T>> mappings) {
            return generateMulti(mappings, null);
        }

        default ObjectList<String>[] generateMulti(ObjectList<ClassMapping<T>> mappings, ClassifiedMappingRemapper remapper) {
            throw new UnsupportedOperationException();
        }
    }
}