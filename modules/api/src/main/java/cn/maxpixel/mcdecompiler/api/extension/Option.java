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

package cn.maxpixel.mcdecompiler.api.extension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Option {
    OptionRegistry registry;
    public final @NotNull @Unmodifiable List<@NotNull String> options;
    public final @Nullable String description;

    Option(@NotNull List<@NotNull String> options, @Nullable String description) {
        this.options = options;
        this.description = description;
    }

    private Option(Option o) {
        this(o.options, o.description);
    }

    public ValueAccepting<String> withRequiredArg() {
        return registry.register(new ValueAccepting<>(true, String.class));
    }

    public ValueAccepting<String> withOptionalArg() {
        return registry.register(new ValueAccepting<>(false, String.class));
    }

    public <T> ValueAccepting<T> withRequiredArg(Class<T> type) {
        return registry.register(new ValueAccepting<>(true, type));
    }

    public <T> ValueAccepting<T> withOptionalArg(Class<T> type) {
        return registry.register(new ValueAccepting<>(false, type));
    }

    public <T> ValueAccepting<T> withRequiredArg(Class<T> type, Function<String, T> converter) {
        return registry.register(new ValueAccepting<>(true, type, converter));
    }

    public <T> ValueAccepting<T> withOptionalArg(Class<T> type, Function<String, T> converter) {
        return registry.register(new ValueAccepting<>(false, type, converter));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Option option)) return false;
        return Objects.equals(options, option.options) && Objects.equals(description, option.description);
    }

    @Override
    public int hashCode() {
        return 31 * options.hashCode() + Objects.hashCode(description);
    }

    @Override
    public String toString() {
        return "Option{" +
                "options=" + options +
                ", description='" + description + '\'' +
                '}';
    }

    public class ValueAccepting<T> extends Option {
        private static final Map<Class<?>, Class<?>> WRAPPER_MAP = Map.of(
                boolean.class, Boolean.class,
                byte.class, Byte.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                char.class, Character.class,
                float.class, Float.class,
                double.class, Double.class
        );

        public final boolean requiresArg;
        public final Class<T> type;
        public final Function<String, T> converter;
        private boolean required;
        private T defaultValue;

        private ValueAccepting(boolean requiresArg, Class<T> type) {
            super(Option.this);
            this.requiresArg = requiresArg;
            this.type = (Class<T>) WRAPPER_MAP.getOrDefault(Objects.requireNonNull(type), type);
            this.converter = null;
        }

        private ValueAccepting(boolean requiresArg, Class<T> type, Function<String, T> converter) {
            super(Option.this);
            this.requiresArg = requiresArg;
            this.type = (Class<T>) WRAPPER_MAP.getOrDefault(Objects.requireNonNull(type), type);
            this.converter = Objects.requireNonNull(converter);
        }

        public void setRequired() {
            if (!requiresArg) throw new IllegalStateException("This already has optional argument, why do you want it to be required?");
            this.required = true;
        }

        public void setDefaultValue(T value) {
            this.defaultValue = Objects.requireNonNull(value);
        }

        public boolean isRequired() {
            return required;
        }

        public T getDefaultValue() {
            return defaultValue;
        }
    }
}