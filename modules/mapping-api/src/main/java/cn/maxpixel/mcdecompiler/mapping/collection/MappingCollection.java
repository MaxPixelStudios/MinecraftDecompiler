package cn.maxpixel.mcdecompiler.mapping.collection;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.trait.MappingTrait;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Base class of a mapping collection.
 *
 * @param <M> The type of the mapping.
 */
public abstract class MappingCollection<M extends Mapping> {
    /**
     * Packages of this mapping.
     */
    public final ObjectArrayList<@NotNull M> packages = new ObjectArrayList<>();

    private final Object2ObjectOpenHashMap<@NotNull Class<? extends MappingTrait>, @NotNull MappingTrait> traits = new Object2ObjectOpenHashMap<>();

    /**
     * No-arg constructor
     */
    protected MappingCollection() {}

    /**
     * Constructor
     *
     * @param traits Traits to add to this mapping
     */
    protected MappingCollection(@NotNull MappingTrait @NotNull ... traits) {
        for (@NotNull MappingTrait trait : traits) {
            this.traits.put(trait.getClass(), trait);
        }
    }

    /**
     * Adds a trait to this mapping collection.
     *
     * @implNote If a trait of the same class exists, replaces that trait
     * @param trait The trait to add or replace with
     */
    public final void addTrait(@NotNull MappingTrait trait) {
        this.traits.put(trait.getClass(), trait);
    }

    /**
     * Removes a trait from this mapping collection.
     *
     * @implNote Do nothing if the trait does not exist.
     * @param trait The class of the trait to remove
     */
    public final void removeTrait(@NotNull Class<? extends MappingTrait> trait) {
        traits.remove(Objects.requireNonNull(trait));
    }

    /**
     * Checks if a trait of given class exists.
     *
     * @param trait The class of the trait
     * @return True if the trait exists, false otherwise
     */
    public final boolean hasTrait(@NotNull Class<? extends MappingTrait> trait) {
        return traits.containsKey(Objects.requireNonNull(trait));
    }

    /**
     * Gets the trait of given class.
     *
     * @param trait The class of the trait
     * @return The trait of given class if it is present, null otherwise
     * @param <T> The type of the trait
     */
    @SuppressWarnings("unchecked")
    public final <T extends MappingTrait> T getTrait(@NotNull Class<T> trait) {
        return (T) this.traits.get(Objects.requireNonNull(trait));
    }

    /**
     * Gets the trait of given class.
     *
     * @param trait The class of the trait
     * @return The trait of given class
     * @param <T> The type of the trait
     */
    public final <T extends MappingTrait> Optional<T> getTraitOptional(@NotNull Class<T> trait) {
        return Optional.ofNullable(getTrait(trait));
    }

    /**
     * Gets all the traits of this mapping collection.
     *
     * @return All the traits of this mapping collection
     */
    public final @NotNull ObjectCollection<? extends MappingTrait> getTraits() {
        return traits.values();
    }

    /**
     * Clears this mapping collection.
     * <p>
     * This won't remove the traits.
     */
    public void clear() {
        packages.clear();
    }
}