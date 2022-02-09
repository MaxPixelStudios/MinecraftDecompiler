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

import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A decompiler implements this interface is lib-recommended.<br>
 *
 * The decompiler implementation uses {@link ILibRecommendedDecompiler#downloadLib(Path, String)} to download all Minecraft libraries.
 * But you need to download it yourself by implementing this method.<br>
 *
 * If you don't want to implement the download method yourself but still lib-recommended,
 * you can extend {@link AbstractLibRecommendedDecompiler} instead of implementing this interface.
 * @see AbstractLibRecommendedDecompiler
 */
public interface ILibRecommendedDecompiler extends IDecompiler {
    /**
     * Download all libraries.
     * @param libDir Where the libs download to.
     * @param version Minecraft version uses to deobfuscate.
     * @throws IOException When IO error occurs.
     * @see AbstractLibRecommendedDecompiler#listLibs()
     */
    void downloadLib(Path libDir, String version) throws IOException;

    void receiveLibs(ObjectList<Path> libs);
}