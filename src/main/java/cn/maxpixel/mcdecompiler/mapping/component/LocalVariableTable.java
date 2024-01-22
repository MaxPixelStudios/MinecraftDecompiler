/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.mapping.component;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class LocalVariableTable implements Component {
    private final @NotNull Int2ObjectOpenHashMap<PairedMapping> lvt = new Int2ObjectOpenHashMap<>();
    private final @NotNull IntSet keys = lvt.keySet();
    private final @NotNull ObjectCollection<@NotNull PairedMapping> values = lvt.values();

    public PairedMapping getLocalVariable(@Range(from = 0, to = 255) int index) {
        return lvt.get(index);
    }

    public void setLocalVariable(@Range(from = 0, to = 255) int index, @Nullable("To remove the previous mapping") PairedMapping mapping) {
        lvt.put(index, mapping);
    }

    public @NotNull IntSet getLocalVariableIndexes() {
        return keys;
    }

    public void reverseAll(ClassifiedMappingRemapper remapper) {
        values.forEach(v -> v.reverse(remapper));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalVariableTable that)) return false;
        return Objects.equals(lvt, that.lvt);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(lvt);
    }

    public static class Namespaced implements Component, NameGetter.Namespaced {
        private String unmappedNamespace;
        private String mappedNamespace;
        private final @NotNull Int2ObjectOpenHashMap<NamespacedMapping> lvt = new Int2ObjectOpenHashMap<>();
        private final @NotNull IntSet keys = lvt.keySet();
        private final @NotNull ObjectCollection<@NotNull NamespacedMapping> values = lvt.values();

        public NamespacedMapping getLocalVariable(@Range(from = 0, to = 255) int index) {
            return lvt.get(index);
        }

        public void setLocalVariable(@Range(from = 0, to = 255) int index, @Nullable("To remove the previous mapping") NamespacedMapping mapping) {
            lvt.put(index, mapping);
        }

        public @NotNull IntSet getLocalVariableIndexes() {
            return keys;
        }

        public void swapAll(@NotNull String fromNamespace, @NotNull String toNamespace, ClassifiedMappingRemapper remapper) {
            values.forEach(value -> value.swap(remapper, fromNamespace, toNamespace));
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
            values.forEach(v -> v.setMappedNamespace(unmappedNamespace));
            return this;
        }

        @Override
        public void setMappedNamespace(@NotNull String namespace) {
            this.mappedNamespace = Objects.requireNonNull(namespace);
            values.forEach(v -> v.setMappedNamespace(mappedNamespace));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Namespaced that)) return false;
            return Objects.equals(unmappedNamespace, that.unmappedNamespace) && Objects.equals(mappedNamespace, that.mappedNamespace) && Objects.equals(lvt, that.lvt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unmappedNamespace, mappedNamespace, lvt);
        }
    }
}