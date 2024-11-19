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

package cn.maxpixel.mcdecompiler.mapping.format;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.Objects;
import java.util.Set;

public final class MappingFormats {
    public static final SrgMappingFormat SRG = SrgMappingFormat.INSTANCE;
    public static final CsrgMappingFormat CSRG = CsrgMappingFormat.INSTANCE;
    public static final TsrgV1MappingFormat TSRG_V1 = TsrgV1MappingFormat.INSTANCE;
    public static final TsrgV2MappingFormat TSRG_V2 = TsrgV2MappingFormat.INSTANCE;
    public static final ProguardMappingFormat PROGUARD = ProguardMappingFormat.INSTANCE;
    public static final TinyV1MappingFormat TINY_V1 = TinyV1MappingFormat.INSTANCE;
    public static final TinyV2MappingFormat TINY_V2 = TinyV2MappingFormat.INSTANCE;
    public static final PdmeMappingFormat PDME = PdmeMappingFormat.INSTANCE;

    private static final Object2ObjectOpenHashMap<String, MappingFormat<?, ?>> MAPPING_FORMATS = new Object2ObjectOpenHashMap<>();

    static {
        registerMappingFormat(SRG);
        registerMappingFormat(CSRG);
        registerMappingFormat(TSRG_V1);
        registerMappingFormat(TSRG_V2);
        registerMappingFormat(PROGUARD);
        registerMappingFormat(TINY_V1);
        registerMappingFormat(TINY_V2);
        registerMappingFormat(PDME);
    }

    public static boolean registerMappingFormat(MappingFormat<?, ?> format) {
        return MAPPING_FORMATS.putIfAbsent(Objects.requireNonNull(format.getName()), format) == null;
    }

    public static MappingFormat<?, ?> get(String name) {
        return MAPPING_FORMATS.get(name);
    }

    public static Set<String> getFormatNames() {
        return ObjectSets.unmodifiable(MAPPING_FORMATS.keySet());
    }
}