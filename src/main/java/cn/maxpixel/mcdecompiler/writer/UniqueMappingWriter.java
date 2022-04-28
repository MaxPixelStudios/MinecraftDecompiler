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
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class UniqueMappingWriter<M extends Mapping> extends AbstractMappingWriter<M, UniqueMapping<M>, MappingType.Unique<M>> {
    private final UniqueMapping<M> mapping = new UniqueMapping<>();

    public UniqueMappingWriter(MappingType.Unique<M> type) {
        super(type);
    }

    public void addClass(@NotNull M mapping) {
        this.mapping.classes.add(mapping);
    }

    public void addClasses(@NotNull Collection<M> mappings) {
        this.mapping.classes.addAll(mappings);
    }

    public void addClasses(@NotNull ObjectList<M> mappings) {
        this.mapping.classes.addAll(mappings);
    }

    public void addField(@NotNull M mapping) {
        this.mapping.fields.add(mapping);
    }

    public void addFields(@NotNull Collection<M> mappings) {
        this.mapping.fields.addAll(mappings);
    }

    public void addFields(@NotNull ObjectList<M> mappings) {
        this.mapping.fields.addAll(mappings);
    }

    public void addMethod(@NotNull M mapping) {
        this.mapping.methods.add(mapping);
    }

    public void addMethods(@NotNull Collection<M> mappings) {
        this.mapping.methods.addAll(mappings);
    }

    public void addMethods(@NotNull ObjectList<M> mappings) {
        this.mapping.methods.addAll(mappings);
    }

    @Override
    public void addMappings(@NotNull UniqueMapping<M> mappings) {
        this.mapping.classes.addAll(mappings.classes);
        this.mapping.fields.addAll(mappings.fields);
        this.mapping.methods.addAll(mappings.methods);
    }

    @Override
    protected @NotNull UniqueMapping<M> getCollection() {
        return mapping;
    }

    @Override
    protected void clearCollection() {
        mapping.classes.clear();
        mapping.fields.clear();
        mapping.methods.clear();
    }
}