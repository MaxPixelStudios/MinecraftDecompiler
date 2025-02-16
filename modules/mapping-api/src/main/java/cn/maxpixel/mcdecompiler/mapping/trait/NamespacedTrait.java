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

package cn.maxpixel.mcdecompiler.mapping.trait;

import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A namespaced mapping trait.
 * Applies to a namespaced mapping collection to indicates all the namespaces(ordered) it has,
 * also indicates that the {@link cn.maxpixel.mcdecompiler.mapping.Mapping} of the collection
 * is {@link cn.maxpixel.mcdecompiler.mapping.NamespacedMapping}.
 *
 * @apiNote A namespaced mapping collection should contain this trait.
 */
public class NamespacedTrait implements MappingTrait, NameGetter.Namespace {
    /**
     * All the namespaces. Stored in a linked set to preserve the order
     */
    public final ObjectLinkedOpenHashSet<String> namespaces;
    private String unmappedNamespace;
    private String mappedNamespace;
    private String fallbackNamespace;

    public NamespacedTrait(@NotNull String @NotNull [] namespaces) {
        this.namespaces = ObjectLinkedOpenHashSet.of(namespaces);
    }

    @Override
    public String getUnmappedNamespace() {
        return Objects.requireNonNull(unmappedNamespace, "The unmapped namespace has not been set");
    }

    @Override
    public void setUnmappedNamespace(@NotNull String namespace) {
        this.unmappedNamespace = Objects.requireNonNull(namespace);
    }

    @Override
    public String getMappedNamespace() {
        return Objects.requireNonNull(mappedNamespace, "The mapped namespace has not been set");
    }

    @Override
    public void setMappedNamespace(@NotNull String namespace) {
        this.mappedNamespace = Objects.requireNonNull(namespace);
    }

    @Override
    public String getFallbackNamespace() {
        return Objects.requireNonNull(fallbackNamespace, "The fallback namespace has not been set");
    }

    @Override
    public void setFallbackNamespace(@NotNull String namespace) {
        this.fallbackNamespace = Objects.requireNonNull(namespace);
    }

    @Override
    public void updateCollection(MappingCollection<?> collection) {
        if (unmappedNamespace == null && mappedNamespace == null) return;
        if (collection instanceof ClassifiedMapping<?> classified) {
            for (ClassMapping<?> cm : classified.classes) {
                if (unmappedNamespace != null) ClassMapping.setUnmappedNamespace((ClassMapping<NamespacedMapping>) cm, unmappedNamespace);
                if (mappedNamespace != null) ClassMapping.setMappedNamespace((ClassMapping<NamespacedMapping>) cm, mappedNamespace);
                if (fallbackNamespace != null) ClassMapping.setFallbackNamespace((ClassMapping<NamespacedMapping>) cm, fallbackNamespace);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NamespacedTrait that)) return false;
        return Objects.equals(namespaces, that.namespaces) && Objects.equals(unmappedNamespace, that.unmappedNamespace) &&
                Objects.equals(mappedNamespace, that.mappedNamespace) && Objects.equals(fallbackNamespace, that.fallbackNamespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespaces, unmappedNamespace, mappedNamespace, fallbackNamespace);
    }

    @Override
    public String toString() {
        return "NamespacedTrait{" +
                "namespaces=" + namespaces +
                ", unmappedNamespace='" + unmappedNamespace + '\'' +
                ", mappedNamespace='" + mappedNamespace + '\'' +
                ", fallbackNamespace='" + fallbackNamespace + '\'' +
                '}';
    }
}