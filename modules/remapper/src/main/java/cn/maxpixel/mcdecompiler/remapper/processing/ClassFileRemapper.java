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

    private ExtraClassesInformation extraClassesInformation;

    public ClassFileRemapper(@NotNull MappingRemapper remapper) {
        this.remapper = Objects.requireNonNull(remapper);
    }

    public ClassFileRemapper setExtraClassesInformation(ExtraClassesInformation extraClassesInformation) {
        this.extraClassesInformation = Objects.requireNonNull(extraClassesInformation);
        return this;
    }

    public ExtraClassesInformation getExtraClassesInformation() {
        return extraClassesInformation;
    }

    @Override
    public String map(String internalName) {
        return remapper.mapClass(internalName);
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if (name.charAt(0) != '<') { // equivalent to !(name.equals("<init>") || name.equals("<clinit>"))
            String mapped = remapper.mapMethod(owner, name, descriptor);
            return mapped.equals(name) ? processSuperMethod(owner, name, descriptor).map(a -> a[1]).orElse(name) : mapped;
        }
        return name;
    }

    private Optional<String[]> processSuperMethod(String owner, String name, String descriptor) {
        if (extraClassesInformation == null) throw new UnsupportedOperationException("ExtraClassesInformation not present");
        return Optional.ofNullable(extraClassesInformation.getSuperNames(owner))
                .flatMap(superNames -> superNames.parallelStream()
                        .map(cls -> {
                            var remapped = remapper.mapMethod(cls, name, descriptor);
                            return remapped.equals(name) ? null : new String[] {cls, remapped, name, descriptor};
                        }).filter(Objects::nonNull)
                        .reduce(this::reduceMethod)
                        .or(() -> superNames.parallelStream()
                                .map(n -> processSuperMethod(n, name, descriptor))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce(this::reduceMethod)
                        )
                );
    }

    // String[] {0: unmapped owner class name, 1: mapped name, 2: unmapped name, 3: unmapped descriptor}
    private String[] reduceMethod(@NotNull String[] left, @NotNull String[] right) {
        if (left[0].equals(right[0])) return left;// just checking owner is enough
        if (left[1].equals(right[1]) && left[3].equals(right[3])) return left;// may be an override
        int leftAcc = extraClassesInformation.getAccessFlags(left[0], left[2].concat(left[3]));
        int rightAcc = extraClassesInformation.getAccessFlags(right[0], right[2].concat(right[3]));
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
        return mapped.equals(name) ? processSuperField(owner, name).map(a -> a[1]).orElse(name) : mapped;
    }

    private Optional<String[]> processSuperField(String owner, String name) {
        if (extraClassesInformation == null) throw new UnsupportedOperationException("ExtraClassesInformation not present");
        return Optional.ofNullable(extraClassesInformation.getSuperNames(owner))
                .flatMap(superNames -> superNames.parallelStream()
                        .map(cls -> {
                            var remapped = remapper.mapField(cls, name);
                            return remapped.equals(name) ? null : new String[] {cls, remapped, name};
                        }).filter(Objects::nonNull)
                        .reduce(this::reduceField)
                        .or(() -> superNames.parallelStream()
                                .map(n -> processSuperField(n, name))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce(this::reduceField)
                        )
                );
    }

    // String[] {0: unmapped owner class name, 1: mapped name, 2: unmapped name}
    private String[] reduceField(@NotNull String[] left, @NotNull String[] right) {
        if (left[0].equals(right[0])) return left;// just checking owner is enough
        int leftAcc = extraClassesInformation.getAccessFlags(left[0], left[2]);
        int rightAcc = extraClassesInformation.getAccessFlags(right[0], right[2]);
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