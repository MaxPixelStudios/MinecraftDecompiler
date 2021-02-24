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
import cn.maxpixel.mcdecompiler.mapping.base.BaseMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.tiny.Namespaced;
import cn.maxpixel.mcdecompiler.mapping.tiny.TinyFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.tiny.TinyMethodMapping;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TinyClassMapping extends ClassMapping implements cn.maxpixel.mcdecompiler.mapping.components.Namespaced {
    private final ObjectArrayList<TinyMethodMapping> methods = new ObjectArrayList<>();
    private final Object2ObjectOpenHashMap<String, TinyFieldMapping> fields = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, String> names = new Object2ObjectOpenHashMap<>();
    public TinyClassMapping(Namespaced... names) {
        for(Namespaced namespaced : names) this.names.put(namespaced.getNamespace(), namespaced.getName());
    }
    public TinyClassMapping(Namespaced name1) { // Methods
        this.names.put(name1.getNamespace(), name1.getName());
    }

    @Override
    public ClassMapping addField(BaseFieldMapping... fields) {
        for (BaseFieldMapping field : fields) {
            this.fields.put(field.getUnmappedName(), (TinyFieldMapping) field);
        }
        return this;
    }
    @Override
    public ClassMapping addMethod(BaseMethodMapping... methods) {
        this.methods.addAll(Arrays.asList((TinyMethodMapping[]) methods));
        return this;
    }
    @Override
    public ClassMapping addField(BaseFieldMapping field) {
        this.fields.put(field.getUnmappedName(), (TinyFieldMapping) field);
        return this;
    }
    @Override
    public ClassMapping addMethod(BaseMethodMapping method) {
        this.methods.add((TinyMethodMapping) method);
        return this;
    }
    @Override
    public List<TinyMethodMapping> getMethods() {
        return methods;
    }
    @Override
    public List<TinyFieldMapping> getFields() {
        return new ObjectArrayList<>(fields.values());
    }
    @Override
    public Map<String, TinyFieldMapping> getFieldMap() {
        return fields;
    }
    @Override
    public TinyFieldMapping getField(String unmappedName) {
        return fields.get(unmappedName);
    }

    @Override
    public String getName(String namespace) {
        return names.get(namespace);
    }

    @Override
    public void setName(Namespaced name) {
        names.put(name.getNamespace(), name.getName());
    }

    @Override
    public String getUnmappedName() {
        return getName(Namespaced.OFFICIAL);
    }

    @Override
    public String getMappedName() {
        String s = getName(Namespaced.YARN);
        return s == null ? getName(Namespaced.INTERMEDIARY) : s;
    }

    @Override
    public void setUnmappedName(String unmappedName) {
        setName(new Namespaced(Namespaced.OFFICIAL, unmappedName));
    }

    @Override
    public void setMappedName(String mappedName) {
        String s = getName(Namespaced.YARN);
        if(s == null) setName(new Namespaced(Namespaced.INTERMEDIARY, mappedName));
        else setName(new Namespaced(Namespaced.YARN, mappedName));
    }
}