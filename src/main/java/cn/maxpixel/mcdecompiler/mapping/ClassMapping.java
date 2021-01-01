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

package cn.maxpixel.mcdecompiler.mapping;

import cn.maxpixel.mcdecompiler.mapping.base.BaseFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseMethodMapping;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClassMapping extends BaseMapping {
    private final List<BaseMethodMapping> methods;
    private final Map<String, BaseFieldMapping> fields;
    {
        this.methods = new ObjectArrayList<>();
        this.fields = new Object2ObjectOpenHashMap<>();
    }
    public ClassMapping() {}
    public ClassMapping(String unmappedName, String mappedName) {
        super(unmappedName, mappedName);
    }
    public ClassMapping(String targetName) { // CSRG
        super(targetName, targetName);
    }

    public ClassMapping addField(BaseFieldMapping... fields) {
        for (BaseFieldMapping field : fields) {
            this.fields.put(field.getUnmappedName(), field);
        }
        return this;
    }
    public ClassMapping addMethod(BaseMethodMapping... methods) {
        this.methods.addAll(Arrays.asList(methods));
        return this;
    }
    public ClassMapping addField(BaseFieldMapping field) {
        this.fields.put(field.getUnmappedName(), field);
        return this;
    }
    public ClassMapping addMethod(BaseMethodMapping method) {
        this.methods.add(method);
        return this;
    }
    public List<BaseMethodMapping> getMethods() {
        return methods;
    }
    public List<BaseFieldMapping> getFields() {
        return new ObjectArrayList<>(fields.values());
    }
    public Map<String, BaseFieldMapping> getFieldMap() {
        return fields;
    }
    public BaseFieldMapping getField(String unmappedName) {
        return fields.get(unmappedName);
    }

    @Override
    public String toString() {
        return "ClassMapping{" +
                "unmapped name=" + getUnmappedName() +
                ", mapped name=" + getMappedName() +
                ", methods=" + Arrays.toString(methods.toArray()) +
                ", fields=" + Arrays.toString(fields.values().toArray()) +
                '}';
    }
}