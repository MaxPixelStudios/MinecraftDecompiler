/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2020  MaxPixelStudios
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

import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;

public class BaseMethodMapping extends BaseMapping implements Descriptor {
    private String unmappedDescriptor;
    public BaseMethodMapping() {}
    public BaseMethodMapping(String unmappedName, String mappedName,
                             String unmappedDescriptor) {
        super(unmappedName, mappedName);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public String getUnmappedDescriptor() {
        return unmappedDescriptor;
    }
    public void setUnmappedDescriptor(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }

    @Override
    public String toString() {
        return "BaseMethodMapping{" +
                "obfuscated name=" + getUnmappedName() +
                ", original name=" + getMappedName() +
                '}';
    }
}