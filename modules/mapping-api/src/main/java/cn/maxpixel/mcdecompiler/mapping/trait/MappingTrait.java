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

package cn.maxpixel.mcdecompiler.mapping.trait;

import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import org.jetbrains.annotations.ApiStatus;

/**
 * A mapping trait is a mark that indicates the all entries of a mapping collection has a common feature
 * or an attachment to a mapping collection to hold extra data.
 *
 * @apiNote The assumption is that mapping traits should apply to all the entries in the collection.
 *          Otherwise, you may get unexpected behavior.
 */
public interface MappingTrait {
    /**
     * Gets the name of this trait.
     *
     * @return The name of this trait.
     */
    String getName();

    /**
     * Updates the given mapping collection containing {@code this} trait.
     *
     * @implSpec This should be called by the {@link MappingCollection} that owns this trait.
     * @param collection The mapping collection to update
     */
    @ApiStatus.OverrideOnly
    default void updateCollection(MappingCollection<?> collection) {
    }
}