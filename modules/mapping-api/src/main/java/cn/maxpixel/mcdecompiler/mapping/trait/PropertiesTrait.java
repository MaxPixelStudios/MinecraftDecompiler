/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2025 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.mapping.trait;

import cn.maxpixel.mcdecompiler.mapping.component.Component;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PropertiesTrait implements MappingTrait, Component {
    public final ObjectOpenHashSet<String> properties = new ObjectOpenHashSet<>();
    public final Object2ObjectOpenHashMap<String, String> propertiesWithValue = new Object2ObjectOpenHashMap<>();

    public void addProperty(String key) {
        properties.add(key);
    }

    public void setProperty(String key, String value) {
        propertiesWithValue.put(key, value);
    }

    public Object2ObjectOpenHashMap<String, String> getPropertiesWithValue() {
        return propertiesWithValue;
    }

    public ObjectOpenHashSet<String> getProperties() {
        return properties;
    }
}