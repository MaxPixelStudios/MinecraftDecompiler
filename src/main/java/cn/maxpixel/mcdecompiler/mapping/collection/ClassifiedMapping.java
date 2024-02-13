package cn.maxpixel.mcdecompiler.mapping.collection;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.trait.MappingTrait;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    /**
     * Clears this {@link ClassifiedMapping}.
     */
    public void clear() {
        classes.clear();
        packages.clear();
    }

    /**
     * Reverses this mapping.
     *
     * @apiNote This method only works for {@link PairedMapping} collections.
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
        if (!m.classes.isEmpty()) {
            ClassifiedMappingRemapper remapper = new ClassifiedMappingRemapper(m);
            m.classes.parallelStream().forEach(cm -> ClassMapping.reverse(cm, remapper));
        }
        if (!m.packages.isEmpty()) m.packages.parallelStream().forEach(PairedMapping::reverse);
    }

    /**
     * Swaps the first namespace with the given namespace.
     *
     * @param targetNamespace target namespace to swap with
     */
    public void swap(@NotNull String targetNamespace) {
        if (!hasTrait(NamespacedTrait.class)) throw new UnsupportedOperationException();
        swap((ClassifiedMapping<NamespacedMapping>) this, targetNamespace);
    }

    /**
     * Swaps this mapping.
     *
     * @apiNote sourceNamespace should always get from {@link NamingUtil#getSourceNamespace(ClassifiedMapping)}.
     *         Thus, you should use {@link #swap(String)} unless you really know what you are doing
     * @param sourceNamespace source namespace to swap with
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
        swap(m, NamingUtil.getSourceNamespace(m), targetNamespace);
    }

    /**
     * Swaps the given mapping.
     *
     * @apiNote sourceNamespace should always get from {@link NamingUtil#getSourceNamespace(ClassifiedMapping)}.
     *          Thus, you should use {@link #swap(ClassifiedMapping, String)} unless you really know what you are doing
     * @param m mapping to swap
     * @param sourceNamespace source namespace to swap with
     * @param targetNamespace target namespace to swap with
     */
    public static void swap(@NotNull ClassifiedMapping<NamespacedMapping> m, @NotNull String sourceNamespace, @NotNull String targetNamespace) {
        if (!m.classes.isEmpty()) {
            ClassifiedMappingRemapper remapper = new ClassifiedMappingRemapper(m, targetNamespace);
            m.classes.parallelStream().forEach(cm -> ClassMapping.swap(cm, remapper, sourceNamespace, targetNamespace));
        }
        if (!m.packages.isEmpty()) m.packages.parallelStream().forEach(nm -> nm.swap(sourceNamespace, targetNamespace));
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassifiedMapping<?> that)) return false;
        return Objects.equals(classes, that.classes) && Objects.equals(packages, that.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classes, packages);
    }

    @Override
    public String toString() {
        return "ClassifiedMapping{" +
                "classes=" + classes +
                ", packages=" + packages +
                '}';
    }
}