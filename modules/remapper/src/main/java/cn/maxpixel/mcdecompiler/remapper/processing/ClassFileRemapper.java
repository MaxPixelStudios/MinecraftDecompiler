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

package cn.maxpixel.mcdecompiler.remapper.processing;

import cn.maxpixel.mcdecompiler.mapping.remapper.MappingRemapper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;

public class ClassFileRemapper extends Remapper {
    public final MappingRemapper remapper;
    public final ExtraClassesInformation eci;

    public ClassFileRemapper(@NotNull MappingRemapper remapper, @NotNull ExtraClassesInformation eci) {
        this.remapper = Objects.requireNonNull(remapper);
        this.eci = Objects.requireNonNull(eci);
    }

    @Override
    public String map(String internalName) {
        return remapper.mapClassOrDefault(internalName);
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if (name.charAt(0) != '<') { // equivalent to !(name.equals("<init>") || name.equals("<clinit>"))
            String mapped = remapper.mapMethod(owner, name, descriptor);
            return mapped == null ? processSuperMethod(owner, name, descriptor).map(a -> a[1]).orElse(name) : mapped;
        }
        return name;
    }

    private Optional<String[]> processSuperMethod(String owner, String name, String descriptor) {
        return Optional.ofNullable(eci.getSuperNames(owner))
                .flatMap(superNames -> {
                    String nameAndDesc = name.concat(descriptor);
                    return superNames.parallelStream().map(cls -> {
                        var mapped = remapper.mapMethod(cls, name, descriptor);
                        return mapped == null ? null : new String[] {cls, mapped};
                    }).filter(Objects::nonNull)
                        .reduce((l, r) -> reduceMethod(nameAndDesc, l, r))
                        .or(() -> superNames.parallelStream()
                                .map(n -> processSuperMethod(n, name, descriptor))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce((l, r) -> reduceMethod(nameAndDesc, l, r))
                        );
                });
    }

    // String[] {0: unmapped owner class name, 1: mapped name}
    private String[] reduceMethod(String nameAndDesc, @NotNull String[] left, @NotNull String[] right) {
        if (left[0].equals(right[0])) return left;// just checking owner is enough
        if (left[1].equals(right[1])) return left;// may be an override
        int leftAcc = eci.getAccessFlags(left[0], nameAndDesc);
        int rightAcc = eci.getAccessFlags(right[0], nameAndDesc);
        if ((leftAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) {
            if ((rightAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) throw new IllegalArgumentException("This can't happen!");
            return left;
        } else if ((rightAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) return right;
        else if (Modifier.isPrivate(leftAcc) || Modifier.isPrivate(rightAcc)) throw new IllegalArgumentException("This can't happen!");
        else throw new IllegalArgumentException("Method duplicated... This should not happen!");
    }

    @Override
    public String mapRecordComponentName(String owner, String name, String descriptor) {
        return mapFieldName(owner, name, descriptor);
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        String mapped = remapper.mapField(owner, name);
        return mapped == null ? processSuperField(owner, name).map(a -> a[1]).orElse(name) : mapped;
    }

    private Optional<String[]> processSuperField(String owner, String name) {
        return Optional.ofNullable(eci.getSuperNames(owner))
                .flatMap(superNames -> superNames.parallelStream()
                        .map(cls -> {
                            var mapped = remapper.mapField(cls, name);
                            return mapped == null ? null : new String[] {cls, mapped};
                        }).filter(Objects::nonNull)
                        .reduce((l, r) -> reduceField(name, l, r))
                        .or(() -> superNames.parallelStream()
                                .map(n -> processSuperField(n, name))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce((l, r) -> reduceField(name, l, r))
                        )
                );
    }

    // String[] {0: unmapped owner class name, 1: mapped name}
    private String[] reduceField(String name, @NotNull String[] left, @NotNull String[] right) {
        if (left[0].equals(right[0])) return left;// just checking owner is enough
        int leftAcc = eci.getAccessFlags(left[0], name);
        int rightAcc = eci.getAccessFlags(right[0], name);
        if ((leftAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) {
            if ((rightAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0)
                throw new IllegalArgumentException("This can't happen!");
            return left;
        } else if ((rightAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) return right;
        else if (Modifier.isPrivate(leftAcc) || Modifier.isPrivate(rightAcc))
            throw new IllegalArgumentException("This can't happen!");
        throw new IllegalArgumentException("Field duplicated... This should not happen!");
    }
}