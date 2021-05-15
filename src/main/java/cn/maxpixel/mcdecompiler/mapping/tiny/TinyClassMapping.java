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

package cn.maxpixel.mcdecompiler.mapping.tiny;

import cn.maxpixel.mcdecompiler.mapping.components.Documented;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;

import java.util.Map;

public class TinyClassMapping extends NamespacedClassMapping implements Documented {
    private String doc;

    public TinyClassMapping(Map<String, String> names) {
        super(names);
    }

    public TinyClassMapping(String namespace, String name) {
        super(namespace, name);
    }

    public TinyClassMapping(String[] namespaces, String[] names) {
        super(namespaces, names);
    }

    public TinyClassMapping(String[] namespaces, String[] names, int nameStart) {
        super(namespaces, names, nameStart);
    }
    public TinyClassMapping() {}

    @Override
    public void setDoc(String doc) {
        this.doc = doc;
    }

    @Override
    public String getDoc() {
        return doc;
    }
}