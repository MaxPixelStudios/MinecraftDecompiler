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

import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.mcdecompiler.common.annotation.MethodOrFieldDesc;
import cn.maxpixel.mcdecompiler.mapping.util.DescriptorRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.Validation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Descriptor component for paired mappings
 */
public abstract class Descriptor implements Component {
    private static final Pattern DESC_PATTERN = Pattern.compile('(' + Constants.FIELD_DESC_PATTERN + ")|(" + Constants.METHOD_DESC_PATTERN + ')');
    private static final ThreadLocal<Matcher> MATCHERS = ThreadLocal.withInitial(() -> DESC_PATTERN.matcher(""));

    public @NotNull @MethodOrFieldDesc String descriptor;

    public Descriptor(@NotNull @MethodOrFieldDesc String descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor);
    }

    public @NotNull @MethodOrFieldDesc String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(@NotNull @MethodOrFieldDesc String desc) {
        this.descriptor = Objects.requireNonNull(desc);
    }

    @Override
    public void validate() throws IllegalStateException {
        Validation.requireNonNull(descriptor, "descriptor");
        if (!MATCHERS.get().reset(descriptor).matches()) {
            throw new IllegalStateException("Invalid descriptor: " + descriptor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Descriptor that)) return false;
        return descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }

    @Override
    public String toString() {
        return "Descriptor{" +
                "descriptor='" + descriptor + '\'' +
                '}';
    }

    public static class Unmapped extends Descriptor implements ConvertingReversible<Mapped> {
        public Unmapped(@NotNull @MethodOrFieldDesc String unmappedDescriptor) {
            super(unmappedDescriptor);
        }

        @Override
        public @NotNull Class<Mapped> getTarget() {
            return Mapped.class;
        }

        @Override
        public @NotNull Mapped convert() {
            return new Mapped(descriptor);
        }

        @Override
        public void reverse(@NotNull Mapped target) {
            String desc = descriptor;
            descriptor = target.descriptor;
            target.descriptor = desc;
        }
    }

    /**
     * Mapped descriptor component for paired mappings
     */
    public static class Mapped extends Descriptor implements ConvertingReversible<Unmapped> {
        public Mapped(@NotNull @MethodOrFieldDesc String mappedDescriptor) {
            super(mappedDescriptor);
        }

        @Override
        public @NotNull Class<Unmapped> getTarget() {
            return Unmapped.class;
        }

        @Override
        public @NotNull Unmapped convert() {
            return new Unmapped(descriptor);
        }

        @Override
        public void reverse(@NotNull Unmapped target) {
            String desc = descriptor;
            descriptor = target.descriptor;
            target.descriptor = desc;
        }
    }

    /**
     * Swappable descriptor component<br>
     * Extends {@link Descriptor} because the currently supported namespaced mappings only have unmapped descriptors
     */
    public static class Namespaced extends Descriptor implements Component.Swappable {
        public @NotNull String descriptorNamespace;

        public Namespaced(@NotNull @MethodOrFieldDesc String unmappedDescriptor, @NotNull String descriptorNamespace) {
            super(unmappedDescriptor);
            this.descriptorNamespace = Objects.requireNonNull(descriptorNamespace);
        }

        public @NotNull String getDescriptorNamespace() {
            return descriptorNamespace;
        }

        public void setDescriptorNamespace(@NotNull String descriptorNamespace) {
            this.descriptorNamespace = Objects.requireNonNull(descriptorNamespace);
        }

        @Override
        public void validate() throws IllegalStateException {
            super.validate();
            if (descriptorNamespace == null) throw new IllegalStateException("Descriptor namespace must not be null");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Namespaced that)) return false;
            if (!super.equals(o)) return false;
            return descriptorNamespace.equals(that.descriptorNamespace);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + descriptorNamespace.hashCode();
        }

        @Override
        public String toString() {
            return "Namespaced{" +
                    "descriptorNamespace='" + descriptorNamespace + '\'' +
                    "} " + super.toString();
        }

        @Override
        public void swap(@NotNull String fromNamespace, @NotNull String toNamespace, DescriptorRemapper remapper) {
            if (!descriptorNamespace.equals(fromNamespace)) throw new IllegalArgumentException();
            descriptor = descriptor.charAt(0) == '(' ? remapper.mapMethodDesc(descriptor) : remapper.mapDesc(descriptor);
        }
    }
}