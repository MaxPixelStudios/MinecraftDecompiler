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

package cn.maxpixel.mcdecompiler.decompiler;

/**
 * Custom decompilers MUST implement this interface.<br>
 *
 * To make the program can find your custom decompiler,
 * you need to create a file named cn.maxpixel.mcdecompiler.decompiler.ICustomizedDecompiler in META-INF/services.
 * Then write the FQCN of your custom decompiler class that implements this interface in it.
 */
public interface ICustomDecompiler extends IDecompiler {
    /**
     * Returns the unique name of your custom decompiler. The value is needed to identify which custom decompiler this is.<br>
     * @return the unique name of your custom decompiler.
     */
    String name();
}