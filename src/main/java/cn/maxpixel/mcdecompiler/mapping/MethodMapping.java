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
	private final int[] linenumber = new int[2];
	private String returnVal;
	private String[] argTypes;
	public MethodMapping() {
		super();
	}
	public MethodMapping(String obfuscatedName, String originalName, int linenumber1,
	                     int linenumber2, String returnVal, String[] argTypes) {
		super(obfuscatedName, originalName);
		this.linenumber[0] = linenumber1;
		this.linenumber[1] = linenumber2;
		this.returnVal = returnVal;
		this.argTypes = argTypes;
	}

	public int[] getLinenumber() {
		return linenumber;
	}
	public void setLinenumber(int linenumber1, int linenumber2) {
		this.linenumber[0] = linenumber1;
		this.linenumber[1] = linenumber2;
	}
	public String getReturnVal() {
		return returnVal;
	}
	public void setReturnVal(String returnVal) {
		this.returnVal = returnVal;
	}
	public String[] getArgTypes() {
		return argTypes;
	}
	public void setArgTypes(String[] argTypes) {
		this.argTypes = argTypes;
	}

	@Override
	public String toString() {
		return "MethodMapping{" +
				"obfuscated name=" + getObfuscatedName() +
				", original name=" + getOriginalName() +
				", linenumber=" + Arrays.toString(linenumber) +
				", returnVal='" + returnVal + '\'' +
				", argTypes=" + Arrays.toString(argTypes) +
				'}';
	}
}