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

package cn.maxpixel.mcdecompiler.mapping;

import java.util.Arrays;

public class MethodMapping extends Mapping {
    private final int[] linenumbers = new int[2];
    private String obfuscatedDescriptor;
    private String originalDescriptor;
    public MethodMapping() {}
    public MethodMapping(String obfuscatedName, String originalName, int linenumber1,
                         int linenumber2, String originalDescriptor) { // for Proguard mappings
        super(obfuscatedName, originalName);
        this.linenumbers[0] = linenumber1;
        this.linenumbers[1] = linenumber2;
        this.originalDescriptor = originalDescriptor;
    }
    public MethodMapping(String obfuscatedName, String originalName,
                         String obfuscatedDescriptor, String originalDescriptor) { // others
        super(obfuscatedName, originalName);
        this.obfuscatedDescriptor = obfuscatedDescriptor;
        this.originalDescriptor = originalDescriptor;
    }

    // Proguard only -- start
    public int[] getLinenumbers() {
        return linenumbers;
    }
    public void setLinenumber(int linenumber1, int linenumber2) {
        this.linenumbers[0] = linenumber1;
        this.linenumbers[1] = linenumber2;
    }
    // Proguard only -- end

    public String getObfuscatedDescriptor() {
        return obfuscatedDescriptor;
    }
    public void setObfuscatedDescriptor(String obfuscatedDescriptor) {
        this.obfuscatedDescriptor = obfuscatedDescriptor;
    }
    public String getOriginalDescriptor() {
        return originalDescriptor;
    }
    public void setOriginalDescriptor(String originalDescriptor) {
        this.originalDescriptor = originalDescriptor;
    }

    @Override
    public String toString() {
        return "MethodMapping{" +
                "obfuscated name=" + getObfuscatedName() +
                ", original name=" + getOriginalName() +
                ", linenumber=" + Arrays.toString(linenumbers) +
                ", obfuscatedDescriptor='" + obfuscatedDescriptor + '\'' +
                ", originalDescriptor=" + originalDescriptor +
                '}';
    }
}