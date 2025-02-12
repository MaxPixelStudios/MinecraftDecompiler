/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2025 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.mapping.remapper;

import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.mcdecompiler.common.util.DescriptorUtil;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

/**
 * Lightweight remapper for descriptors of one direction in place of the general heavyweight {@link MappingRemapper}s.
 */
public class UniDescriptorRemapper {
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByUnm;

    public UniDescriptorRemapper(Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByUnm) {
        this.mappingByUnm = mappingByUnm;
    }

    private String mapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByUnm.get(name);
        if (classMapping != null) return classMapping.mapping.getMappedName();
        return name;
    }

    protected String unmapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByUnm.get(name);
        if (classMapping != null) return classMapping.mapping.getUnmappedName();
        return name;
    }

    @Subst("I")
    public @Pattern(Constants.FIELD_DESC_PATTERN) String mapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String unmappedDesc) {
        return mapDesc(unmappedDesc, true);
    }

    @Subst("()V")
    public @Pattern(Constants.METHOD_DESC_PATTERN) String mapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String unmappedDesc) {
        return mapMethodDesc(unmappedDesc, true);
    }

    @Subst("I")
    public @Pattern(Constants.FIELD_DESC_PATTERN) String unmapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String mappedDesc) {
        return mapDesc(mappedDesc, false);
    }

    @Subst("()V")
    public @Pattern(Constants.METHOD_DESC_PATTERN) String unmapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String mappedDesc) {
        return mapMethodDesc(mappedDesc, false);
    }

    @Subst("I")
    private String mapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String desc, boolean map) {
        int i = 0;
        if (desc.charAt(0) == '[') while (desc.charAt(++i) == '[');
        return switch (desc.charAt(i)) {
            case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S' -> desc;
            case 'L' -> {
                StringBuilder ret = new StringBuilder(desc.length()).append(desc, 0, ++i);
                int j = desc.indexOf(';', i + 1);// skip 'L' and the first char
                if (j < 0) DescriptorUtil.throwInvalid(false);
                yield ret.append(map ? mapClass(desc.substring(i, j)) : unmapClass(desc.substring(i, j)))
                        .append(desc, j, desc.length()).toString();
            }
            default -> DescriptorUtil.throwInvalid(false);
        };
    }

    @Subst("()V")
    private String mapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String desc, boolean map) {
        if (desc.length() == 3 || desc.indexOf('L') < 0) return desc;// no need to map
        StringBuilder ret = new StringBuilder(desc.length());
        int start = 0;
        for (int i = 1; i < desc.length(); i++) {
            switch (desc.charAt(i)) {
                case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'V', '[', ')' -> {} // no op
                case 'L' -> {
                    ret.append(desc, start, ++i);
                    start = desc.indexOf(';', i + 1);// skip 'L'(++i) and the first char
                    if (start < 0) DescriptorUtil.throwInvalid(true);
                    ret.append(map ? mapClass(desc.substring(i, start)) : unmapClass(desc.substring(i, start)));
                    i = start;// will do i++, so don't assign `start + 1` here
                }
                default -> DescriptorUtil.throwInvalid(true);
            }
        }
        return ret.append(desc, start, desc.length()).toString();
    }
}