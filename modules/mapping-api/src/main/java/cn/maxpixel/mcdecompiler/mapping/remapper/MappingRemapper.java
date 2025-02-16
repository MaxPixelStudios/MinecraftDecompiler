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

package cn.maxpixel.mcdecompiler.mapping.remapper;

import cn.maxpixel.mcdecompiler.mapping.util.MethodOrFieldDesc;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MappingRemapper {
    boolean hasClassMapping(String name);

    boolean isMethodStaticIdentifiable();

    @Nullable("When no corresponding mapping found") String mapClass(@NotNull String name);

    default @NotNull String mapClassOrDefault(@NotNull String name) {
        String ret = mapClass(name);
        return ret == null ? name : ret;
    }

    @Nullable("When no corresponding mapping found") String unmapClass(@NotNull String name);

    default @NotNull String unmapClassOrDefault(@NotNull String name) {
        String ret = unmapClass(name);
        return ret == null ? name : ret;
    }

    /**
     * Map a field.
     *
     * @implSpec Should directly return "name"(no copies, etc.) when mapped name is not found.
     * @param owner Owner of the method.
     * @param name Name to map.
     * @return Mapped name if present. Provided name otherwise.
     */
    @Nullable("When no corresponding mapping found") String mapField(@NotNull String owner, @NotNull String name);

    default @NotNull String mapFieldOrDefault(@NotNull String owner, @NotNull String name) {
        String ret = mapField(owner, name);
        return ret == null ? name : ret;
    }

    /**
     * Map a method.
     *
     * @apiNote When desc is null and multiple matches are found, a random match is returned.
     * @implSpec Should directly return "name"(no copies, etc.) when mapped name is not found.
     * @param owner Owner of the method.
     * @param name Name to map.
     * @param desc Descriptor of the method.
     * @return Mapped name if present. Provided name otherwise.
     */
    @Nullable("When no corresponding mapping found") String mapMethod(@NotNull String owner, @NotNull String name, @Nullable("When desc doesn't matter") String desc);

    default @NotNull String mapMethodOrDefault(@NotNull String owner, @NotNull String name, @Nullable("When desc doesn't matter") String desc) {
        String ret = mapMethod(owner, name, desc);
        return ret == null ? name : ret;
    }

    DescriptorRemapper getDescriptorRemapper();

    @Subst("I")
    default @Pattern(MethodOrFieldDesc.FIELD_DESC_PATTERN) String mapDesc(@Pattern(MethodOrFieldDesc.FIELD_DESC_PATTERN) String unmappedDesc) {
        return getDescriptorRemapper().mapDesc(unmappedDesc);
    }

    @Subst("()V")
    default @Pattern(MethodOrFieldDesc.METHOD_DESC_PATTERN) String mapMethodDesc(@Pattern(MethodOrFieldDesc.METHOD_DESC_PATTERN) String unmappedDesc) {
        return getDescriptorRemapper().mapMethodDesc(unmappedDesc);
    }

    @Subst("I")
    default @Pattern(MethodOrFieldDesc.FIELD_DESC_PATTERN) String unmapDesc(@Pattern(MethodOrFieldDesc.FIELD_DESC_PATTERN) String mappedDesc) {
        return getDescriptorRemapper().unmapDesc(mappedDesc);
    }

    @Subst("()V")
    default @Pattern(MethodOrFieldDesc.METHOD_DESC_PATTERN) String unmapMethodDesc(@Pattern(MethodOrFieldDesc.METHOD_DESC_PATTERN) String mappedDesc) {
        return getDescriptorRemapper().unmapMethodDesc(mappedDesc);
    }
}