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

package cn.maxpixel.mcdecompiler.writer;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class ClassifiedMappingWriter<M extends Mapping> extends AbstractMappingWriter<M, ObjectList<ClassMapping<M>>, MappingType.Classified<M>> {
    private final ObjectArrayList<ClassMapping<M>> mappings = new ObjectArrayList<>();

    public ClassifiedMappingWriter(MappingType.Classified<M> type) {
        super(type);
    }

    public void addMapping(@NotNull ClassMapping<M> mapping) {
        this.mappings.add(mapping);
    }

    public void addMappings(@NotNull Collection<ClassMapping<M>> mappings) {
        this.mappings.addAll(mappings);
    }

    @Override
    public void addMappings(@NotNull ObjectList<ClassMapping<M>> mappings) {
        this.mappings.addAll(mappings);
    }

    @Override
    protected @NotNull ObjectList<ClassMapping<M>> getCollection() {
        return mappings;
    }

    @Override
    protected void clearCollection() {
        mappings.clear();
    }
}