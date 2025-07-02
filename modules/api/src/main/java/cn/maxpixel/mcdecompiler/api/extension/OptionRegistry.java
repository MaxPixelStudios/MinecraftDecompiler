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

import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import cn.maxpixel.mcdecompiler.utils.LambdaUtil;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class OptionRegistry {
    private static final Object2ObjectOpenHashMap<Class<?>, MethodHandle> CONVERTER_CACHE = new Object2ObjectOpenHashMap<>();
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class, String.class);
    private final ObjectOpenHashSet<Option> options = new ObjectOpenHashSet<>();
    private final Object2ObjectOpenHashMap<String, Option> optionMap = new Object2ObjectOpenHashMap<>();
    private final ObjectOpenHashSet<Option> presented = new ObjectOpenHashSet<>();
    private final Object2ObjectOpenHashMap<Option.ValueAccepting<?>, ObjectArrayList<Object>> values = new Object2ObjectOpenHashMap<>();
    private final ValueGetter valueGetter = new ValueGetter();
    private boolean optionRegistered;
    private boolean optionReceived;

    public void registerOptions(Iterable<Extension> extensions) {
        if (optionRegistered) throw new IllegalStateException("Options already registered");
        optionRegistered = true;
        for (Extension extension : extensions) {
            extension.onRegistering(new Registrar(extension.getName()));
        }
    }

    public void receiveOptions(Iterable<Extension> extensions) {
        if (optionReceived) throw new IllegalStateException("Options already received");
        optionReceived = true;
        for (Extension extension : extensions) {
            extension.onReceivingOptions(valueGetter);
        }
    }

    public Set<Option> getOptions() {
        return ObjectSets.unmodifiable(options);
    }

    private Option addOption0(String option) {
        var o = optionMap.get(Objects.requireNonNull(option));
        if (o == null) throw new IllegalArgumentException("Unknown option: " + option);
        presented.add(o);
        return o;
    }

    public void addOption(String option) {
        var o = addOption0(option);
        if (o instanceof Option.ValueAccepting<?> v && v.requiresArg) {
            throw new IllegalArgumentException("Option \"" + option + "\" requires arguments");
        }
    }

    private void addValue0(Option.ValueAccepting<?> v, Object value, ObjectArrayList<Object> valueList) {
        if (v.type.isInstance(Objects.requireNonNull(value))) {
            valueList.add(value);
        } else if (value instanceof String s) {
            try {
                valueList.add(v.converter != null ? v.converter.apply(s) : findConverter(v.type).invoke(s));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else throw new IllegalArgumentException("Incompatible types: Expected " + (v.type == String.class ? "String" :
                "String or " + v.type.getSimpleName()) + ", got " + value.getClass().getSimpleName());
    }

    public void addOption(String option, Object value) {
        if (addOption0(option) instanceof Option.ValueAccepting<?> v) {
            addValue0(v, value, values.computeIfAbsent(v, v1 -> new ObjectArrayList<>()));
        } else throwNoValuesAllowed(option);
    }

    public void addOption(String option, Object... values) {
        if (addOption0(option) instanceof Option.ValueAccepting<?> v) {
            ObjectArrayList<Object> valueList = this.values.computeIfAbsent(v, v1 -> new ObjectArrayList<>());
            for (Object obj : Objects.requireNonNull(values)) {
                addValue0(v, obj, valueList);
            }
        } else throwNoValuesAllowed(option);
    }

    public void addOption(String option, List<?> values) {
        if (addOption0(option) instanceof Option.ValueAccepting<?> v) {
            ObjectArrayList<Object> valueList = this.values.computeIfAbsent(v, v1 -> new ObjectArrayList<>());
            for (Object obj : Objects.requireNonNull(values)) {
                addValue0(v, obj, valueList);
            }
        } else throwNoValuesAllowed(option);
    }

    private static MethodHandle findConverter(Class<?> clazz) {
        return CONVERTER_CACHE.computeIfAbsent(clazz, LambdaUtil.unwrap(c -> {
            try {
                return MethodHandles.publicLookup().findStatic(c, "valueOf", MethodType.methodType(c, String.class));
            } catch (NoSuchMethodException e) {
                return MethodHandles.publicLookup().findConstructor(c, CONSTRUCTOR_TYPE);
            }
        }));
    }

    private static void throwNoValuesAllowed(String option) {
        throw new IllegalArgumentException("Option \"" + option + "\" doesn't accept values");
    }

    public <T extends Option> T register(T o) {
        o.registry = this;
        options.remove(o);
        options.add(o);
        for (String s : o.options) {
            optionMap.put(s, o);
        }
        return o;
    }

    public class Registrar {
        private final @NotNull String prefix;

        private Registrar(@NotNull String prefix) {
            this.prefix = prefix;
        }

        public Option register(@NotNull String option) {
            return register(option, null);
        }

        public Option register(@NotNull String option, @Nullable String description) {
            if (MappingUtils.isStringNotBlank(option)) return OptionRegistry.this.register(new Option(List.of(prefix + '-' + option), description));
            else throw new IllegalArgumentException("Option cannot be null or empty");
        }

        public Option register(@NotNull List<@NotNull String> options) {
            return register(options, null);
        }

        public Option register(@NotNull List<@NotNull String> options, @Nullable String description) {
            String[] sa = new String[Objects.requireNonNull(options).size()];
            for (int i = 0; i < sa.length; i++) {
                String o = options.get(i);
                if (MappingUtils.isStringNotBlank(o)) sa[i] = prefix + '-' + o;
                else throw new IllegalArgumentException("Option cannot be null or empty");
            }
            return OptionRegistry.this.register(new Option(new ObjectImmutableList<>(sa), description));
        }
    }

    public class ValueGetter {
        public boolean hasOption(Option o) {
            return presented.contains(o);
        }

        public boolean hasArgument(Option o) {
            if (o instanceof Option.ValueAccepting<?> v) {
                var list = values.get(v);
                return list != null && !list.isEmpty();
            }
            return false;
        }

        public <T> T valueOf(Option.ValueAccepting<T> o) {
            var list = values.get(o);
            if (list == null || list.isEmpty()) return null;
            if (list.size() > 1) throw new IllegalArgumentException("This option has multiple values");
            return (T) list.get(0);
        }

        public <T> List<T> valuesOf(Option.ValueAccepting<T> o) {
            var list = values.get(o);
            if (list == null || list.isEmpty()) return null;
            return (List<T>) list;
        }
    }
}