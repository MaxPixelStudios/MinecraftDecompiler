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

package cn.maxpixel.mcdecompiler.mapping.tsrg;

import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;

import java.util.Map;

public class TsrgMethodMapping extends NamespacedMethodMapping {
    public boolean isStatic;

    public TsrgMethodMapping(Map<String, String> names, String unmappedDescriptor) {
        super(names, unmappedDescriptor);
    }

    public TsrgMethodMapping(String namespace, String name, String unmappedDescriptor) {
        super(namespace, name, unmappedDescriptor);
    }

    public TsrgMethodMapping(String[] namespaces, String[] names, String unmappedDescriptor) {
        super(namespaces, names, unmappedDescriptor);
    }

    public TsrgMethodMapping(String[] namespaces, String[] names, int nameStart, String unmappedDescriptor) {
        super(namespaces, names, nameStart, unmappedDescriptor);
    }

    public TsrgMethodMapping(String unmappedDescriptor) {
        super(unmappedDescriptor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TsrgMethodMapping that)) return false;
        if (!super.equals(o)) return false;
        return isStatic == that.isStatic;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Boolean.hashCode(isStatic);
    }
}