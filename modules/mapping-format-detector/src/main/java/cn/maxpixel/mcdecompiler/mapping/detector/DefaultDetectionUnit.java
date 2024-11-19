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
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import it.unimi.dsi.fastutil.Pair;

import java.util.List;
import java.util.Optional;

public class DefaultDetectionUnit implements DetectionUnit {
    @Override
    public boolean canDetectHeader() {
        return true;
    }

    @Override
    public Optional<MappingFormat<?, ?>> detectHeader(String firstLine) {
        if (firstLine.startsWith("v1")) return Optional.of(MappingFormats.TINY_V1);
        else if (firstLine.startsWith("tiny\t2\t0")) return Optional.of(MappingFormats.TINY_V2);
        else if (firstLine.startsWith("tsrg2")) return Optional.of(MappingFormats.TSRG_V2);
        return Optional.empty();
    }

    @Override
    public Pair</*@Nullable*/ MappingFormat<?, ?>, /*@NotNull*/ Percentage> detectContent(List<String> contents) {
        String s = contents.get(0);
        if (s.startsWith("PK: ") || s.startsWith("CL: ") || s.startsWith("FD: ") || s.startsWith("MD: ")) return Pair.of(MappingFormats.SRG, Percentage.NINETY_NINE);
        else if (s.endsWith(":")) return Pair.of(MappingFormats.PROGUARD, Percentage.NINETY);
        else if (s.contains("\u00B6")) return Pair.of(MappingFormats.PDME, Percentage.NINETY);
        s = contents.get(1);
        if (s.startsWith("\t")) return Pair.of(MappingFormats.TSRG_V1, Percentage.FIFTY);
        else return Pair.of(MappingFormats.CSRG, Percentage.FIFTY);
    }
}