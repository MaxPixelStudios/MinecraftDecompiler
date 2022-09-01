/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

package cn.maxpixel.mcdecompiler.util;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.StringJoiner;

public record MixinTargetSelector(@Nullable Type owner, @Nullable String name, @Nullable String quantifier, @Nullable String descriptor, boolean field) {
    public static MixinTargetSelector parseMethod(String pattern) {
        String p = pattern;
        Type owner = null;
        if (p.charAt(0) == 'L') {
            int i = p.indexOf(';');
            int j = p.indexOf('(');
            if (i != -1 && (j == -1 || i < j)) {
                owner = Type.getType(p.substring(0, i + 1));
                p = p.substring(i + 1);
            }
        } else if (p.indexOf('.') != -1) {
            String[] sa = p.split("\\.");
            StringJoiner joiner = new StringJoiner("/");
            for (int i = 0; i < sa.length - 1; i++) {
                joiner.add(sa[i]);
            }
            owner = Type.getObjectType(joiner.toString());
            p = sa[sa.length - 1];
        }

        String name = null, quantifier = null, descriptor = null;
        int i = 0;

        if ((i = p.indexOf('*')) != -1) {
            if (i > 0) name = p.substring(0, i);
            quantifier = "*";
            p = p.substring(i + 1);
        } else if ((i = p.indexOf('+')) != -1) {
            if (i > 0) name = p.substring(0, i);
            quantifier = "+";
            p = p.substring(i + 1);
        } else if ((i = p.indexOf('{')) != -1) {
            int end = p.indexOf('}');
            if (i > 0) name = p.substring(0, i);
            quantifier = p.substring(i, end + 1);
            p = p.substring(end + 1);
        } else {
            i = p.indexOf('(');
            if (i != -1) {
                name = p.substring(0, i);
                p = p.substring(i);
            } else {
                name = p;
                p = "";
            }
        }

        if (!p.isEmpty()) {
            if (p.charAt(0) == '(') {
                descriptor = p;
            } else throw new IllegalArgumentException("Cannot parse pattern: " + pattern);
        }
        return new MixinTargetSelector(owner, name, quantifier, descriptor, false);
    }

    public MixinTargetSelector remap(ClassifiedMappingRemapper remapper, String mappedName) {
        return new MixinTargetSelector(owner == null ? null : Type.getType(remapper.mapToMapped(owner)), mappedName, quantifier,
                descriptor == null ? null : remapper.getMappedDescByUnmappedDesc(descriptor), field);
    }

    public String toSelectorString() {
        StringBuilder builder = new StringBuilder();
        if (owner != null) builder.append(owner.getDescriptor());
        if (name != null) builder.append(name);
        if (quantifier != null) builder.append(quantifier);
        if (descriptor != null) {
            if (field) builder.append(':');
            builder.append(descriptor);
        }
        return builder.toString();
    }
}