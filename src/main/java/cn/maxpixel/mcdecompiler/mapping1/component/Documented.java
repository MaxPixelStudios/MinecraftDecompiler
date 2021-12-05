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

package cn.maxpixel.mcdecompiler.mapping1.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class Documented implements Component {
    public String doc;

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public static class LocalVariable extends Documented {
        private final Int2ObjectOpenHashMap<String> lvtDoc = new Int2ObjectOpenHashMap<>();

        public void setLocalVariableDoc(int index, String doc) {
            if(index < 0) throw new IndexOutOfBoundsException();
            lvtDoc.put(index, doc);
        }

        public String getLocalVariableDoc(int index) {
            if(index < 0) throw new IndexOutOfBoundsException();
            return lvtDoc.get(index);
        }
    }
}