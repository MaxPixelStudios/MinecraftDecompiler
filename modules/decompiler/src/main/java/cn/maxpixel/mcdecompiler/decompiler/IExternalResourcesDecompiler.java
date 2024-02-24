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

package cn.maxpixel.mcdecompiler.decompiler;

import java.io.IOException;
import java.nio.file.Path;

/**
 * If your custom decompiler uses extra resources(executables, etc.) to work, implements this interface.
 */
public interface IExternalResourcesDecompiler extends IDecompiler {
    /**
     * Extract your extra resources.
     * @param extractPath the path you need to extract your extra executables to
     * @throws IOException When IO error occurs
     */
    void extractTo(Path extractPath) throws IOException;
}