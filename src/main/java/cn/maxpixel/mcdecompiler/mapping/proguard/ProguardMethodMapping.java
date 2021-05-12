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

package cn.maxpixel.mcdecompiler.mapping.proguard;

import cn.maxpixel.mcdecompiler.mapping.components.LineNumber;
import cn.maxpixel.mcdecompiler.mapping.paired.MappedDescriptoredPairedMethodMapping;

import java.util.Arrays;

public class ProguardMethodMapping extends MappedDescriptoredPairedMethodMapping implements LineNumber {
    private final int[] lineNums = new int[2];

    public ProguardMethodMapping(String unmappedName, String mappedName, String mappedDescriptor) {
        super(unmappedName, mappedName, mappedDescriptor);
    }

    public ProguardMethodMapping(String unmappedName, String mappedName,
                                 String mappedDescriptor, int startLineNumber, int endLineNumber) {
        super(unmappedName, mappedName, mappedDescriptor);
        this.lineNums[0] = startLineNumber;
        this.lineNums[1] = endLineNumber;
    }

    @Override
    public int getStartLineNumber() {
        return lineNums[0];
    }

    @Override
    public int getEndLineNumber() {
        return lineNums[1];
    }

    @Override
    public void setStartLineNumber(int ns) {
        this.lineNums[0] = ns;
    }

    @Override
    public void setEndLineNumber(int ne) {
        this.lineNums[1] = ne;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProguardMethodMapping)) return false;
        if (!super.equals(o)) return false;
        ProguardMethodMapping that = (ProguardMethodMapping) o;
        return Arrays.equals(lineNums, that.lineNums);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Arrays.hashCode(lineNums);
    }

    @Override
    public String toString() {
        return "ProguardMethodMapping{" +
                "lineNums=" + Arrays.toString(lineNums) +
                "} " + super.toString();
    }
}