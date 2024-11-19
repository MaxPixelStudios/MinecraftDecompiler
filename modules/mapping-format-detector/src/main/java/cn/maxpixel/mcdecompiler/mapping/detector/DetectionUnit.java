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

package cn.maxpixel.mcdecompiler.mapping.detector;

import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@ApiStatus.OverrideOnly
public interface DetectionUnit {
    default boolean canDetectHeader() {
        return false;
    }

    default Predicate<String> getLineFilter() {
        return s -> !s.startsWith("#");
    }

    default Optional<MappingFormat<?, ?>> detectHeader(String firstLine) {
        return Optional.empty();
    }

    Pair</*@Nullable*/ MappingFormat<?, ?>, /*@NotNull*/ Percentage> detectContent(List<String> contents);

    enum Percentage {
        ZERO,
        FIFTY,
        NINETY,
        NINETY_NINE,
        ONE_HUNDRED;

        public boolean isHigherThan(Percentage p) {
            return ordinal() > p.ordinal();
        }
    }
}