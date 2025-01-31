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

package cn.maxpixel.mcdecompiler.mapping.collection;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.trait.MappingTrait;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.DescriptorRemapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

/**
 * A collection of mappings of classes and packages.
 * <p>
 * The mappings of classes are stored in a hierarchical structure.
 * Check the docs of {@link ClassMapping} for more information.
 *
 * @apiNote These fields should not contain null values.
 *          There are no checks but the assumption is made.
 *          Put null values at your own risk.
 * @see ClassMapping
 * @param <T> The type of this mapping
 */
public class ClassifiedMapping<T extends Mapping> extends MappingCollection<T> {
    /**
     * Classes of this mapping.
     */
    public final ObjectArrayList<@NotNull ClassMapping<@NotNull T>> classes = new ObjectArrayList<>();
    private final ObjectOpenHashSet<@NotNull ClassMapping<@NotNull T>> classSet = new ObjectOpenHashSet<>();

    public ClassifiedMapping() {}

    public ClassifiedMapping(@NotNull MappingTrait @NotNull ... traits) {
        super(traits);
    }

    /**
     * Adds the contents of another {@link ClassifiedMapping} to this one.
     *
     * @param m the other mapping
     */
    public void add(ClassifiedMapping<T> m) {
        classes.addAll(m.classes);
        packages.addAll(m.packages);
    }

    /**
     * Adds the contents of this {@link ClassifiedMapping} to another one.
     *
     * @param m the other mapping
     */
    public void addTo(ClassifiedMapping<T> m) {
        m.classes.addAll(classes);
        m.packages.addAll(packages);
    }

    @Override
    public void clear() {
        super.clear();
        classes.clear();
    }

    /**
     * Reverses this mapping.
     *
     * @apiNote This method only works for {@link PairedMapping} collections.
     * @throws UnsupportedOperationException if this is not a {@link PairedMapping} collection
     */
    public void reverse() {
        if (hasTrait(NamespacedTrait.class)) throw new UnsupportedOperationException();
        reverse((ClassifiedMapping<PairedMapping>) this);
    }

    /**
     * Reverses the given mapping.
     *
     * @param m mapping to reverse
     */
    public static void reverse(@NotNull ClassifiedMapping<PairedMapping> m) {
        if (!m.classes.isEmpty()) m.classes.parallelStream().forEach(ClassMapping::reverse);
        if (!m.packages.isEmpty()) m.packages.parallelStream().forEach(PairedMapping::reverse);
    }

    /**
     * Swaps the first namespace with the given namespace.
     *
     * @apiNote This method only works for {@link NamespacedMapping} collections
     * @throws UnsupportedOperationException if this is not a {@link NamespacedMapping} collection
     * @param targetNamespace target namespace to swap with
     */
    public void swap(@NotNull String targetNamespace) {
        if (!hasTrait(NamespacedTrait.class)) throw new UnsupportedOperationException();
        swap((ClassifiedMapping<NamespacedMapping>) this, targetNamespace);
    }

    /**
     * Swaps this mapping.
     *
     * @apiNote This method only works for {@link NamespacedMapping} collections.
     * @throws UnsupportedOperationException if this is not a {@link NamespacedMapping} collection
     * @param sourceNamespace source namespace to swap with. This should always get from {@link #getSourceNamespace(ClassifiedMapping)}
     *                        or {@link #getSourceNamespace()}. Thus, you should use {@link #swap(String)} unless you really know what you are doing.
     * @param targetNamespace target namespace to swap with
     */
    public void swap(@NotNull String sourceNamespace, @NotNull String targetNamespace) {
        if (!hasTrait(NamespacedTrait.class)) throw new UnsupportedOperationException();
        swap((ClassifiedMapping<NamespacedMapping>) this, sourceNamespace, targetNamespace);
    }

    /**
     * Swaps the first namespace with given namespace of the given mapping.
     *
     * @implSpec Swapping swaps the contents, not the namespaces.
     *          That means, the namespace of a namespaced descriptor content won't change after swapping.
     * @param m mapping to swap
     * @param targetNamespace target namespace to swap with
     */
    public static void swap(@NotNull ClassifiedMapping<NamespacedMapping> m, @NotNull String targetNamespace) {
        swap(m, getSourceNamespace(m), targetNamespace);
    }

    /**
     * Swaps the given mapping.
     *
     * @implSpec Swapping swaps the contents, not the namespaces.
     *          That means, the namespace of a namespaced descriptor content won't change after swapping.
     * @apiNote sourceNamespace should always get from {@link #getSourceNamespace(ClassifiedMapping)} or {@link #getSourceNamespace()}.
     *          Thus, you should use {@link #swap(ClassifiedMapping, String)} unless you really know what you are doing
     * @param m mapping to swap
     * @param sourceNamespace source namespace to swap with
     * @param targetNamespace target namespace to swap with
     */
    public static void swap(@NotNull ClassifiedMapping<NamespacedMapping> m, @NotNull String sourceNamespace, @NotNull String targetNamespace) {
        if (!m.classes.isEmpty()) {
            DescriptorRemapper remapper = new DescriptorRemapper(m, targetNamespace);
            m.classes.parallelStream().forEach(cm -> ClassMapping.swap(cm, remapper, sourceNamespace, targetNamespace));
        }
        if (!m.packages.isEmpty()) m.packages.parallelStream().forEach(nm -> nm.swap(sourceNamespace, targetNamespace));
    }

    /**
     * Convenient method to get the source namespace.
     *
     * @apiNote This method only works for {@link NamespacedMapping} collections.
     * @throws UnsupportedOperationException if this is not a {@link NamespacedMapping} collection
     * @return The source namespace of this mapping collection.
     */
    public String getSourceNamespace() {
        if (!hasTrait(NamespacedTrait.class)) throw new UnsupportedOperationException();
        return getSourceNamespace((ClassifiedMapping<NamespacedMapping>) this);
    }

    /**
     * Convenient method to get the source namespace.
     *
     * @param mappings A namespaced mapping collection.
     * @return The source namespace of that mapping collection.
     */
    public static String getSourceNamespace(@NotNull ClassifiedMapping<NamespacedMapping> mappings) {
        return mappings.getTrait(NamespacedTrait.class).getUnmappedNamespace();
    }

    private ObjectOpenHashSet<@NotNull ClassMapping<@NotNull T>> populateClassSet() {
        classSet.clear();
        classSet.addAll(classes);
        return classSet;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassifiedMapping<?> that)) return false;
        if (!super.equals(o)) return false;
        return classes.size() == that.classes.size() && populateClassSet().containsAll(that.classes);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + populateClassSet().hashCode();
    }

    @Override
    public String toString() {
        return "ClassifiedMapping{" +
                "classes=" + classes +
                "} " + super.toString();
    }
}