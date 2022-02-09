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

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Collection;
import java.util.Objects;

public class UniqueMappingWriter<M extends Mapping> extends AbstractMappingWriter<M, UniqueMapping<M>, MappingType.Unique<M>> {
    private final UniqueMapping<M> mapping = new UniqueMapping<>();

    public UniqueMappingWriter(MappingType.Unique<M> type) {
        super(type);
    }

    public void addClass(M mapping) {
        this.mapping.classes.add(Objects.requireNonNull(mapping));
    }

    public void addClasses(Collection<M> mappings) {
        this.mapping.classes.addAll(Objects.requireNonNull(mappings));
    }

    public void addClasses(ObjectList<M> mappings) {
        this.mapping.classes.addAll(Objects.requireNonNull(mappings));
    }

    public void addField(M mapping) {
        this.mapping.fields.add(Objects.requireNonNull(mapping));
    }

    public void addFields(Collection<M> mappings) {
        this.mapping.fields.addAll(Objects.requireNonNull(mappings));
    }

    public void addFields(ObjectList<M> mappings) {
        this.mapping.fields.addAll(Objects.requireNonNull(mappings));
    }

    public void addMethod(M mapping) {
        this.mapping.methods.add(Objects.requireNonNull(mapping));
    }

    public void addMethods(Collection<M> mappings) {
        this.mapping.methods.addAll(Objects.requireNonNull(mappings));
    }

    public void addMethods(ObjectList<M> mappings) {
        this.mapping.methods.addAll(Objects.requireNonNull(mappings));
    }

    @Override
    public void addMappings(UniqueMapping<M> mappings) {
        this.mapping.classes.addAll(mappings.classes);
        this.mapping.fields.addAll(mappings.fields);
        this.mapping.methods.addAll(mappings.methods);
    }

    @Override
    protected UniqueMapping<M> getCollection() {
        return mapping;
    }

    @Override
    protected void clearCollection() {
        mapping.classes.clear();
        mapping.fields.clear();
        mapping.methods.clear();
    }
}