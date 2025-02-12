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

package cn.maxpixel.mcdecompiler.mapping;

import org.jetbrains.annotations.NotNull;

/**
 * Intends to be a universal interface for getting names
 */
public interface NameGetter {
    String getUnmappedName();

    String getMappedName();

    /**
     * Universal interface for getting/setting namespaces
     *
     * @implSpec unmapped namespace should be set by mapping processors
     */
    interface Namespace {
        String getUnmappedNamespace();

        void setUnmappedNamespace(@NotNull String namespace);

        String getMappedNamespace();

        void setMappedNamespace(@NotNull String namespace);

        String getFallbackNamespace();

        void setFallbackNamespace(@NotNull String namespace);
    }
}