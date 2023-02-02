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

package cn.maxpixel.mcdecompiler.mapping.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Objects;

public class Documented implements Component {
    private String doc;

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = Objects.requireNonNull(doc, "null documentation isn't meaningful huh?");
    }

    public static class LocalVariable implements Component {
        private final Int2ObjectOpenHashMap<String> lvtDoc = new Int2ObjectOpenHashMap<>();

        public void setLocalVariableDoc(int index, String doc) {
            if(index < 0) throw new IndexOutOfBoundsException();
            lvtDoc.put(index, Objects.requireNonNull(doc, "null documentation isn't meaningful huh?"));
        }

        public String getLocalVariableDoc(int index) {
            if(index < 0) throw new IndexOutOfBoundsException();
            return lvtDoc.get(index);
        }
    }
}