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

package cn.maxpixel.mcdecompiler.mapping.paired;

import cn.maxpixel.mcdecompiler.asm.remapper.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.AbstractClassMapping;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Collection;
import java.util.Objects;

public class PairedClassMapping extends PairedMapping implements AbstractClassMapping {
    private final ObjectArrayList<PairedMethodMapping> methods = new ObjectArrayList<>();
    private final Object2ObjectOpenHashMap<String, PairedFieldMapping> fields = new Object2ObjectOpenHashMap<>();

    public PairedClassMapping(String unmappedName, String mappedName) {
        super(unmappedName, mappedName);
    }

    public PairedClassMapping(String targetName) { // CSRG
        super(targetName, targetName);
    }

    public PairedClassMapping addField(PairedFieldMapping... fields) {
        for(PairedFieldMapping field : fields) addField(field);
        return this;
    }

    public PairedClassMapping addField(Collection<? extends PairedFieldMapping> fields) {
        fields.forEach(this::addField);
        return this;
    }

    public PairedClassMapping addMethod(PairedMethodMapping... methods) {
        for(PairedMethodMapping method : methods) addMethod(method);
        return this;
    }

    public PairedClassMapping addMethod(Collection<? extends PairedMethodMapping> methods) {
        methods.forEach(this::addMethod);
        return this;
    }

    public PairedClassMapping addField(PairedFieldMapping field) {
        fields.put(field.setOwner(this).getUnmappedName(), field);
        return this;
    }

    public PairedClassMapping addMethod(PairedMethodMapping method) {
        methods.add(method.setOwner(this));
        return this;
    }

    public ObjectList<? extends PairedMethodMapping> getMethods() {
        return methods;
    }

    public ObjectList<? extends PairedFieldMapping> getFields() {
        return new ObjectArrayList<>(fields.values());
    }

    public Object2ObjectMap<String, ? extends PairedFieldMapping> getFieldMap() {
        return fields;
    }

    public PairedFieldMapping getField(String unmappedName) {
        return fields.get(unmappedName);
    }

    @Override
    @Deprecated
    public void reverse() {
        throw new UnsupportedOperationException("Use reverse(MappingRemapper) instead");
    }

    public void reverse(MappingRemapper remapper) {
        super.reverse();
        methods.forEach(m -> m.reverse(remapper));
        fields.values().forEach(f -> f.reverse(remapper));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PairedClassMapping)) return false;
        if (!super.equals(o)) return false;
        PairedClassMapping that = (PairedClassMapping) o;
        return methods.equals(that.methods) && fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(methods, fields);
    }

    @Override
    public String toString() {
        return "PairedClassMapping{" +
                "methods=" + methods +
                ", fields=" + fields +
                "} " + super.toString();
    }
}