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

import cn.maxpixel.mcdecompiler.Info;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class DescriptorUtil {
    public static String getMethodReturnDescriptor(@NotNull String methodDescriptor) {
        int index = methodDescriptor.lastIndexOf(')');
        if (index == -1) throw new IllegalArgumentException("Invalid descriptor");
        return methodDescriptor.substring(index + 1);
    }

    public static int getArgumentCount(@NotNull @Pattern(Info.METHOD_DESC_PATTERN) String descriptor) {
        int count = 0;
        for (int i = 1, max = descriptor.lastIndexOf(')'); i < max; i++) {
            switch (descriptor.charAt(i)) {
                case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S' -> count++;
                case 'L' -> {
                    count++;
                    if ((i = descriptor.indexOf(';', i)) == -1)
                        throw new IllegalArgumentException("Invalid method descriptor");
                }
                case '[' -> {} // no op
                default -> throw new IllegalArgumentException("Invalid method descriptor");
            }
        }
        return count;
    }
}