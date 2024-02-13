package cn.maxpixel.mcdecompiler.mapping.trait;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;

/**
 * A namespaced mapping trait.
 * Applies to a namespaced mapping collection to indicates all the namespaces(ordered) it has,
 * also indicates that the {@link cn.maxpixel.mcdecompiler.mapping.Mapping} of the collection
 * is {@link cn.maxpixel.mcdecompiler.mapping.NamespacedMapping}.
 *
 * @apiNote A namespaced mapping collection should contain this trait.
 */
public class NamespacedTrait implements MappingTrait {
    /**
     * Ordered list that shows all the namespaces.
     */
    public final ObjectLinkedOpenHashSet<String> namespaces;

    public NamespacedTrait(@NotNull String @NotNull [] namespaces) {
        this.namespaces = ObjectLinkedOpenHashSet.of(namespaces);
    }

    @Override
    public String getName() {
        return "namespaced";
    }
}