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

package cn.maxpixel.mcdecompiler.mapping.type;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.reader.MappingProcessor;
import cn.maxpixel.mcdecompiler.writer.MappingGenerator;
import it.unimi.dsi.fastutil.objects.ObjectList;

public interface MappingType<M extends Mapping, C> {
    default boolean isNamespaced() {
        return false;
    }

    default boolean supportPackage() {
        return false;
    }

    MappingProcessor<M, C> getProcessor();

    MappingGenerator<M, C> getGenerator();

    interface Classified<M extends Mapping> extends MappingType<M, ObjectList<ClassMapping<M>>> {
        @Override
        MappingProcessor.Classified<M> getProcessor();

        @Override
        MappingGenerator.Classified<M> getGenerator();
    }

    interface Unique<M extends Mapping> extends MappingType<M, UniqueMapping<M>> {
        @Override
        MappingProcessor.Unique<M> getProcessor();

        @Override
        MappingGenerator.Unique<M> getGenerator();
    }
}