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

package cn.maxpixel.mcdecompiler.mapping.component;

import org.jetbrains.annotations.NotNull;

/**
 * Base component class. Every component must implement this interface
 */
public interface Component {
    /**
     * Validates this component.
     * @throws IllegalStateException if the validation fails
     */
    default void validate() throws IllegalStateException {
    }

    interface ConvertingReversible<T extends Component> {
        @NotNull Class<T> getTarget();

        @NotNull T convert();

        void reverse(@NotNull T target);
    }

    interface Reversible {
        void reverse();
    }

    interface Swappable {
        void swap(@NotNull String fromNamespace, @NotNull String toNamespace);
    }
}