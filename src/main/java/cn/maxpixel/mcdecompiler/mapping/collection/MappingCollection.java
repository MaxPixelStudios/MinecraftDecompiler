package cn.maxpixel.mcdecompiler.mapping.collection;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.trait.MappingTrait;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Base class of a mapping collection.
 *
 * @param <T> The type of the mapping. No use in this class.
 *           Put here so that others can restrict the mapping using this parent class.
 */
public abstract class MappingCollection<T extends Mapping> {
    /**
     * Packages of this mapping.
     */
    public final ObjectArrayList<@NotNull T> packages = new ObjectArrayList<>();

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
     * If a trait of the same class exists, replaces that trait.
     *
     * @param trait The trait to add or replace with
     */
    public void addTrait(@NotNull MappingTrait trait) {
        this.traits.put(trait.getClass(), trait);
    }

    /**
     * Removes a trait from this mapping collection.
     * Do nothing if the trait does not exist.
     *
     * @param trait The class of the trait to remove
     */
    public void removeTrait(@NotNull Class<? extends MappingTrait> trait) {
        traits.remove(Objects.requireNonNull(trait));
    }

    /**
     * Checks if a trait of given class exists.
     *
     * @param trait The class of the trait
     * @return True if the trait exists, false otherwise.
     */
    public boolean hasTrait(@NotNull Class<? extends MappingTrait> trait) {
        return traits.containsKey(Objects.requireNonNull(trait));
    }

    /**
     * Gets the trait of given class.
     *
     * @param trait The class of the trait
     * @return The trait of given class if it is present, null otherwise.
     * @param <T> The type of the trait
     */
    @SuppressWarnings("unchecked")
    public <T extends MappingTrait> T getTrait(@NotNull Class<T> trait) {
        return (T) this.traits.get(Objects.requireNonNull(trait));
    }

    /**
     * Gets the trait of given class.
     *
     * @param trait The class of the trait
     * @return The trait of given class
     * @param <T> The type of the trait
     */
    public <T extends MappingTrait> Optional<T> getTraitOptional(@NotNull Class<T> trait) {
        return Optional.ofNullable(getTrait(trait));
    }
}