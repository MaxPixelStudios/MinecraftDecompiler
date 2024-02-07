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

package cn.maxpixel.mcdecompiler.mapping.collection;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Objects;

/**
 * A mapping with classes, fields and methods.
 * @param <T> The type of this mapping
 */
public class UniqueMapping<T extends Mapping> {
    public final ObjectArrayList<T> classes = new ObjectArrayList<>();

    public final ObjectArrayList<T> fields = new ObjectArrayList<>();

    public final ObjectArrayList<T> methods = new ObjectArrayList<>();

    public final ObjectArrayList<T> params = new ObjectArrayList<>();

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueMapping<?> that)) return false;
        return classes.equals(that.classes) && fields.equals(that.fields) && methods.equals(that.methods) && params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classes, fields, methods, params);
    }

    @Override
    public String toString() {
        return "UniqueMapping{" +
                "classes=" + classes +
                ", fields=" + fields +
                ", methods=" + methods +
                ", params=" + params +
                '}';
    }
}