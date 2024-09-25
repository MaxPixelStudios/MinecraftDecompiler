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

package cn.maxpixel.mcdecompiler.mapping.component;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;

import java.util.Objects;

public class Owned<T extends Mapping> implements Component {
    public ClassMapping<T> owner;

    public Owned() {}

    public Owned(ClassMapping<T> owner) {
        this.owner = owner;
    }

    public ClassMapping<T> getOwner() {
        return owner;
    }

    public void setOwner(ClassMapping<T> owner) {
        this.owner = owner;
    }

    @Override
    public void validate() throws IllegalStateException {
        if (owner == null) throw new IllegalStateException("Owner is null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Owned<?> owned)) return false;
        return ClassMapping.mappingEquals(owner, owned.owner);
    }

    @Override
    public int hashCode() {
        return owner == null ? 0 : Objects.hashCode(owner.mapping);
    }
}