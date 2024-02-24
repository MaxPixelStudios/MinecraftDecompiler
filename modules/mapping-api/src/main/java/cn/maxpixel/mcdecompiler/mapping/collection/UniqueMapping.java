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
import cn.maxpixel.mcdecompiler.mapping.trait.MappingTrait;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A collection of mappings of classes, fields, methods, parameters and packages,
 * which their names are unique, and stored in a flat structure.
 *
 * @apiNote These fields should not contain null values.
 *          There are no checks but the assumption is made.
 *          Put null values at your own risk.
 * @param <T> The type of this mapping
 */
public class UniqueMapping<T extends Mapping> extends MappingCollection<T> {
    /**
     * Classes of this mapping.
     */
    public final ObjectArrayList<@NotNull T> classes = new ObjectArrayList<>();

    /**
     * Fields of this mapping.
     */
    public final ObjectArrayList<@NotNull T> fields = new ObjectArrayList<>();

    /**
     * Methods of this mapping.
     */
    public final ObjectArrayList<@NotNull T> methods = new ObjectArrayList<>();

    /**
     * Parameters of this mapping.
     */
    public final ObjectArrayList<@NotNull T> params = new ObjectArrayList<>();

    public UniqueMapping() {}

    public UniqueMapping(@NotNull MappingTrait @NotNull ... traits) {
        super(traits);
    }

    @Override
    public void clear() {
        super.clear();
        classes.clear();
        fields.clear();
        methods.clear();
        params.clear();
    }

    /**
     * Adds the contents of another {@link UniqueMapping} to this one.
     *
     * @param m Mappings to be added
     */
    public void add(@NotNull UniqueMapping<T> m) {
        classes.addAll(m.classes);
        fields.addAll(m.fields);
        methods.addAll(m.methods);
        params.addAll(m.params);
        packages.addAll(m.packages);
    }

    /**
     * Adds the contents of this {@link UniqueMapping} into another one.
     *
     * @param m Mappings to add to
     */
    public void addTo(@NotNull UniqueMapping<T> m) {
        m.classes.addAll(classes);
        m.fields.addAll(fields);
        m.methods.addAll(methods);
        m.params.addAll(params);
        m.packages.addAll(packages);
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueMapping<?> that)) return false;
        return Objects.equals(classes, that.classes) && Objects.equals(fields, that.fields) &&
                Objects.equals(methods, that.methods)&& Objects.equals(params, that.params) &&
                Objects.equals(packages, that.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classes, fields, methods, params, packages);
    }

    @Override
    public String toString() {
        return "UniqueMapping{" +
                "classes=" + classes +
                ", fields=" + fields +
                ", methods=" + methods +
                ", params=" + params +
                ", packages=" + packages +
                '}';
    }
}