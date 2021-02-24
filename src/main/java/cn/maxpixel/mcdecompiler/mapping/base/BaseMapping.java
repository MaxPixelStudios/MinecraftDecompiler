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

package cn.maxpixel.mcdecompiler.mapping.base;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.components.Owner;

public abstract class BaseMapping {
    private String unmappedName;
    private String mappedName;
    protected BaseMapping(String unmappedName, String mappedName) {
        this.unmappedName = unmappedName;
        this.mappedName = mappedName;
    }
    protected BaseMapping() {}
    public String getUnmappedName() {
        return unmappedName;
    }
    public void setUnmappedName(String unmappedName) {
        this.unmappedName = unmappedName;
    }
    public String getMappedName() {
        return mappedName;
    }
    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }
}
class BaseFieldMethodShared extends BaseMapping implements Owner<BaseFieldMethodShared, ClassMapping> {
    private ClassMapping owner;
    protected BaseFieldMethodShared(String unmappedName, String mappedName) {
        super(unmappedName, mappedName);
    }
    protected BaseFieldMethodShared() {}

    public boolean isDescriptor() {
        return this instanceof Descriptor;
    }
    public boolean isMappedDescriptor() {
        return this instanceof Descriptor.Mapped;
    }
    public Descriptor asDescriptor() {
        return (Descriptor) this;
    }
    public Descriptor.Mapped asMappedDescriptor() {
        return (Descriptor.Mapped) this;
    }

    @Override
    public ClassMapping getOwner() {
        return owner;
    }
    @Override
    public BaseFieldMethodShared setOwner(ClassMapping owner) {
        this.owner = owner;
        return this;
    }
}