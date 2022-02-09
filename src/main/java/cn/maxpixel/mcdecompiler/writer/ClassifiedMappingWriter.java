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

import cn.maxpixel.mcdecompiler.mapping1.Mapping;
import cn.maxpixel.mcdecompiler.mapping1.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping1.type.MappingType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Collection;
import java.util.Objects;

public final class ClassifiedMappingWriter<M extends Mapping> extends AbstractMappingWriter<M, ObjectList<ClassMapping<M>>, MappingType.Classified<M>> {
    private final ObjectArrayList<ClassMapping<M>> mappings = new ObjectArrayList<>();

    public ClassifiedMappingWriter(MappingType.Classified<M> type) {
        super(type);
    }

    public void addMapping(ClassMapping<M> mapping) {
        this.mappings.add(Objects.requireNonNull(mapping));
    }

    public void addMappings(Collection<ClassMapping<M>> mappings) {
        this.mappings.addAll(Objects.requireNonNull(mappings));
    }

    @Override
    public void addMappings(ObjectList<ClassMapping<M>> mappings) {
        this.mappings.addAll(Objects.requireNonNull(mappings));
    }

    @Override
    protected ObjectList<ClassMapping<M>> getCollection() {
        return mappings;
    }

    @Override
    protected void clearCollection() {
        mappings.clear();
    }
}