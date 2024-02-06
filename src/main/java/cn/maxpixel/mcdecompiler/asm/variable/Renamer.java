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

package cn.maxpixel.mcdecompiler.asm.variable;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.util.Locale;

public class Renamer {
    private record Holder(boolean skipZero, @NotNull String... names) {
        private Holder(@NotNull String... names) {
            this(true, names);
        }
    }

    private static final Object2ObjectOpenHashMap<String, Holder> PREDEF = new Object2ObjectOpenHashMap<>();

    static {
        Holder h = new Holder("i", "j", "k", "l");
        PREDEF.put("int", h);
        PREDEF.put("long", h);
        PREDEF.put("byte", new Holder(false, "b"));
        PREDEF.put("char", new Holder(false, "c"));
        PREDEF.put("short", new Holder(false, "short"));
        PREDEF.put("double", new Holder(false, "d"));
        PREDEF.put("boolean", new Holder("flag"));
        PREDEF.put("float", new Holder("f"));
        PREDEF.put("String", new Holder("s"));
        PREDEF.put("Class", new Holder("oclass"));
        PREDEF.put("Long", new Holder("olong"));
        PREDEF.put("Byte", new Holder("obyte"));
        PREDEF.put("Short", new Holder("oshort"));
        PREDEF.put("Boolean", new Holder("obool"));
        PREDEF.put("Float", new Holder("ofloat"));
        PREDEF.put("Double", new Holder("odouble"));
        PREDEF.put("Package", new Holder("opackage"));
        PREDEF.put("Enum", new Holder("oenum"));
        PREDEF.put("Void", new Holder("ovoid"));
    }

    private final Object2IntOpenHashMap<String> vars = new Object2IntOpenHashMap<>();

    {
        vars.defaultReturnValue(0);
    }

    public String addExistingName(String name) {
        if (vars.addTo(name, 1) > 0) throw new IllegalArgumentException("Duplicated var name");
        return name;
    }

    public String getVarName(Type type) {
        boolean isArray = false;
        if (type.getSort() == Type.ARRAY) {
            type = type.getElementType();
            isArray = true;
        }
        String varBaseName = type.getClassName();
        if (type.getSort() == Type.OBJECT) varBaseName = varBaseName.substring(varBaseName.lastIndexOf('.') + 1);
        Holder holder = isArray ? null : PREDEF.get(varBaseName);
        if (holder != null) {
            for (int i = 0; ; i++) {
                for (String s : holder.names) {
                    if (i >= vars.getInt(s)) {
                        int j = vars.addTo(s, 1);
                        return s + (j == 0 && holder.skipZero ? "" : j);
                    }
                }
            }
        } else {
            varBaseName = varBaseName.replace('$', '_').toLowerCase(Locale.ENGLISH);
            if (isArray) varBaseName = "a" + varBaseName;
            int count = vars.addTo(varBaseName, 1);
            return varBaseName + (count > 0 ? count : "");
        }
    }
}
