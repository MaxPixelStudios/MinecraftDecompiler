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

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.util.DescriptorRemapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

/**
 * This component represents a local variable table mapping
 * @param <T> The type of the mapping
 * @apiNote The default behavior is to treat the index in {@link LocalVariableTable} as the actual lvt index.
 *          However, when {@link StaticIdentifiable} presents and {@code isStatic == false}, the index in {@link LocalVariableTable}
 *          will be treated as the actual lvt index - 1, which means that index 0 in {@link LocalVariableTable} represents
 *          the index 1 in the actual lvt(omitting {@code this}).
 */
public abstract class LocalVariableTable<T extends Mapping> {
    protected final @NotNull Int2ObjectOpenHashMap<T> lvt = new Int2ObjectOpenHashMap<>();

    public T getLocalVariable(@Range(from = 0, to = 255) int index) {
        return lvt.get(index);
    }

    public void setLocalVariable(@Range(from = 0, to = 255) int index, @Nullable("To remove the previous mapping") T mapping) {
        lvt.put(index, mapping);
    }

    public @NotNull IntSet getLocalVariableIndexes() {
        return lvt.keySet();
    }

    public int getLocalVariableCount() {
        return lvt.size();
    }

    public boolean isEmpty() {
        return lvt.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalVariableTable<?> that)) return false;
        return Objects.equals(lvt, that.lvt);
    }

    @Override
    public int hashCode() {
        return lvt.hashCode();
    }

    @Override
    public String toString() {
        return "LocalVariableTable{" +
                "lvt=" + lvt +
                '}';
    }

    public static class Paired extends LocalVariableTable<PairedMapping> implements Component, Component.Reversible {
        @Override
        public void validate() throws IllegalStateException {
            lvt.int2ObjectEntrySet().fastForEach(entry -> {
                if (entry.getIntKey() < 0 || entry.getIntKey() > 255) throw new IllegalStateException("Illegal LVT index");
                entry.getValue().validateComponents();
            });
        }

        @Override
        public void reverse() {
            lvt.values().forEach(PairedMapping::reverse);
        }
    }

    public static class Namespaced extends LocalVariableTable<NamespacedMapping> implements Component, NameGetter.Namespace, Component.Swappable {
        private String unmappedNamespace;
        private String mappedNamespace;
        private String fallbackNamespace;

        @Override
        public void setLocalVariable(@Range(from = 0, to = 255) int index, @Nullable("To remove the previous mapping") NamespacedMapping mapping) {
            if (mapping != null) {
                if (unmappedNamespace != null) mapping.setUnmappedNamespace(unmappedNamespace);
                if (mappedNamespace != null) mapping.setMappedNamespace(mappedNamespace);
                if (fallbackNamespace != null) mapping.setFallbackNamespace(fallbackNamespace);
            }
            super.setLocalVariable(index, mapping);
        }

        @Override
        public void swap(@NotNull String fromNamespace, @NotNull String toNamespace, DescriptorRemapper remapper) {
            lvt.values().forEach(value -> value.swap(remapper, fromNamespace, toNamespace));
        }

        @Override
        public String getUnmappedNamespace() {
            return unmappedNamespace;
        }

        @Override
        public String getMappedNamespace() {
            return mappedNamespace;
        }

        public Namespaced setUnmappedNamespace(@NotNull String namespace) {
            this.unmappedNamespace = Objects.requireNonNull(namespace);
            for (NamespacedMapping v : lvt.values()) {
                v.setUnmappedNamespace(namespace);
            }
            return this;
        }

        @Override
        public void setMappedNamespace(@NotNull String namespace) {
            this.mappedNamespace = Objects.requireNonNull(namespace);
            for (NamespacedMapping v : lvt.values()) {
                v.setMappedNamespace(namespace);
            }
        }

        @Override
        public String getFallbackNamespace() {
            return fallbackNamespace;
        }

        @Override
        public void setFallbackNamespace(@NotNull String namespace) {
            this.fallbackNamespace = Objects.requireNonNull(namespace);
            for (NamespacedMapping v : lvt.values()) {
                v.setFallbackNamespace(namespace);
            }
        }

        @Override
        public void validate() throws IllegalStateException {
            if (unmappedNamespace == null) throw new IllegalStateException();
            lvt.int2ObjectEntrySet().fastForEach(entry -> {
                if (entry.getIntKey() < 0 || entry.getIntKey() > 255)
                    throw new IllegalStateException("Illegal LVT index");
                entry.getValue().validateComponents();
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Namespaced that)) return false;
            if (!super.equals(o)) return false;
            return Objects.equals(unmappedNamespace, that.unmappedNamespace) &&
                    Objects.equals(mappedNamespace, that.mappedNamespace) &&
                    Objects.equals(fallbackNamespace, that.fallbackNamespace);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Objects.hash(unmappedNamespace, mappedNamespace, fallbackNamespace);
        }
    }
}