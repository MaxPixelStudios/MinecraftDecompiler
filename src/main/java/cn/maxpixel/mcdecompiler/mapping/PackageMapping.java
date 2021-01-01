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

import cn.maxpixel.mcdecompiler.mapping.base.BaseMapping;

public class PackageMapping extends BaseMapping {
    public PackageMapping(String unmappedName, String mappedName) {
        super(unmappedName, mappedName);
    }
    public PackageMapping() {}

    @Override
    public String toString() {
        return "PackageMapping{unmapped name=" + getUnmappedName() +
                ", mapped name=" + getMappedName() + '}';
    }
}