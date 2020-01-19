/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2020  XiaoPangxie732
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

package cn.maxpixel.mcdecompiler;

public class NamingUtil {
	public static String getClassName(String name) {
		String fullClassName = name.replace("/", ".");
		return fullClassName.substring(fullClassName.lastIndexOf("."));
	}
	public static String getPackageName(String name) {
		String fullClassName = name.replace("/", ".");
		return fullClassName.substring(0, fullClassName.lastIndexOf("."));
	}
	public static String asJavaName(String nativeName) {
		return nativeName.replace('/', '.');
	}
	public static String asNativeName(String javaName) {
		return javaName.replace('/', '.');
	}
	public static String asFQCN(String javaName) {
		if(!javaName.contains("[]"))
			if(javaName.equals("boolean")) return "Z";
			else if(javaName.equals("byte")) return "B";
			else if(javaName.equals("char")) return "C";
			else if(javaName.equals("double")) return "D";
			else if(javaName.equals("float")) return "F";
			else if(javaName.equals("int")) return "I";
			else if(javaName.equals("long")) return "J";
			else if(javaName.equals("short")) return "S";
			else return "L" + javaName.replace('.', '/') + ";";
		else {
			StringBuilder buf = new StringBuilder();
			int arrDimension = 0;
			for(int index = 0;index < javaName.length();index+=2)
				if((index = javaName.indexOf("[]", index)) != -1) arrDimension++;
			for(;arrDimension!=0;arrDimension--) buf.append('[');
			javaName = javaName.replace("[]", "");
			switch (javaName) {
				case "boolean":
					buf.append('Z');
					break;
				case "byte":
					buf.append('B');
					break;
				case "char":
					buf.append('C');
					break;
				case "double":
					buf.append('D');
					break;
				case "float":
					buf.append('F');
					break;
				case "int":
					buf.append('I');
					break;
				case "long":
					buf.append('J');
					break;
				case "short":
					buf.append('S');
					break;
				default:
					buf.append('L').append(javaName.replace('.', '/')).append(';');
					break;
			}
			return buf.toString();
		}
	}
}