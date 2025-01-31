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
import cn.maxpixel.mcdecompiler.mapping.trait.MappingTrait;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
    private final ObjectOpenHashSet<@NotNull M> packageSet = new ObjectOpenHashSet<>();

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
     * Updates this collection with all the traits.
     */
    public final void updateCollection() {
        for (MappingTrait value : traits.values()) {
            value.updateCollection(this);
        }
    }

    /**
     * Clears this mapping collection.
     * <p>
     * This won't remove the traits.
     */
    public void clear() {
        packages.clear();
    }

    private ObjectOpenHashSet<@NotNull M> populatePackageSet() {
        if (!packageSet.containsAll(packages)) {// TODO: Profile to see whether this condition is needed
            packageSet.clear();
            packageSet.addAll(packages);
        }
        return packageSet;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MappingCollection<?> that)) return false;
        return Objects.equals(traits, that.traits) && packages.size() == that.packages.size() &&
                populatePackageSet().containsAll(that.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traits, populatePackageSet());
    }

    @Override
    public String toString() {
        return "MappingCollection{" +
                "traits=" + traits +
                ", packages=" + packages +
                '}';
    }
}